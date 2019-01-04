package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ElementEffectType {
	CommonMonster(1), // 1 对普通怪物弱属性克制时有破甲加成.
	BOOS(2), // 2 对BOSS怪物弱属性克制时有破甲加成。
	WeakMonster(3), // 3 对弱属性克制时有总伤害加成.（对怪物）
	SkillAddMonster(4), // 4 与技能属性一致时使用技能时产生的属性伤害有额外加成（对怪物）.
	SkillAddHuman(5), // 5 与技能属性一致时使用技能时产生的属性伤害有额外加成（对主角）.
	WeakMonsterRepel(6), // 6 对弱属性克制产生破甲后使对象相应抗性衰减.（对怪物）
	EnchantmentAdd(7), // 7 附魔获得与标签一致元素时，属性值获得加成.
	EnchantmentRepel(8), // 8 附魔获得与标签一致元素时，抗性值获得加成.

	; 
	
	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (ElementEffectType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private ElementEffectType(int value) {
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
	public static ElementEffectType get(String name) {
		ElementEffectType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static ElementEffectType getByValue(int value) {
		ElementEffectType type = null;
		if (containsValue(value)) {
			for (ElementEffectType t : values()) {
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
		for (ElementEffectType k : ElementEffectType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
