package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 装备的品质
 */
public enum ItemGradeType {
	WHITE(1), // 白
	GREEN(2), // 绿
	BLUE(3), // 蓝
	PURPLE(4), // 紫
	GOLD(5), // 金
	
	;

	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (ItemGradeType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private ItemGradeType(int value) {
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
	public static ItemGradeType get(String name) {
		ItemGradeType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static ItemGradeType getByValue(int value) {
		ItemGradeType type = null;
		if (containsValue(value)) {
			for (ItemGradeType t : values()) {
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
		for (ItemGradeType k : ItemGradeType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
