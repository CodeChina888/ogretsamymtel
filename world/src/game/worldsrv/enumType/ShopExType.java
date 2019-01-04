package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.worldsrv.enumType.ShopExType;

/**
 * 商店类型
 * @author shenjh
 */
public enum ShopExType {
	Non(0), // 不限制
	TrialShop(1),   // 试炼商店
	CompeteShop(2), // 竞技商店
	GuildShop(3),   // 公会商店
	MysticalShop(4),//神秘商店
	SoulShop(5),	 //魂点商店

	;

	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (ShopExType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private ShopExType(int value) {
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
	public static ShopExType get(String name) {
		ShopExType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static ShopExType getByValue(int value) {
		ShopExType type = null;
		if (containsValue(value)) {
			for (ShopExType t : values()) {
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
		for (ShopExType k : ShopExType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
