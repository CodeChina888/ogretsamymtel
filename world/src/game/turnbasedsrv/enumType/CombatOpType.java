package game.turnbasedsrv.enumType;

import java.util.HashMap;
import java.util.Map;

public enum CombatOpType{
	/** 创建怪物 **/
	CreateMonter(1),
	/** 创建怪物 **/
	RemoveFighter(2),
	/** 初始化战斗者 **/
	InitFightObj(3),
	/** 创建主角 **/
	CreateHumanObj(4),
	/** 获取显示倍数 **/
	GetStepMultipe(5),
	/** 配置为怪物属性 **/
	InitPropMonsterConf(6),
	
	/** action结束 **/
	ActionFinish(101),
	/** 初始化战斗者 **/
	ActionInitFightObj(102),
	/** 剧情结束 **/
	ActionPlotFinish(103),
	/** 战斗者进出场结束 **/
	ActionFighterShowFinish(104),
	
	/** 发送剧情触发消息 **/
	SendPlotMsg(201),
	/** 战斗者入场 **/
	SendFighterEnter(202),
	/** 战斗者离场 **/
	SendFighterLeave(203),	
	/** 发送场景战斗者信息 **/
	SendStepInfoMsg(204),
	
	/** 主角手动释放技能 **/
	HumanCommmonSkill(301),
	/** 等待主角出手 **/
	WaitHumanOp(302),

	;
	/** 值 **/
	private long value;
	/** 映射表 **/
	private static final Map<Long, String> mapEnums = new HashMap<>();

	static {
		for (CombatOpType type : values()) {
			mapEnums.put(type.value(), type.name());
		}
	}

	/**
	 * 内部构造函数
	 * 
	 * @param value
	 */
	private CombatOpType(long value) {
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
	public static CombatOpType get(String name) {
		CombatOpType type = null;
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
	public static CombatOpType getByValue(long value) {
		CombatOpType type = null;
		String name = mapEnums.get(value);
		if (name != null) {
			type = valueOf(name);
		}
		return type;
	}
}
