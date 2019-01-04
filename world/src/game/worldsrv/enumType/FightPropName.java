package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用属性名枚举 此类型与游戏相关，根据需要修改，与属性配置表对应
 */
public enum FightPropName {
	HpCur("HpCur"), // 当前生命
	HpMax("HpMax"), // 最大生命
	HpMaxPct("HpMaxPct", "HpMax"), // 最大生命万分比
	HpMaxEx("HpMaxEx"), // 最大生命附加
	RageCur("RageCur"), // 当前怒气
	RageMax("RageMax"), // 最大怒气
	Atk("Atk"), // 攻击力
	AtkPct("AtkPct", "Atk"), // 攻击万分比
	AtkEx("AtkEx"), // 攻击力附加
	AtkPhy("AtkPhy"), // 物理攻击
	AtkPhyPct("AtkPhyPct", "AtkPhy"), // 物理攻击万分比
	AtkPhyEx("AtkPhyEx"), // 物理攻击附加
	AtkMag("AtkMag"), // 法术攻击
	AtkMagPct("AtkMagPct", "AtkMag"), // 法术攻击万分比
	AtkMagEx("AtkMagEx"), // 法术攻击附加
	Def("Def"), // 防御
	DefPct("DefPct", "Def"), // 防御万分比
	DefEx("DefEx"), // 防御附加
	DefPhy("DefPhy"), // 物理防御
	DefPhyPct("DefPhyPct", "DefPhy"), // 物理防御万分比
	DefPhyEx("DefPhyEx"), // 物理防御附加
	DefMag("DefMag"), // 法术防御
	DefMagPct("DefMagPct", "DefMag"), // 法术防御万分比
	DefMagEx("DefMagEx"), // 法术防御附加
	Hit("Hit"), // 命中
	Dodge("Dodge"), // 闪避
	Crit("Crit"), // 暴击
	AntiCrit("AntiCrit"), // 坚韧
	CritAdd("CritAdd"), // 必杀
	AntiCritAdd("AntiCritAdd"), // 守护
	Pene("Pene"), // 防御穿透
	PenePhy("PenePhy"), // 物理穿透
	PeneMag("PeneMag"), // 法术穿透
	Block("Block"), // 格挡
	AntiBlock("AntiBlock"), // 破击
	BloodSuck("BloodSuck"), // 吸血
	BloodSucked("BloodSucked"), // 被吸血
	Control("Control"), // 控制几率
	AntiControl("AntiControl"), // 抵抗控制几率
	
	DamAdd("DamAdd"), // 最终增伤率
	DamAddEx("DamAddEx"), // 最终增伤附加
	DamRed("DamRed"), // 最终减伤率
	DamRedEx("DamRedEx"), // 最终减伤附加
	DamPhyAdd("DamPhyAdd"), // 最终物理增伤率
	DamPhyAddEx("DamPhyAddEx"), // 最终物理增伤附加
	DamPhyRed("DamPhyRed"), // 最终物理减伤率
	DamPhyRedEx("DamPhyRedEx"), // 最终物理减伤附加
	DamMagAdd("DamMagAdd"), // 最终法术增伤率
	DamMagAddEx("DamMagAddEx"), // 最终法术增伤附加
	DamMagRed("DamMagRed"), // 最终法术减伤率
	DamMagRedEx("DamMagRedEx"), // 最终法术减伤附加
	DamComAdd("DamComAdd"), // 普攻增伤率
	DamComRed("DamComRed"), // 普攻减伤率
	DamRageAdd("DamRageAdd"), // 怒攻增伤率
	DamRageRed("DamRageRed"), // 怒攻减伤率
	CureAdd("CureAdd"), // 治疗率
	CureAddEx("CureAddEx"), // 治疗量
	HealAdd("HealAdd"), // 被治疗率
	HealAddEx("HealAddEx"), // 被治疗量
	Shield("Shield"), // 护盾
	ShieldPhy("ShieldPhy"), // 物理护盾
	ShieldMag("ShieldMag"), // 法术护盾
	DamBack("DamBack"), // 反伤率
	PoisonAdd("PoisonAdd"), // 中毒伤害率
	PoisonAddEx("PoisonAddEx"), // 中毒伤害附加
	AntiPoisonAdd("AntiPoisonAdd"), // 中毒伤害减免率
	AntiPoisonAddEx("AntiPoisonAddEx"), // 中毒伤害减免附加
	BurnAdd("BurnAdd"), // 灼烧伤害率
	BurnAddEx("BurnAddEx"), // 灼烧伤害附加
	AntiBurnAdd("AntiBurnAdd"), // 灼烧伤害减免率
	AntiBurnAddEx("AntiBurnAddEx"), // 灼烧伤害减免附加
	BloodAdd("BloodAdd"), // 流血伤害率
	BloodAddEx("BloodAddEx"), // 流血伤害附加
	AntiBloodAdd("AntiBloodAdd"), // 流血伤害减免率
	AntiBloodAddEx("AntiBloodAddEx"), // 流血伤害减免附加
	Stun("Stun"), // 定身
	Chaos("Chaos"), // 混乱
	Paralytic("Paralytic"), // 麻痹
	BanHeal("BanHeal"), // 禁疗
	BanRage("BanRage"), // 封怒
	Silent("Silent"), // 封技
	Immortal("Immortal"), // 不死
	ImmunePhy("ImmunePhy"), // 物理免疫
	ImmuneMag("ImmuneMag"), // 法术免疫
	Invincible("Invincible"), // 无敌
	CertainlyHit("CertainlyHit"), // 必中
	CertainlyControl("CertainlyControl"), // 必控
	Weak("Weak"), // 虚弱（必定被控制）
	// 战中使用属性
	BorrowedTime("BorrowedTime", true), // 回光返照（伤害转变成治疗）
	DamCeof("DamCeof", true), // 最终增伤倍数
	DamRedCeof("DamRedCeof", true), // 最终减伤倍数
	ImmuneBuffGroup("ImmuneBuffGroup",true),	//buff免疫组
	
	;
	private String value;
	private String reflectName;
	private boolean isFightProp = false; // 战斗中使用的

	// 存放所有枚举信息，<枚举值,枚举名>
	private static final Map<String, String> mapEnums = new HashMap<>();
	private static final Map<String, String> mapReflectEnums = new HashMap<>();

	static {
		for (FightPropName type : values()) {
			mapEnums.put(type.value(), type.name());
			mapReflectEnums.put(type.value, type.reflectName);
		}
	}

	private FightPropName(String value) {
		this.value = value;
		this.reflectName = value;
	}

	private FightPropName(String value, boolean isFightProp) {
		this.value = value;
		this.reflectName = value;
		this.isFightProp = isFightProp;
	}

	private FightPropName(String value, String name) {
		this.value = value;
		this.reflectName = name;
	}

	public String value() {
		return value;
	}

	/**
	 * 是否存在指定的枚举值
	 * @return
	 */
	public static boolean containsKey(String value) {
		return mapEnums.containsKey(value);
	}

	/**
	 * 是否存在指定的枚举名
	 * @return
	 */
	public static boolean containsValue(String name) {
		return mapEnums.containsValue(name);
	}

	/**
	 * 是否存在指定的枚举值
	 * 
	 * @return
	 */
	public static boolean containsReflectKey(String value) {
		return mapReflectEnums.containsKey(value);
	}

	/**
	 * 是否存在指定的枚举名
	 * 
	 * @return
	 */
	public static boolean containsReflectValue(String name) {
		return mapReflectEnums.containsValue(name);
	}

	/**
	 * 获取指定枚举名的枚举
	 * 
	 * @param name
	 * @return
	 */
	public static FightPropName getReflectValue(String name) {
		FightPropName type = null;
		if (containsReflectValue(name))
			type = valueOf(name);
		return type;
	}

	public String getReflectName() {
		return this.reflectName;
	}

	public boolean isFightProp() {
		return this.isFightProp;
	}

	/**
	 * 获取指定枚举名的枚举
	 * 
	 * @param name
	 * @return
	 */
	public static FightPropName get(String name) {
		FightPropName type = null;
		if (containsValue(name)) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 * 
	 * @param value
	 * @return
	 */
	public static FightPropName getByValue(String value) {
		FightPropName type = null;
		String name = mapEnums.get(value);
		if (name != null) {
			type = valueOf(name);
		}
		return type;
	}

	/**
	 * 获取指定枚举值的枚举
	 * 
	 * @return
	 */
	public static FightPropName getByReflectName(String reflectName) {
		FightPropName type = null;
		if (containsReflectKey(reflectName)) {
			String value = mapReflectEnums.get(reflectName);
			if (containsValue(value))
				type = valueOf(value);
		}
		return type;
	}

	/**
	 * 获取枚举名列表
	 */
	public static List<String> toList() {
		List<String> result = new ArrayList<>();
		for (FightPropName k : FightPropName.values()) {
			result.add(k.name());
		}
		return result;
	}
}
