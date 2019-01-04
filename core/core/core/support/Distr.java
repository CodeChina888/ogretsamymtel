package core.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import core.support.Utils;

/**
 * 节点设置
 */
public class Distr {
	// 配置文件名称
	private static final String DISTR_NAME = "distr.properties";

	// 配置前缀
	private static final String PREFIX_NODE_ADDR = "node.addr";
	private static final String PREFIX_PORT_STARTWITH = "port.startwith";
	private static final String PREFIX_SERV_STARTWITH = "serv.startwith";
	private static final String PREFIX_NODE_SERVER_TYPE = "node.type";

	// Node地址
	private static final Map<String, String> nodeAddrMap = new HashMap<>();
	// Port所属Node
	private static final Map<String, String> portStartwithMap = new HashMap<>();
	// Service所属Port:<服务名,端口名>，例如：<name,game1>
	private static final Map<String, String> servStartwithMap = new HashMap<>();
	// Port下属的所有服务Service:<端口名,服务名列表>，例如：<game1,[name,compete,...]>
	private static final Map<String, ArrayList<String>> servInPortMap = new HashMap<>();
	// Node所属类型
	private static final Map<String, String> nodeTypeMap = new HashMap<>();

	// 默认参数
	public static final String NODE_DEFAULT = "world0"; // 默认主控游戏服务Node
	public static final String PORT_DEFAULT = "port0"; // 默认主控游戏服务Port
	public static final String SERV_DEFAULT = "serv0"; // 默认主控游戏服务Serv

	// 前缀
	public static final String NODE_CONNECT_PREFIX = "conn"; // 连接服务器Node前缀
	public static final String PORT_CONNECT_PREFIX = "conn"; // 连接服务器Port前缀
	public static final String PORT_DB_PART_PREFIX = "dbPart"; // 数据分配Port前缀
	public static final String PORT_DB_LINE_PREFIX = "dbLine"; // 数据执行Port前缀

	// Node
	public static final String NODE_DB = "db"; // 数据库Node名称

	// Port
	public static final String PORT_ID_ALLOT = "idAllot"; // ID分配Port

	// Service
	public static final String SERV_CONN = "conn"; // 连接总服务
	public static final String SERV_SEAM = "seam"; // 整合服务
	public static final String SERV_GATE = "gate"; // 选人服务
	
	public static final String SERV_ID_ALLOT = "idAllot"; // ID分配服务
	public static final String SERV_STATISTICS = "statistics"; // 运行信息统计服务

	// 其他配置
	public static final int PORT_STARTUP_NUM_CONN; // 连接服务实例数
	public static final int PORT_STARTUP_NUM_DB_PART; // 数据分配服务实例数
	public static final int PORT_STARTUP_NUM_DB_LINE; // 数据执行服务实例数
	
	public static final String NODE_SERVER_TYPE_GAME = "world";//游戏服节点所属名
	
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String CROSS_NODE_DEFAULT = "cross0";					//默认主控游戏服务Node
	public static final String CROSS_PORT_DEFAULT = "crossPort0";				//默认主控游戏服务Port
	public static final String CROSS_NODE_CONNECT_PREFIX = "crossConn";			//跨服连接服务器Node前缀
	public static final String CROSS_DEFAULT_PORT_PREFIX = "crossPort";			//跨服默认服务器Port前缀
	public static final String CROSS_NODE_DB = "crossDB";						//跨服数据库Node名称
	public static final String CROSS_PORT_DB_PART_PREFIX = "crossDBPart";		//数据分配Port前缀
	public static final String CROSS_PORT_DB_LINE_PREFIX = "crossDBLine";		//数据执行Port前缀
	public static final String CROSS_SERV_ID_ALLOT = "crossIdAllot";			//ID分配服务
	public static final String CROSS_PORT_ID_ALLOT = "crossIdAllot";			//ID分配Port
	public static final String NODE_SERVER_TYPE_CROSS = "cross";				//跨服节点所属名
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////

	static {
		// 核心配置
		Properties prop = Utils.readProperties(DISTR_NAME);
		// 连接服务配置
		PORT_STARTUP_NUM_CONN = Integer.valueOf(prop.getProperty("port.startup.num.conn"));
		PORT_STARTUP_NUM_DB_PART = Integer.parseInt(prop.getProperty("port.startup.num.dbPart"));
		PORT_STARTUP_NUM_DB_LINE = Integer.parseInt(prop.getProperty("port.startup.num.dbLine"));
				
		// 遍历配置
		for (Entry<Object, Object> e : prop.entrySet()) {
			// 配置的值
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			if (key.startsWith(PREFIX_NODE_ADDR)) {
				// Node地址配置
				String keyName = key.substring(PREFIX_NODE_ADDR.length() + 1);
				nodeAddrMap.put(keyName, value);
			} else if (key.startsWith(PREFIX_PORT_STARTWITH)) {
				// Port启动配置
				String keyName = key.substring(PREFIX_PORT_STARTWITH.length() + 1);
				portStartwithMap.put(keyName, value);
			} else if (key.startsWith(PREFIX_SERV_STARTWITH)) {
				// Service启动配置
				String keyName = key.substring(PREFIX_SERV_STARTWITH.length() + 1);
				servStartwithMap.put(keyName, value);
				// keyName为服务名，value为端口名
				ArrayList<String> listServ = servInPortMap.get(value);
				if (listServ != null) {
					listServ.add(keyName);
				} else {
					listServ = new ArrayList<>();
					listServ.add(keyName);
					servInPortMap.put(value, listServ);
				}
			} else if(key.startsWith(PREFIX_NODE_SERVER_TYPE)) {
				//Node所属server类型配置
				String keyName = key.substring(PREFIX_NODE_SERVER_TYPE.length() + 1);
				nodeTypeMap.put(keyName, value);
			}
		}
		
	}
	
	/**
	 * 获取Node连接地址
	 * @param nodeId
	 * @return
	 */
	public static String getNodeAddr(String nodeId) {
		String addr = nodeAddrMap.get(nodeId);
		if(addr == null) {
			String[] s = nodeId.split(Config.ID_SPLIT);
			if(s.length > 1) {
				String realNodeId = s[s.length-1];
				if(nodeServerTypeIsCross(realNodeId)) {
					return Config.getCrossNodeAddr(realNodeId);
				}
				addr = nodeAddrMap.get(realNodeId);
			} else {
				if(nodeServerTypeIsCross(nodeId)) {
					return Config.getCrossNodeAddr(nodeId);
				}
			}
		}
		return addr;
	}

	/**
	 * 通过Port来获取父节点Node
	 * @param portId
	 * @return
	 */
	public static String getNodeId(String portId) {
		String nodeId = portStartwithMap.get(portId);
		if(nodeId == null) {
			return null;
		}
		return Config.getDefaultNodeId(nodeId);
	}
	/**
	 * 通过Port来获取父节点Node
	 * @param portId
	 * @return
	 */
	public static String getPartNodeId(String portId) {
		return portStartwithMap.get(portId);
	}
	/**
	 * 通过Serv来获取父节点Port
	 * @param servId
	 * @return
	 */
	public static String getPortId(String servId) {
		return servStartwithMap.get(servId);
	}
	
	/**
	 * 获取端口下属的所有服务
	 * @param portId
	 * @return
	 */
	public static ArrayList<String> getPortServ(String portId) {
		return servInPortMap.get(portId);
	}

	/**
	 * 通过Node来获取子节点Port集合
	 * @return
	 */
	public static List<String> getPortIds(String nodeId) {
		List<String> ids = new ArrayList<>();
		for (String id : portIds()) {
			if (nodeId.equals(getNodeId(id))) {
				ids.add(id);
			}
		}
		return ids;
	}

	/**
	 * 通过Port来获取子节点Serv集合
	 * @return
	 */
	public static List<String> getServIds(String portId) {
		List<String> ids = new ArrayList<>();
		for (String id : servIds()) {
			if (portId.equals(getPortId(id))) {
				ids.add(id);
			}
		}
		return ids;
	}

	/**
	 * 所有Port的关键字
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<String> portIds() {
		return new ArrayList(portStartwithMap.keySet());
	}

	/**
	 * 所有Serv的关键字
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<String> servIds() {
		return new ArrayList(servStartwithMap.keySet());
	}

	
	/**
	 * 获取crossNode连接地址
	 * @param crossIndex
	 * @param nodeId
	 * @return
	 */
	public static String getCrossNodeAddr(int crossIndex, String nodeId) {
		if(nodeServerTypeIsCommon(nodeId)) {
			return nodeAddrMap.get(nodeId);
		}
		String addr = nodeAddrMap.get(nodeId);
		if(addr == null) {
			String[] s = nodeId.split(Config.ID_SPLIT);
			if(s.length > 1) {
				String realNodeId = s[s.length-1];
				if(nodeServerTypeIsCross(realNodeId)) {
					return Config.getCrossNodeAddr(crossIndex,realNodeId);
				}
				addr = nodeAddrMap.get(realNodeId);
			} else {
				if(nodeServerTypeIsCross(nodeId)) {
					return Config.getCrossNodeAddr(crossIndex,nodeId);
				}
			}
		}
		return addr;
	}
	/**
	 * 判断节点是否是跨服节点
	 * @param nodeId
	 * @return
	 */
	public static boolean nodeServerTypeIsCross(String nodeId){
		String type = nodeTypeMap.get(nodeId);
		if(type == null){
			return false;
		}
		if(NODE_SERVER_TYPE_CROSS.equals(type)){
			return true;
		}
		return false;
	}
	/**
	 * 判断节点是否是游戏服节点
	 * @param nodeId
	 * @return
	 */
	public static boolean nodeServerTypeIsGame(String nodeId){
		String type = nodeTypeMap.get(nodeId);
		if(type == null){
			return false;
		}
		if(NODE_SERVER_TYPE_GAME.equals(type)){
			return true;
		}
		return false;
	}
	/**
	 * 判断节点是否是普通节点
	 * @param nodeId
	 * @return
	 */
	public static boolean nodeServerTypeIsCommon(String nodeId){
		String type = nodeTypeMap.get(nodeId);
		if(type == null){
			return true;
		}
		if(NODE_SERVER_TYPE_GAME.equals(type)){
			return false;
		}
		if(NODE_SERVER_TYPE_CROSS.equals(type)){
			return false;
		}
		return true;
	}
	/**
	 * 增加Port和Node的对应关系
	 * @param key
	 * @param value
	 */
	public static void addStartPort(String key, String value) {
		portStartwithMap.put(key, value);
	}
	/**
	 * 增加Serv和Port的对应关系
	 * @param key
	 * @param value
	 */
	public static void addStartServ(String key, String value) {
		servStartwithMap.put(key, value);
	}
}