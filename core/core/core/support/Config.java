package core.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import core.support.Sys;
import core.support.SysException;
import core.support.Utils;
import core.support.log.LogCore;

/**
 * 配置参数
 */
public class Config {
	// 配置文件名称及键名key
	private static final String CONFIG_NAME = "config.properties";

	// RPC通信秘钥
	public static final String CORE_SECRET_KEY = "01AKIKE^%";
	// 以下为键值value
	public static final long SERVER_STARTDATE; // 开服时间

	public static final int CONN_PORT; // 连接服务器端口
	public static final boolean CONN_ENCRYPT; // 是否加密网络消息
	public static final int CONN_PING_SECOND = 10; // 连接服务器的心跳检测秒数，默认10秒

	// 数据服务配置
	public static final String DB_SCHEMA; // 数据库名
	public static final String DB_URL; // 数据库连接串
	public static final String DB_USER; // 数据库用户名
	public static final String DB_PWD; // 数据库密码
	public static final int DB_CACHED_SYNC_SEC; // 缓存同步间隔(秒) 设置为0时关闭缓存

	//public static final int GATE_TIMEOUT_SEC; // 多少秒后关闭处于创角阶段的玩家连接(默认150秒)
	public static final int STATIS_PULSE_INTERVAL; // 统计心跳间隔，记录超时心跳(默认1000毫秒)

	public static final String GAME_PLATFORM_NAME; // 运营平台名称
	public static final int GAME_PLATFORM_ID; // 运营平台ID[1,91]：不可设置为0，1到91为可设置范围
	public static final int GAME_SERVER_ID;// 游戏区服ID[0,9999]：0为测试服，1到9999为正式服
	public static final String SERVER_ID; // 游戏区服ID(4位数字组成的字符串，如："0001")

    public static final boolean PAYBACK; //公测返还
	
	public static final boolean STATISTICS_ENABLE; // 启动执行信息统计
	public static final int STATISTICS_TOP_NUM; // 列出高消耗列的数量
	public static final int STATISTICS_RESULT_TIME;// 显示统计结果间隔(秒)
	
	public static final boolean DEBUG_ENABLE; // 是否启动调试，开启则不处理心跳检测
	
	public static final String ID_SPLIT = "~";	// 分隔符
	// 前缀标识
	public static final String GAME_DEFAULT_NODE_PREFIX = "world";
	public static final String GAME_DEFAULT_PORT_PREFIX = "port";
	
	//服务器分支ID，由启动时赋值
	public static int SERVER_PART_ID = 0;
	//默认非跨服配置，由跨服启动时赋值
	public static boolean isCrossSrv = false;
	
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	//配置文件名称
	private static final String CONFIG_CROSS_NAME = "configCross.properties";
	public static int CROSS_PLUSE_INTERVAL = 5;		//跨服心跳时间，默认5ms
	//前缀标识
	public static final String CROSS_DEFAULT_NODE_PREFIX = "cross";
	public static final String CROSS_DEFAULT_PORT_PREFIX = "crossPort";
	//跨服服务器数量及当前启动的跨服ID
	public static int CROSS_SERVER_NUM;		//跨服服务器数量
	public static int CROSS_SERVER_INDEX;		//跨服服务器索引
	public static int CROSS_SERVER_ID;		//跨服服务器ID
	//保存所有的跨服ID配置:<跨服索引index,跨服id>
	public static final Map<Integer, Integer> crossSrvIDMap = new HashMap<>();
	//保存所有的跨服连接IP及端口配置:<跨服索引index,<跨服分支ID,跨服分支连接IP及端口>>
	public static final Map<Integer, Map<Integer, String>> crossSrvConnMap = new HashMap<>();
	//保存所有的跨服连接IP及端口配置:<跨服索引index,<跨服分支ID,跨服分支udp连接IP及端口>>
	public static final Map<Integer, Map<Integer, String>> crossSrvUdpConnMap = new HashMap<>();
	//保存所有的跨服数据库schema配置:<跨服索引index,跨服数据库schema>
	public static final Map<Integer, String> crossSrvDBSchemaMap = new HashMap<>();
	//保存所有的跨服数据库url配置:<跨服索引index,跨服数据库url>
	public static final Map<Integer, String> crossSrvDBUrlMap = new HashMap<>();
	//保存所有的跨服数据库user配置:<跨服索引index,跨服数据库user>
	public static final Map<Integer, String> crossSrvDBUserMap = new HashMap<>();
	//保存所有的跨服数据库pwd配置:<跨服索引index,跨服数据库pwd>
	public static final Map<Integer, String> crossSrvDBPwdMap = new HashMap<>();
	//保存所有的跨服数据库cached配置:<跨服索引index,跨服数据库cached>
	public static final Map<Integer, Integer> crossSrvDBCachedMap = new HashMap<>();
	//保存所有的跨服nodeAddr配置:<跨服索引index,<跨服nodeId,跨服nodeAddr>>
	public static final Map<Integer, Map<String,String>> crossSrvNodeAddrMap= new HashMap<>();
	//跨服的数据库设置
	public static String CROSS_DB_SCHEMA;		//跨服数据库名
	public static String CROSS_DB_URL;		//跨服数据库连接串
	public static String CROSS_DB_USER;		//跨服数据库用户名
	public static String CROSS_DB_PWD;		//跨服数据库密码
	public static int CROSS_DB_CACHED;		//跨服数据库缓存同步间隔
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	static {
		//游戏服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 获取配置
		Properties prop = Utils.readProperties(CONFIG_NAME);

		// 开服时间，格式："yyyy/MM/dd HH:mm"
		if (prop.containsKey("server.startDate")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			try {
				String startDate = prop.getProperty("server.startDate");
				Date date = sdf.parse(startDate);
				SERVER_STARTDATE = date.getTime();
				LogCore.core.info("===开服时间：{}, 解析date:{}===", startDate, date);
			} catch (ParseException e) {
				throw new SysException("==={}配置错误，请检查：开服时间", CONFIG_NAME);
			}
		} else {
			throw new SysException("==={}配置错误，请检查：开服时间", CONFIG_NAME);
		}
		
		// 连接服务器设置
		if (prop.containsKey("conn.port")) {
			CONN_PORT = Utils.intValue(prop.getProperty("conn.port"));
		} else {// 默认连接端口10000
			CONN_PORT = 10000;
		}
		if (prop.containsKey("conn.encrypt")) {
			CONN_ENCRYPT = Utils.booleanValue(prop.getProperty("conn.encrypt"));
		} else {// 默认加密true
			CONN_ENCRYPT = true;
		}

		// 数据服务配置
		DB_SCHEMA = prop.getProperty("db.schema");
		String db_url = prop.getProperty("db.url");
		if (db_url.indexOf(DB_SCHEMA) == -1) {
			// db.url=jdbc:mysql://127.0.0.1:3306/
			if (!db_url.endsWith("/")) {
				db_url += "/";
			}
			// db.url=jdbc:mysql://127.0.0.1:3306/gameDB?useUnicode=true&characterEncoding=utf8
			db_url += (DB_SCHEMA + "?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false");
		}
		DB_URL = db_url;
		DB_USER = prop.getProperty("db.user");
		DB_PWD = prop.getProperty("db.pwd");
		// 缓存同步间隔
		if (Sys.isWin()) {// windows下认为是开发环境，1秒同步一次数据到数据库
			DB_CACHED_SYNC_SEC = 1;
		} else {// 非windows下认为是运营环境，默认10秒同步一次
			if (prop.containsKey("db.cached.sync.sec")) {
				DB_CACHED_SYNC_SEC = Utils.intValue(prop.getProperty("db.cached.sync.sec"));
			} else {
				DB_CACHED_SYNC_SEC = 10;
			}
		}

		// 多少秒后关闭处于创角阶段的玩家连接(默认150秒)
//		if (prop.containsKey("gate.timeout.sec")) {
//			GATE_TIMEOUT_SEC = Utils.intValue(prop.getProperty("gate.timeout.sec"));
//		} else {
//			GATE_TIMEOUT_SEC = 150;
//		}

		// 统计心跳间隔，记录超时心跳(默认1000毫秒)
		if (prop.containsKey("statis.pulse.interval")) {
			STATIS_PULSE_INTERVAL = Utils.intValue(prop.getProperty("statis.pulse.interval"));
		} else {
			STATIS_PULSE_INTERVAL = 1000;
		}
		
		// 运营平台名称
		if (prop.containsKey("game.platform.name")) {
			GAME_PLATFORM_NAME = prop.getProperty("game.platform.name");
		} else {
			GAME_PLATFORM_NAME = "test";// test测试平台
		}
		// 运营平台ID[1,91]：不可设置为0，1到91为可设置范围
		if (prop.containsKey("game.platform.id")) {
			GAME_PLATFORM_ID = Utils.intValue(prop.getProperty("game.platform.id"));
		} else {
			GAME_PLATFORM_ID = -1;
		}
		if (GAME_PLATFORM_ID < 1 || GAME_PLATFORM_ID > 91) {
			throw new SysException("==={}配置错误，请检查：运营平台ID={}", CONFIG_NAME, GAME_PLATFORM_ID);
		}
		// 游戏区服ID[0,9999]：0为测试服，1到9999为正式服
		if (prop.containsKey("game.server.id")) {
			GAME_SERVER_ID = Utils.intValue(prop.getProperty("game.server.id"));
		} else {
			GAME_SERVER_ID = -1;
		}
		if (GAME_SERVER_ID < 0 || GAME_SERVER_ID > 9999) {
			throw new SysException("==={}配置错误，请检查：游戏区服ID={}", CONFIG_NAME, GAME_SERVER_ID);
		}
		SERVER_ID = String.format("%04d", GAME_SERVER_ID);
        if (prop.containsKey("game.payback")) {
            PAYBACK = Utils.booleanValue(prop.getProperty("game.payback"));
        } else {
            PAYBACK = false;
        }

		// 启动执行信息统计
		if (prop.containsKey("statistics.enable")) {
			STATISTICS_ENABLE = Utils.booleanValue(prop.getProperty("statistics.enable"));
		} else {
			STATISTICS_ENABLE = false;
		}
		// 列出高消耗列的数量
		if (prop.containsKey("statistics.top.num")) {
			STATISTICS_TOP_NUM = Utils.intValue(prop.getProperty("statistics.top.num"));
		} else {
			STATISTICS_TOP_NUM = 20;
		}
		// 显示统计结果间隔(秒)
		if (prop.containsKey("statistics.result.time")) {
			STATISTICS_RESULT_TIME = Utils.intValue(prop.getProperty("statistics.result.time"));
		} else {
			STATISTICS_RESULT_TIME = 300;
		}
		
		// 是否启动调试，开启则不处理心跳检测
		if (prop.containsKey("debug.enable")) {
			DEBUG_ENABLE = Utils.booleanValue(prop.getProperty("debug.enable"));
		} else {// 默认关闭调试false
			DEBUG_ENABLE = false;
		}
		//游戏服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
		Properties propCross = Utils.readProperties(CONFIG_CROSS_NAME);//获取跨服配置
		//跨服服务器数量
		CROSS_SERVER_NUM = Utils.intValue(propCross.getProperty("cross.server.num"));
		for(int i = 0; i < CROSS_SERVER_NUM; i++){
			//跨服ID配置
			int crossSrvID = Utils.intValue(propCross.getProperty("crossSrv" + i + ".id"));
			if(crossSrvID >= 0){
				crossSrvIDMap.put(i, crossSrvID);
			}
			//跨服连接IP及端口配置
			int connNum = Utils.intValue(propCross.getProperty("crossSrv" + i + ".conn.num"));
			Map<Integer, String> connIpPortMap = new HashMap<>();
			Map<Integer, String> udpConnIpPortMap = new HashMap<>();
			for (int j = 0; j < connNum; j++) {
				String crossSrvConn = propCross.getProperty("crossSrv" + i + ".conn" + j + ".ipPort");
				if(crossSrvConn != null){
					connIpPortMap.put(j, crossSrvConn);
				}
				String crossSrvUdpConn = propCross.getProperty("crossSrv" + i + ".udpConn" + j + ".ipPort");
				if(crossSrvUdpConn != null){
					udpConnIpPortMap.put(j, crossSrvUdpConn);
				}
			}
			crossSrvConnMap.put(i, connIpPortMap);			
			crossSrvUdpConnMap.put(i, udpConnIpPortMap);
			//跨服数据库schema配置
			String crossSrvDBSchema = propCross.getProperty("crossSrv" + i + ".db.schema");
			if(crossSrvDBSchema != null){
				crossSrvDBSchemaMap.put(i, crossSrvDBSchema);
			}
			//跨服数据库url配置
			String crossSrvDBUrl = propCross.getProperty("crossSrv" + i + ".db.url");
			if (crossSrvDBUrl.indexOf(crossSrvDBSchema) == -1) {
				// db.url=jdbc:mysql://127.0.0.1:3306/
				if (!crossSrvDBUrl.endsWith("/")) {
					crossSrvDBUrl += "/";
				}
				// db.url=jdbc:mysql://127.0.0.1:3306/gameDB?useUnicode=true&characterEncoding=utf8
				crossSrvDBUrl += (crossSrvDBSchema + "?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false");
			}
			if(crossSrvDBUrl != null){
				crossSrvDBUrlMap.put(i, crossSrvDBUrl);
			}
			//跨服数据库user配置
			String crossSrvDBUser = propCross.getProperty("crossSrv" + i + ".db.user");
			if(crossSrvDBUser != null){
				crossSrvDBUserMap.put(i, crossSrvDBUser);
			}
			//跨服数据库pwd配置
			String crossSrvDBPwd = propCross.getProperty("crossSrv" + i + ".db.pwd");
			if(crossSrvDBPwd != null){
				crossSrvDBPwdMap.put(i, crossSrvDBPwd);
			}
			//跨服数据库cached配置
			int crossSrvDBCached = Utils.intValue(propCross.getProperty("crossSrv" + i + ".db.cached.sync.sec"));
			if(crossSrvDBCached >= 0){
				crossSrvDBCachedMap.put(i, crossSrvDBCached);
			}
			//跨服nodeAddr配置
			String nodeAddrPrefix = "crossSrv" + i + ".node.addr";
			Map<String,String> nodeAddr = new HashMap<>();
			for(Entry<Object, Object> e : propCross.entrySet()){
				//配置的值
				String pk = (String) e.getKey();
				String pv = (String) e.getValue();
				
				//分解Key为前缀和关键字
				int lastIndexPoint = pk.lastIndexOf(".");
				String prefix = pk.substring(0, lastIndexPoint);
				String key = pk.substring(lastIndexPoint + 1);
				//Node地址配置
				if(nodeAddrPrefix.equals(prefix)) {
					nodeAddr.put(key, pv);
				}
				crossSrvNodeAddrMap.put(i, nodeAddr);
			}
		}
		CROSS_SERVER_INDEX = Utils.intValue(propCross.getProperty("cross.server.index"));
		if (CROSS_SERVER_NUM > 0) {
            CROSS_SERVER_ID = crossSrvIDMap.get(CROSS_SERVER_INDEX);
            //跨服的数据库设置
            CROSS_DB_SCHEMA = crossSrvDBSchemaMap.get(CROSS_SERVER_INDEX);
            CROSS_DB_URL = crossSrvDBUrlMap.get(CROSS_SERVER_INDEX);
            CROSS_DB_USER = crossSrvDBUserMap.get(CROSS_SERVER_INDEX);
            CROSS_DB_PWD = crossSrvDBPwdMap.get(CROSS_SERVER_INDEX);
            CROSS_DB_CACHED = crossSrvDBCachedMap.get(CROSS_SERVER_INDEX);
        }
		//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
		
	}
	
	/**
	 * 获取游戏服分支实际NodeId
	 * @param worldNodeId 配置的分支nodeId
	 * @return
	 */
	public static String getGameWorldPartDefaultNodeId(String worldNodeId) {
		return Config.GAME_SERVER_ID + Config.ID_SPLIT + worldNodeId;
	}
	/**
	 * 获取游戏服分支实际NodeId
	 * @param worldPartId 分支ID
	 * @return
	 */
	public static String getGameWorldPartDefaultNodeId(int worldPartId) {
		return Config.GAME_SERVER_ID + Config.ID_SPLIT+ Config.GAME_DEFAULT_NODE_PREFIX + worldPartId;
	}
	/**
	 * 获取本进程游戏服默认分支实际NodeId
	 * @return
	 */
	public static String getGameWorldPartDefaultNodeId() {
		return Config.GAME_SERVER_ID + Config.ID_SPLIT + Config.GAME_DEFAULT_NODE_PREFIX + Config.SERVER_PART_ID;
	}
	/**
	 * 获取本进程游戏服默认分支实际portId
	 * @return
	 */
	public static String getGameWorldPartDefaultPortId() {
		return Config.GAME_DEFAULT_PORT_PREFIX + Config.SERVER_PART_ID;
	}
	/**
	 * 获取本服游戏服分支实际portId
	 * @param worldPart 分支ID
	 * @return
	 */
	public static String getGameWorldPartDefaultPortId(int worldPart) {
		return Config.GAME_DEFAULT_PORT_PREFIX + worldPart;
	}
	//游戏服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取跨服主NodeId
	 * @param serverId
	 * @return
	 */
	public static String getCrossDefaultNodeId(int serverId){
		return serverId + Config.ID_SPLIT + Distr.CROSS_NODE_DEFAULT;
	}
	/**
	 * 获取跨服NodeId
	 * @param serverId
	 * @param nodeId
	 * @return
	 */
	public static String getCrossNodeId(int serverId, String nodeId){
		return serverId + Config.ID_SPLIT + nodeId;
	}

	/**
	 * 获取跨服分支实际NodeId
	 * @param crossNodeId 配置的分支nodeId
	 * @return
	 */
	public static String getCrossPartDefaultNodeId(String crossNodeId){
		return Config.CROSS_SERVER_ID + Config.ID_SPLIT + crossNodeId;
	}

	/**
	 * 获取跨服分支实际NodeId
	 * @param crossPartId 分支ID
	 * @return
	 */
	public static String getCrossPartDefaultNodeId(int crossPartId){
		return Config.CROSS_SERVER_ID + Config.ID_SPLIT+ Config.CROSS_DEFAULT_NODE_PREFIX + crossPartId;
	}

	/**
	 * 获取本进程跨服默认分支实际NodeId
	 * @return
	 */
	public static String getCrossPartDefaultNodeId(){
		return Config.CROSS_SERVER_ID + Config.ID_SPLIT + Config.CROSS_DEFAULT_NODE_PREFIX + Config.SERVER_PART_ID;
	}

	/**
	 *  获取本服跨服分支实际portId
	 * @param crossPart 分支ID
	 * @return
	 */
	public static String getCrossPartDefaultPortId(int crossPart){
		return Config.CROSS_DEFAULT_PORT_PREFIX + crossPart;
	}
	
	/**
	 *  获取本进程跨服默认分支实际portId
	 * @return
	 */
	public static String getCrossPartDefaultPortId(){
		return Config.CROSS_DEFAULT_PORT_PREFIX + Config.SERVER_PART_ID;
	}
	
	/**
	 * 获取本进程跨服默认连接Ip
	 * @return
	 */
	public static String getCrossMainNodeConnIp(){
		Map<Integer, String> connIpPortMap = crossSrvConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = connIpPortMap.get(Config.SERVER_PART_ID);
		int index = ipPort.indexOf(":");
		String ip = ipPort.substring(0, index);
		return ip;
	}
	/**
	 * 获取本进程跨服默认连接port
	 * @return
	 */
	public static int getCrossMainNodeConnPort(){
		Map<Integer, String> connIpPortMap = crossSrvConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = connIpPortMap.get(Config.SERVER_PART_ID);
		int index = ipPort.indexOf(":");
		int port = Utils.intValue(ipPort.substring(index+1));
		return port;
	}
	/**
	 * 获取本进程跨服分支连接Ip
	 * @return
	 */
	public static String getCrossMainNodeConnIp(int partId){
		Map<Integer, String> connIpPortMap = crossSrvConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = connIpPortMap.get(partId);
		int index = ipPort.indexOf(":");
		String ip = ipPort.substring(0, index);
		return ip;
	}
	/**
	 * 获取本进程跨服分支连接port
	 * @return
	 */
	public static int getCrossMainNodeConnPort(int partId){
		Map<Integer, String> connIpPortMap = crossSrvConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = connIpPortMap.get(partId);
		int index = ipPort.indexOf(":");
		int port = Utils.intValue(ipPort.substring(index+1));
		return port;
	}

	/**
	 * 获取本进程跨服默认连接Ip
	 * @return
	 */
	public static String getCrossMainNodeUdpConnIp(){
		Map<Integer, String> udpConnIpPortMap = crossSrvUdpConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = udpConnIpPortMap.get(Config.SERVER_PART_ID);
		int index = ipPort.indexOf(":");
		String ip = ipPort.substring(0, index);
		return ip;
	}
	/**
	 * 获取本进程跨服默认连接port
	 * @return
	 */
	public static int getCrossMainNodeUdpConnPort(){
		Map<Integer, String> udpConnIpPortMap = crossSrvUdpConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = udpConnIpPortMap.get(Config.SERVER_PART_ID);
		int index = ipPort.indexOf(":");
		int port = Utils.intValue(ipPort.substring(index+1));
		return port;
	}
	/**
	 * 获取本进程跨服分支连接Ip
	 * @return
	 */
	public static String getCrossMainNodeUdpConnIp(int partId){
		Map<Integer, String> udpConnIpPortMap = crossSrvUdpConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = udpConnIpPortMap.get(partId);
		int index = ipPort.indexOf(":");
		String ip = ipPort.substring(0, index);
		return ip;
	}
	/**
	 * 获取本进程跨服分支连接port
	 * @return
	 */
	public static int getCrossMainNodeUdpConnPort(int partId){
		Map<Integer, String> udpConnIpPortMap = crossSrvUdpConnMap.get(CROSS_SERVER_INDEX);
		String ipPort = udpConnIpPortMap.get(partId);
		int index = ipPort.indexOf(":");
		int port = Utils.intValue(ipPort.substring(index+1));
		return port;
	}
	/**
	 * 获取本服nodeId的实际ID
	 * @param nodeId 配置的nodeId
	 * @return
	 */
	public static String getDefaultNodeId(String nodeId){
		if(Distr.nodeServerTypeIsCommon(nodeId)){
			return nodeId;
		}
		if(Distr.nodeServerTypeIsCross(nodeId)){
			return getCrossPartDefaultNodeId(nodeId);
		}
		if(Distr.nodeServerTypeIsGame(nodeId)){
			return getGameWorldPartDefaultNodeId(nodeId);
		}
		return nodeId;
	}
	
	/**
	 * 获取本服crossNodeAddr
	 * @param nodeId
	 * @return
	 */
	public static String getCrossNodeAddr(String nodeId){
		Map<String,String> nodeAddrMap = crossSrvNodeAddrMap.get(CROSS_SERVER_INDEX);
		return nodeAddrMap.get(nodeId);
	}
	
	/**
	 * 获取crossNodeAddr
	 * @param crossIndex
	 * @param nodeId
	 * @return
	 */
	public static String getCrossNodeAddr(int crossIndex, String nodeId){
		Map<String,String> nodeAddrMap = crossSrvNodeAddrMap.get(crossIndex);
		return nodeAddrMap.get(nodeId);
	}
	
}