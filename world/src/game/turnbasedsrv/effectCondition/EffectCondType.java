package game.turnbasedsrv.effectCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 条件类型枚举
 */
public enum EffectCondType {
	Round("Round"), // 当前回合数
	Probability("Probability"), // 万分比几率

	CountFriend("CountFriend"), // 友方人数
	CountEnemy("CountEnemy"), // 敌方人数

	SelfProp("SelfProp"), // 自身某属性值
	TargetProp("TargetProp"), // 目标某属性值
	TriggerSelfProp("TriggerSelfProp"), // 触发者某属性值
	TriggerTargetProp("TriggerTargetProp"), // 触发目标某属性值

	SelfPropPct("SelfPropPct"), // 自身某属性万分比值
	TargetPropPct("TargetPropPct"), // 目标某属性万分比值
	TriggerSelfPropPct("TriggerSelfPropPct"), // 触发者某属性万分比值
	TriggerTargetPropPct("TriggerTargetPropPct"), // 触发目标某属性万分比值

	SelfCountry("SelfCountry"), // 自身派别
	TargetCountry("TargetCountry"), // 目标派别
	TriggerSelfCountry("TriggerSelfCountry"), // 获取触发者派别
	TriggerTargetCountry("TriggerTargetCountry"), // 获取触发目标派别

	TargetIsFriend("TargetIsFriend"), // 目标是友方
	TriggerTargetIsFriend("TriggerTargetIsFriend"), // 触发目标是友方
	TriggerSelfIsFriend("TriggerSelfIsFriend"), // 触发者是友方

	SelfHasBuffType("SelfHasBuffType"), // 自身含有某类型buff
	TargetHasBuffType("TargetHasBuffType"), // 目标含有某类型buff
	TriggerSelfHasBuffType("TriggerSelfHasBuffType"), // 触发者含有某类型buff
	TriggerTargetHasBuffType("TriggerTargetHasBuffType"), // 触发目标含有某类型buff
	
	SkillKillCount("SkillKillCount"), // 本次技能击杀的人数
	;

	private String value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, String> mapEnums = new HashMap<>();

	static {
		for (EffectCondType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private EffectCondType(String value) {
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
	public static EffectCondType get(String name) {
		EffectCondType type = null;
		if (containsKey(name)) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 */
	public static EffectCondType getByValue(String value) {
		EffectCondType type = null;
		if (containsValue(value)) {
			for (EffectCondType t : values()) {
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
		for (EffectCondType k : EffectCondType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
