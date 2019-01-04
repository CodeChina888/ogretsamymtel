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
 * 部位类型*1000+精炼品质等级 [zhCN]
 * EquipRefine.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfEquipRefine {
	public final int sn;			//部位类型*1000+精炼品质等级
	public final int refineLv;			//部件精炼品质
	public final int nextRefineLv;			//下一品质
	public final int quality;			//部件进阶品质
	public final int qualityLev;			//进阶等级
	public final int[] slotUnlock;			//槽位解锁情况0未解锁，1解锁
	public final int lvQm;			//玩家等级限制
	public final int equipAdLv;			//该装备进阶等级限制
	public final int[] normalCost;			//普通精炼消耗
	public final int[] normalCostNum;			//普通精炼消耗数量
	public final int[] perfectCost;			//无损精炼消耗
	public final int[] perfectCostNum;			//无损精炼消耗数量
	public final int[] slotLv;			//槽位段位影响数值
	public final int[] weight;			//段位变化权重
	public final int[] highSlotLv;			//无损槽位段位影响数值
	public final int[] highWeight;			//无损段位变化权重
	public final String[] slotAttr;			//对应槽位加成属性
	public final int[] slotLvAttrValue;			//对应槽位段位影响的属性值
	public final int maxSlotLv;			//槽位满段等级（品质提升条件）
	public final int refineRatio;			//精炼品质给装备属性增加的比例
	public final String[] refineAttr;			//精炼品质加成属性
	public final int[] refineAttrValue;			//精炼品质加成属性值
	public final int noticeSn;			//走马灯sn为0不推送

	public ConfEquipRefine(int sn, int refineLv, int nextRefineLv, int quality, int qualityLev, int[] slotUnlock, int lvQm, int equipAdLv, int[] normalCost, int[] normalCostNum, int[] perfectCost, int[] perfectCostNum, int[] slotLv, int[] weight, int[] highSlotLv, int[] highWeight, String[] slotAttr, int[] slotLvAttrValue, int maxSlotLv, int refineRatio, String[] refineAttr, int[] refineAttrValue, int noticeSn) {
			this.sn = sn;		
			this.refineLv = refineLv;		
			this.nextRefineLv = nextRefineLv;		
			this.quality = quality;		
			this.qualityLev = qualityLev;		
			this.slotUnlock = slotUnlock;		
			this.lvQm = lvQm;		
			this.equipAdLv = equipAdLv;		
			this.normalCost = normalCost;		
			this.normalCostNum = normalCostNum;		
			this.perfectCost = perfectCost;		
			this.perfectCostNum = perfectCostNum;		
			this.slotLv = slotLv;		
			this.weight = weight;		
			this.highSlotLv = highSlotLv;		
			this.highWeight = highWeight;		
			this.slotAttr = slotAttr;		
			this.slotLvAttrValue = slotLvAttrValue;		
			this.maxSlotLv = maxSlotLv;		
			this.refineRatio = refineRatio;		
			this.refineAttr = refineAttr;		
			this.refineAttrValue = refineAttrValue;		
			this.noticeSn = noticeSn;		
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
	public static Collection<ConfEquipRefine> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfEquipRefine[] findArray() {
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
	public static ConfEquipRefine get(Integer sn) {
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
	public static ConfEquipRefine getBy(Object...params) {
		List<ConfEquipRefine> list = utilBase(params);
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
	public static List<ConfEquipRefine> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfEquipRefine> utilBase(Object...params) {
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
		List<ConfEquipRefine> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfEquipRefine c : DATA.getList()) {
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
	private int compareTo(ConfEquipRefine cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfEquipRefine cell, Object...params) {
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
			case "refineLv": {
				value = this.refineLv;
				break;
			}
			case "nextRefineLv": {
				value = this.nextRefineLv;
				break;
			}
			case "quality": {
				value = this.quality;
				break;
			}
			case "qualityLev": {
				value = this.qualityLev;
				break;
			}
			case "slotUnlock": {
				value = this.slotUnlock;
				break;
			}
			case "lvQm": {
				value = this.lvQm;
				break;
			}
			case "equipAdLv": {
				value = this.equipAdLv;
				break;
			}
			case "normalCost": {
				value = this.normalCost;
				break;
			}
			case "normalCostNum": {
				value = this.normalCostNum;
				break;
			}
			case "perfectCost": {
				value = this.perfectCost;
				break;
			}
			case "perfectCostNum": {
				value = this.perfectCostNum;
				break;
			}
			case "slotLv": {
				value = this.slotLv;
				break;
			}
			case "weight": {
				value = this.weight;
				break;
			}
			case "highSlotLv": {
				value = this.highSlotLv;
				break;
			}
			case "highWeight": {
				value = this.highWeight;
				break;
			}
			case "slotAttr": {
				value = this.slotAttr;
				break;
			}
			case "slotLvAttrValue": {
				value = this.slotLvAttrValue;
				break;
			}
			case "maxSlotLv": {
				value = this.maxSlotLv;
				break;
			}
			case "refineRatio": {
				value = this.refineRatio;
				break;
			}
			case "refineAttr": {
				value = this.refineAttr;
				break;
			}
			case "refineAttrValue": {
				value = this.refineAttrValue;
				break;
			}
			case "noticeSn": {
				value = this.noticeSn;
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
		public static final String sn = "sn";	//部位类型*1000+精炼品质等级
		public static final String refineLv = "refineLv";	//部件精炼品质
		public static final String nextRefineLv = "nextRefineLv";	//下一品质
		public static final String quality = "quality";	//部件进阶品质
		public static final String qualityLev = "qualityLev";	//进阶等级
		public static final String slotUnlock = "slotUnlock";	//槽位解锁情况0未解锁，1解锁
		public static final String lvQm = "lvQm";	//玩家等级限制
		public static final String equipAdLv = "equipAdLv";	//该装备进阶等级限制
		public static final String normalCost = "normalCost";	//普通精炼消耗
		public static final String normalCostNum = "normalCostNum";	//普通精炼消耗数量
		public static final String perfectCost = "perfectCost";	//无损精炼消耗
		public static final String perfectCostNum = "perfectCostNum";	//无损精炼消耗数量
		public static final String slotLv = "slotLv";	//槽位段位影响数值
		public static final String weight = "weight";	//段位变化权重
		public static final String highSlotLv = "highSlotLv";	//无损槽位段位影响数值
		public static final String highWeight = "highWeight";	//无损段位变化权重
		public static final String slotAttr = "slotAttr";	//对应槽位加成属性
		public static final String slotLvAttrValue = "slotLvAttrValue";	//对应槽位段位影响的属性值
		public static final String maxSlotLv = "maxSlotLv";	//槽位满段等级（品质提升条件）
		public static final String refineRatio = "refineRatio";	//精炼品质给装备属性增加的比例
		public static final String refineAttr = "refineAttr";	//精炼品质加成属性
		public static final String refineAttrValue = "refineAttrValue";	//精炼品质加成属性值
		public static final String noticeSn = "noticeSn";	//走马灯sn为0不推送
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfEquipRefine> _map;
		//按表顺序存储的数组
		private static ConfEquipRefine[] _array;

		public static ConfEquipRefine[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfEquipRefine> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfEquipRefine> getMap() {
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
			Map<Integer, ConfEquipRefine> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfEquipRefine[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfEquipRefine object = new ConfEquipRefine(conf.getIntValue("sn"), conf.getIntValue("refineLv"), conf.getIntValue("nextRefineLv"), conf.getIntValue("quality"), 
				conf.getIntValue("qualityLev"), parseIntArray(conf.getString("slotUnlock")), conf.getIntValue("lvQm"), conf.getIntValue("equipAdLv"), 
				parseIntArray(conf.getString("normalCost")), parseIntArray(conf.getString("normalCostNum")), parseIntArray(conf.getString("perfectCost")), parseIntArray(conf.getString("perfectCostNum")), 
				parseIntArray(conf.getString("slotLv")), parseIntArray(conf.getString("weight")), parseIntArray(conf.getString("highSlotLv")), parseIntArray(conf.getString("highWeight")), 
				parseStringArray(conf.getString("slotAttr")), parseIntArray(conf.getString("slotLvAttrValue")), conf.getIntValue("maxSlotLv"), conf.getIntValue("refineRatio"), 
				parseStringArray(conf.getString("refineAttr")), parseIntArray(conf.getString("refineAttrValue")), conf.getIntValue("noticeSn"));
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
				String basePath = Utils.getClassPath("json/ConfEquipRefine.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfEquipRefine.json");
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
