package game.platform.main;

import game.platform.DistrPF;
import game.platform.HttpPort;
import game.platform.ListenerInit;
import game.platform.LogPF;
import game.platform.login.LoginPort;
import game.platform.login.LoginService;
import game.platform.observer.EventPF;
import game.platform.chat.ChatPort;
import game.platform.chat.ChatService;
import game.platform.gift.GiftPort;
import game.platform.gift.GiftService;
import core.Node;
import game.platform.http.HttpServer;

public class PlatformStartup {
	public static Node PLATFORM_NODE;

	public static void main(String[] args) {
		if(true)
			return;// 整合到world里启动了
		
//		// 设置log4j2日志文件名称
//		System.setProperty("logFileName", "platform");
//
//		// 创建Node
//		Node node = new Node(DistrPF.NODE_ID, DistrPF.NODE_ADDR);
//		
//		startup(node);
//		
//		// 启动Node
//		node.startup();
//		// 启动日志信息
//		LogPF.platform.info("================================================");
//		LogPF.platform.info(DistrPF.NODE_ID + " started.");
//		LogPF.platform.info("Listen:" + DistrPF.NODE_ADDR);
//		LogPF.platform.info("HTTP Accetp:" + DistrPF.HTTP_PORT1);
//		LogPF.platform.info("================================================");
//		
//		// 系统关闭时进行清理
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			public void run() {
//			}
//		});
	}
		
	public static void startup(Node node) {
//		//设置启动node
//		PLATFORM_NODE = node;
//		
//		//初始化环境
//		ListenerInit.init(EventPF.instance);
//		
//		// 启动登录验证Port
//		for (int i = 0; i < DistrPF.PORT_STARTUP_NUM_LOGIN; i++) {
//			LoginPort loginPort = new LoginPort(DistrPF.PORT_LOGIN_PREFIX + i);
//			loginPort.startup(node);
//
//			// 启动验证服务
//			LoginService loginServ = new LoginService(loginPort);
//			loginServ.startup();
//			loginPort.addService(loginServ);
//		}
//		
//		// 启动礼包码Port
//		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_GIFT; i++) {
//			GiftPort gift = new GiftPort(DistrPF.PORT_GIFT_PREFIX + i);
//			gift.startup(node);
//			
//			// 启动验证服务
//			GiftService giftServ = new GiftService(gift);
//			giftServ.startup();
//			gift.addService(giftServ);
//		}
//		
//		//启动上传聊天Port
//		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_CHAT; i++) {
//			ChatPort chat = new ChatPort(DistrPF.PORT_CHAT_PREFIX + i);
//			chat.startup(node);
//		
//			//启动验证服务
//			ChatService chatServ = new ChatService(chat);
//			chatServ.startup();
//			chat.addService(chatServ);
//		}
//		
//		// 启动HttpPort
//		for(int i = 0; i < DistrPF.PORT_STARTUP_NUM_HTTP; i++) {
//			HttpPort port = new HttpPort(DistrPF.PORT_HTTP_PREFIX + i);
//			port.startup(node);
//			
//			// HTTP服务记录登陆Port
//			HttpPort.addPort(port);
//		}
//		
//		// 启动HTTP服务
//		new HttpServer().start();
	}
}
