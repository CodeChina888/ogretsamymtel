package game.turnbasedsrv.prop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性系数类型枚举
 */
public enum PropCoefType {
	SelfLoseHpPctCoef("SelfLoseHpPctCoef"), // 获取自身减少的生命万分比倍数
	TargetLoseHpPctCoef("TargetLoseHpPctCoef"), // 获取目标减少的生命万分比倍数
	TriggerTargetLoseHpPctCoef("TriggerTargetLoseHpPctCoef"), // 获取触发目标减少的生命万分比倍数
	TriggerSelfLoseHpPctCoef("TriggerSelfLoseHpPctCoef"), // 获取触发者减少的生命万分比倍数

	;

	private String value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, String> mapEnums = new HashMap<>();

	static {
		for (PropCoefType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private PropCoefType(String value) {
		this.value = value;
	}

	public String value() {
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
	public static boolean containsValue(String value) {
		return mapEnums.containsValue(value);
	}

	/**
	 * 获取指定枚举名的枚举
	 */
	public static PropCoefType get(String name) {
		PropCoefType type = null;
		if (containsKey(name)) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 */
	public static PropCoefType getByValue(String value) {
		PropCoefType type = null;
		if (containsValue(value)) {
			for (PropCoefType t : values()) {
				if (t.value().equals(value)) {
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
		for (PropCoefType k : PropCoefType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
