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
 * 送月卡 [zhCN]
 * PayMonthCard.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfPayMonthCard {
	public final int sn;			//送月卡
	public final int type;			//类型
	public final int rmb;			//人民币
	public final int gold;			//元宝[vip经验]
	public final int mailGold;			//好友月卡邮件元宝数
	public final int retGold;			//每日返还
	public final int gift;			//赠送
	public final int giftOnce;			//赠送一次
	public final String name;			//名称
	public final String desc;			//描述
	public final String p_name;			//红包名称
	public final int p_count;			//红包份数
	public final int p_moneyType;			//每份货币类型
	public final int p_moneyCount;			//每份数量
	public final int p_rateHit;			//领取成功率
	public final int p_rateTotal;			//分母为10000
	public final int edition;			//版本
	public final int favorDegree;			//增加好感度
	public final boolean isRecommend;			//是否为推荐项
	public final int itemSn;			//物品sn

	public ConfPayMonthCard(int sn, int type, int rmb, int gold, int mailGold, int retGold, int gift, int giftOnce, String name, String desc, String p_name, int p_count, int p_moneyType, int p_moneyCount, int p_rateHit, int p_rateTotal, int edition, int favorDegree, boolean isRecommend, int itemSn) {
			this.sn = sn;		
			this.type = type;		
			this.rmb = rmb;		
			this.gold = gold;		
			this.mailGold = mailGold;		
			this.retGold = retGold;		
			this.gift = gift;		
			this.giftOnce = giftOnce;		
			this.name = name;		
			this.desc = desc;		
			this.p_name = p_name;		
			this.p_count = p_count;		
			this.p_moneyType = p_moneyType;		
			this.p_moneyCount = p_moneyCount;		
			this.p_rateHit = p_rateHit;		
			this.p_rateTotal = p_rateTotal;		
			this.edition = edition;		
			this.favorDegree = favorDegree;		
			this.isRecommend = isRecommend;		
			this.itemSn = itemSn;		
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
	public static Collection<ConfPayMonthCard> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfPayMonthCard[] findArray() {
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
	public static ConfPayMonthCard get(Integer sn) {
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
	public static ConfPayMonthCard getBy(Object...params) {
		List<ConfPayMonthCard> list = utilBase(params);
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
	public static List<ConfPayMonthCard> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfPayMonthCard> utilBase(Object...params) {
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
		List<ConfPayMonthCard> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfPayMonthCard c : DATA.getList()) {
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
	private int compareTo(ConfPayMonthCard cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfPayMonthCard cell, Object...params) {
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
			case "type": {
				value = this.type;
				break;
			}
			case "rmb": {
				value = this.rmb;
				break;
			}
			case "gold": {
				value = this.gold;
				break;
			}
			case "mailGold": {
				value = this.mailGold;
				break;
			}
			case "retGold": {
				value = this.retGold;
				break;
			}
			case "gift": {
				value = this.gift;
				break;
			}
			case "giftOnce": {
				value = this.giftOnce;
				break;
			}
			case "name": {
				value = this.name;
				break;
			}
			case "desc": {
				value = this.desc;
				break;
			}
			case "p_name": {
				value = this.p_name;
				break;
			}
			case "p_count": {
				value = this.p_count;
				break;
			}
			case "p_moneyType": {
				value = this.p_moneyType;
				break;
			}
			case "p_moneyCount": {
				value = this.p_moneyCount;
				break;
			}
			case "p_rateHit": {
				value = this.p_rateHit;
				break;
			}
			case "p_rateTotal": {
				value = this.p_rateTotal;
				break;
			}
			case "edition": {
				value = this.edition;
				break;
			}
			case "favorDegree": {
				value = this.favorDegree;
				break;
			}
			case "isRecommend": {
				value = this.isRecommend;
				break;
			}
			case "itemSn": {
				value = this.itemSn;
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
		public static final String sn = "sn";	//送月卡
		public static final String type = "type";	//类型
		public static final String rmb = "rmb";	//人民币
		public static final String gold = "gold";	//元宝[vip经验]
		public static final String mailGold = "mailGold";	//好友月卡邮件元宝数
		public static final String retGold = "retGold";	//每日返还
		public static final String gift = "gift";	//赠送
		public static final String giftOnce = "giftOnce";	//赠送一次
		public static final String name = "name";	//名称
		public static final String desc = "desc";	//描述
		public static final String p_name = "p_name";	//红包名称
		public static final String p_count = "p_count";	//红包份数
		public static final String p_moneyType = "p_moneyType";	//每份货币类型
		public static final String p_moneyCount = "p_moneyCount";	//每份数量
		public static final String p_rateHit = "p_rateHit";	//领取成功率
		public static final String p_rateTotal = "p_rateTotal";	//分母为10000
		public static final String edition = "edition";	//版本
		public static final String favorDegree = "favorDegree";	//增加好感度
		public static final String isRecommend = "isRecommend";	//是否为推荐项
		public static final String itemSn = "itemSn";	//物品sn
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfPayMonthCard> _map;
		//按表顺序存储的数组
		private static ConfPayMonthCard[] _array;

		public static ConfPayMonthCard[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfPayMonthCard> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfPayMonthCard> getMap() {
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
			Map<Integer, ConfPayMonthCard> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfPayMonthCard[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfPayMonthCard object = new ConfPayMonthCard(conf.getIntValue("sn"), conf.getIntValue("type"), conf.getIntValue("rmb"), conf.getIntValue("gold"), 
				conf.getIntValue("mailGold"), conf.getIntValue("retGold"), conf.getIntValue("gift"), conf.getIntValue("giftOnce"), 
				conf.getString("name"), conf.getString("desc"), conf.getString("p_name"), conf.getIntValue("p_count"), 
				conf.getIntValue("p_moneyType"), conf.getIntValue("p_moneyCount"), conf.getIntValue("p_rateHit"), conf.getIntValue("p_rateTotal"), 
				conf.getIntValue("edition"), conf.getIntValue("favorDegree"), conf.getBooleanValue("isRecommend"), conf.getIntValue("itemSn"));
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
				String basePath = Utils.getClassPath("json/ConfPayMonthCard.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfPayMonthCard.json");
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
