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
 * 活动副本sn(=type*10000+type2*100+difficulty) [zhCN]
 * InstActConfig.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfInstActConfig {
	public final int sn;			//活动副本sn(=type*10000+type2*100+difficulty)
	public final int type;			//1级类型(0-99)
	public final int type2;			//2级类型(0-99)
	public final int difficulty;			//关卡难度(0-99)
	public final int instSn;			//关联副本sn
	public final int reviveType;			//复活类型
	public final int reviveSecond;			//复活倒计时秒数
	public final int preSn;			//前置副本sn
	public final int lvEnter;			//进入等级
	public final int numMin;			//最少人数
	public final int numMax;			//最大人数
	public final int numFit;			//推荐人数
	public final int pvpMatchSec;			//PVP匹配秒数
	public final int dayJoinNum;			//每天可参与次数
	public final int dayAwardNum;			//每天给予奖励次数
	public final int passAward;			//通关奖励礼包(手动领取)
	public final int baseAward;			//固定奖励礼包
	public final int winAward;			//胜利奖励礼包
	public final int loseAward;			//失败奖励礼包
	public final int tieAward;			//平局奖励礼包
	public final int extraAward;			//额外奖励礼包
	public final int[] startHourExtra;			//额外奖励小时段
	public final int[] endHourExtra;			//额外奖励小时段
	public final int[] openHour;			//开启小时点
	public final int[] openMinute;			//开启分钟点
	public final int[] totalMinute;			//开放总分钟数
	public final int[] openWeekDay;			//每周星期几开放

	public ConfInstActConfig(int sn, int type, int type2, int difficulty, int instSn, int reviveType, int reviveSecond, int preSn, int lvEnter, int numMin, int numMax, int numFit, int pvpMatchSec, int dayJoinNum, int dayAwardNum, int passAward, int baseAward, int winAward, int loseAward, int tieAward, int extraAward, int[] startHourExtra, int[] endHourExtra, int[] openHour, int[] openMinute, int[] totalMinute, int[] openWeekDay) {
			this.sn = sn;		
			this.type = type;		
			this.type2 = type2;		
			this.difficulty = difficulty;		
			this.instSn = instSn;		
			this.reviveType = reviveType;		
			this.reviveSecond = reviveSecond;		
			this.preSn = preSn;		
			this.lvEnter = lvEnter;		
			this.numMin = numMin;		
			this.numMax = numMax;		
			this.numFit = numFit;		
			this.pvpMatchSec = pvpMatchSec;		
			this.dayJoinNum = dayJoinNum;		
			this.dayAwardNum = dayAwardNum;		
			this.passAward = passAward;		
			this.baseAward = baseAward;		
			this.winAward = winAward;		
			this.loseAward = loseAward;		
			this.tieAward = tieAward;		
			this.extraAward = extraAward;		
			this.startHourExtra = startHourExtra;		
			this.endHourExtra = endHourExtra;		
			this.openHour = openHour;		
			this.openMinute = openMinute;		
			this.totalMinute = totalMinute;		
			this.openWeekDay = openWeekDay;		
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
	public static Collection<ConfInstActConfig> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfInstActConfig[] findArray() {
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
	public static ConfInstActConfig get(Integer sn) {
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
	public static ConfInstActConfig getBy(Object...params) {
		List<ConfInstActConfig> list = utilBase(params);
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
	public static List<ConfInstActConfig> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfInstActConfig> utilBase(Object...params) {
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
		List<ConfInstActConfig> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfInstActConfig c : DATA.getList()) {
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
	private int compareTo(ConfInstActConfig cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfInstActConfig cell, Object...params) {
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
			case "type2": {
				value = this.type2;
				break;
			}
			case "difficulty": {
				value = this.difficulty;
				break;
			}
			case "instSn": {
				value = this.instSn;
				break;
			}
			case "reviveType": {
				value = this.reviveType;
				break;
			}
			case "reviveSecond": {
				value = this.reviveSecond;
				break;
			}
			case "preSn": {
				value = this.preSn;
				break;
			}
			case "lvEnter": {
				value = this.lvEnter;
				break;
			}
			case "numMin": {
				value = this.numMin;
				break;
			}
			case "numMax": {
				value = this.numMax;
				break;
			}
			case "numFit": {
				value = this.numFit;
				break;
			}
			case "pvpMatchSec": {
				value = this.pvpMatchSec;
				break;
			}
			case "dayJoinNum": {
				value = this.dayJoinNum;
				break;
			}
			case "dayAwardNum": {
				value = this.dayAwardNum;
				break;
			}
			case "passAward": {
				value = this.passAward;
				break;
			}
			case "baseAward": {
				value = this.baseAward;
				break;
			}
			case "winAward": {
				value = this.winAward;
				break;
			}
			case "loseAward": {
				value = this.loseAward;
				break;
			}
			case "tieAward": {
				value = this.tieAward;
				break;
			}
			case "extraAward": {
				value = this.extraAward;
				break;
			}
			case "startHourExtra": {
				value = this.startHourExtra;
				break;
			}
			case "endHourExtra": {
				value = this.endHourExtra;
				break;
			}
			case "openHour": {
				value = this.openHour;
				break;
			}
			case "openMinute": {
				value = this.openMinute;
				break;
			}
			case "totalMinute": {
				value = this.totalMinute;
				break;
			}
			case "openWeekDay": {
				value = this.openWeekDay;
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
		public static final String sn = "sn";	//活动副本sn(=type*10000+type2*100+difficulty)
		public static final String type = "type";	//1级类型(0-99)
		public static final String type2 = "type2";	//2级类型(0-99)
		public static final String difficulty = "difficulty";	//关卡难度(0-99)
		public static final String instSn = "instSn";	//关联副本sn
		public static final String reviveType = "reviveType";	//复活类型
		public static final String reviveSecond = "reviveSecond";	//复活倒计时秒数
		public static final String preSn = "preSn";	//前置副本sn
		public static final String lvEnter = "lvEnter";	//进入等级
		public static final String numMin = "numMin";	//最少人数
		public static final String numMax = "numMax";	//最大人数
		public static final String numFit = "numFit";	//推荐人数
		public static final String pvpMatchSec = "pvpMatchSec";	//PVP匹配秒数
		public static final String dayJoinNum = "dayJoinNum";	//每天可参与次数
		public static final String dayAwardNum = "dayAwardNum";	//每天给予奖励次数
		public static final String passAward = "passAward";	//通关奖励礼包(手动领取)
		public static final String baseAward = "baseAward";	//固定奖励礼包
		public static final String winAward = "winAward";	//胜利奖励礼包
		public static final String loseAward = "loseAward";	//失败奖励礼包
		public static final String tieAward = "tieAward";	//平局奖励礼包
		public static final String extraAward = "extraAward";	//额外奖励礼包
		public static final String startHourExtra = "startHourExtra";	//额外奖励小时段
		public static final String endHourExtra = "endHourExtra";	//额外奖励小时段
		public static final String openHour = "openHour";	//开启小时点
		public static final String openMinute = "openMinute";	//开启分钟点
		public static final String totalMinute = "totalMinute";	//开放总分钟数
		public static final String openWeekDay = "openWeekDay";	//每周星期几开放
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfInstActConfig> _map;
		//按表顺序存储的数组
		private static ConfInstActConfig[] _array;

		public static ConfInstActConfig[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfInstActConfig> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfInstActConfig> getMap() {
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
			Map<Integer, ConfInstActConfig> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfInstActConfig[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfInstActConfig object = new ConfInstActConfig(conf.getIntValue("sn"), conf.getIntValue("type"), conf.getIntValue("type2"), conf.getIntValue("difficulty"), 
				conf.getIntValue("instSn"), conf.getIntValue("reviveType"), conf.getIntValue("reviveSecond"), conf.getIntValue("preSn"), 
				conf.getIntValue("lvEnter"), conf.getIntValue("numMin"), conf.getIntValue("numMax"), conf.getIntValue("numFit"), 
				conf.getIntValue("pvpMatchSec"), conf.getIntValue("dayJoinNum"), conf.getIntValue("dayAwardNum"), conf.getIntValue("passAward"), 
				conf.getIntValue("baseAward"), conf.getIntValue("winAward"), conf.getIntValue("loseAward"), conf.getIntValue("tieAward"), 
				conf.getIntValue("extraAward"), parseIntArray(conf.getString("startHourExtra")), parseIntArray(conf.getString("endHourExtra")), parseIntArray(conf.getString("openHour")), 
				parseIntArray(conf.getString("openMinute")), parseIntArray(conf.getString("totalMinute")), parseIntArray(conf.getString("openWeekDay")));
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
				String basePath = Utils.getClassPath("json/ConfInstActConfig.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfInstActConfig.json");
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
