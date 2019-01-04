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
 * 方案ID+100*伙伴阶数 [zhCN]
 * CimeliaConstitutions.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfCimeliaConstitutions {
	public final int sn;			//方案ID+100*伙伴阶数
	public final int planId;			//方案id
	public final int advlevel;			//伙伴阶数
	public final String constitutionName;			//伙伴品阶名
	public final int rolelevQm;			//等级限制
	public final int addAptitude;			//增加的资质
	public final int customPartner;			//突破消耗伙伴数量
	public final int[] itemSn;			//进阶所需要消耗的材料
	public final int[] itemNum;			//进阶所需要消耗的材料数量
	public final int costCoin;			//升阶需要消耗的铜币
	public final String[] advancedAttribute;			//进阶属性类型
	public final int[] addHeroGrowRate;			//进阶提供属性值
	public final int[] skill;			//技能库
	public final int[] addSkillId;			//进阶增加的技能id
	public final int[] skillType;			//升星强化技能类型
	public final int[] power;			//所提升技能威力值
	public final int[] value;			//提升技能固定值
	public final String[] practiceAttribute;			//修炼属性类型
	public final int[] attributeValue;			//修炼属性上限值
	public final int[] levelHash;			//升级分解后获得道具
	public final int[] levelHashNum;			//升级分解后获得道具数量
	public final int[] advHash;			//突破分解后获得道具
	public final int[] advHashNum;			//突破分解后获得道具数量
	public final int[] cultivationHash;			//修炼分解后获得道具
	public final int[] cultivationHashNum;			//修炼分解后获得道具数量
	public final int[] resolveHash;			//献祭分解所获得的道具
	public final int[] resolveHashNum;			//献祭分解所获得的道具数量

	public ConfCimeliaConstitutions(int sn, int planId, int advlevel, String constitutionName, int rolelevQm, int addAptitude, int customPartner, int[] itemSn, int[] itemNum, int costCoin, String[] advancedAttribute, int[] addHeroGrowRate, int[] skill, int[] addSkillId, int[] skillType, int[] power, int[] value, String[] practiceAttribute, int[] attributeValue, int[] levelHash, int[] levelHashNum, int[] advHash, int[] advHashNum, int[] cultivationHash, int[] cultivationHashNum, int[] resolveHash, int[] resolveHashNum) {
			this.sn = sn;		
			this.planId = planId;		
			this.advlevel = advlevel;		
			this.constitutionName = constitutionName;		
			this.rolelevQm = rolelevQm;		
			this.addAptitude = addAptitude;		
			this.customPartner = customPartner;		
			this.itemSn = itemSn;		
			this.itemNum = itemNum;		
			this.costCoin = costCoin;		
			this.advancedAttribute = advancedAttribute;		
			this.addHeroGrowRate = addHeroGrowRate;		
			this.skill = skill;		
			this.addSkillId = addSkillId;		
			this.skillType = skillType;		
			this.power = power;		
			this.value = value;		
			this.practiceAttribute = practiceAttribute;		
			this.attributeValue = attributeValue;		
			this.levelHash = levelHash;		
			this.levelHashNum = levelHashNum;		
			this.advHash = advHash;		
			this.advHashNum = advHashNum;		
			this.cultivationHash = cultivationHash;		
			this.cultivationHashNum = cultivationHashNum;		
			this.resolveHash = resolveHash;		
			this.resolveHashNum = resolveHashNum;		
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
	public static Collection<ConfCimeliaConstitutions> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfCimeliaConstitutions[] findArray() {
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
	public static ConfCimeliaConstitutions get(Integer sn) {
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
	public static ConfCimeliaConstitutions getBy(Object...params) {
		List<ConfCimeliaConstitutions> list = utilBase(params);
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
	public static List<ConfCimeliaConstitutions> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfCimeliaConstitutions> utilBase(Object...params) {
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
		List<ConfCimeliaConstitutions> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfCimeliaConstitutions c : DATA.getList()) {
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
	private int compareTo(ConfCimeliaConstitutions cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfCimeliaConstitutions cell, Object...params) {
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
			case "planId": {
				value = this.planId;
				break;
			}
			case "advlevel": {
				value = this.advlevel;
				break;
			}
			case "constitutionName": {
				value = this.constitutionName;
				break;
			}
			case "rolelevQm": {
				value = this.rolelevQm;
				break;
			}
			case "addAptitude": {
				value = this.addAptitude;
				break;
			}
			case "customPartner": {
				value = this.customPartner;
				break;
			}
			case "itemSn": {
				value = this.itemSn;
				break;
			}
			case "itemNum": {
				value = this.itemNum;
				break;
			}
			case "costCoin": {
				value = this.costCoin;
				break;
			}
			case "advancedAttribute": {
				value = this.advancedAttribute;
				break;
			}
			case "addHeroGrowRate": {
				value = this.addHeroGrowRate;
				break;
			}
			case "skill": {
				value = this.skill;
				break;
			}
			case "addSkillId": {
				value = this.addSkillId;
				break;
			}
			case "skillType": {
				value = this.skillType;
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
			case "practiceAttribute": {
				value = this.practiceAttribute;
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
			default: 
				break;
		}
		return (T) value;
	}

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String sn = "sn";	//方案ID+100*伙伴阶数
		public static final String planId = "planId";	//方案id
		public static final String advlevel = "advlevel";	//伙伴阶数
		public static final String constitutionName = "constitutionName";	//伙伴品阶名
		public static final String rolelevQm = "rolelevQm";	//等级限制
		public static final String addAptitude = "addAptitude";	//增加的资质
		public static final String customPartner = "customPartner";	//突破消耗伙伴数量
		public static final String itemSn = "itemSn";	//进阶所需要消耗的材料
		public static final String itemNum = "itemNum";	//进阶所需要消耗的材料数量
		public static final String costCoin = "costCoin";	//升阶需要消耗的铜币
		public static final String advancedAttribute = "advancedAttribute";	//进阶属性类型
		public static final String addHeroGrowRate = "addHeroGrowRate";	//进阶提供属性值
		public static final String skill = "skill";	//技能库
		public static final String addSkillId = "addSkillId";	//进阶增加的技能id
		public static final String skillType = "skillType";	//升星强化技能类型
		public static final String power = "power";	//所提升技能威力值
		public static final String value = "value";	//提升技能固定值
		public static final String practiceAttribute = "practiceAttribute";	//修炼属性类型
		public static final String attributeValue = "attributeValue";	//修炼属性上限值
		public static final String levelHash = "levelHash";	//升级分解后获得道具
		public static final String levelHashNum = "levelHashNum";	//升级分解后获得道具数量
		public static final String advHash = "advHash";	//突破分解后获得道具
		public static final String advHashNum = "advHashNum";	//突破分解后获得道具数量
		public static final String cultivationHash = "cultivationHash";	//修炼分解后获得道具
		public static final String cultivationHashNum = "cultivationHashNum";	//修炼分解后获得道具数量
		public static final String resolveHash = "resolveHash";	//献祭分解所获得的道具
		public static final String resolveHashNum = "resolveHashNum";	//献祭分解所获得的道具数量
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfCimeliaConstitutions> _map;
		//按表顺序存储的数组
		private static ConfCimeliaConstitutions[] _array;

		public static ConfCimeliaConstitutions[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfCimeliaConstitutions> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfCimeliaConstitutions> getMap() {
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
			Map<Integer, ConfCimeliaConstitutions> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfCimeliaConstitutions[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfCimeliaConstitutions object = new ConfCimeliaConstitutions(conf.getIntValue("sn"), conf.getIntValue("planId"), conf.getIntValue("advlevel"), conf.getString("constitutionName"), 
				conf.getIntValue("rolelevQm"), conf.getIntValue("addAptitude"), conf.getIntValue("customPartner"), parseIntArray(conf.getString("itemSn")), 
				parseIntArray(conf.getString("itemNum")), conf.getIntValue("costCoin"), parseStringArray(conf.getString("advancedAttribute")), parseIntArray(conf.getString("addHeroGrowRate")), 
				parseIntArray(conf.getString("skill")), parseIntArray(conf.getString("addSkillId")), parseIntArray(conf.getString("skillType")), parseIntArray(conf.getString("power")), 
				parseIntArray(conf.getString("value")), parseStringArray(conf.getString("practiceAttribute")), parseIntArray(conf.getString("attributeValue")), parseIntArray(conf.getString("levelHash")), 
				parseIntArray(conf.getString("levelHashNum")), parseIntArray(conf.getString("advHash")), parseIntArray(conf.getString("advHashNum")), parseIntArray(conf.getString("cultivationHash")), 
				parseIntArray(conf.getString("cultivationHashNum")), parseIntArray(conf.getString("resolveHash")), parseIntArray(conf.getString("resolveHashNum")));
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
				String basePath = Utils.getClassPath("json/ConfCimeliaConstitutions.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfCimeliaConstitutions.json");
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
