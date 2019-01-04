package game.seam.main;

import game.CommonSerializer;
import game.ListenerInit;
import game.MsgReceiverInit;
import game.MsgSerializer;
import game.platform.DistrPF;
import game.platform.HttpPort;
import game.platform.LogPF;
import game.platform.chat.ChatPort;
import game.platform.chat.ChatService;
import game.platform.gift.GiftPort;
import game.platform.gift.GiftService;
import game.platform.http.HttpAsyncPort;
import game.platform.http.HttpAsyncSendService;
import game.platform.http.HttpServer;
import game.platform.login.LoginPort;
import game.platform.login.LoginService;
import game.platform.observer.EventPF;
import game.seam.DefaultPort;
import game.seam.SeamService;
import game.seam.account.AccountService;
import game.support.ClassScanProcess;
import game.support.DataReloadManager;
import game.worldsrv.inform.ChatServiceProxy;
import game.worldsrv.integration.PFService;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageGlobalService;
import game.worldsrv.support.D;
import game.worldsrv.support.InitFieldTable;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.pwrd.op.LogOp;
//import com.dataeye.sdk.client.DCAgent;

import core.Node;
import core.Port;
import core.PortPulseQueue;
import core.connsrv.main.ConnStartup;
import core.dbsrv.main.DBStartup;
import core.statistics.StatisticsService;
import core.support.Config;
import core.support.Distr;
import core.support.log.LogCore;
import core.support.observer.MsgSender;

public class WorldStartup {
	public static void main(String[] args) throws Exception {
		// 设置个默认值 便于系统调试
		if (args.length == 0) {
			args = new String[]{"0"};
			
			// 开发调试环境，启动世界服时先启动数据库
			try {
				DBStartup.main(null);
			} catch (Exception e) {
				Log.game.info("数据库启动失败");
				e.printStackTrace();
			}
		}
		
		// 设置log4j2日志文件名称
		System.setProperty("logFileName", "world" + args[0]);
				
		// 验证参数数量
		if (args.length < 1) {
			Log.game.error("===世界服启动失败：缺少验证参数，WorldStartup fail in args.length < 1");
			return;
		}
		
		//启动时指定服务器分支ID
		Config.SERVER_PART_ID = Integer.parseInt(args[0]);
				
		//启动世界服
		startupWorldService(Config.SERVER_PART_ID);
		
		if (Config.SERVER_PART_ID == 0) {
			//启动认证服
			startupPlatformService();
		}
		
		// 系统关闭时进行清理
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				//DCAgent.stopAll(); // 关闭数据分析平台SDK
				ChatServiceProxy.newInstance().uploadChat();
				Log.game.info("===Close WorldServer=================");
			}
		});
		
		// 设定support外围环境并加载
		//if (ParamManager.openReloadData) {
		//	DataReloadManager.inst().initReloadSupport();
		//	DataScanProcess.getInstance().init();
		//	Log.game.info("开启数据热更新扫描...");
		//}
		if (ParamManager.openReloadClass) {
			ClassScanProcess.getInstance().init();
			Log.game.info("开启类热更新扫描...");
		}
		
		
		Log.game.info("启动完成...");
	}
	
	private static void startupWorldService(int worldNum) throws Exception {
		// 初始化基本环境
		MsgReceiverInit.init(MsgSender.instance);
		ListenerInit.init(Event.instance);
		MsgSerializer.init();
		CommonSerializer.init();

		//PathFinding.init();// add by shenjh,加载导航网格资源
		if (!DataReloadManager.inst().reloadConf()) { // add by shenjh,加载所有JSON表格数据
			System.exit(1);
		}
		
		// 创建Node
		String partId = D.NODE_WORLD_PREFIX + worldNum;
		String nodeAddr = Distr.getNodeAddr(partId);
		String nodeId = Config.getGameWorldPartDefaultNodeId();
		Node node = new Node(nodeId, nodeAddr, partId);
		
		/* 1 设置远程Node */
//		String connId = Distr.NODE_CONNECT_PREFIX + 0;
//		node.addRemoteNode(connId, Distr.getNodeAddr(connId));
		//for (int i = 0; i < Distr.PORT_STARTUP_NUM_CONN; i++) {
		//	String id = Distr.NODE_CONNECT_PREFIX + i;
		//	node.addRemoteNode(id, Distr.getNodeAddr(id));
		//}
		
		// 1.2 数据服务器
		node.addRemoteNode(Distr.NODE_DB, Distr.getNodeAddr(Distr.NODE_DB));
		// 等待成功建立连接
		Log.game.info("====================正在建立连接中，请稍等片刻====================");
		// 1.2 数据服务器检查是否连接成功
		while (!node.isRemoteNodeConnected(Distr.NODE_DB)) {
			try {
				Thread.sleep(10);
				node.pulse();
			} catch (InterruptedException e) {
				LogCore.core.error(ExceptionUtils.getStackTrace(e));
			}
		}
		// 1.3 平台服务器检查是否连接成功
//		while (!node.isRemoteNodeConnected(DistrPF.NODE_ID)) {
//			try {
//				Thread.sleep(10);
//				node.pulse();
//			} catch (InterruptedException e) {
//				LogCore.core.error(ExceptionUtils.getStackTrace(e));
//			}
//		}
		Log.game.info("====================成功建立连接了，启动世界服====================");
		
		// 跨服服务器
		for(int i = 0; i < Config.CROSS_SERVER_NUM; i++){
			int crossServerId = Config.crossSrvIDMap.get(i);
			node.addRemoteNode(Config.getCrossDefaultNodeId(crossServerId), Distr.getCrossNodeAddr(i,Distr.CROSS_NODE_DEFAULT));
		}
		// 1.3 平台服务器
		node.addRemoteNode(DistrPF.NODE_ID, Distr.getNodeAddr(DistrPF.NODE_ID));
		
		// 1.4 游戏服务器
		for (int i = 0; i < D.NODE_WORLD_STARTUP_NUM; i++) {
			// 不用连接自己
			if (i == worldNum)
				continue;
			// 远程nodeId
			String id = D.NODE_WORLD_PREFIX + i;
			// 连接远程
			node.addRemoteNode(id, Distr.getNodeAddr(id));
		}
		
		// 等待1秒，再继续初始化
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			LogCore.core.error(ExceptionUtils.getStackTrace(e1));
//		}
		
		//启动远程数据日志
		String logPath = Utils.class.getClassLoader().getResource("operlog.properties").getPath();
		LogOp.init(logPath);
		
		/* 2 加载系统数据 */
		// 2.1 创建个临时Port
		DefaultPort portDef = new DefaultPort(Config.getGameWorldPartDefaultPortId());
		portDef.startup(node);

		// 2.2 加载系统数据
		//FieldTable.init();// 初始化表信息
		final InitFieldTable servInitData = new InitFieldTable();
		portDef.addQueue(new PortPulseQueue() {
			@Override
			public void execute(Port port) {
				servInitData.init();
			}
		});
		
		//等待加载完成
		while(!servInitData.isCompleted()) {
			try {
				Thread.sleep(10);
				node.pulse();
			} catch (InterruptedException e) {
				LogCore.core.error(ExceptionUtils.getStackTrace(e));
			}
		}

		// 2.3整合服务
		SeamService seamServ = new SeamService(portDef);
		seamServ.startup();
		portDef.addService(seamServ);
		
		// 2.4平台服务
		PFService pfService = new PFService(portDef);
		pfService.startup();
		portDef.addService(pfService);

		/* 3 启动系统默认服务 */
		// 只在默认Node上启动
		if (partId.equals(Distr.NODE_DEFAULT)) {
			// 登陆服务
			AccountService servGate = new AccountService(portDef);
			servGate.startup();
			portDef.addService(servGate);
			
			// 初始化地图全局服务 必须在初始化地图前就开启本服务 所以就先放这了
			StageGlobalService servStageGlobal = new StageGlobalService(portDef);
			servStageGlobal.startup();
			portDef.addService(servStageGlobal);
			
			//运行信息统计
			if(Config.STATISTICS_ENABLE) {
				StatisticsService statisticsService = new StatisticsService(portDef);
				statisticsService.startup();
				portDef.addService(statisticsService);
			}
		}

		// 发布服务器初始化开始事件
		Event.fire(EventKey.GameStartUpBefore, "node", node);

		// Node正式启动
		node.startup();
		// 启动日志信息
		Log.game.info("====================");
		Log.game.info(nodeId + " started.");
		Log.game.info("Listen:" + nodeAddr);
		Log.game.info("====================");
		
		// 发布服务器初始化结束事件
		Event.fire(EventKey.GameStartUpFinish, "node", node, "port", portDef);
		
		// 连接服务器
		if (worldNum == 0) {
			ConnStartup.startup(node);
		}
	}
	
	private static void startupPlatformService() {
		// 创建Node
		Node node = new Node(DistrPF.NODE_ID, DistrPF.NODE_ADDR);
		
		//初始化环境
		ListenerInit.init(EventPF.instance);
		
		game.platform.ListenerInit.init(EventPF.instance);// 0616
		
		// 启动登录验证Port
		for (int i = 0; i < DistrPF.PORT_STARTUP_NUM_LOGIN; i++) {
			LoginPort loginPort = new LoginPort(DistrPF.PORT_LOGIN_PREFIX + i);
			loginPort.startup(node);

			// 启动验证服务
			LoginService loginServ = new LoginService(loginPort);
			loginServ.startup();
			loginPort.addService(loginServ);
		}
		
		//http异步请求Port
		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_HTTP_ASYNC; i++) {
			HttpAsyncPort asyncPort = new HttpAsyncPort(DistrPF.PORT_HTTP_ASYNC_PREFIX + i);
			asyncPort.startup(node);	
			
			//启动http异步请求服务
			HttpAsyncSendService httpAsyncS = new HttpAsyncSendService(asyncPort);
			httpAsyncS.startup();
			asyncPort.addService(httpAsyncS);
		}
		
		// 启动礼包码Port
		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_GIFT; i++) {
			GiftPort gift = new GiftPort(DistrPF.PORT_GIFT_PREFIX + i);
			gift.startup(node);
			
			// 启动验证服务
			GiftService giftServ = new GiftService(gift);
			giftServ.startup();
			gift.addService(giftServ);
		}
		
		//启动上传聊天Port
		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_CHAT; i++) {
			ChatPort chat = new ChatPort(DistrPF.PORT_CHAT_PREFIX + i);
			chat.startup(node);
		
			//启动验证服务
			ChatService chatServ = new ChatService(chat);
			chatServ.startup();
			chat.addService(chatServ);
		}
		
		// 启动HttpPort
		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_HTTP; i++) {
			HttpPort port = new HttpPort(DistrPF.PORT_HTTP_PREFIX + i);
			port.startup(node);
			
			// HTTP服务记录登陆Port
			HttpPort.addPort(port);
		}
		
		// 启动HTTP服务
		new HttpServer().start();
		
		// 启动Node
		node.startup();
		// 启动日志信息
		LogPF.platform.info("================================================");
		LogPF.platform.info(DistrPF.NODE_ID + " started.");
		LogPF.platform.info("Listen:" + DistrPF.NODE_ADDR);
		LogPF.platform.info("================================================");
	}
	
}
