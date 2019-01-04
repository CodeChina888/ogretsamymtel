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
 * id [zhCN]
 * RunesSuit.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfRunesSuit {
	public final int sn;			//id
	public final String Name;			//名称
	public final int order;			//排序ID
	public final int qualityId;			//套装品质
	public final int dropInfoId;			//掉落ID
	public final int exchangeLevel;			//兑换所需等级
	public final int itemId;			//兑换资源id
	public final int number;			//兑换消耗数量
	public final int[] limit;			//附加属性条件
	public final String[] attrType1;			//属性类型1
	public final int[] attrValue1;			//属性值1
	public final int skill1;			//技能库1
	public final String[] attrType2;			//属性类型2
	public final int[] attrValue2;			//属性值2
	public final int skill2;			//技能库2

	public ConfRunesSuit(int sn, String Name, int order, int qualityId, int dropInfoId, int exchangeLevel, int itemId, int number, int[] limit, String[] attrType1, int[] attrValue1, int skill1, String[] attrType2, int[] attrValue2, int skill2) {
			this.sn = sn;		
			this.Name = Name;		
			this.order = order;		
			this.qualityId = qualityId;		
			this.dropInfoId = dropInfoId;		
			this.exchangeLevel = exchangeLevel;		
			this.itemId = itemId;		
			this.number = number;		
			this.limit = limit;		
			this.attrType1 = attrType1;		
			this.attrValue1 = attrValue1;		
			this.skill1 = skill1;		
			this.attrType2 = attrType2;		
			this.attrValue2 = attrValue2;		
			this.skill2 = skill2;		
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
	public static Collection<ConfRunesSuit> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfRunesSuit[] findArray() {
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
	public static ConfRunesSuit get(Integer sn) {
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
	public static ConfRunesSuit getBy(Object...params) {
		List<ConfRunesSuit> list = utilBase(params);
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
	public static List<ConfRunesSuit> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfRunesSuit> utilBase(Object...params) {
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
		List<ConfRunesSuit> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfRunesSuit c : DATA.getList()) {
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
	private int compareTo(ConfRunesSuit cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfRunesSuit cell, Object...params) {
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
			case "Name": {
				value = this.Name;
				break;
			}
			case "order": {
				value = this.order;
				break;
			}
			case "qualityId": {
				value = this.qualityId;
				break;
			}
			case "dropInfoId": {
				value = this.dropInfoId;
				break;
			}
			case "exchangeLevel": {
				value = this.exchangeLevel;
				break;
			}
			case "itemId": {
				value = this.itemId;
				break;
			}
			case "number": {
				value = this.number;
				break;
			}
			case "limit": {
				value = this.limit;
				break;
			}
			case "attrType1": {
				value = this.attrType1;
				break;
			}
			case "attrValue1": {
				value = this.attrValue1;
				break;
			}
			case "skill1": {
				value = this.skill1;
				break;
			}
			case "attrType2": {
				value = this.attrType2;
				break;
			}
			case "attrValue2": {
				value = this.attrValue2;
				break;
			}
			case "skill2": {
				value = this.skill2;
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
		public static final String sn = "sn";	//id
		public static final String Name = "Name";	//名称
		public static final String order = "order";	//排序ID
		public static final String qualityId = "qualityId";	//套装品质
		public static final String dropInfoId = "dropInfoId";	//掉落ID
		public static final String exchangeLevel = "exchangeLevel";	//兑换所需等级
		public static final String itemId = "itemId";	//兑换资源id
		public static final String number = "number";	//兑换消耗数量
		public static final String limit = "limit";	//附加属性条件
		public static final String attrType1 = "attrType1";	//属性类型1
		public static final String attrValue1 = "attrValue1";	//属性值1
		public static final String skill1 = "skill1";	//技能库1
		public static final String attrType2 = "attrType2";	//属性类型2
		public static final String attrValue2 = "attrValue2";	//属性值2
		public static final String skill2 = "skill2";	//技能库2
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfRunesSuit> _map;
		//按表顺序存储的数组
		private static ConfRunesSuit[] _array;

		public static ConfRunesSuit[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfRunesSuit> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfRunesSuit> getMap() {
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
			Map<Integer, ConfRunesSuit> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfRunesSuit[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfRunesSuit object = new ConfRunesSuit(conf.getIntValue("sn"), conf.getString("Name"), conf.getIntValue("order"), conf.getIntValue("qualityId"), 
				conf.getIntValue("dropInfoId"), conf.getIntValue("exchangeLevel"), conf.getIntValue("itemId"), conf.getIntValue("number"), 
				parseIntArray(conf.getString("limit")), parseStringArray(conf.getString("attrType1")), parseIntArray(conf.getString("attrValue1")), conf.getIntValue("skill1"), 
				parseStringArray(conf.getString("attrType2")), parseIntArray(conf.getString("attrValue2")), conf.getIntValue("skill2"));
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
				String basePath = Utils.getClassPath("json/ConfRunesSuit.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfRunesSuit.json");
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
