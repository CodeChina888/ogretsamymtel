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
 * 道具SN类别*1000+序号 [zhCN]
 * Item.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfItem {
	public final int sn;			//道具SN类别*1000+序号
	public final String name;			//道具名称
	public final int itemType;			//类型
	public final String[] param;			//类型参数
	public final int quality;			//品质（白绿蓝紫橙红金,对应0123456）
	public final int compoundId;			//合成id
	public final int[] hashId;			//分解id
	public final int[] hashNum;			//分解值
	public final int maxNum;			//叠加数量
	public final int autoUse;			//是否自动使用
	public final int use;			//可否使用
	public final int needLv;			//需求等级
	public final int salePrice;			//出售价格
	public final String[] propKey;			//增加属性KEY
	public final int[] propValue;			//增加属性VALUE
	public final int suitSn;			//套装SN
	public final int profession;			//职业限制
	public final int fashionSn;			//
	public final boolean destroy;			//可否销毁
	public final boolean batch;			//可否批量使用
	public final int life;			//物品寿命（单位：秒）
	public final boolean delOnOffLine;			//下线时删除
	public final boolean delOnTurnStage;			//切换场景时删除
	public final int sysNotice;			//系统公告类型
	public final int enchantExp;			//附魔经验参数,为0则不可添加为附魔材料
	public final int undergo;			//当消耗品时的经验
	public final int elementSn;			//冰火雷表SN

	public ConfItem(int sn, String name, int itemType, String[] param, int quality, int compoundId, int[] hashId, int[] hashNum, int maxNum, int autoUse, int use, int needLv, int salePrice, String[] propKey, int[] propValue, int suitSn, int profession, int fashionSn, boolean destroy, boolean batch, int life, boolean delOnOffLine, boolean delOnTurnStage, int sysNotice, int enchantExp, int undergo, int elementSn) {
			this.sn = sn;		
			this.name = name;		
			this.itemType = itemType;		
			this.param = param;		
			this.quality = quality;		
			this.compoundId = compoundId;		
			this.hashId = hashId;		
			this.hashNum = hashNum;		
			this.maxNum = maxNum;		
			this.autoUse = autoUse;		
			this.use = use;		
			this.needLv = needLv;		
			this.salePrice = salePrice;		
			this.propKey = propKey;		
			this.propValue = propValue;		
			this.suitSn = suitSn;		
			this.profession = profession;		
			this.fashionSn = fashionSn;		
			this.destroy = destroy;		
			this.batch = batch;		
			this.life = life;		
			this.delOnOffLine = delOnOffLine;		
			this.delOnTurnStage = delOnTurnStage;		
			this.sysNotice = sysNotice;		
			this.enchantExp = enchantExp;		
			this.undergo = undergo;		
			this.elementSn = elementSn;		
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
	public static Collection<ConfItem> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfItem[] findArray() {
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
	public static ConfItem get(Integer sn) {
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
	public static ConfItem getBy(Object...params) {
		List<ConfItem> list = utilBase(params);
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
	public static List<ConfItem> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfItem> utilBase(Object...params) {
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
		List<ConfItem> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfItem c : DATA.getList()) {
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
	private int compareTo(ConfItem cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfItem cell, Object...params) {
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
			case "name": {
				value = this.name;
				break;
			}
			case "itemType": {
				value = this.itemType;
				break;
			}
			case "param": {
				value = this.param;
				break;
			}
			case "quality": {
				value = this.quality;
				break;
			}
			case "compoundId": {
				value = this.compoundId;
				break;
			}
			case "hashId": {
				value = this.hashId;
				break;
			}
			case "hashNum": {
				value = this.hashNum;
				break;
			}
			case "maxNum": {
				value = this.maxNum;
				break;
			}
			case "autoUse": {
				value = this.autoUse;
				break;
			}
			case "use": {
				value = this.use;
				break;
			}
			case "needLv": {
				value = this.needLv;
				break;
			}
			case "salePrice": {
				value = this.salePrice;
				break;
			}
			case "propKey": {
				value = this.propKey;
				break;
			}
			case "propValue": {
				value = this.propValue;
				break;
			}
			case "suitSn": {
				value = this.suitSn;
				break;
			}
			case "profession": {
				value = this.profession;
				break;
			}
			case "fashionSn": {
				value = this.fashionSn;
				break;
			}
			case "destroy": {
				value = this.destroy;
				break;
			}
			case "batch": {
				value = this.batch;
				break;
			}
			case "life": {
				value = this.life;
				break;
			}
			case "delOnOffLine": {
				value = this.delOnOffLine;
				break;
			}
			case "delOnTurnStage": {
				value = this.delOnTurnStage;
				break;
			}
			case "sysNotice": {
				value = this.sysNotice;
				break;
			}
			case "enchantExp": {
				value = this.enchantExp;
				break;
			}
			case "undergo": {
				value = this.undergo;
				break;
			}
			case "elementSn": {
				value = this.elementSn;
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
		public static final String sn = "sn";	//道具SN类别*1000+序号
		public static final String name = "name";	//道具名称
		public static final String itemType = "itemType";	//类型
		public static final String param = "param";	//类型参数
		public static final String quality = "quality";	//品质（白绿蓝紫橙红金,对应0123456）
		public static final String compoundId = "compoundId";	//合成id
		public static final String hashId = "hashId";	//分解id
		public static final String hashNum = "hashNum";	//分解值
		public static final String maxNum = "maxNum";	//叠加数量
		public static final String autoUse = "autoUse";	//是否自动使用
		public static final String use = "use";	//可否使用
		public static final String needLv = "needLv";	//需求等级
		public static final String salePrice = "salePrice";	//出售价格
		public static final String propKey = "propKey";	//增加属性KEY
		public static final String propValue = "propValue";	//增加属性VALUE
		public static final String suitSn = "suitSn";	//套装SN
		public static final String profession = "profession";	//职业限制
		public static final String fashionSn = "fashionSn";	//
		public static final String destroy = "destroy";	//可否销毁
		public static final String batch = "batch";	//可否批量使用
		public static final String life = "life";	//物品寿命（单位：秒）
		public static final String delOnOffLine = "delOnOffLine";	//下线时删除
		public static final String delOnTurnStage = "delOnTurnStage";	//切换场景时删除
		public static final String sysNotice = "sysNotice";	//系统公告类型
		public static final String enchantExp = "enchantExp";	//附魔经验参数,为0则不可添加为附魔材料
		public static final String undergo = "undergo";	//当消耗品时的经验
		public static final String elementSn = "elementSn";	//冰火雷表SN
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfItem> _map;
		//按表顺序存储的数组
		private static ConfItem[] _array;

		public static ConfItem[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfItem> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfItem> getMap() {
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
			Map<Integer, ConfItem> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfItem[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfItem object = new ConfItem(conf.getIntValue("sn"), conf.getString("name"), conf.getIntValue("itemType"), parseStringArray(conf.getString("param")), 
				conf.getIntValue("quality"), conf.getIntValue("compoundId"), parseIntArray(conf.getString("hashId")), parseIntArray(conf.getString("hashNum")), 
				conf.getIntValue("maxNum"), conf.getIntValue("autoUse"), conf.getIntValue("use"), conf.getIntValue("needLv"), 
				conf.getIntValue("salePrice"), parseStringArray(conf.getString("propKey")), parseIntArray(conf.getString("propValue")), conf.getIntValue("suitSn"), 
				conf.getIntValue("profession"), conf.getIntValue("fashionSn"), conf.getBooleanValue("destroy"), conf.getBooleanValue("batch"), 
				conf.getIntValue("life"), conf.getBooleanValue("delOnOffLine"), conf.getBooleanValue("delOnTurnStage"), conf.getIntValue("sysNotice"), 
				conf.getIntValue("enchantExp"), conf.getIntValue("undergo"), conf.getIntValue("elementSn"));
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
				String basePath = Utils.getClassPath("json/ConfItem.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfItem.json");
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
