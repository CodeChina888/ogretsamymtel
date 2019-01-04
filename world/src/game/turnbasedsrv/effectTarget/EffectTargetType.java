package game.turnbasedsrv.effectTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目标类型枚举
 */
public enum EffectTargetType {
	FriendRand("FriendRand"), // 取随机数量的友方
	FriendRandOther("FriendRandOther"), // 取随机数量的友方,优先他人，数量不足时再补自己
	EnemyRand("EnemyRand"), // 取随机数量的敌方

	EnemyPropDESC("EnemyPropDESC"), // 取某属性从高到低的敌方数量
	EnemyPropASC("EnemyPropASC"), // 取某属性从低到高的敌方数量
	FriendPropDESC("FriendPropDESC"), // 取某属性从高到低的友方数量
	FriendPropASC("FriendPropASC"), // 取某属性从低到高的友方数量

	EnemyPropHighest("EnemyPropHighest"), // 取某属性最高的敌方(单人，所在列，所在排)
	EnemyPropLowest("EnemyPropLowest"), // 取某属性最低的敌方(单人，所在列，所在排)
	FriendPropHighest("FriendPropHighest"), // 取某属性最高的友方(单人，所在列，所在排)
	FriendPropLowest("FriendPropLowest"), // 取某属性最低的友方(单人，所在列，所在排)

	Caster("Caster"), // 取施法者(单人，所在列，所在排)
	BuffOwner("BuffOwner"), // 取buff拥有者(单人，所在列，所在排)

	Enemy("Enemy"), // 取敌方(全体，单人，所在列，所在排，前排，后排，前排单人，后排单人，随机2列)
	Friend("Friend"), // 取友方(全体，单人，所在列，所在排，前排，后排，前排单人，后排单人，随机2列)

	TriggerSelf("TriggerSelf"), // 触发点触发者(单人，所在列，所在排)
	TriggerTarget("TriggerTarget"), // 触发点触发目标(单人，所在列，所在排)

	;

	private String value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, String> mapEnums = new HashMap<>();

	static {
		for (EffectTargetType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private EffectTargetType(String value) {
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
	public static EffectTargetType get(String name) {
		EffectTargetType type = null;
		if (containsKey(name)) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 */
	public static EffectTargetType getByValue(String value) {
		EffectTargetType type = null;
		if (containsValue(value)) {
			for (EffectTargetType t : values()) {
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
		for (EffectTargetType k : EffectTargetType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
