package game.turnbasedsrv.enumType;

import java.util.HashMap;
import java.util.Map;

public enum CombatStepType {
	CombatInit(1), // 场景初始化
	CombatWaitFighter(2), // 等待战斗者进入场景
	CombatStart(3), // 场景开始
	CombatEnd(6), // 场景结束
	
	StepStart(101), // 场景阶段开始
	StepEnd(102), // 场景阶段结束

	RoundStart(201), // 回合开始
	RoundOrderStart(202), // 回合普攻前
	RoundOrderEnd(203), // 回合普攻后
	RoundEnd(204), // 回合结束
	RoundWaitEnd(205),// 等待回合结束
	RoundOrderWaitEnd(206), // 等待出手结束

	;
	/** 值 **/
	private long value;
	/** 映射表 **/
	private static final Map<Long, String> mapEnums = new HashMap<>();

	static {
		for (CombatStepType type : values()) {
			mapEnums.put(type.value(), type.name());
		}
	}

	/**
	 * 内部构造函数
	 * 
	 * @param value
	 */
	private CombatStepType(long value) {
		this.value = value;
	}

	/**
	 * 获取对应值
	 * 
	 * @return
	 */
	public long value() {
		return this.value;
	}

	/**
	 * 是否存在指定的枚举值
	 * 
	 * @return
	 */
	public static boolean containsKey(String value) {
		return mapEnums.containsKey(value);
	}

	/**
	 * 是否存在指定的枚举名
	 * 
	 * @return
	 */
	public static boolean containsValue(String name) {
		return mapEnums.containsValue(name);
	}

	/**
	 * 获取指定枚举名的枚举
	 * 
	 * @param name
	 * @return
	 */
	public static CombatStepType get(String name) {
		CombatStepType type = null;
		if (containsValue(name))
			type = valueOf(name);
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 * 
	 * @param value
	 * @return
	 */
	public static CombatStepType getByValue(long value) {
		CombatStepType type = null;
		String name = mapEnums.get(value);
		if (name != null) {
			type = valueOf(name);
		}
		return type;
	}
}
