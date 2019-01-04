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
 * VIP等级 [zhCN]
 * VipUpgrade.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfVipUpgrade {
	public final int sn;			//VIP等级
	public final int amount;			//升级所需充值总额
	public final int actBuyNum;			//每日购买体力次数
	public final int coinBuyNum;			//每日购买金币次数
	public final int instResetNum;			//每日重置副本次数
	public final int towerBuyNum;			//爬塔玩法多倍奖励购买
	public final int towerRebornNum;			//爬塔复活购买
	public final int occupyBuyNum;			//每日购买开采令次数
	public final int grabBuyNum;			//每日购买抢夺令次数
	public final int guildInstBuyNum;			//每日购买仙盟副本挑战次数
	public final int competeFightNum;			//购买竞技场挑战次数
	public final int winksNum;			//魔法表情免费次数
	public final int generalShopNum;			//将魂商店刷新次数
	public final int goldShopNum;			//元宝商店刷新次数
	public final int goldShopNum1;			//将魂商店刷新次数
	public final int goldShopNum2;			//声望商店刷新次数
	public final int goldShopNum3;			//仙玉商店刷新次数
	public final int goldShopNum4;			//仙缘商店刷新次数
	public final int goldShopNum5;			//仙盟商店刷新次数
	public final int vipLvGift;			//VIP等级礼包
	public final int oldGold;			//VIP礼包原价
	public final int nowGold;			//VIP礼包现价
	public final int vipAward;			//VIP特殊奖励

	public ConfVipUpgrade(int sn, int amount, int actBuyNum, int coinBuyNum, int instResetNum, int towerBuyNum, int towerRebornNum, int occupyBuyNum, int grabBuyNum, int guildInstBuyNum, int competeFightNum, int winksNum, int generalShopNum, int goldShopNum, int goldShopNum1, int goldShopNum2, int goldShopNum3, int goldShopNum4, int goldShopNum5, int vipLvGift, int oldGold, int nowGold, int vipAward) {
			this.sn = sn;		
			this.amount = amount;		
			this.actBuyNum = actBuyNum;		
			this.coinBuyNum = coinBuyNum;		
			this.instResetNum = instResetNum;		
			this.towerBuyNum = towerBuyNum;		
			this.towerRebornNum = towerRebornNum;		
			this.occupyBuyNum = occupyBuyNum;		
			this.grabBuyNum = grabBuyNum;		
			this.guildInstBuyNum = guildInstBuyNum;		
			this.competeFightNum = competeFightNum;		
			this.winksNum = winksNum;		
			this.generalShopNum = generalShopNum;		
			this.goldShopNum = goldShopNum;		
			this.goldShopNum1 = goldShopNum1;		
			this.goldShopNum2 = goldShopNum2;		
			this.goldShopNum3 = goldShopNum3;		
			this.goldShopNum4 = goldShopNum4;		
			this.goldShopNum5 = goldShopNum5;		
			this.vipLvGift = vipLvGift;		
			this.oldGold = oldGold;		
			this.nowGold = nowGold;		
			this.vipAward = vipAward;		
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
	public static Collection<ConfVipUpgrade> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfVipUpgrade[] findArray() {
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
	public static ConfVipUpgrade get(Integer sn) {
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
	public static ConfVipUpgrade getBy(Object...params) {
		List<ConfVipUpgrade> list = utilBase(params);
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
	public static List<ConfVipUpgrade> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfVipUpgrade> utilBase(Object...params) {
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
		List<ConfVipUpgrade> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfVipUpgrade c : DATA.getList()) {
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
	private int compareTo(ConfVipUpgrade cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfVipUpgrade cell, Object...params) {
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
			case "amount": {
				value = this.amount;
				break;
			}
			case "actBuyNum": {
				value = this.actBuyNum;
				break;
			}
			case "coinBuyNum": {
				value = this.coinBuyNum;
				break;
			}
			case "instResetNum": {
				value = this.instResetNum;
				break;
			}
			case "towerBuyNum": {
				value = this.towerBuyNum;
				break;
			}
			case "towerRebornNum": {
				value = this.towerRebornNum;
				break;
			}
			case "occupyBuyNum": {
				value = this.occupyBuyNum;
				break;
			}
			case "grabBuyNum": {
				value = this.grabBuyNum;
				break;
			}
			case "guildInstBuyNum": {
				value = this.guildInstBuyNum;
				break;
			}
			case "competeFightNum": {
				value = this.competeFightNum;
				break;
			}
			case "winksNum": {
				value = this.winksNum;
				break;
			}
			case "generalShopNum": {
				value = this.generalShopNum;
				break;
			}
			case "goldShopNum": {
				value = this.goldShopNum;
				break;
			}
			case "goldShopNum1": {
				value = this.goldShopNum1;
				break;
			}
			case "goldShopNum2": {
				value = this.goldShopNum2;
				break;
			}
			case "goldShopNum3": {
				value = this.goldShopNum3;
				break;
			}
			case "goldShopNum4": {
				value = this.goldShopNum4;
				break;
			}
			case "goldShopNum5": {
				value = this.goldShopNum5;
				break;
			}
			case "vipLvGift": {
				value = this.vipLvGift;
				break;
			}
			case "oldGold": {
				value = this.oldGold;
				break;
			}
			case "nowGold": {
				value = this.nowGold;
				break;
			}
			case "vipAward": {
				value = this.vipAward;
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
		public static final String sn = "sn";	//VIP等级
		public static final String amount = "amount";	//升级所需充值总额
		public static final String actBuyNum = "actBuyNum";	//每日购买体力次数
		public static final String coinBuyNum = "coinBuyNum";	//每日购买金币次数
		public static final String instResetNum = "instResetNum";	//每日重置副本次数
		public static final String towerBuyNum = "towerBuyNum";	//爬塔玩法多倍奖励购买
		public static final String towerRebornNum = "towerRebornNum";	//爬塔复活购买
		public static final String occupyBuyNum = "occupyBuyNum";	//每日购买开采令次数
		public static final String grabBuyNum = "grabBuyNum";	//每日购买抢夺令次数
		public static final String guildInstBuyNum = "guildInstBuyNum";	//每日购买仙盟副本挑战次数
		public static final String competeFightNum = "competeFightNum";	//购买竞技场挑战次数
		public static final String winksNum = "winksNum";	//魔法表情免费次数
		public static final String generalShopNum = "generalShopNum";	//将魂商店刷新次数
		public static final String goldShopNum = "goldShopNum";	//元宝商店刷新次数
		public static final String goldShopNum1 = "goldShopNum1";	//将魂商店刷新次数
		public static final String goldShopNum2 = "goldShopNum2";	//声望商店刷新次数
		public static final String goldShopNum3 = "goldShopNum3";	//仙玉商店刷新次数
		public static final String goldShopNum4 = "goldShopNum4";	//仙缘商店刷新次数
		public static final String goldShopNum5 = "goldShopNum5";	//仙盟商店刷新次数
		public static final String vipLvGift = "vipLvGift";	//VIP等级礼包
		public static final String oldGold = "oldGold";	//VIP礼包原价
		public static final String nowGold = "nowGold";	//VIP礼包现价
		public static final String vipAward = "vipAward";	//VIP特殊奖励
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfVipUpgrade> _map;
		//按表顺序存储的数组
		private static ConfVipUpgrade[] _array;

		public static ConfVipUpgrade[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfVipUpgrade> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfVipUpgrade> getMap() {
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
			Map<Integer, ConfVipUpgrade> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfVipUpgrade[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfVipUpgrade object = new ConfVipUpgrade(conf.getIntValue("sn"), conf.getIntValue("amount"), conf.getIntValue("actBuyNum"), conf.getIntValue("coinBuyNum"), 
				conf.getIntValue("instResetNum"), conf.getIntValue("towerBuyNum"), conf.getIntValue("towerRebornNum"), conf.getIntValue("occupyBuyNum"), 
				conf.getIntValue("grabBuyNum"), conf.getIntValue("guildInstBuyNum"), conf.getIntValue("competeFightNum"), conf.getIntValue("winksNum"), 
				conf.getIntValue("generalShopNum"), conf.getIntValue("goldShopNum"), conf.getIntValue("goldShopNum1"), conf.getIntValue("goldShopNum2"), 
				conf.getIntValue("goldShopNum3"), conf.getIntValue("goldShopNum4"), conf.getIntValue("goldShopNum5"), conf.getIntValue("vipLvGift"), 
				conf.getIntValue("oldGold"), conf.getIntValue("nowGold"), conf.getIntValue("vipAward"));
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
				String basePath = Utils.getClassPath("json/ConfVipUpgrade.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfVipUpgrade.json");
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
