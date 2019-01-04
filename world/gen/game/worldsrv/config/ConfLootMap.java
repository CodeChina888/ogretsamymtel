package game.worldsrv.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.support.ConfigJSON;
import core.support.IReloadSupport;
import core.support.OrderBy;
import core.support.OrderByField;
import core.support.SysException;
import core.support.Utils;

/**
 * 编号 [zhCN]
 * LootMap.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfLootMap {
	public final int sn;			//编号
	public final int type;			//入口类型
	public final int level;			//层
	public final int group;			//组
	public final int mapWeight;			//地图权重
	public final int[] layoutSn;			//刷新物件id
	public final int sceneId;			//场景编号
	public final int generateType;			//生成类型
	public final int generateMask;			//生成遮罩
	public final int warProtect;			//战斗保护
	public final int lifeRecovery;			//生命回复
	public final int birthDistance;			//出生距离
	public final int warScene;			//战斗场景
	public final int closeDoor;			//是否关闭传送门
	public final int mapSn;			//地图id
	public final int mission;			//是否有任务
	public final int[] missionSn;			//任务sn
	public final int[] missionWeight;			//任务权重

	public ConfLootMap(int sn, int type, int level, int group, int mapWeight, int[] layoutSn, int sceneId, int generateType, int generateMask, int warProtect, int lifeRecovery, int birthDistance, int warScene, int closeDoor, int mapSn, int mission, int[] missionSn, int[] missionWeight) {
			this.sn = sn;		
			this.type = type;		
			this.level = level;		
			this.group = group;		
			this.mapWeight = mapWeight;		
			this.layoutSn = layoutSn;		
			this.sceneId = sceneId;		
			this.generateType = generateType;		
			this.generateMask = generateMask;		
			this.warProtect = warProtect;		
			this.lifeRecovery = lifeRecovery;		
			this.birthDistance = birthDistance;		
			this.warScene = warScene;		
			this.closeDoor = closeDoor;		
			this.mapSn = mapSn;		
			this.mission = mission;		
			this.missionSn = missionSn;		
			this.missionWeight = missionWeight;		
	}

	private static IReloadSupport support = null;
	
	public static void initReloadSupport(IReloadSupport s){
		support = s;
	}

	public static void reLoad() {
		if(support != null)
			support.beforeReload();

		DATA._init();

		if(support != null)
			support.afterReload();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfLootMap> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfLootMap[] findArray() {
		return DATA.getArray();
	}

	/**
	 * 是否存在key=sn的数据
	 * @param sn
	 * @return
	 */
	public static boolean containsKey(Integer sn) {
		return DATA.getMap().containsKey(sn);
	}
	
	/**
	 * 通过sn获取数据
	 * @param sn
	 * @return
	 */
	public static ConfLootMap get(Integer sn) {
		if(DATA.getMap().containsKey(sn)) {
			return DATA.getMap().get(sn);
		} else {
			return null;
		}
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfLootMap getBy(Object...params) {
		List<ConfLootMap> list = utilBase(params);
		if(list.isEmpty()) 
			return null;
		else 
			return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfLootMap> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfLootMap> utilBase(Object...params) {
		List<Object> settings = Utils.ofList(params);
		
		//查询参数
		final Map<String, Object> paramsFilter = new LinkedHashMap<>();	//过滤条件
		final List<OrderByField> paramsOrder = new ArrayList<>();		//排序规则
				
		//参数数量
		int len = settings.size();
		
		//参数必须成对出现
		if(len % 2 != 0) {
			throw new SysException("查询参数必须成对出现:query={}", settings);
		}
		
		//处理成对参数
		for(int i = 0; i < len; i += 2) {
			String key = (String)settings.get(i);
			Object val = settings.get(i + 1);
			
			//参数 排序规则
			if(val instanceof OrderBy) {
				paramsOrder.add(new OrderByField(key, (OrderBy) val));
			} else {	//参数 过滤条件
				paramsFilter.put(key, val);
			}
		}
		
		//返回结果
		List<ConfLootMap> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfLootMap c : DATA.getList()) {
				//本行数据是否符合过滤条件
				boolean find = true;
				
				//判断过滤条件
				for(Entry<String, Object> p : paramsFilter.entrySet()) {
					//实际结果
					Object valTrue = c.getFieldValue(p.getKey());
					//期望结果
					Object valWish = p.getValue();
					
					//有不符合过滤条件的
					if(!valWish.equals(valTrue)) {
						find = false;
						break;
					}
				}
				
				//记录符合结果
				if(find) {
					result.add(c);
				}
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
		
		//对结果进行排序
		Collections.sort(result, (a, b) -> a.compareTo(b, paramsOrder));
		
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int compareTo(ConfLootMap cell, List<OrderByField> paramsOrder) {
		try {
			for(OrderByField e : paramsOrder) {
				//两方字段值
				Comparable va = this.getFieldValue(e.getKey());
				Comparable vb = cell.getFieldValue(e.getKey());
				
				//值排序结果
				int compareResult = va.compareTo(vb);
				
				//相等时 根据下一个值进行排序
				if(va.compareTo(vb) == 0) 
					continue;
				
				//配置排序规则
				OrderBy order = e.getOrderBy();
				if(order == OrderBy.ASC) 
					return compareResult;		//正序
				else 
					return -1 * compareResult;					//倒序
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
		return 0;
	}

	/**
	 * 比较函数
	 * @param cell 比较的对象
	 * @param params 自定义排序字段
	 * @return
	 */
	public int compare(ConfLootMap cell, Object...params) {
		List<Object> settings = Utils.ofList(params);
		List<OrderByField> paramsOrder = new ArrayList<>();		//排序规则
		
		//参数数量
		int len = settings.size();
		
		//参数必须成对出现
		if(len % 2 != 0) {
			throw new SysException("查询参数必须成对出现:query={}", settings);
		}
		
		//处理成对参数
		for(int i = 0; i < len; i += 2) {
			String key = (String)settings.get(i);
			Object val = settings.get(i + 1);
			
			//参数 排序规则
			if(val instanceof OrderBy) {
				paramsOrder.add(new OrderByField(key, (OrderBy) val));
			}
		}
		return compareTo(cell, paramsOrder);
	}

	/**
	 * 取得属性值
	 * @param classInstance 实例
	 * @key 属性名称
	 */
	@SuppressWarnings("unchecked")
	public <T> T getFieldValue(String key) {
		Object value = null;
		switch (key) {
			case "sn": {
				value = this.sn;
				break;
			}
			case "type": {
				value = this.type;
				break;
			}
			case "level": {
				value = this.level;
				break;
			}
			case "group": {
				value = this.group;
				break;
			}
			case "mapWeight": {
				value = this.mapWeight;
				break;
			}
			case "layoutSn": {
				value = this.layoutSn;
				break;
			}
			case "sceneId": {
				value = this.sceneId;
				break;
			}
			case "generateType": {
				value = this.generateType;
				break;
			}
			case "generateMask": {
				value = this.generateMask;
				break;
			}
			case "warProtect": {
				value = this.warProtect;
				break;
			}
			case "lifeRecovery": {
				value = this.lifeRecovery;
				break;
			}
			case "birthDistance": {
				value = this.birthDistance;
				break;
			}
			case "warScene": {
				value = this.warScene;
				break;
			}
			case "closeDoor": {
				value = this.closeDoor;
				break;
			}
			case "mapSn": {
				value = this.mapSn;
				break;
			}
			case "mission": {
				value = this.mission;
				break;
			}
			case "missionSn": {
				value = this.missionSn;
				break;
			}
			case "missionWeight": {
				value = this.missionWeight;
				break;
			}
			default: 
				break;
		}
		return (T) value;
	}

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String sn = "sn";	//编号
		public static final String type = "type";	//入口类型
		public static final String level = "level";	//层
		public static final String group = "group";	//组
		public static final String mapWeight = "mapWeight";	//地图权重
		public static final String layoutSn = "layoutSn";	//刷新物件id
		public static final String sceneId = "sceneId";	//场景编号
		public static final String generateType = "generateType";	//生成类型
		public static final String generateMask = "generateMask";	//生成遮罩
		public static final String warProtect = "warProtect";	//战斗保护
		public static final String lifeRecovery = "lifeRecovery";	//生命回复
		public static final String birthDistance = "birthDistance";	//出生距离
		public static final String warScene = "warScene";	//战斗场景
		public static final String closeDoor = "closeDoor";	//是否关闭传送门
		public static final String mapSn = "mapSn";	//地图id
		public static final String mission = "mission";	//是否有任务
		public static final String missionSn = "missionSn";	//任务sn
		public static final String missionWeight = "missionWeight";	//任务权重
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfLootMap> _map;
		//按表顺序存储的数组
		private static ConfLootMap[] _array;

		public static ConfLootMap[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfLootMap> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfLootMap> getMap() {
			//延迟初始化
			if(_map == null) {
				synchronized (DATA.class) {
					if(_map == null) {
						_init();
					}
				}
			}
			return _map;
		}

		/**
		 * 初始化数据
		 */
		private static void _init() {
			Map<Integer, ConfLootMap> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfLootMap[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfLootMap object = new ConfLootMap(conf.getIntValue("sn"), conf.getIntValue("type"), conf.getIntValue("level"), conf.getIntValue("group"), 
				conf.getIntValue("mapWeight"), parseIntArray(conf.getString("layoutSn")), conf.getIntValue("sceneId"), conf.getIntValue("generateType"), 
				conf.getIntValue("generateMask"), conf.getIntValue("warProtect"), conf.getIntValue("lifeRecovery"), conf.getIntValue("birthDistance"), 
				conf.getIntValue("warScene"), conf.getIntValue("closeDoor"), conf.getIntValue("mapSn"), conf.getIntValue("mission"), 
				parseIntArray(conf.getString("missionSn")), parseIntArray(conf.getString("missionWeight")));
				dataMap.put(conf.getInteger("sn"), object);
				_array[i] = object;
			}
			
			//保存数据
			_map = Collections.unmodifiableMap(dataMap);
		}

		/**
		 * 读取游戏配置
		 */
		private static String _readConfFile() {
			String result = "";
			JarFile jar = null;
			FileInputStream fis = null;
			InputStreamReader isr = null;
			BufferedReader reader = null;
			try {
				String basePath = Utils.getClassPath("json/ConfLootMap.json");
				//System.out.println("===basePath=" + basePath);
				File file = new File(basePath);
				if(file.exists()) { // assets里找到了
					fis = new FileInputStream(file);
					if (fis != null) {
						isr = new InputStreamReader(fis, "UTF-8");
						//System.out.println("===assets里找到了，basePath=" + basePath);
					}
				} else { // assets里没有，到JAR包里找
					basePath = Utils.getClassPath("game-data.jar");
					jar = new JarFile(basePath);
					if (jar != null) {
						JarEntry dbEntry = jar.getJarEntry("json/ConfLootMap.json");
						if (dbEntry != null) {
							isr = new InputStreamReader(jar.getInputStream(dbEntry), "UTF-8");
							//System.out.println("===JAR包里找到了，basePath=" + basePath);
						}
					}
				}

				if (isr != null) {
					reader = new BufferedReader(isr);
					String tempString = "";
					while ((tempString = reader.readLine()) != null) {
						result += tempString;
					}
				}
				
			} catch (Exception e) {
			    throw new RuntimeException(e);
			} finally {
				// 关闭操作
				try {
					if (reader != null)
						reader.close();
					if(isr != null)
						isr.close();
					if(fis != null)
						fis.close();
					if(jar != null)
						jar.close();
				} catch (IOException e) {
				    throw new RuntimeException(e);
				}
			}
			return result;
		}
		
		public static double[] parseDoubleArray(String value) {
			return Utils.strToDoubleArray(value);
	  	}
		
		public static float[] parseFloatArray(String value) {
			return Utils.strToFloatArray(value);
		}
		
		public static int[] parseIntArray(String value) {
			return Utils.strToIntArray(value);
		}
		
		public static long[] parseLongArray(String value) {
			return Utils.strToLongArray(value);
		}
		
		public static String[] parseStringArray(String value) {
			return Utils.strToStrArray(value);
		}

	 	public static boolean[] parseBoolArray(String value) {
	 		return Utils.strToBoolArray(value);
	 	}
	}
    
}
