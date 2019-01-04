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
 * 技能编号 [zhCN]
 * SkillVk.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkillVk {
	public final int sn;			//技能编号
	public final int level;			//技能等级
	public final int cd;			//冷却CD
	public final int skillType;			//技能类型
	public final int useMp;			//能量消耗
	public final int useHp;			//HP消耗
	public final int hurtType;			//伤害类型
	public final int sourceType;			//计算依据
	public final int targetFlag;			//目标类型
	public final int targetCon;			//目标条件
	public final int targetCout;			//目标数量
	public final int power;			//技能威力
	public final int value;			//固定伤害/回复
	public final int[] ability;			//关联被动
	public final int interval;			//生效时间
	public final int castChance;			//释放几率
	public final int castCon;			//释放条件
	public final int conditionP1;			//释放条件参数1
	public final int[] conditionP2;			//释放条件参数2
	public final int controlJudgment;			//受控制生效判断
	public final int[] targetPercent;			//多个目标伤害分配比例
	public final int multipleStrike;			//连击判断
	public final int[] triggerBuff;			//触发Buff
	public final int[] triggerBuffCon;			//触发Buff概率
	public final int[] triggerBuffType;			//Buff目标
	public final int[] bulletType;			//出手表现

	public ConfSkillVk(int sn, int level, int cd, int skillType, int useMp, int useHp, int hurtType, int sourceType, int targetFlag, int targetCon, int targetCout, int power, int value, int[] ability, int interval, int castChance, int castCon, int conditionP1, int[] conditionP2, int controlJudgment, int[] targetPercent, int multipleStrike, int[] triggerBuff, int[] triggerBuffCon, int[] triggerBuffType, int[] bulletType) {
			this.sn = sn;		
			this.level = level;		
			this.cd = cd;		
			this.skillType = skillType;		
			this.useMp = useMp;		
			this.useHp = useHp;		
			this.hurtType = hurtType;		
			this.sourceType = sourceType;		
			this.targetFlag = targetFlag;		
			this.targetCon = targetCon;		
			this.targetCout = targetCout;		
			this.power = power;		
			this.value = value;		
			this.ability = ability;		
			this.interval = interval;		
			this.castChance = castChance;		
			this.castCon = castCon;		
			this.conditionP1 = conditionP1;		
			this.conditionP2 = conditionP2;		
			this.controlJudgment = controlJudgment;		
			this.targetPercent = targetPercent;		
			this.multipleStrike = multipleStrike;		
			this.triggerBuff = triggerBuff;		
			this.triggerBuffCon = triggerBuffCon;		
			this.triggerBuffType = triggerBuffType;		
			this.bulletType = bulletType;		
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
	public static Collection<ConfSkillVk> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfSkillVk[] findArray() {
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
	public static ConfSkillVk get(Integer sn) {
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
	public static ConfSkillVk getBy(Object...params) {
		List<ConfSkillVk> list = utilBase(params);
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
	public static List<ConfSkillVk> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkillVk> utilBase(Object...params) {
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
		List<ConfSkillVk> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkillVk c : DATA.getList()) {
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
	private int compareTo(ConfSkillVk cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfSkillVk cell, Object...params) {
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
			case "level": {
				value = this.level;
				break;
			}
			case "cd": {
				value = this.cd;
				break;
			}
			case "skillType": {
				value = this.skillType;
				break;
			}
			case "useMp": {
				value = this.useMp;
				break;
			}
			case "useHp": {
				value = this.useHp;
				break;
			}
			case "hurtType": {
				value = this.hurtType;
				break;
			}
			case "sourceType": {
				value = this.sourceType;
				break;
			}
			case "targetFlag": {
				value = this.targetFlag;
				break;
			}
			case "targetCon": {
				value = this.targetCon;
				break;
			}
			case "targetCout": {
				value = this.targetCout;
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
			case "ability": {
				value = this.ability;
				break;
			}
			case "interval": {
				value = this.interval;
				break;
			}
			case "castChance": {
				value = this.castChance;
				break;
			}
			case "castCon": {
				value = this.castCon;
				break;
			}
			case "conditionP1": {
				value = this.conditionP1;
				break;
			}
			case "conditionP2": {
				value = this.conditionP2;
				break;
			}
			case "controlJudgment": {
				value = this.controlJudgment;
				break;
			}
			case "targetPercent": {
				value = this.targetPercent;
				break;
			}
			case "multipleStrike": {
				value = this.multipleStrike;
				break;
			}
			case "triggerBuff": {
				value = this.triggerBuff;
				break;
			}
			case "triggerBuffCon": {
				value = this.triggerBuffCon;
				break;
			}
			case "triggerBuffType": {
				value = this.triggerBuffType;
				break;
			}
			case "bulletType": {
				value = this.bulletType;
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
		public static final String sn = "sn";	//技能编号
		public static final String level = "level";	//技能等级
		public static final String cd = "cd";	//冷却CD
		public static final String skillType = "skillType";	//技能类型
		public static final String useMp = "useMp";	//能量消耗
		public static final String useHp = "useHp";	//HP消耗
		public static final String hurtType = "hurtType";	//伤害类型
		public static final String sourceType = "sourceType";	//计算依据
		public static final String targetFlag = "targetFlag";	//目标类型
		public static final String targetCon = "targetCon";	//目标条件
		public static final String targetCout = "targetCout";	//目标数量
		public static final String power = "power";	//技能威力
		public static final String value = "value";	//固定伤害/回复
		public static final String ability = "ability";	//关联被动
		public static final String interval = "interval";	//生效时间
		public static final String castChance = "castChance";	//释放几率
		public static final String castCon = "castCon";	//释放条件
		public static final String conditionP1 = "conditionP1";	//释放条件参数1
		public static final String conditionP2 = "conditionP2";	//释放条件参数2
		public static final String controlJudgment = "controlJudgment";	//受控制生效判断
		public static final String targetPercent = "targetPercent";	//多个目标伤害分配比例
		public static final String multipleStrike = "multipleStrike";	//连击判断
		public static final String triggerBuff = "triggerBuff";	//触发Buff
		public static final String triggerBuffCon = "triggerBuffCon";	//触发Buff概率
		public static final String triggerBuffType = "triggerBuffType";	//Buff目标
		public static final String bulletType = "bulletType";	//出手表现
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfSkillVk> _map;
		//按表顺序存储的数组
		private static ConfSkillVk[] _array;

		public static ConfSkillVk[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkillVk> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkillVk> getMap() {
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
			Map<Integer, ConfSkillVk> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfSkillVk[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfSkillVk object = new ConfSkillVk(conf.getIntValue("sn"), conf.getIntValue("level"), conf.getIntValue("cd"), conf.getIntValue("skillType"), 
				conf.getIntValue("useMp"), conf.getIntValue("useHp"), conf.getIntValue("hurtType"), conf.getIntValue("sourceType"), 
				conf.getIntValue("targetFlag"), conf.getIntValue("targetCon"), conf.getIntValue("targetCout"), conf.getIntValue("power"), 
				conf.getIntValue("value"), parseIntArray(conf.getString("ability")), conf.getIntValue("interval"), conf.getIntValue("castChance"), 
				conf.getIntValue("castCon"), conf.getIntValue("conditionP1"), parseIntArray(conf.getString("conditionP2")), conf.getIntValue("controlJudgment"), 
				parseIntArray(conf.getString("targetPercent")), conf.getIntValue("multipleStrike"), parseIntArray(conf.getString("triggerBuff")), parseIntArray(conf.getString("triggerBuffCon")), 
				parseIntArray(conf.getString("triggerBuffType")), parseIntArray(conf.getString("bulletType")));
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
				String basePath = Utils.getClassPath("json/ConfSkillVk.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfSkillVk.json");
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
