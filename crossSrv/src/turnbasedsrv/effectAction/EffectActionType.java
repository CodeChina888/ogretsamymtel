package turnbasedsrv.effectAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 条件类型枚举
 */
public enum EffectActionType {
	OperateDirect("OperateDirect"), // 对目标进行直接血量运算（正值时为最大血量万分比，负值时为当前血量万分比）
	OperateHurt("OperateHurt"), // 对目标进行攻防运算
	OperateCure("OperateCure"), // 对目标进行治疗运算
	OperateBuffCure("OperateBuffCure"), // 对目标进行Buff治疗运算
	OperatePoison("OperatePoison"), // 对目标进行中毒运算
	OperateBlood("OperateBlood"), // 对目标进行流血运算
	OperateBurn("OperateBurn"), // 对目标进行灼烧运算
	OperateKill("OperateKill"), // 对目标直接击杀，无视任何状态（包括无敌）

	BuffAdd("BuffAdd"), // 给目标增加buff
	BuffRemoveBySn("BuffRemoveBySn"), // 给目标移除指定sn的buff
	BuffRemoveByType("BuffRemoveByType"), // 给目标移除指定类型的buff
	BuffRemoveByGroup("BuffRemoveByGroup"), // 给目标移除指定组的buff

	RageAdd("RageAdd"), // 给目标加怒

	TriggerSetTimes("TriggerSetTimes"), // 设置触发次数，次数数据不存在时设置数据（-1），存在时减少1，当次数为0时不再触发

	;

	private String value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, String> mapEnums = new HashMap<>();

	static {
		for (EffectActionType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private EffectActionType(String value) {
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
	public static EffectActionType get(String name) {
		EffectActionType type = null;
		if (containsKey(name)) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 */
	public static EffectActionType getByValue(String value) {
		EffectActionType type = null;
		if (containsValue(value)) {
			for (EffectActionType t : values()) {
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
		for (EffectActionType k : EffectActionType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
