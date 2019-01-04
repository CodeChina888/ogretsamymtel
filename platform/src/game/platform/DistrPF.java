package game.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.support.Utils;

/**
 * 系统参数
 */
public class DistrPF {
	// 配置文件名称
	private static final String DISTR_NAME = "distr.properties";
	private static final String DISTR_PF_NAME = "distrPF.properties";

	// 前缀
	public static final String PORT_LOGIN_PREFIX = "login"; 		// 登陆服务Port前缀
	public static final String PORT_HTTP_PREFIX = "http";			// HTTP服务Port前缀
	public static final String PORT_GIFT_PREFIX = "gift";			// Gift服务Port前缀
	public static final String PORT_CHAT_PREFIX = "chat";			// chat服务Port前缀
	public static final String PORT_HTTP_ASYNC_PREFIX = "httpSend";	//chat服务Port前缀

	// 服务
	public static final String SERV_LOGIN = "login"; 				// 登陆服务ID
	public static final String SERV_WORLD_PF = "integration.PFService";// 平台服和世界服的通讯服务
	public static final String SERV_CHAT = "chat.ChatService";		// 聊天服务
	public static final String SERV_GIFT = "gift.GiftServer";		// 礼包服务（目前无用）
	public static final String SERV_HTTP_SEND = "httpSend";		//登陆验证过程异步http服务
		
	// 配置
	public static final String NODE_ID = "platform"; // NodeID
	public static final String NODE_ADDR; // Node地址

	// 验证服务实例数
	public static final int PORT_STARTUP_NUM_LOGIN;
	
	//验证服务实例数
	public static final int PORT_STARTUP_NUM_HTTP;
	
	//验证服务实例数
	public static final int PORT_STARTUP_NUM_GIFT;
	
	//验证服务实例数
	public static final int PORT_STARTUP_NUM_CHAT;
	
	//登陆验证过程异步http服务实例数
	public static final int PORT_STARTUP_NUM_HTTP_ASYNC;

	// 游戏登录SDK验证地址
	public static final String HTTP_SDK_TOKEN;
		
	// HTTP服务
	public static final String HTTP_IP;					// HTTP服务器IP	
	public static final int HTTP_PORT1;					// HTTP服务器端口
	public static final int HTTP_SYNC_PORT;				// HTTP同步端口
	
	// gm领取礼包服务
	public static final String HTTP_GM_IP;
	public static final int HTTP_GM_PORT;
	// 充值验证服务
	public static final String HTTP_GM_IP2;
	public static final int HTTP_GM_PORT2;
	
	// 游戏服务器密钥
	public static final String SECRET_KEY;
	
	public static final String HTTP_SESSION;
	
	// 充值上传记录
	public static final String HTTP_TALKING_GAME_SERVER;
	
	// 聊天记录上传IP
	public static final String HTTP_CHATSERVER_IP;		// 聊天监控服务器的ip
	public static final String HTTP_CHATSERVER_SERVICE;	// 聊天监控服务器的service
	public static final String HTTP_CHATSERVER_PRODUCTID; // 聊天监控服务器的产品ID
	
	public static List<String> httpAcceptIpList = new ArrayList<>();
	
	
	static {
		// 获取配置
		Properties prop = Utils.readProperties(DISTR_NAME);
		NODE_ADDR = prop.getProperty("node.addr.platform");
		
		// 获取配置
		Properties propPF = Utils.readProperties(DISTR_PF_NAME);
		
		PORT_STARTUP_NUM_LOGIN = Utils.intValue(propPF.getProperty("port.startup.num.login"));
		PORT_STARTUP_NUM_HTTP = Utils.intValue(propPF.getProperty("port.startup.num.http"));
		PORT_STARTUP_NUM_GIFT = Utils.intValue(propPF.getProperty("port.startup.num.gift"));
		PORT_STARTUP_NUM_CHAT = Utils.intValue(propPF.getProperty("port.startup.num.chat"));
		PORT_STARTUP_NUM_HTTP_ASYNC = Utils.intValue(propPF.getProperty("port.startup.num.httpAsync"));
		
		// 游戏登录SDK验证地址
		HTTP_SDK_TOKEN = propPF.getProperty("http.sdk.token");
		
		HTTP_IP = propPF.getProperty("http.ip");
		HTTP_PORT1 = Utils.intValue(propPF.getProperty("http.port1"));
		HTTP_SYNC_PORT = Utils.intValue(propPF.getProperty("http.sync.port"));
		
		HTTP_GM_IP = propPF.getProperty("http.gm.ip");
		HTTP_GM_PORT = Utils.intValue(propPF.getProperty("http.gm.port"));
		
		HTTP_GM_IP2 = propPF.getProperty("http.gm.ip2");
		HTTP_GM_PORT2 = Utils.intValue(propPF.getProperty("http.gm.port2"));
		
		httpAcceptIpList.add(propPF.getProperty("http.charge.ip"));
		httpAcceptIpList.add(HTTP_GM_IP);
		
		//服务器允许接收的IP
		for (int i = 1; i < 100; i++) {
			String ip = propPF.getProperty("http.accept.ip" + i);
			if (ip == null) {
				break;
			}
			httpAcceptIpList.add(ip);
		}
		
		SECRET_KEY = propPF.getProperty("game.secret.key");
		
		HTTP_SESSION = propPF.getProperty("http.session.key");
		
		HTTP_TALKING_GAME_SERVER = propPF.getProperty("http.talking.game.server");
		
		HTTP_CHATSERVER_IP = propPF.getProperty("http.chatserver.ip");
		HTTP_CHATSERVER_SERVICE = propPF.getProperty("http.chatserver.service");
		HTTP_CHATSERVER_PRODUCTID = propPF.getProperty("http.chatserver.productId");
	}
}