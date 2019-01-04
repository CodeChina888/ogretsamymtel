package turnbasedsrv.enumType;

import java.util.HashMap;
import java.util.Map;

public enum StageStep {
	StageInit(1), // 场景初始化
	StageWaitFighter(2), // 等待战斗者进入场景
	StageStart(3), // 场景开始
	StageStepStart(4), // 场景阶段开始
	StageStepEnd(5), // 场景阶段结束
	StageEnd(6), // 场景结束

	RoundStart(101), // 回合开始
	RoundOrderStart(102), // 回合普攻前
	RoundOrderEnd(103), // 回合普攻后
	RoundEnd(104), // 回合结束
	RoundWaitEnd(105),// 等待回合结束

	;
	/** 值 **/
	private long value;
	/** 映射表 **/
	private static final Map<Long, String> mapEnums = new HashMap<>();

	static {
		for (StageStep type : values()) {
			mapEnums.put(type.value(), type.name());
		}
	}

	/**
	 * 内部构造函数
	 * 
	 * @param value
	 */
	private StageStep(long value) {
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
	 * @param name
	 * @return
	 */
	public static boolean containsKey(String value) {
		return mapEnums.containsKey(value);
	}

	/**
	 * 是否存在指定的枚举名
	 * 
	 * @param value
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
	public static StageStep get(String name) {
		StageStep type = null;
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
	public static StageStep getByValue(long value) {
		StageStep type = null;
		String name = mapEnums.get(value);
		if (name != null) {
			type = valueOf(name);
		}
		return type;
	}
}
