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
 * 伙伴sn [zhCN]
 * PartnerRecruit.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfPartnerRecruit {
	public final int sn;			//伙伴sn
	public final int itemCost;			//消耗道具id
	public final int itemNum;			//消耗道具数量
	public final int needNotice;			//获取否播放公告
	public final int constitutionPlanId;			//突破方案SN
	public final int[] fateAll;			//所有缘分关系id
	public final int[] attribute;			//图鉴加成属性ID
	public final int[] attributeValue;			//加成属性值
	public final int[] levelHash;			//升级分解后获得道具
	public final int[] levelHashNum;			//升级分解后获得道具数量
	public final int[] advHash;			//突破分解后获得道具
	public final int[] advHashNum;			//突破分解后获得道具数量
	public final int[] cultivationHash;			//修炼分解后获得道具
	public final int[] cultivationHashNum;			//修炼分解后获得道具数量
	public final int[] resolveHash;			//献祭分解所获得的道具
	public final int[] resolveHashNum;			//献祭分解所获得的道具数量
	public final String content;			//备注
	public final int cimelia;			//法宝sn

	public ConfPartnerRecruit(int sn, int itemCost, int itemNum, int needNotice, int constitutionPlanId, int[] fateAll, int[] attribute, int[] attributeValue, int[] levelHash, int[] levelHashNum, int[] advHash, int[] advHashNum, int[] cultivationHash, int[] cultivationHashNum, int[] resolveHash, int[] resolveHashNum, String content, int cimelia) {
			this.sn = sn;		
			this.itemCost = itemCost;		
			this.itemNum = itemNum;		
			this.needNotice = needNotice;		
			this.constitutionPlanId = constitutionPlanId;		
			this.fateAll = fateAll;		
			this.attribute = attribute;		
			this.attributeValue = attributeValue;		
			this.levelHash = levelHash;		
			this.levelHashNum = levelHashNum;		
			this.advHash = advHash;		
			this.advHashNum = advHashNum;		
			this.cultivationHash = cultivationHash;		
			this.cultivationHashNum = cultivationHashNum;		
			this.resolveHash = resolveHash;		
			this.resolveHashNum = resolveHashNum;		
			this.content = content;		
			this.cimelia = cimelia;		
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
	public static Collection<ConfPartnerRecruit> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfPartnerRecruit[] findArray() {
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
	public static ConfPartnerRecruit get(Integer sn) {
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
	public static ConfPartnerRecruit getBy(Object...params) {
		List<ConfPartnerRecruit> list = utilBase(params);
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
	public static List<ConfPartnerRecruit> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfPartnerRecruit> utilBase(Object...params) {
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
		List<ConfPartnerRecruit> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfPartnerRecruit c : DATA.getList()) {
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
	private int compareTo(ConfPartnerRecruit cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfPartnerRecruit cell, Object...params) {
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
			case "itemCost": {
				value = this.itemCost;
				break;
			}
			case "itemNum": {
				value = this.itemNum;
				break;
			}
			case "needNotice": {
				value = this.needNotice;
				break;
			}
			case "constitutionPlanId": {
				value = this.constitutionPlanId;
				break;
			}
			case "fateAll": {
				value = this.fateAll;
				break;
			}
			case "attribute": {
				value = this.attribute;
				break;
			}
			case "attributeValue": {
				value = this.attributeValue;
				break;
			}
			case "levelHash": {
				value = this.levelHash;
				break;
			}
			case "levelHashNum": {
				value = this.levelHashNum;
				break;
			}
			case "advHash": {
				value = this.advHash;
				break;
			}
			case "advHashNum": {
				value = this.advHashNum;
				break;
			}
			case "cultivationHash": {
				value = this.cultivationHash;
				break;
			}
			case "cultivationHashNum": {
				value = this.cultivationHashNum;
				break;
			}
			case "resolveHash": {
				value = this.resolveHash;
				break;
			}
			case "resolveHashNum": {
				value = this.resolveHashNum;
				break;
			}
			case "content": {
				value = this.content;
				break;
			}
			case "cimelia": {
				value = this.cimelia;
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
		public static final String sn = "sn";	//伙伴sn
		public static final String itemCost = "itemCost";	//消耗道具id
		public static final String itemNum = "itemNum";	//消耗道具数量
		public static final String needNotice = "needNotice";	//获取否播放公告
		public static final String constitutionPlanId = "constitutionPlanId";	//突破方案SN
		public static final String fateAll = "fateAll";	//所有缘分关系id
		public static final String attribute = "attribute";	//图鉴加成属性ID
		public static final String attributeValue = "attributeValue";	//加成属性值
		public static final String levelHash = "levelHash";	//升级分解后获得道具
		public static final String levelHashNum = "levelHashNum";	//升级分解后获得道具数量
		public static final String advHash = "advHash";	//突破分解后获得道具
		public static final String advHashNum = "advHashNum";	//突破分解后获得道具数量
		public static final String cultivationHash = "cultivationHash";	//修炼分解后获得道具
		public static final String cultivationHashNum = "cultivationHashNum";	//修炼分解后获得道具数量
		public static final String resolveHash = "resolveHash";	//献祭分解所获得的道具
		public static final String resolveHashNum = "resolveHashNum";	//献祭分解所获得的道具数量
		public static final String content = "content";	//备注
		public static final String cimelia = "cimelia";	//法宝sn
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfPartnerRecruit> _map;
		//按表顺序存储的数组
		private static ConfPartnerRecruit[] _array;

		public static ConfPartnerRecruit[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfPartnerRecruit> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfPartnerRecruit> getMap() {
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
			Map<Integer, ConfPartnerRecruit> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfPartnerRecruit[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfPartnerRecruit object = new ConfPartnerRecruit(conf.getIntValue("sn"), conf.getIntValue("itemCost"), conf.getIntValue("itemNum"), conf.getIntValue("needNotice"), 
				conf.getIntValue("constitutionPlanId"), parseIntArray(conf.getString("fateAll")), parseIntArray(conf.getString("attribute")), parseIntArray(conf.getString("attributeValue")), 
				parseIntArray(conf.getString("levelHash")), parseIntArray(conf.getString("levelHashNum")), parseIntArray(conf.getString("advHash")), parseIntArray(conf.getString("advHashNum")), 
				parseIntArray(conf.getString("cultivationHash")), parseIntArray(conf.getString("cultivationHashNum")), parseIntArray(conf.getString("resolveHash")), parseIntArray(conf.getString("resolveHashNum")), 
				conf.getString("content"), conf.getIntValue("cimelia"));
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
				String basePath = Utils.getClassPath("json/ConfPartnerRecruit.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfPartnerRecruit.json");
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
