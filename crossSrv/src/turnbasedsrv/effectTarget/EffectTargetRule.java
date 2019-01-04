package turnbasedsrv.effectTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目标规则枚举
 */
public enum EffectTargetRule {
	All("All"), // 全体
	One("One"), // 单人
	RandomOne("RandomOne"), // 随机单人
	Others("Others"), // 除自己外的其他人
	Col("Col"), // 所在列
	Row("Row"), // 所在排
	FrontRow("FrontRow"), // 前排，如前排没人则继续找后排
	BackRow("BackRow"), // 后排，如后排没人则继续找前排
	FrontRowOne("FrontRowOne"), // 前排单人
	BackRowOne("BackRowOne"), // 后排单人
	Col2("Col2"), // 随机2列
	Triangle("Triangle"), // 三角范围
	;

	private String value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, String> mapEnums = new HashMap<>();

	static {
		for (EffectTargetRule type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private EffectTargetRule(String value) {
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
	public static EffectTargetRule get(String name) {
		EffectTargetRule type = null;
		if (containsKey(name)) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 */
	public static EffectTargetRule getByValue(String value) {
		EffectTargetRule type = null;
		if (containsValue(value)) {
			for (EffectTargetRule t : values()) {
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
		for (EffectTargetRule k : EffectTargetRule.values()) {
			result.add(k.name());
		}
		return result;
	}
}
