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
 * 表格id [zhCN]
 * SkillEffect.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkillEffect {
	public final int sn;			//表格id
	public final boolean targetInherit;			//目标是否继承
	public final String targetType;			//目标类型
	public final String targetParam1;			//目标参数1
	public final String targetParam2;			//目标参数2
	public final String actionType;			//逻辑类型
	public final String actionParam1;			//逻辑参数1
	public final String actionParam2;			//逻辑参数2
	public final int triggerType;			//触发类型
	public final boolean conditionInherit;			//条件是否继承
	public final String conditionType;			//条件类型
	public final String conditionOP;			//条件运算符
	public final String conditionParam1;			//条件参数1
	public final String conditionParam2;			//条件参数2
	public final String propCoef;			//属性系数
	public final String[] prop;			//属性
	public final String[] propValue;			//属性值
	public final boolean isTargetChaos;			//魅惑时目标是否改变敌友

	public ConfSkillEffect(int sn, boolean targetInherit, String targetType, String targetParam1, String targetParam2, String actionType, String actionParam1, String actionParam2, int triggerType, boolean conditionInherit, String conditionType, String conditionOP, String conditionParam1, String conditionParam2, String propCoef, String[] prop, String[] propValue, boolean isTargetChaos) {
			this.sn = sn;		
			this.targetInherit = targetInherit;		
			this.targetType = targetType;		
			this.targetParam1 = targetParam1;		
			this.targetParam2 = targetParam2;		
			this.actionType = actionType;		
			this.actionParam1 = actionParam1;		
			this.actionParam2 = actionParam2;		
			this.triggerType = triggerType;		
			this.conditionInherit = conditionInherit;		
			this.conditionType = conditionType;		
			this.conditionOP = conditionOP;		
			this.conditionParam1 = conditionParam1;		
			this.conditionParam2 = conditionParam2;		
			this.propCoef = propCoef;		
			this.prop = prop;		
			this.propValue = propValue;		
			this.isTargetChaos = isTargetChaos;		
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
	public static Collection<ConfSkillEffect> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfSkillEffect[] findArray() {
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
	public static ConfSkillEffect get(Integer sn) {
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
	public static ConfSkillEffect getBy(Object...params) {
		List<ConfSkillEffect> list = utilBase(params);
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
	public static List<ConfSkillEffect> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkillEffect> utilBase(Object...params) {
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
		List<ConfSkillEffect> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkillEffect c : DATA.getList()) {
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
	private int compareTo(ConfSkillEffect cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfSkillEffect cell, Object...params) {
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
			case "targetInherit": {
				value = this.targetInherit;
				break;
			}
			case "targetType": {
				value = this.targetType;
				break;
			}
			case "targetParam1": {
				value = this.targetParam1;
				break;
			}
			case "targetParam2": {
				value = this.targetParam2;
				break;
			}
			case "actionType": {
				value = this.actionType;
				break;
			}
			case "actionParam1": {
				value = this.actionParam1;
				break;
			}
			case "actionParam2": {
				value = this.actionParam2;
				break;
			}
			case "triggerType": {
				value = this.triggerType;
				break;
			}
			case "conditionInherit": {
				value = this.conditionInherit;
				break;
			}
			case "conditionType": {
				value = this.conditionType;
				break;
			}
			case "conditionOP": {
				value = this.conditionOP;
				break;
			}
			case "conditionParam1": {
				value = this.conditionParam1;
				break;
			}
			case "conditionParam2": {
				value = this.conditionParam2;
				break;
			}
			case "propCoef": {
				value = this.propCoef;
				break;
			}
			case "prop": {
				value = this.prop;
				break;
			}
			case "propValue": {
				value = this.propValue;
				break;
			}
			case "isTargetChaos": {
				value = this.isTargetChaos;
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
		public static final String sn = "sn";	//表格id
		public static final String targetInherit = "targetInherit";	//目标是否继承
		public static final String targetType = "targetType";	//目标类型
		public static final String targetParam1 = "targetParam1";	//目标参数1
		public static final String targetParam2 = "targetParam2";	//目标参数2
		public static final String actionType = "actionType";	//逻辑类型
		public static final String actionParam1 = "actionParam1";	//逻辑参数1
		public static final String actionParam2 = "actionParam2";	//逻辑参数2
		public static final String triggerType = "triggerType";	//触发类型
		public static final String conditionInherit = "conditionInherit";	//条件是否继承
		public static final String conditionType = "conditionType";	//条件类型
		public static final String conditionOP = "conditionOP";	//条件运算符
		public static final String conditionParam1 = "conditionParam1";	//条件参数1
		public static final String conditionParam2 = "conditionParam2";	//条件参数2
		public static final String propCoef = "propCoef";	//属性系数
		public static final String prop = "prop";	//属性
		public static final String propValue = "propValue";	//属性值
		public static final String isTargetChaos = "isTargetChaos";	//魅惑时目标是否改变敌友
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfSkillEffect> _map;
		//按表顺序存储的数组
		private static ConfSkillEffect[] _array;

		public static ConfSkillEffect[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkillEffect> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkillEffect> getMap() {
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
			Map<Integer, ConfSkillEffect> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfSkillEffect[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfSkillEffect object = new ConfSkillEffect(conf.getIntValue("sn"), conf.getBooleanValue("targetInherit"), conf.getString("targetType"), conf.getString("targetParam1"), 
				conf.getString("targetParam2"), conf.getString("actionType"), conf.getString("actionParam1"), conf.getString("actionParam2"), 
				conf.getIntValue("triggerType"), conf.getBooleanValue("conditionInherit"), conf.getString("conditionType"), conf.getString("conditionOP"), 
				conf.getString("conditionParam1"), conf.getString("conditionParam2"), conf.getString("propCoef"), parseStringArray(conf.getString("prop")), 
				parseStringArray(conf.getString("propValue")), conf.getBooleanValue("isTargetChaos"));
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
				String basePath = Utils.getClassPath("json/ConfSkillEffect.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfSkillEffect.json");
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
