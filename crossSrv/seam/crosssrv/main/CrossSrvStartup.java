package crosssrv.main;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.pwrd.op.LogOp;

import core.InputStream;
import core.Node;
import core.Port;
import core.PortPulseQueue;
import core.connsrv.main.ConnStartup;
import core.dbsrv.main.DBStartup;
import core.statistics.StatisticsService;
import core.support.Config;
import core.support.Distr;
import core.support.Utils;
import core.support.log.LogCore;
import core.support.observer.MsgSender;
import crosssrv.CommonSerializer;
import crosssrv.ListenerInit;
import crosssrv.MsgReceiverInit;
import crosssrv.combatant.CombatantGlobalService;
import crosssrv.groupFight.GroupFightService;
import crosssrv.inform.InformCrossServer;
import crosssrv.seam.CrossPort;
import crosssrv.seam.SeamService;
import crosssrv.seam.token.TokenService;
import crosssrv.singleFight.SingleFightService;
import crosssrv.stage.CrossStageGlobalService;
import crosssrv.stage.CrossStageService;
import crosssrv.support.ClassScanProcess;
import crosssrv.support.DataReloadManager;
import crosssrv.support.DataScanProcess;
import crosssrv.support.Log;
import game.MsgSerializer;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.InitFieldTable;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class CrossSrvStartup {
	public static void main(String[] args) throws Exception {
		// 设置个默认值 便于系统调试
		if (args.length == 0) {
			args = new String[] {"0"};
			
			// 开发调试环境，启动世界服时先启动数据库
			try {
				DBStartup.main(new String[]{Distr.CROSS_NODE_DB});
			} catch (Exception e) {
				Log.cross.info("CorssDB数据库启动失败");
				e.printStackTrace();
			}
		}
		

		System.setProperty("logFileName", "cross" + args[0]);

		// 验证参数数量
		if (args.length < 1) {
			Log.cross.error("useage: nodeIndex");
			return;
		}

		// 跨服配置
		Config.isCrossSrv = true; // 设置为跨服标识
		Config.CROSS_PLUSE_INTERVAL = 5; // 跨服节点心跳间隔5ms
		Config.SERVER_PART_ID = Integer.parseInt(args[0]); // 启动时指定服务器分支ID

		// 启动世界服
		startupCrossService(Config.SERVER_PART_ID);

		// 系统关闭时进行清理
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
			}
		});

		Log.cross.error("****************************************************************");
		// Log.cross.error("* 当前版本号{} 客户端版本号{}.{}.{} ",C.CLIENT_VERSION,
		// C.CLIENT_VERSION/1000000,C.CLIENT_VERSION/1000%1000,C.CLIENT_VERSION%1000);
		Log.cross.error("****************************************************************");

		// 设定support外围环境并加载
		//if (ParamManager.openReloadData) {
		//	DataReloadManager.inst().initReloadSupport();
		//	DataScanProcess.getInstance().init();
		//	Log.cross.error("开启数据热更新扫描...");
		//}
		if (ParamManager.openReloadClass) {
			ClassScanProcess.getInstance().init();
			Log.cross.error("开启类热更新扫描...");
		}

		Log.cross.error("启动完成...");
	}

	private static void startupCrossService(int crossPartId) throws Exception {
		// 初始化基本环境
		MsgReceiverInit.init(MsgSender.instance);
		ListenerInit.init(Event.instance);
		MsgSerializer.init();
		CommonSerializer.init();
		InputStream.addCreateCommonFunc(game.CommonSerializer::create);

		DataReloadManager.inst().reloadConf();// 加载所有JSON表格数据

		// 创建Node
		String partId = D.CROSS_NODE_PREFIX + crossPartId;
		String nodeAddr = Distr.getNodeAddr(partId);
		String nodeId = Config.getCrossPartDefaultNodeId();
		Node node = new Node(nodeId, nodeAddr, partId);

		/* 1 设置远程Node */
		// 1.1 连接服务器
		ConnStartup.startup(node, Config.CROSS_PLUSE_INTERVAL);
		// 默认Ping注册(拦截)
		// Connection.registerPing(MsgIds.CSPing, MsgIds.SCPing, new
		// Chunk(SCPing.newBuilder()), C.IGNORE_PING);

		// 1.2 数据服务器
		// if(crossPartId == 0){
		// 需要导入数据处理字段
		String dbNode = Config.getCrossNodeId(Config.CROSS_SERVER_ID, Distr.CROSS_NODE_DB);
		node.addRemoteNode(dbNode, Distr.getNodeAddr(dbNode));

		// 等待连接DB服务器
		Log.cross.error("等待连接数据服务器...");
		while (!node.isRemoteNodeConnected(dbNode)) {
			try {
				Thread.sleep(10);
				node.pulse();
			} catch (InterruptedException e) {
				LogCore.core.error(ExceptionUtils.getStackTrace(e));
			}
		}
		// }

		// 等待1秒 在继续初始化
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			LogCore.core.error(ExceptionUtils.getStackTrace(e1));
		}

		// 1.3 游戏服务器
		// 从服连接主服
		if (crossPartId > 0) {
			node.addRemoteNode(Config.getCrossDefaultNodeId(Config.CROSS_SERVER_ID),
					Distr.getNodeAddr(Distr.CROSS_NODE_DEFAULT));
		}

		// 启动远程数据日志
		String logPath = Utils.class.getClassLoader().getResource("operlog.properties").getPath();
		LogOp.init(logPath);

		/* 2 加载系统数据 */
		// 2.1 创建个临时Port
		String portName = Distr.CROSS_DEFAULT_PORT_PREFIX + crossPartId;
		CrossPort crossPort = new CrossPort(portName, Config.CROSS_PLUSE_INTERVAL);
		crossPort.startup(node);

		// 2.2 加载系统数据
		final InitFieldTable servInitData = new InitFieldTable();
		crossPort.addQueue(new PortPulseQueue() {
			@Override
			public void execute(Port port) {
				servInitData.init();
			}
		});

		// 等待加载完成
		while (!servInitData.isCompleted()) {
			try {
				Thread.sleep(10);
				node.pulse();
			} catch (InterruptedException e) {
				LogCore.core.error(ExceptionUtils.getStackTrace(e));
			}
		}

		// 2.3整合服务
		SeamService seamServ = new SeamService(crossPort);
		seamServ.startup();
		crossPort.addService(seamServ);

		/* 3 启动系统默认服务 */
		// 登陆服务
		TokenService gateServ = new TokenService(crossPort);
		gateServ.startup();
		crossPort.addService(gateServ);

		if (crossPartId == 0) {
			// 多人战斗服务
			GroupFightService groupFightSrv = new GroupFightService(crossPort);
			groupFightSrv.startup();
			crossPort.addService(groupFightSrv);
			// 单人战斗服务
			SingleFightService singleFightSrv = new SingleFightService(crossPort);
			singleFightSrv.startup();
			crossPort.addService(singleFightSrv);
			// 跨服弹幕
			InformCrossServer InformCrossServer = new InformCrossServer(crossPort);
			InformCrossServer.startup();
			crossPort.addService(InformCrossServer);
		}

		// 初始化地图全局服务 必须在初始化地图前就开启本服务 所以就先放这了
		for(int i = 0; i < D.CROSS_STAGE_PORT_NUM; ++i) {
			//拼PortId
			String portId = D.CROSS_STAGE_PORT_PREFIX + i;
			CrossPort crossStagePort = new CrossPort(portId, Config.CROSS_PLUSE_INTERVAL);
			crossStagePort.startup(node);
			
			CrossStageService stageServ = new CrossStageService(crossStagePort);
			stageServ.startup();
			crossStagePort.addService(stageServ);
			
			//手动增加对应关系
			Distr.addStartPort(portId, node.getId());
		}
//		CrossStageService stageServ = new CrossStageService(crossPort);
//		stageServ.startup();
//		crossPort.addService(stageServ);

		CrossStageGlobalService stageGlobalServ = new CrossStageGlobalService(crossPort);
		stageGlobalServ.startup();
		crossPort.addService(stageGlobalServ);
		// 初始化全局角色服务 必须在初始化地图前就开启本服务
		CombatantGlobalService combatantGlobalServ = new CombatantGlobalService(crossPort);
		combatantGlobalServ.startup();
		crossPort.addService(combatantGlobalServ);
		// 运行信息统计
		if (Config.STATISTICS_ENABLE) {
			StatisticsService statisticsService = new StatisticsService(crossPort);
			statisticsService.startup();
			crossPort.addService(statisticsService);
		}

		// 发布服务器初始化开始事件
		Event.fire(EventKey.CrossStartupBefore, "node", node);

		// Node正式启动
		node.startup();

		// 发布服务器初始化结束事件
		Event.fire(EventKey.CrossStartupFinish, "node", node);

		// 启动日志信息
		Log.cross.error("====================");
		Log.cross.error(nodeId + " started.");
		Log.cross.error("Listen:" + nodeAddr);
		Log.cross.error("====================");
	}
}
