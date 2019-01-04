package game.platform.integration;

import java.util.HashMap;
import java.util.Map;

public enum PayPlaceKey {
	dev("dev"),						//测试
	zhangqu("zhangqu"),		//掌趣SDK
	sina("sina"),     					//新浪SDK
	;
	
	//关键字与枚举的对应关系
	private final static Map<String, PayPlaceKey> keys = new HashMap<>();
	
	//关键字 有些平台关键字是数字开头 无法做枚举变量
	private String key;
	//汇率，1分多少元宝,默认是0.1
	private double rate;
	
	static {
		for(PayPlaceKey k : values()) {
			keys.put(k.getKey(), k);
		}
	}
	
	/**
	 * 构造函数
	 * @param key
	 */
	PayPlaceKey(String key) {
		this.key = key;
		this.rate = 0.1;
	}
	
	PayPlaceKey(String key, double rate) {
		this.key = key;
		this.rate = rate;
	}

	public String getKey() {
		return key;
	}
	
	/**
	 * 根据关键字获取枚举类型
	 * @param key
	 * @return
	 */
	public static PayPlaceKey getByKey(String key) {
		return keys.get(key);
	}

	public double getRate() {
		return rate;
	}
}