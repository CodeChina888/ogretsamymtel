package core.dbsrv.main;

import core.Node;
import core.Port;
import core.PortPulseQueue;
import core.Service;
import core.db.FieldTable;
import core.db.VerTables;
import core.dbsrv.DBLineService;
import core.dbsrv.DBPartService;
import core.dbsrv.DBPort;
import core.statistics.StatisticsService;
import core.support.Config;
import core.support.Distr;
import core.support.idAllot.CrossIdAllotService;
import core.support.idAllot.IdAllotService;
import core.support.log.LogCore;

public class DBStartup {

	public static void main(String[] args) {
		// 设置log4j2日志文件名称
		System.setProperty("logFileName", "db");
				
		//默认为游戏服数据库
		String dbSchema = Config.DB_SCHEMA;
		String dbUrl = Config.DB_URL;
		String dbUser = Config.DB_USER;
		String dbPwd = Config.DB_PWD;
		String dbNode = Distr.NODE_DB;
		String idAllot = Distr.PORT_ID_ALLOT;
		if(args != null && args.length > 0 && args[0].equals(Distr.CROSS_NODE_DB)) {
			Config.isCrossSrv = true;
			//跨服数据库
			dbSchema = Config.CROSS_DB_SCHEMA;
			dbUrl = Config.CROSS_DB_URL;
			dbUser = Config.CROSS_DB_USER;
			dbPwd = Config.CROSS_DB_PWD;
			dbNode = Config.getCrossPartDefaultNodeId(Distr.CROSS_NODE_DB);
			idAllot = Distr.CROSS_PORT_ID_ALLOT;
		}
		
		// 创建Node
		Node node = new Node(dbNode, Distr.getNodeAddr(dbNode));
		
		// 初始化表信息
		FieldTable.init(dbSchema, dbUrl, dbUser, dbPwd);
		// 初始化执行版本号
		VerTables.init(FieldTable.getTableNames());
		
		// 启动分配任务线程
		for (int i = 0; i < Distr.PORT_STARTUP_NUM_DB_PART; i++) {
			DBPort port = new DBPort(Distr.PORT_DB_PART_PREFIX + i);
			port.startup(node);

			// 分配服务
			DBPartService serv = new DBPartService(port);
			serv.startup();
			port.addService(serv);
		}

		// 启动执行任务线程
		for (int i = 0; i < Distr.PORT_STARTUP_NUM_DB_LINE; i++) {
			DBPort port = new DBPort(Distr.PORT_DB_LINE_PREFIX + i);
			port.startup(node);

			// 执行服务
			DBLineService serv = new DBLineService(port, dbUrl, dbUser, dbPwd);
			serv.startup();
			port.addService(serv);
		}

		// 启动ID分配服务
		DBPort portIdAllot = new DBPort(idAllot);
		portIdAllot.startup(node);
		portIdAllot.addQueue(new PortPulseQueue() {
			@Override
			public void execute(Port port) {
				if(Config.isCrossSrv) {
					CrossIdAllotService serv = new CrossIdAllotService(port);
					serv.startup();
					serv.init();
					port.addService(serv);
				} else {
					IdAllotService serv = new IdAllotService(port);
					serv.startup();
					serv.init();
					port.addService(serv);
				}
			}
		});

		// 启动运行信息统计
		if (Config.STATISTICS_ENABLE) {
			Service statisticsService = new StatisticsService(portIdAllot);
			statisticsService.startup();
			portIdAllot.addService(statisticsService);
		}

		// 启动Node
		node.startup();
		// 启动日志信息
		LogCore.core.info("================================================");
		LogCore.core.info("dbsrv started.");
		LogCore.core.info("Listen:" + Distr.getNodeAddr(dbNode));
		LogCore.core.info("================================================");
		
		// 系统关闭时进行清理
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					// 先等待2秒 世界服务清理数据
					Thread.sleep(2000);

					// 通知立即刷新缓存服务器
					for (int i = 0; i < Distr.PORT_STARTUP_NUM_DB_PART; i++) {
						Port port = node.getPort(Distr.PORT_DB_PART_PREFIX + i);
						// 在队列中进行清理
						port.addQueue(new PortPulseQueue() {
							@Override
							public void execute(Port port) {
								DBPartService serv = port.getService(Distr.SERV_DEFAULT);
								serv.flushAll();
							}
						});
					}

					// 再等待2秒 持久化数据
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
