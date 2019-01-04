package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商店兑换类型枚举
 * @author shenjh
 */
public enum ShopExchangeType {
	Non(0), // 不限制
	HumanLevel(1), // 人物等级
	Compete(2), // 竞技场排名
	GuildLevel(3), // 公会等级
	Viplevel(4), // vip等级

	;

	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (ShopExchangeType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private ShopExchangeType(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}

	/**
	 * 是否存在指定的枚举名
	 */
	public static boolean containsKey(String name) {
		return mapEnums.containsKey(name);
	}
	/**
	 * 是否存在指定的枚举值
	 */
	public static boolean containsValue(int value) {
		return mapEnums.containsValue(value);
	}
	
	/**
	 * 获取指定枚举名的枚举
	 */
	public static ShopExchangeType get(String name) {
		ShopExchangeType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static ShopExchangeType getByValue(int value) {
		ShopExchangeType type = null;
		if (containsValue(value)) {
			for (ShopExchangeType t : values()) {
				if (t.value() == value) {
					type = t;
					break;
				}
			}
		}
		return type;
	}
	
	/**
	 * 获取枚举名列表
	 */
	public static List<String> toList() {
		List<String> result = new ArrayList<>();
		for (ShopExchangeType k : ShopExchangeType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
