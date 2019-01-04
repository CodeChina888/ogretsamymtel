package core.connsrv.main;

import core.Node;
import core.Port;
import core.connsrv.ConnPort;
import core.connsrv.ConnService;
import core.connsrv.netty.Server;
import core.support.Distr;
import core.support.log.LogCore;

public class ConnStartup {
	public static Node CONN_NODE;

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		// 设置个默认值 便于系统调试
		if (args.length == 0) {
			args = new String[]{"0"};
		}

		// 验证参数数量
		if (args.length < 1) {
			System.out.println("useage: servId");
			return;
		}

		// 设置log4j2日志文件名称
		System.setProperty("logFileName", "conn" + args[0]);

		// 创建Node
		String serverId = args[0];
		String nodeId = Distr.NODE_CONNECT_PREFIX + serverId;
		String nodeAddr = Distr.getNodeAddr(nodeId);
		Node node = new Node(nodeId, nodeAddr);
		
		// 启动连接服务
		startup(node);
		node.startup();
		// 启动日志信息
		LogCore.core.info("================================================");
		LogCore.core.info(nodeId + " started.");
		LogCore.core.info("Listen:" + nodeAddr);
		LogCore.core.info("ServerId:" + serverId);
		LogCore.core.info("================================================");
		
		// 系统关闭时进行清理
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {

			}
		});
	}

	/**
	 * 启动连接服务
	 * @param node
	 */
	public static void startup(Node node) {
		// 设置启动node
		CONN_NODE = node;

		// 启动Port
		for (int i = 0; i < Distr.PORT_STARTUP_NUM_CONN; i++) {
			Port port = new ConnPort(Distr.PORT_CONNECT_PREFIX + i);
			port.startup(node);

			// 每个Port有一个默认服务
			ConnService connService = new ConnService(port);
			connService.startup();
			port.addService(connService);
		}

		// 启动socket监听
		new Server().start();
	}
	/**
	 * 启动连接服务
	 * @param node
	 */
	public static void startup(Node node, int interval) {
		// 设置启动node
		CONN_NODE = node;

		// 启动Port
		for (int i = 0; i < Distr.PORT_STARTUP_NUM_CONN; i++) {
			Port port = new ConnPort(Distr.PORT_CONNECT_PREFIX + i, interval);
			port.startup(node);

			// 每个Port有一个默认服务
			ConnService connService = new ConnService(port);
			connService.startup();
			port.addService(connService);
		}

		// 启动socket监听
		new Server().start();
	}
}
