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
 * 商店格子sn [zhCN]
 * ShopProps.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfShopProps {
	public final int sn;			//商店格子sn
	public final int storeType;			//商店类型 
	public final boolean isRandom;			//是否随机
	public final int[] goodsIDList;			//商品ID列表
	public final int[] goodsCountList;			//商品数量列表
	public final int[] goodsWeightList;			//商品权重列表
	public final int[] goodsShowMinLv;			//显示最低等级必须从小到大填写
	public final int[] goodsShowMaxLv;			//显示最高等级必须从小到大填写
	public final int vipLv;			//vip等级限制
	public final int guildLv;			//仙盟等级限制
	public final int buyType;			//购买类型
	public final int[] goodsPriceList;			//商品价格列表
	public final int[] goodsLevel;			//推荐标签显示
	public final int buyTime;			//限购次数
	public final int discount;			//万分比折扣
	public final boolean isRefresh;			//是否整点刷新

	public ConfShopProps(int sn, int storeType, boolean isRandom, int[] goodsIDList, int[] goodsCountList, int[] goodsWeightList, int[] goodsShowMinLv, int[] goodsShowMaxLv, int vipLv, int guildLv, int buyType, int[] goodsPriceList, int[] goodsLevel, int buyTime, int discount, boolean isRefresh) {
			this.sn = sn;		
			this.storeType = storeType;		
			this.isRandom = isRandom;		
			this.goodsIDList = goodsIDList;		
			this.goodsCountList = goodsCountList;		
			this.goodsWeightList = goodsWeightList;		
			this.goodsShowMinLv = goodsShowMinLv;		
			this.goodsShowMaxLv = goodsShowMaxLv;		
			this.vipLv = vipLv;		
			this.guildLv = guildLv;		
			this.buyType = buyType;		
			this.goodsPriceList = goodsPriceList;		
			this.goodsLevel = goodsLevel;		
			this.buyTime = buyTime;		
			this.discount = discount;		
			this.isRefresh = isRefresh;		
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
	public static Collection<ConfShopProps> findAll() {
		return DATA.getList();
	}
	
	/**
	 * 获取全部数据数组（按表顺序）
	 * @return
	 */
	public static ConfShopProps[] findArray() {
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
	public static ConfShopProps get(Integer sn) {
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
	public static ConfShopProps getBy(Object...params) {
		List<ConfShopProps> list = utilBase(params);
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
	public static List<ConfShopProps> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfShopProps> utilBase(Object...params) {
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
		List<ConfShopProps> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfShopProps c : DATA.getList()) {
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
	private int compareTo(ConfShopProps cell, List<OrderByField> paramsOrder) {
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
	public int compare(ConfShopProps cell, Object...params) {
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
			case "storeType": {
				value = this.storeType;
				break;
			}
			case "isRandom": {
				value = this.isRandom;
				break;
			}
			case "goodsIDList": {
				value = this.goodsIDList;
				break;
			}
			case "goodsCountList": {
				value = this.goodsCountList;
				break;
			}
			case "goodsWeightList": {
				value = this.goodsWeightList;
				break;
			}
			case "goodsShowMinLv": {
				value = this.goodsShowMinLv;
				break;
			}
			case "goodsShowMaxLv": {
				value = this.goodsShowMaxLv;
				break;
			}
			case "vipLv": {
				value = this.vipLv;
				break;
			}
			case "guildLv": {
				value = this.guildLv;
				break;
			}
			case "buyType": {
				value = this.buyType;
				break;
			}
			case "goodsPriceList": {
				value = this.goodsPriceList;
				break;
			}
			case "goodsLevel": {
				value = this.goodsLevel;
				break;
			}
			case "buyTime": {
				value = this.buyTime;
				break;
			}
			case "discount": {
				value = this.discount;
				break;
			}
			case "isRefresh": {
				value = this.isRefresh;
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
		public static final String sn = "sn";	//商店格子sn
		public static final String storeType = "storeType";	//商店类型 
		public static final String isRandom = "isRandom";	//是否随机
		public static final String goodsIDList = "goodsIDList";	//商品ID列表
		public static final String goodsCountList = "goodsCountList";	//商品数量列表
		public static final String goodsWeightList = "goodsWeightList";	//商品权重列表
		public static final String goodsShowMinLv = "goodsShowMinLv";	//显示最低等级必须从小到大填写
		public static final String goodsShowMaxLv = "goodsShowMaxLv";	//显示最高等级必须从小到大填写
		public static final String vipLv = "vipLv";	//vip等级限制
		public static final String guildLv = "guildLv";	//仙盟等级限制
		public static final String buyType = "buyType";	//购买类型
		public static final String goodsPriceList = "goodsPriceList";	//商品价格列表
		public static final String goodsLevel = "goodsLevel";	//推荐标签显示
		public static final String buyTime = "buyTime";	//限购次数
		public static final String discount = "discount";	//万分比折扣
		public static final String isRefresh = "isRefresh";	//是否整点刷新
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static Map<Integer, ConfShopProps> _map;
		//按表顺序存储的数组
		private static ConfShopProps[] _array;

		public static ConfShopProps[] getArray() {
			return _array;
		}
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfShopProps> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfShopProps> getMap() {
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
			Map<Integer, ConfShopProps> dataMap = new ConcurrentHashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) 
				return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			//设置数组长度
			_array = new ConfShopProps[confs.size()];
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				if (!conf.containsKey("sn"))
					continue;
				
				ConfShopProps object = new ConfShopProps(conf.getIntValue("sn"), conf.getIntValue("storeType"), conf.getBooleanValue("isRandom"), parseIntArray(conf.getString("goodsIDList")), 
				parseIntArray(conf.getString("goodsCountList")), parseIntArray(conf.getString("goodsWeightList")), parseIntArray(conf.getString("goodsShowMinLv")), parseIntArray(conf.getString("goodsShowMaxLv")), 
				conf.getIntValue("vipLv"), conf.getIntValue("guildLv"), conf.getIntValue("buyType"), parseIntArray(conf.getString("goodsPriceList")), 
				parseIntArray(conf.getString("goodsLevel")), conf.getIntValue("buyTime"), conf.getIntValue("discount"), conf.getBooleanValue("isRefresh"));
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
				String basePath = Utils.getClassPath("json/ConfShopProps.json");
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
						JarEntry dbEntry = jar.getJarEntry("json/ConfShopProps.json");
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
