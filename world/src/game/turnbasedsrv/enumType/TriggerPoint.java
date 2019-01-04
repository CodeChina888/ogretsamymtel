package game.turnbasedsrv.enumType;

import java.util.HashMap;
import java.util.Map;

public enum TriggerPoint {
	/** 立即生效Buff **/
	StartBuff(0),
	
	/** 战斗开始 **/
	CombatStart(1),
	/** 回合开始 **/
	RoundStart(2),
	/** 回合结束 **/
	RoundEnd(3),
	/** 回合普攻前 **/
	RoundOrderStart(4),
	/** 回合普攻后 **/
	RoundOrderEnd(5),
	/** 地图阶段开始 **/
	StepStart(6),
	/** 地图阶段结束 **/
	StepEnd(7),
	/** 战斗开始被动技能立即触发 **/
	PassiveStart(8),
	/** 死亡被动技能立即触发 **/
	PassiveDeath(9),

	/**************************************
	 * 以下的触发，被触发的会马上执行, 属于特定触发 *
	 **************************************/
	/** 对目标属性 **/
	ImTargetProp(101, true, true),
	/** 对攻击者属性 **/
	ImFightProp(102, true, true),
	/** 普攻对目标属性 **/
	ImNormalAtkToTargetProp(103, true, true),
	/** 普攻对攻击者属性 **/
	ImNormalAtkToFightProp(104, true, true),
	/** 怒攻对目标属性 **/
	ImSuperAtkToTargetProp(105, true, true),
	/** 怒攻对攻击者属性 **/
	ImSuperAtkToFightProp(106, true, true),
	/** 治疗对目标属性 **/
	ImCureTargetProp(107, true, true),
	/** 治疗对攻击者属性 **/
	ImCureFightProp(108, true, true),
	/*****************************************************
	 * 以下的触发，触发时会产生一个触发器，被触发的将在下一轮触发中被执行 *
	 *****************************************************/
	/** 普攻后 **/
	AfterNormalAtk(201, false, true),
	/** 被普攻后 **/
	AfterNormalAtked(202, false, true),
	/** 怒攻后 **/
	AfterSuperAtk(203, false, true),
	/** 被怒攻后 **/
	AfterSuperAtked(204, false, true),
	/** 攻击后 **/
	AfterAttack(205, false, true),
	/** 被攻击后 **/
	AfterAttacked(206, false, true),
	/** 治疗后 **/
	AfterCure(207, false, true),
	/** 被治疗后 **/
	AfterCured(208, false, true),
	/** 伤害类型攻击后 **/
	AfterDamageAttack(209, false, true),
	/** 被伤害类型攻击后 **/
	AfterDamageAttacked(210, false, true),

	/** hp减少 **/
	DecreaseHp(301, false, true),
	/** hp增加 **/
	InCreaseHp(302, false, true),
	/** 命中后 **/
	Hit(303, false, true),
	/** 被命中后 **/
	Hited(304, false, true),
	/** 被闪避后 **/
	Dodged(305, false, true),
	/** 闪避后 **/
	Dodge(306, false, true),
	/** 被格挡 **/
	Blocked(307, false, true),
	/** 格挡 **/
	Block(308, false, true),
	/** 暴击 **/
	Crit(309, false, true),
	/** 被暴击 */
	Crited(310, false, true),
	/** 死亡 */
	Dead(311, false, false),
	/** 击杀 */
	Kill(312, false, true),
	/** 普攻命中后 **/
	NormalHit(313, false, true),
	/** 被普攻命中后 **/
	NormalHited(314, false, true),
	/** 怒攻命中后 **/
	SuperHit(315, false, true),
	/** 被怒攻命中后 **/
	SuperHited(316, false, true),
	/** 命中敌方后 **/
	DifTeamHit(317, false, true),
	/** 被敌方命中后 **/
	DifTeamHited(318, false, true),
	/** 普攻命中敌方后 **/
	DifTeamNormalHit(319, false, true),
	/** 被敌方普攻命中后 **/
	DifTeamNormalHited(320, false, true),
	/** 怒攻命中敌方后 **/
	DifTeamSuperHit(321, false, true),
	/** 被敌方怒攻命中后 **/
	DifTeamSuperHited(322, false, true),

	/** buff移除 **/
	BuffRemove(401),
	/** buff增加 **/
	BuffAdd(402);

	/** 值 **/
	private int value;
	/** 是否是直接触发类型 **/
	private boolean isDirectTrigger;
	/** 是否判定战斗者 **/
	private boolean isCheckFightObj;
	/** 映射表 **/
	private static final Map<Integer, String> mapEnums = new HashMap<>();

	static {
		for (TriggerPoint type : values()) {
			mapEnums.put(type.value(), type.name());
		}
	}

	/**
	 * 内部构造函数
	 * 
	 * @param value
	 * @param isCheckFightObj
	 */
	private TriggerPoint(int value, boolean isDirectTrigger, boolean isCheckFightObj) {
		this.value = value;
		this.isDirectTrigger = isDirectTrigger;
		this.isCheckFightObj = isCheckFightObj;
	}

	/**
	 * 内部构造函数
	 * 
	 * @param value
	 */
	private TriggerPoint(int value, boolean isDirectTrigger) {
		this.value = value;
		this.isDirectTrigger = isDirectTrigger;
		this.isCheckFightObj = false;
	}

	/**
	 * 内部构造函数
	 * 
	 * @param value
	 */
	private TriggerPoint(int value) {
		this.value = value;
		this.isCheckFightObj = false;
		this.isDirectTrigger = false;
	}

	/**
	 * 是否是直接触发类型
	 * 
	 * @return
	 */
	public boolean isDirectTrigger() {
		return this.isDirectTrigger;
	}

	/**
	 * 是否判定战斗者
	 * 
	 * @return
	 */
	public boolean isCheckFightObj() {
		return this.isCheckFightObj;
	}

	/**
	 * 获取对应值
	 * 
	 * @return
	 */
	public int value() {
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
	public static TriggerPoint get(String name) {
		TriggerPoint type = null;
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
	public static TriggerPoint getByValue(int value) {
		TriggerPoint type = null;
		String name = mapEnums.get(value);
		if (name != null) {
			type = valueOf(name);
		}
		return type;
	}
}
