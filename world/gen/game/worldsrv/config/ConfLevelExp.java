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
 * 等级 [zhCN]
 * LevelExp.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfLevelExp {
	public final int sn;			//等级
	public final int roleExp;			//主角升级经验
	public final int partner1Exp;			//伙伴品质1升级经验
	public final int partner2Exp;			//伙伴品质2升级经验
	public final int partner3Exp;			//伙伴品质3升级经验
	public final int partner4Exp;			//伙伴品质4升级经验
	public final int partner5Exp;			//伙伴品质5升级经验
	public final int partner6Exp;			//伙伴品质6升级经验
	public final int cimelia1Exp;			//法宝品质1经验
	public final int cimelia2Exp;			//法宝品质2经验
	public final int cimelia3Exp;			//法宝品质3经验
	public final int cimelia4Exp;			//法宝品质4经验
	public final int cimelia5Exp;			//法宝品质5经验
	public final int cimelia6Exp;			//法宝品质6经验
	public final int partnerCoin;			//伙伴升级消耗铜币
	public final int cimeliaCoin;			//法宝升级消耗铜币
	public final int staminaAdd;			//主角升级体力增加
	public final int staminaMax;			//主角升级体力上限
	public final int[] coinInfo;			//全服BOSS单次结算铜钱奖励
	public final int[] gradeCoinInfo;			//等级BOSS单次结算铜钱奖励
	public final int[] lootMapInfo;			//抢夺本攻击与血
	public final int towerCorrect;			//爬塔等级对应的修正战斗力
	public final int rune1Exp;			//命格品质1升级经验
	public final int rune2Exp;			//命格品质2升级经验
	public final int rune3Exp;			//命格品质3升级经验
	public final int rune4Exp;			//命格品质4升级经验
	public final int rune5Exp;			//命格品质5升级经验
	public final int[] levelHash;			//升级分解后获得道具
	public final int[] levelHashNum;			//升级分解后获得道具数量
	public final int[] advHash;			//突破分解后获得道具
	public final int[] advHashNum;			//突破分解后获得道具数量
	public final int[] cultivationHash;			//修炼分解后获得道具
	public final int[] cultivationHashNum;			//修炼分解后获得道具数量
	public final int[] resolveHash;			//献祭分解所获得的道具
	public final int[] resolveHashNum;			//献祭分解所获得的道具数量
	public final int dailyCoinBuyValue;			//招财铜钱
	public final int[] cimeliaLevelHash;			//法宝升级分解后获得道具
	public final int[] cimeliaLevelHashNum;			//升级分解后获得道具数量
	public final int[] cimeliaAdvHash;			//法宝突破分解后获得道具
	public final int[] cimeliaAdvHashNum;			//法宝突破分解后获得道具数量
	public final int[] cimeliaResolveHash;			//法宝献祭分解所获得的道具
	public final int[] cimeliaResolveHashNum;			//法宝献祭分解所获得的道具数量
	public final int domainExp;			//仙域熟练等级经验

	public ConfLevelExp(int sn, int roleExp, int partner1Exp, int partner2Exp, int partner3Exp, int partner4Exp, int partner5Exp, int partner6Exp, int cimelia1Exp, int cimelia2Exp, int cimelia3Exp, int cimelia4Exp, int cimelia5Exp, int cimelia6Exp, int partnerCoin, int cimeliaCoin, int staminaAdd, int staminaMax, int[] coinInfo, int[] gradeCoinInfo, int[] lootMapInfo, int towerCorrect, int rune1Exp, int rune2Exp, int rune3Exp, int rune4Exp, int rune5Exp, int[] levelHash, int[] levelHashNum, int[] advHash, int[] advHashNum, int[] cultivationHash, int[] cultivationHashNum, int[] resolveHash, int[] resolveHashNum, int dailyCoinBuyValue, int[] cimeliaLevelHash, int[] cimeliaLevelHashNum, int[] cimeliaAdvHash, int[] cimeliaAdvHashNum, int[] cimeliaResolveHash, int[] cimeliaResolveHashNum, int domainExp) {
			this.sn = sn;		
			this.roleExp = roleExp;		
			this.partner1Exp = partner1Exp;		
			this.partner2Exp = partner2Exp;		
			this.partner3Exp = partner3Exp;		
			this.partner4Exp = partner4Exp;		
			this.partner5Exp = partner5Exp;		
			this.partner6Exp = partner6Exp;		
			this.cimelia1Exp = cimelia1Exp;		
			this.cimelia2Exp = cimelia2Exp;		
			this.cimelia3Exp = cimelia3Exp;		
			this.cimelia4Exp = cimelia4Exp;		
			this.cimelia5Exp = cimelia5Exp;		
			this.cimelia6Exp = cimelia6Exp;		
			this.partnerCoin = partnerCoin;		
			this.cimeliaCoin = cimeliaCoin;		
			this.staminaAdd = staminaAdd;		
			this.staminaMax = staminaMax;		
			this.coinInfo = coinInfo;		
			this.gradeCoinInfo = gradeCoinInfo;		
			this.lootMapInfo = lootMapInfo;		
			this.towerCorrect = towerCorrect;		
			this.rune1Exp = rune1Exp;		
			this.rune2Exp = rune2Exp;		
			this.rune3Exp = rune3Exp;		
			this.rune4Exp = rune4Exp;		
			this.rune5Exp = rune5Exp;		
			this.levelHash = levelHash;		
			this.levelHashNum = levelHashNum;		
			this.advHash = advHash;		
			this.advHashNum = advHashNum;		
			this.cultivationHash = cultivationHash;		
			this.cultivationHashNum = cultivationHashNum;		
			this.resolveHash = resolveHash;		
			this.resolveHashNum = resolveHashNum;		
			this.dailyCoinBuyValue = dailyCoinBuyValue;		
			this.cimeliaLevelHash = cimeliaLevelHash;		
			this.cimeliaLevelHashNum = cimeliaLevelHashNum;		
			this.cimeliaAdvHash = cimeliaAdvHash;		
			this.cimeliaAdvHashNum = cimeliaAdvHashNum;		
			this.cimeliaResolveHash = cimeliaResolveHash;		
			this.cimeliaResolveHashNum = cimeliaResolveHashNum;		
			this.domainExp = domainExp;		
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
	public static Collection<ConfLevelExp> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfLevelExp[] findArray() {
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
	public static ConfLevelExp get(Integer sn) {
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
	public static ConfLevelExp getBy(Object...params) {
		List<ConfLevelExp> list = utilBase(params);
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
	public static List<ConfLevelExp> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfLevelExp> utilBase(Object...params) {
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
		List<ConfLevelExp> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfLevelExp c : DATA.getList()) {
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
	private int compareTo(ConfLevelExp cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfLevelExp cell, Object...params) {
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
			case "roleExp": {
				value = this.roleExp;
				break;
			}
			case "partner1Exp": {
				value = this.partner1Exp;
				break;
			}
			case "partner2Exp": {
				value = this.partner2Exp;
				break;
			}
			case "partner3Exp": {
				value = this.partner3Exp;
				break;
			}
			case "partner4Exp": {
				value = this.partner4Exp;
				break;
			}
			case "partner5Exp": {
				value = this.partner5Exp;
				break;
			}
			case "partner6Exp": {
				value = this.partner6Exp;
				break;
			}
			case "cimelia1Exp": {
				value = this.cimelia1Exp;
				break;
			}
			case "cimelia2Exp": {
				value = this.cimelia2Exp;
				break;
			}
			case "cimelia3Exp": {
				value = this.cimelia3Exp;
				break;
			}
			case "cimelia4Exp": {
				value = this.cimelia4Exp;
				break;
			}
			case "cimelia5Exp": {
				value = this.cimelia5Exp;
				break;
			}
			case "cimelia6Exp": {
				value = this.cimelia6Exp;
				break;
			}
			case "partnerCoin": {
				value = this.partnerCoin;
				break;
			}
			case "cimeliaCoin": {
				value = this.cimeliaCoin;
				break;
			}
			case "staminaAdd": {
				value = this.staminaAdd;
				break;
			}
			case "staminaMax": {
				value = this.staminaMax;
				break;
			}
			case "coinInfo": {
				value = this.coinInfo;
				break;
			}
			case "gradeCoinInfo": {
				value = this.gradeCoinInfo;
				break;
			}
			case "lootMapInfo": {
				value = this.lootMapInfo;
				break;
			}
			case "towerCorrect": {
				value = this.towerCorrect;
				break;
			}
			case "rune1Exp": {
				value = this.rune1Exp;
				break;
			}
			case "rune2Exp": {
				value = this.rune2Exp;
				break;
			}
			case "rune3Exp": {
				value = this.rune3Exp;
				break;
			}
			case "rune4Exp": {
				value = this.rune4Exp;
				break;
			}
			case "rune5Exp": {
				value = this.rune5Exp;
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
			case "dailyCoinBuyValue": {
				value = this.dailyCoinBuyValue;
				break;
			}
			case "cimeliaLevelHash": {
				value = this.cimeliaLevelHash;
				break;
			}
			case "cimeliaLevelHashNum": {
				value = this.cimeliaLevelHashNum;
				break;
			}
			case "cimeliaAdvHash": {
				value = this.cimeliaAdvHash;
				break;
			}
			case "cimeliaAdvHashNum": {
				value = this.cimeliaAdvHashNum;
				break;
			}
			case "cimeliaResolveHash": {
				value = this.cimeliaResolveHash;
				break;
			}
			case "cimeliaResolveHashNum": {
				value = this.cimeliaResolveHashNum;
				break;
			}
			case "domainExp": {
				value = this.domainExp;
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
		public static final String sn = "sn";	//等级
		public static final String roleExp = "roleExp";	//主角升级经验
		public static final String partner1Exp = "partner1Exp";	//伙伴品质1升级经验
		public static final String partner2Exp = "partner2Exp";	//伙伴品质2升级经验
		public static final String partner3Exp = "partner3Exp";	//伙伴品质3升级经验
		public static final String partner4Exp = "partner4Exp";	//伙伴品质4升级经验
		public static final String partner5Exp = "partner5Exp";	//伙伴品质5升级经验
		public static final String partner6Exp = "partner6Exp";	//伙伴品质6升级经验
		public static final String cimelia1Exp = "cimelia1Exp";	//法宝品质1经验
		public static final String cimelia2Exp = "cimelia2Exp";	//法宝品质2经验
		public static final String cimelia3Exp = "cimelia3Exp";	//法宝品质3经验
		public static final String cimelia4Exp = "cimelia4Exp";	//法宝品质4经验
		public static final String cimelia5Exp = "cimelia5Exp";	//法宝品质5经验
		public static final String cimelia6Exp = "cimelia6Exp";	//法宝品质6经验
		public static final String partnerCoin = "partnerCoin";	//伙伴升级消耗铜币
		public static final String cimeliaCoin = "cimeliaCoin";	//法宝升级消耗铜币
		public static final String staminaAdd = "staminaAdd";	//主角升级体力增加
		public static final String staminaMax = "staminaMax";	//主角升级体力上限
		public static final String coinInfo = "coinInfo";	//全服BOSS单次结算铜钱奖励
		public static final String gradeCoinInfo = "gradeCoinInfo";	//等级BOSS单次结算铜钱奖励
		public static final String lootMapInfo = "lootMapInfo";	//抢夺本攻击与血
		public static final String towerCorrect = "towerCorrect";	//爬塔等级对应的修正战斗力
		public static final String rune1Exp = "rune1Exp";	//命格品质1升级经验
		public static final String rune2Exp = "rune2Exp";	//命格品质2升级经验
		public static final String rune3Exp = "rune3Exp";	//命格品质3升级经验
		public static final String rune4Exp = "rune4Exp";	//命格品质4升级经验
		public static final String rune5Exp = "rune5Exp";	//命格品质5升级经验
		public static final String levelHash = "levelHash";	//升级分解后获得道具
		public static final String levelHashNum = "levelHashNum";	//升级分解后获得道具数量
		public static final String advHash = "advHash";	//突破分解后获得道具
		public static final String advHashNum = "advHashNum";	//突破分解后获得道具数量
		public static final String cultivationHash = "cultivationHash";	//修炼分解后获得道具
		public static final String cultivationHashNum = "cultivationHashNum";	//修炼分解后获得道具数量
		public static final String resolveHash = "resolveHash";	//献祭分解所获得的道具
		public static final String resolveHashNum = "resolveHashNum";	//献祭分解所获得的道具数量
		public static final String dailyCoinBuyValue = "dailyCoinBuyValue";	//招财铜钱
		public static final String cimeliaLevelHash = "cimeliaLevelHash";	//法宝升级分解后获得道具
		public static final String cimeliaLevelHashNum = "cimeliaLevelHashNum";	//升级分解后获得道具数量
		public static final String cimeliaAdvHash = "cimeliaAdvHash";	//法宝突破分解后获得道具
		public static final String cimeliaAdvHashNum = "cimeliaAdvHashNum";	//法宝突破分解后获得道具数量
		public static final String cimeliaResolveHash = "cimeliaResolveHash";	//法宝献祭分解所获得的道具
		public static final String cimeliaResolveHashNum = "cimeliaResolveHashNum";	//法宝献祭分解所获得的道具数量
		public static final String domainExp = "domainExp";	//仙域熟练等级经验
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfLevelExp> _map;
		//按表顺序存储的数组
		private static ConfLevelExp[] _array;

		public static ConfLevelExp[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfLevelExp> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfLevelExp> getMap() {
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
			Map<Integer, ConfLevelExp> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfLevelExp[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfLevelExp object = new ConfLevelExp(conf.getIntValue("sn"), conf.getIntValue("roleExp"), conf.getIntValue("partner1Exp"), conf.getIntValue("partner2Exp"), 
				conf.getIntValue("partner3Exp"), conf.getIntValue("partner4Exp"), conf.getIntValue("partner5Exp"), conf.getIntValue("partner6Exp"), 
				conf.getIntValue("cimelia1Exp"), conf.getIntValue("cimelia2Exp"), conf.getIntValue("cimelia3Exp"), conf.getIntValue("cimelia4Exp"), 
				conf.getIntValue("cimelia5Exp"), conf.getIntValue("cimelia6Exp"), conf.getIntValue("partnerCoin"), conf.getIntValue("cimeliaCoin"), 
				conf.getIntValue("staminaAdd"), conf.getIntValue("staminaMax"), parseIntArray(conf.getString("coinInfo")), parseIntArray(conf.getString("gradeCoinInfo")), 
				parseIntArray(conf.getString("lootMapInfo")), conf.getIntValue("towerCorrect"), conf.getIntValue("rune1Exp"), conf.getIntValue("rune2Exp"), 
				conf.getIntValue("rune3Exp"), conf.getIntValue("rune4Exp"), conf.getIntValue("rune5Exp"), parseIntArray(conf.getString("levelHash")), 
				parseIntArray(conf.getString("levelHashNum")), parseIntArray(conf.getString("advHash")), parseIntArray(conf.getString("advHashNum")), parseIntArray(conf.getString("cultivationHash")), 
				parseIntArray(conf.getString("cultivationHashNum")), parseIntArray(conf.getString("resolveHash")), parseIntArray(conf.getString("resolveHashNum")), conf.getIntValue("dailyCoinBuyValue"), 
				parseIntArray(conf.getString("cimeliaLevelHash")), parseIntArray(conf.getString("cimeliaLevelHashNum")), parseIntArray(conf.getString("cimeliaAdvHash")), parseIntArray(conf.getString("cimeliaAdvHashNum")), 
				parseIntArray(conf.getString("cimeliaResolveHash")), parseIntArray(conf.getString("cimeliaResolveHashNum")), conf.getIntValue("domainExp"));
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
				String basePath = Utils.getClassPath("json/ConfLevelExp.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfLevelExp.json");
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
