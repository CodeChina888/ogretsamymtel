package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Neak
 * @see :战斗条件
 */
public enum BattleConditionType {
	enemyAllDie(0), // 敌方全部阵亡
	ourDieNum(1), // 我方阵亡数量
	roundNum(2), // 回合数
	remainHp(3); // 我方剩余血量
	
	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (BattleConditionType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private BattleConditionType(int value) {
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
	public static BattleConditionType get(String name) {
		BattleConditionType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static BattleConditionType getByValue(int value) {
		BattleConditionType type = null;
		if (containsValue(value)) {
			for (BattleConditionType t : values()) {
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
		for (BattleConditionType k : BattleConditionType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
