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
 * 表snTag * 100000 + star*1000+lev [zhCN]
 * SkillGodsStar.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkillGodsStar {
	public final int sn;			//表snTag * 100000 + star*1000+lev
	public final int godsSn;			//神兽编号
	public final int Lev;			//神兽等级
	public final int star;			//神兽星级
	public final int lvQm;			//升星需要的等级
	public final int[] costMoney;			//消耗货币
	public final int[] costMoneyNum;			//消耗货币数量
	public final int[] costItem;			//消耗资源
	public final int[] costItemNum;			//消耗道具数量
	public final String[] attr;			//加成属性
	public final int[] attrValue;			//加成属性直接替换加成值
	public final int skillSn;			//当前星级指向的技能sn（Skill.sn）
	public final int[] talent;			//当前星级拥有的天赋
	public final int power;			//技能威力万份比值百分比显示
	public final int value;			//技能固定伤害
	public final int isMax;			//是否最高等级

	public ConfSkillGodsStar(int sn, int godsSn, int Lev, int star, int lvQm, int[] costMoney, int[] costMoneyNum, int[] costItem, int[] costItemNum, String[] attr, int[] attrValue, int skillSn, int[] talent, int power, int value, int isMax) {
			this.sn = sn;		
			this.godsSn = godsSn;		
			this.Lev = Lev;		
			this.star = star;		
			this.lvQm = lvQm;		
			this.costMoney = costMoney;		
			this.costMoneyNum = costMoneyNum;		
			this.costItem = costItem;		
			this.costItemNum = costItemNum;		
			this.attr = attr;		
			this.attrValue = attrValue;		
			this.skillSn = skillSn;		
			this.talent = talent;		
			this.power = power;		
			this.value = value;		
			this.isMax = isMax;		
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
	public static Collection<ConfSkillGodsStar> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfSkillGodsStar[] findArray() {
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
	public static ConfSkillGodsStar get(Integer sn) {
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
	public static ConfSkillGodsStar getBy(Object...params) {
		List<ConfSkillGodsStar> list = utilBase(params);
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
	public static List<ConfSkillGodsStar> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkillGodsStar> utilBase(Object...params) {
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
		List<ConfSkillGodsStar> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkillGodsStar c : DATA.getList()) {
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
	private int compareTo(ConfSkillGodsStar cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfSkillGodsStar cell, Object...params) {
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
			case "godsSn": {
				value = this.godsSn;
				break;
			}
			case "Lev": {
				value = this.Lev;
				break;
			}
			case "star": {
				value = this.star;
				break;
			}
			case "lvQm": {
				value = this.lvQm;
				break;
			}
			case "costMoney": {
				value = this.costMoney;
				break;
			}
			case "costMoneyNum": {
				value = this.costMoneyNum;
				break;
			}
			case "costItem": {
				value = this.costItem;
				break;
			}
			case "costItemNum": {
				value = this.costItemNum;
				break;
			}
			case "attr": {
				value = this.attr;
				break;
			}
			case "attrValue": {
				value = this.attrValue;
				break;
			}
			case "skillSn": {
				value = this.skillSn;
				break;
			}
			case "talent": {
				value = this.talent;
				break;
			}
			case "power": {
				value = this.power;
				break;
			}
			case "value": {
				value = this.value;
				break;
			}
			case "isMax": {
				value = this.isMax;
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
		public static final String sn = "sn";	//表snTag * 100000 + star*1000+lev
		public static final String godsSn = "godsSn";	//神兽编号
		public static final String Lev = "Lev";	//神兽等级
		public static final String star = "star";	//神兽星级
		public static final String lvQm = "lvQm";	//升星需要的等级
		public static final String costMoney = "costMoney";	//消耗货币
		public static final String costMoneyNum = "costMoneyNum";	//消耗货币数量
		public static final String costItem = "costItem";	//消耗资源
		public static final String costItemNum = "costItemNum";	//消耗道具数量
		public static final String attr = "attr";	//加成属性
		public static final String attrValue = "attrValue";	//加成属性直接替换加成值
		public static final String skillSn = "skillSn";	//当前星级指向的技能sn（Skill.sn）
		public static final String talent = "talent";	//当前星级拥有的天赋
		public static final String power = "power";	//技能威力万份比值百分比显示
		public static final String value = "value";	//技能固定伤害
		public static final String isMax = "isMax";	//是否最高等级
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfSkillGodsStar> _map;
		//按表顺序存储的数组
		private static ConfSkillGodsStar[] _array;

		public static ConfSkillGodsStar[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkillGodsStar> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkillGodsStar> getMap() {
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
			Map<Integer, ConfSkillGodsStar> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfSkillGodsStar[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfSkillGodsStar object = new ConfSkillGodsStar(conf.getIntValue("sn"), conf.getIntValue("godsSn"), conf.getIntValue("Lev"), conf.getIntValue("star"), 
				conf.getIntValue("lvQm"), parseIntArray(conf.getString("costMoney")), parseIntArray(conf.getString("costMoneyNum")), parseIntArray(conf.getString("costItem")), 
				parseIntArray(conf.getString("costItemNum")), parseStringArray(conf.getString("attr")), parseIntArray(conf.getString("attrValue")), conf.getIntValue("skillSn"), 
				parseIntArray(conf.getString("talent")), conf.getIntValue("power"), conf.getIntValue("value"), conf.getIntValue("isMax"));
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
				String basePath = Utils.getClassPath("json/ConfSkillGodsStar.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfSkillGodsStar.json");
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
