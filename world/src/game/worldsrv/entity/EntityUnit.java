package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Unit", isSuper = true)
public enum EntityUnit {
	@Column(type = String.class, length = 32, comment = "名称", index = true)
	Name,
	@Column(type = int.class, comment = "职业")
	Profession,
	@Column(type = int.class, comment = "性别：1男，2女")
	Sex,
	@Column(type = int.class, comment = "等级", defaults = "1")
	Level,
	@Column(type = int.class, comment = "战斗力")
	Combat, 
	
	@Column(type = int.class, comment = "配表sn即PartnerProperty.sn")
	Sn, 
	@Column(type = int.class, comment = "时装对应模型sn（时装变化时修改，不会受变身卡影响）")
	DefaultModelSn,
	@Column(type = int.class, comment = "模型sn")
	ModelSn, 
	@Column(type = int.class, comment = "技能组sn")
	SkillGroupSn,
	@Column(type = String.class, length = 512, comment = "所有技能sn", defaults = "")
	SkillAllSn,
	
	@Column(type = String.class, length = 256, comment = "上阵技能sn,lv,power,value|sn,lv,power,value|...", defaults = "")
	InstallSkillJSON,
	@Column(type = String.class, length = 256, comment = "被动技能snList")
	PassiveSkill,
	@Column(type = String.class, length = 256, comment = "符文信息（大于0纹石id，等于0没装备）", defaults = "")
	RuneInfo,
	
	@Column(type = float.class, comment = "移动速度", defaults = "1")
	Speed, 
//	@Column(type = float.class, comment = "攻击速度", defaults = "1")
//	AtkSpeed,
	
	@Column(type = int.class, comment = "资质（初始值为PartnerProperty.aptitude）")
	Aptitude,
	
	// 通用属性
	@Column(type = int.class, comment = "当前生命")
	HpCur, 
	@Column(type = int.class, comment = "最大生命")
	HpMax,
	@Column(type = int.class, comment = "最大生命百分比")
	HpMaxPct,
	@Column(type = int.class, comment = "最大生命附加")
	HpMaxEx,
	@Column(type = int.class, comment = "当前怒气")
	RageCur,
	@Column(type = int.class, comment = "最大怒气")
	RageMax,

	// 详细属性
	@Column(type = int.class, comment = "攻击力")
	Atk, 
	@Column(type = int.class, comment = "攻击百分比")
	AtkPct,	
	@Column(type = int.class, comment = "攻击力附加")
	AtkEx,
	@Column(type = int.class, comment = "物理攻击")
	AtkPhy, 
	@Column(type = int.class, comment = "物理攻击百分比")
	AtkPhyPct,
	@Column(type = int.class, comment = "物理攻击附加")
	AtkPhyEx,
	@Column(type = int.class, comment = "法术攻击")
	AtkMag, 
	@Column(type = int.class, comment = "法术攻击百分比")
	AtkMagPct,
	@Column(type = int.class, comment = "法术攻击附加")
	AtkMagEx,
	@Column(type = int.class, comment = "防御")
	Def,
	@Column(type = int.class, comment = "防御百分比")
	DefPct,	
	@Column(type = int.class, comment = "防御附加")
	DefEx,
	@Column(type = int.class, comment = "物理防御")
	DefPhy, 
	@Column(type = int.class, comment = "物理防御百分比")
	DefPhyPct,
	@Column(type = int.class, comment = "物理防御附加")
	DefPhyEx,
	@Column(type = int.class, comment = "法术防御")
	DefMag, 
	@Column(type = int.class, comment = "法术防御百分比")
	DefMagPct,
	@Column(type = int.class, comment = "法术防御附加")
	DefMagEx,
	@Column(type = int.class, comment = "命中")
	Hit, 
	@Column(type = int.class, comment = "闪避")
	Dodge,
	@Column(type = int.class, comment = "暴击")
	Crit, 
	@Column(type = int.class, comment = "坚韧（抵抗暴击）")
	AntiCrit,
	@Column(type = int.class, comment = "必杀（暴击加成）")
	CritAdd,
	@Column(type = int.class, comment = "守护（抵抗必杀）")
	AntiCritAdd,
	@Column(type = int.class, comment = "穿透")
	Pene, 
	@Column(type = int.class, comment = "物理穿透")
	PenePhy, 
	@Column(type = int.class, comment = "法术穿透")
	PeneMag, 
	@Column(type = int.class, comment = "格挡")
	Block, 
	@Column(type = int.class, comment = "破击")
	AntiBlock,
	@Column(type = int.class, comment = "吸血")
	BloodSuck,
	@Column(type = int.class, comment = "被吸血")
	BloodSucked,
	@Column(type = int.class, comment = "控制")
	Control,
	@Column(type = int.class, comment = "抵抗控制")
	AntiControl,
	
	@Column(type = int.class, comment = "最终增伤率")
	DamAdd,
	@Column(type = int.class, comment = "最终增伤附加")
	DamAddEx,
	@Column(type = int.class, comment = "最终减伤率")
	DamRed,
	@Column(type = int.class, comment = "最终减伤附加")
	DamRedEx,
	@Column(type = int.class, comment = "最终物理增伤率")
	DamPhyAdd,
	@Column(type = int.class, comment = "最终物理增伤附加")
	DamPhyAddEx,
	@Column(type = int.class, comment = "最终物理减伤率")
	DamPhyRed,
	@Column(type = int.class, comment = "最终物理减伤附加")
	DamPhyRedEx,
	@Column(type = int.class, comment = "最终法术增伤率")
	DamMagAdd,
	@Column(type = int.class, comment = "最终法术增伤附加")
	DamMagAddEx,
	@Column(type = int.class, comment = "最终法术减伤率")
	DamMagRed,
	@Column(type = int.class, comment = "最终法术减伤附加")
	DamMagRedEx,
	@Column(type = int.class, comment = "普攻增伤率")
	DamComAdd,
	@Column(type = int.class, comment = "普攻减伤率")
	DamComRed,
	@Column(type = int.class, comment = "怒攻增伤率")
	DamRageAdd,
	@Column(type = int.class, comment = "怒攻减伤率")
	DamRageRed,
	@Column(type = int.class, comment = "治疗率")
	CureAdd,	
	@Column(type = int.class, comment = "治疗量")
	CureAddEx,
	@Column(type = int.class, comment = "被治疗率")
	HealAdd,	
	@Column(type = int.class, comment = "被治疗量")
	HealAddEx,
	@Column(type = int.class, comment = "护盾")
	Shield,
	@Column(type = int.class, comment = "物理护盾")
	ShieldPhy,
	@Column(type = int.class, comment = "法术护盾")
	ShieldMag,
	@Column(type = int.class, comment = "反伤率")
	DamBack,	
	@Column(type = int.class, comment = "中毒伤害率")
	PoisonAdd,
	@Column(type = int.class, comment = "中毒伤害附加")
	PoisonAddEx,
	@Column(type = int.class, comment = "中毒伤害减免率")
	AntiPoisonAdd,
	@Column(type = int.class, comment = "中毒伤害减免附加")
	AntiPoisonAddEx,
	@Column(type = int.class, comment = "灼烧伤害率")
	BurnAdd,	
	@Column(type = int.class, comment = "灼烧伤害附加")
	BurnAddEx,
	@Column(type = int.class, comment = "灼烧伤害减免率")
	AntiBurnAdd,
	@Column(type = int.class, comment = "灼烧伤害减免附加")
	AntiBurnAddEx,
	@Column(type = int.class, comment = "流血伤害率")
	BloodAdd,
	@Column(type = int.class, comment = "流血伤害附加")
	BloodAddEx,
	@Column(type = int.class, comment = "流血伤害减免率")
	AntiBloodAdd,
	@Column(type = int.class, comment = "流血伤害减免附加")
	AntiBloodAddEx,
	@Column(type = int.class, comment = "定身")
	Stun,
	@Column(type = int.class, comment = "混乱")
	Chaos,	
	@Column(type = int.class, comment = "禁疗")
	BanHeal,	
	@Column(type = int.class, comment = "麻痹")
	Paralytic,
	@Column(type = int.class, comment = "封怒")
	BanRage,	
	@Column(type = int.class, comment = "封技")
	Silent,
	@Column(type = int.class, comment = "不死")
	Immortal,
	@Column(type = int.class, comment = "物理免疫")
	ImmunePhy,
	@Column(type = int.class, comment = "法术免疫")
	ImmuneMag,
	@Column(type = int.class, comment = "无敌")
	Invincible,
	@Column(type = int.class, comment = "必中（无视无敌,闪避,丢失）")
	CertainlyHit,
	@Column(type = int.class, comment = "必控（无视无敌,控免,魔免）")
	CertainlyControl,
	@Column(type = int.class, comment = "虚弱（必定被控制）")
	Weak,

}