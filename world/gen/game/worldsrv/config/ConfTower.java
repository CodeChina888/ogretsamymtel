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
 * Tower.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfTower {
	public final int sn;			//编号
	public final int ChapterLevel;			//关卡数
	public final int matching;			//匹配类型
	public final int[] common1;			//匹配参数1
	public final int[] common2;			//匹配参数2
	public final int[] Range1;			//简单-对手战斗力系数区间
	public final int[] Range2;			//普通-对手战斗力系数区间
	public final int[] Range3;			//困难-对手战斗力系数区间
	public final String condition;			//通关条件类型
	public final int[] conditionWeight;			//条件权重
	public final int[] Chestlevel;			//宝箱等级间隔
	public final int[] Chest1;			//简单-免费宝箱掉落
	public final int[] CostChest1;			//简单-付费宝箱掉落
	public final int[] Chest2;			//普通-免费宝箱掉落
	public final int[] CostChest2;			//普通-付费宝箱掉落
	public final int[] Chest3;			//困难-免费宝箱掉落
	public final int[] CostChest3;			//困难-付费宝箱掉落
	public final int[] level;			//奖励等级间隔
	public final int[] reward1;			//简单-通关奖励
	public final int[] reward2;			//普通-通关奖励
	public final int[] reward3;			//困难-通关奖励
	public final int[] score1;			//简单-积分
	public final int[] score2;			//普通-积分
	public final int[] score3;			//困难-积分
	public final int[] Cost;			//付费宝箱元宝消耗

	public ConfTower(int sn, int ChapterLevel, int matching, int[] common1, int[] common2, int[] Range1, int[] Range2, int[] Range3, String condition, int[] conditionWeight, int[] Chestlevel, int[] Chest1, int[] CostChest1, int[] Chest2, int[] CostChest2, int[] Chest3, int[] CostChest3, int[] level, int[] reward1, int[] reward2, int[] reward3, int[] score1, int[] score2, int[] score3, int[] Cost) {
			this.sn = sn;		
			this.ChapterLevel = ChapterLevel;		
			this.matching = matching;		
			this.common1 = common1;		
			this.common2 = common2;		
			this.Range1 = Range1;		
			this.Range2 = Range2;		
			this.Range3 = Range3;		
			this.condition = condition;		
			this.conditionWeight = conditionWeight;		
			this.Chestlevel = Chestlevel;		
			this.Chest1 = Chest1;		
			this.CostChest1 = CostChest1;		
			this.Chest2 = Chest2;		
			this.CostChest2 = CostChest2;		
			this.Chest3 = Chest3;		
			this.CostChest3 = CostChest3;		
			this.level = level;		
			this.reward1 = reward1;		
			this.reward2 = reward2;		
			this.reward3 = reward3;		
			this.score1 = score1;		
			this.score2 = score2;		
			this.score3 = score3;		
			this.Cost = Cost;		
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
	public static Collection<ConfTower> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfTower[] findArray() {
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
	public static ConfTower get(Integer sn) {
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
	public static ConfTower getBy(Object...params) {
		List<ConfTower> list = utilBase(params);
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
	public static List<ConfTower> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfTower> utilBase(Object...params) {
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
		List<ConfTower> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfTower c : DATA.getList()) {
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
	private int compareTo(ConfTower cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfTower cell, Object...params) {
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
			case "ChapterLevel": {
				value = this.ChapterLevel;
				break;
			}
			case "matching": {
				value = this.matching;
				break;
			}
			case "common1": {
				value = this.common1;
				break;
			}
			case "common2": {
				value = this.common2;
				break;
			}
			case "Range1": {
				value = this.Range1;
				break;
			}
			case "Range2": {
				value = this.Range2;
				break;
			}
			case "Range3": {
				value = this.Range3;
				break;
			}
			case "condition": {
				value = this.condition;
				break;
			}
			case "conditionWeight": {
				value = this.conditionWeight;
				break;
			}
			case "Chestlevel": {
				value = this.Chestlevel;
				break;
			}
			case "Chest1": {
				value = this.Chest1;
				break;
			}
			case "CostChest1": {
				value = this.CostChest1;
				break;
			}
			case "Chest2": {
				value = this.Chest2;
				break;
			}
			case "CostChest2": {
				value = this.CostChest2;
				break;
			}
			case "Chest3": {
				value = this.Chest3;
				break;
			}
			case "CostChest3": {
				value = this.CostChest3;
				break;
			}
			case "level": {
				value = this.level;
				break;
			}
			case "reward1": {
				value = this.reward1;
				break;
			}
			case "reward2": {
				value = this.reward2;
				break;
			}
			case "reward3": {
				value = this.reward3;
				break;
			}
			case "score1": {
				value = this.score1;
				break;
			}
			case "score2": {
				value = this.score2;
				break;
			}
			case "score3": {
				value = this.score3;
				break;
			}
			case "Cost": {
				value = this.Cost;
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
		public static final String ChapterLevel = "ChapterLevel";	//关卡数
		public static final String matching = "matching";	//匹配类型
		public static final String common1 = "common1";	//匹配参数1
		public static final String common2 = "common2";	//匹配参数2
		public static final String Range1 = "Range1";	//简单-对手战斗力系数区间
		public static final String Range2 = "Range2";	//普通-对手战斗力系数区间
		public static final String Range3 = "Range3";	//困难-对手战斗力系数区间
		public static final String condition = "condition";	//通关条件类型
		public static final String conditionWeight = "conditionWeight";	//条件权重
		public static final String Chestlevel = "Chestlevel";	//宝箱等级间隔
		public static final String Chest1 = "Chest1";	//简单-免费宝箱掉落
		public static final String CostChest1 = "CostChest1";	//简单-付费宝箱掉落
		public static final String Chest2 = "Chest2";	//普通-免费宝箱掉落
		public static final String CostChest2 = "CostChest2";	//普通-付费宝箱掉落
		public static final String Chest3 = "Chest3";	//困难-免费宝箱掉落
		public static final String CostChest3 = "CostChest3";	//困难-付费宝箱掉落
		public static final String level = "level";	//奖励等级间隔
		public static final String reward1 = "reward1";	//简单-通关奖励
		public static final String reward2 = "reward2";	//普通-通关奖励
		public static final String reward3 = "reward3";	//困难-通关奖励
		public static final String score1 = "score1";	//简单-积分
		public static final String score2 = "score2";	//普通-积分
		public static final String score3 = "score3";	//困难-积分
		public static final String Cost = "Cost";	//付费宝箱元宝消耗
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfTower> _map;
		//按表顺序存储的数组
		private static ConfTower[] _array;

		public static ConfTower[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfTower> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfTower> getMap() {
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
			Map<Integer, ConfTower> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfTower[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfTower object = new ConfTower(conf.getIntValue("sn"), conf.getIntValue("ChapterLevel"), conf.getIntValue("matching"), parseIntArray(conf.getString("common1")), 
				parseIntArray(conf.getString("common2")), parseIntArray(conf.getString("Range1")), parseIntArray(conf.getString("Range2")), parseIntArray(conf.getString("Range3")), 
				conf.getString("condition"), parseIntArray(conf.getString("conditionWeight")), parseIntArray(conf.getString("Chestlevel")), parseIntArray(conf.getString("Chest1")), 
				parseIntArray(conf.getString("CostChest1")), parseIntArray(conf.getString("Chest2")), parseIntArray(conf.getString("CostChest2")), parseIntArray(conf.getString("Chest3")), 
				parseIntArray(conf.getString("CostChest3")), parseIntArray(conf.getString("level")), parseIntArray(conf.getString("reward1")), parseIntArray(conf.getString("reward2")), 
				parseIntArray(conf.getString("reward3")), parseIntArray(conf.getString("score1")), parseIntArray(conf.getString("score2")), parseIntArray(conf.getString("score3")), 
				parseIntArray(conf.getString("Cost")));
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
				String basePath = Utils.getClassPath("json/ConfTower.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfTower.json");
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
