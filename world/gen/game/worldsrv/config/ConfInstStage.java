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
 * 副本关卡编号 [zhCN]
 * InstStage.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfInstStage {
	public final int sn;			//副本关卡编号
	public final int chapterSN;			//所属章节编号
	public final int type;			//关卡类型
	public final int mapSN;			//地图编号
	public final String name;			//关卡名称
	public final int preStageSN;			//前置关卡编号
	public final int power;			//推荐战力
	public final int[] monsterSN;			//怪物编号
	public final int fightNum;			//次数限制
	public final int needLevel;			//等级限制
	public final int needManual;			//体力限制
	public final int costItemSN;			//进入花费道具SN
	public final int costItemNumber;			//进入花费数量
	public final int limitSec;			//通关限时秒数：0即不限时
	public final int[] storyIds;			//剧情ID
	public final int[] tempArmy;			//我方临时阵型
	public final int tempFormation;			//临时阵型
	public final int[] winCondition;			//怪物逃跑
	public final int fucId;			//功能引导ID
	public final int rewardsSN;			//首次通关奖励
	public final int dropInfosSN;			//结算物品
	public final int exp;			//结算经验
	public final int coin;			//结算铜币
	public final boolean repression;			//战力压制控制

	public ConfInstStage(int sn, int chapterSN, int type, int mapSN, String name, int preStageSN, int power, int[] monsterSN, int fightNum, int needLevel, int needManual, int costItemSN, int costItemNumber, int limitSec, int[] storyIds, int[] tempArmy, int tempFormation, int[] winCondition, int fucId, int rewardsSN, int dropInfosSN, int exp, int coin, boolean repression) {
			this.sn = sn;		
			this.chapterSN = chapterSN;		
			this.type = type;		
			this.mapSN = mapSN;		
			this.name = name;		
			this.preStageSN = preStageSN;		
			this.power = power;		
			this.monsterSN = monsterSN;		
			this.fightNum = fightNum;		
			this.needLevel = needLevel;		
			this.needManual = needManual;		
			this.costItemSN = costItemSN;		
			this.costItemNumber = costItemNumber;		
			this.limitSec = limitSec;		
			this.storyIds = storyIds;		
			this.tempArmy = tempArmy;		
			this.tempFormation = tempFormation;		
			this.winCondition = winCondition;		
			this.fucId = fucId;		
			this.rewardsSN = rewardsSN;		
			this.dropInfosSN = dropInfosSN;		
			this.exp = exp;		
			this.coin = coin;		
			this.repression = repression;		
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
	public static Collection<ConfInstStage> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfInstStage[] findArray() {
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
	public static ConfInstStage get(Integer sn) {
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
	public static ConfInstStage getBy(Object...params) {
		List<ConfInstStage> list = utilBase(params);
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
	public static List<ConfInstStage> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfInstStage> utilBase(Object...params) {
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
		List<ConfInstStage> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfInstStage c : DATA.getList()) {
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
	private int compareTo(ConfInstStage cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfInstStage cell, Object...params) {
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
			case "chapterSN": {
				value = this.chapterSN;
				break;
			}
			case "type": {
				value = this.type;
				break;
			}
			case "mapSN": {
				value = this.mapSN;
				break;
			}
			case "name": {
				value = this.name;
				break;
			}
			case "preStageSN": {
				value = this.preStageSN;
				break;
			}
			case "power": {
				value = this.power;
				break;
			}
			case "monsterSN": {
				value = this.monsterSN;
				break;
			}
			case "fightNum": {
				value = this.fightNum;
				break;
			}
			case "needLevel": {
				value = this.needLevel;
				break;
			}
			case "needManual": {
				value = this.needManual;
				break;
			}
			case "costItemSN": {
				value = this.costItemSN;
				break;
			}
			case "costItemNumber": {
				value = this.costItemNumber;
				break;
			}
			case "limitSec": {
				value = this.limitSec;
				break;
			}
			case "storyIds": {
				value = this.storyIds;
				break;
			}
			case "tempArmy": {
				value = this.tempArmy;
				break;
			}
			case "tempFormation": {
				value = this.tempFormation;
				break;
			}
			case "winCondition": {
				value = this.winCondition;
				break;
			}
			case "fucId": {
				value = this.fucId;
				break;
			}
			case "rewardsSN": {
				value = this.rewardsSN;
				break;
			}
			case "dropInfosSN": {
				value = this.dropInfosSN;
				break;
			}
			case "exp": {
				value = this.exp;
				break;
			}
			case "coin": {
				value = this.coin;
				break;
			}
			case "repression": {
				value = this.repression;
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
		public static final String sn = "sn";	//副本关卡编号
		public static final String chapterSN = "chapterSN";	//所属章节编号
		public static final String type = "type";	//关卡类型
		public static final String mapSN = "mapSN";	//地图编号
		public static final String name = "name";	//关卡名称
		public static final String preStageSN = "preStageSN";	//前置关卡编号
		public static final String power = "power";	//推荐战力
		public static final String monsterSN = "monsterSN";	//怪物编号
		public static final String fightNum = "fightNum";	//次数限制
		public static final String needLevel = "needLevel";	//等级限制
		public static final String needManual = "needManual";	//体力限制
		public static final String costItemSN = "costItemSN";	//进入花费道具SN
		public static final String costItemNumber = "costItemNumber";	//进入花费数量
		public static final String limitSec = "limitSec";	//通关限时秒数：0即不限时
		public static final String storyIds = "storyIds";	//剧情ID
		public static final String tempArmy = "tempArmy";	//我方临时阵型
		public static final String tempFormation = "tempFormation";	//临时阵型
		public static final String winCondition = "winCondition";	//怪物逃跑
		public static final String fucId = "fucId";	//功能引导ID
		public static final String rewardsSN = "rewardsSN";	//首次通关奖励
		public static final String dropInfosSN = "dropInfosSN";	//结算物品
		public static final String exp = "exp";	//结算经验
		public static final String coin = "coin";	//结算铜币
		public static final String repression = "repression";	//战力压制控制
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfInstStage> _map;
		//按表顺序存储的数组
		private static ConfInstStage[] _array;

		public static ConfInstStage[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfInstStage> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfInstStage> getMap() {
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
			Map<Integer, ConfInstStage> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfInstStage[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfInstStage object = new ConfInstStage(conf.getIntValue("sn"), conf.getIntValue("chapterSN"), conf.getIntValue("type"), conf.getIntValue("mapSN"), 
				conf.getString("name"), conf.getIntValue("preStageSN"), conf.getIntValue("power"), parseIntArray(conf.getString("monsterSN")), 
				conf.getIntValue("fightNum"), conf.getIntValue("needLevel"), conf.getIntValue("needManual"), conf.getIntValue("costItemSN"), 
				conf.getIntValue("costItemNumber"), conf.getIntValue("limitSec"), parseIntArray(conf.getString("storyIds")), parseIntArray(conf.getString("tempArmy")), 
				conf.getIntValue("tempFormation"), parseIntArray(conf.getString("winCondition")), conf.getIntValue("fucId"), conf.getIntValue("rewardsSN"), 
				conf.getIntValue("dropInfosSN"), conf.getIntValue("exp"), conf.getIntValue("coin"), conf.getBooleanValue("repression"));
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
				String basePath = Utils.getClassPath("json/ConfInstStage.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfInstStage.json");
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
