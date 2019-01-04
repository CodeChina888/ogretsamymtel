package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "UnitPropPlus", tableName = "unit_propplus")
public enum EntityUnitPropPlus {
	@Column(type = String.class, length = 32, comment = "名字", defaults = "{}")
	Name,
	@Column(type = int.class, comment = "类型（0：玩家，9：武将）详见：EWorldObjectType")
	Type, 
	@Column(type = String.class, length = 512, comment = "1基础能力", defaults = "{}")
	Base, 
	@Column(type = String.class, length = 512, comment = "2等级", defaults = "{}")
	Level, 
	@Column(type = String.class, length = 512, comment = "3身上所有装备", defaults = "{}")
	ItemEquip,
	@Column(type = String.class, length = 512, comment = "4装备精炼加成", defaults = "{}")
	EquipRefine,
	@Column(type = String.class, length = 512, comment = "5身上所有命格", defaults = "{}")
	Rune,
	@Column(type = String.class, length = 512, comment = "6技能加成", defaults = "{}")
	Skill,
	@Column(type = String.class, length = 512, comment = "7技能进阶加成", defaults = "{}")
	SkillStage,
	@Column(type = String.class, length = 512, comment = "8技能培养加成", defaults = "{}")
	SkillTrain,
	@Column(type = String.class, length = 512, comment = "9被动技能加成", defaults = "{}")
	PassivitySkill,
	@Column(type = String.class, length = 512, comment = "10爆点技能等级加成", defaults = "{}")
	SkillGodsLv,
	@Column(type = String.class, length = 512, comment = "11爆点技能星级加成", defaults = "{}")
	SkillGodsStar,
	@Column(type = String.class, length = 512, comment = "12称号加成", defaults = "{}")
	Title,
	@Column(type = String.class, length = 512, comment = "13拥有的时装", defaults = "{}")
	Fashion,
	@Column(type = String.class, length = 512, comment = "14变身加成", defaults = "{}")
	FashionHenshin,
	@Column(type = String.class, length = 512, comment = "15进阶加成属性", defaults = "{}")
	Advance,
	@Column(type = String.class, length = 512, comment = "16升星加成属性", defaults = "{}")
	Star,
	@Column(type = String.class, length = 512, comment = "17修炼加成属性", defaults = "{}")
	Practice,
	@Column(type = String.class, length = 512, comment = "18缘分加成属性", defaults = "{}")
	Fate,
	@Column(type = String.class, length = 512, comment = "19护法加成属性", defaults = "{}")
	Servant,
	@Column(type = String.class, length = 512, comment = "20护法加成属性", defaults = "{}")
	PartnerSkil,
	@Column(type = String.class, length = 512, comment = "21法宝基础属性", defaults = "{}")
	CimeliaBase,
	@Column(type = String.class, length = 512, comment = "22法宝升级属性", defaults = "{}")
	CimeliaLv,
	@Column(type = String.class, length = 512, comment = "23法宝进阶属性", defaults = "{}")
	CimeliaAdv,
	@Column(type = String.class, length = 512, comment = "24法宝升星属性", defaults = "{}")
	CimeliaStar,
    @Column(type = String.class, length = 512, comment = "帮派技能属性", defaults = "{}")
    GuildSkill,
}
