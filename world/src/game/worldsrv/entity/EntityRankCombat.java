package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "RankCombat", tableName = "rank_combat")
public enum EntityRankCombat {
	@Column(type = int.class, comment = "排名")
	Rank,
	@Column(type = long.class, comment = "最近一次更新排行值的时间")
	RankTime,
	// 角色特殊字段
	@Column(type = long.class, comment = "角色Id", index = true)
	HumanId,
	@Column(type = String.class, length = 32, comment = "角色名字")
	Name, 
	@Column(type = int.class, comment = "角色等级")
	Level,
	@Column(type = int.class, comment = "角色VIP等级")
	VipLevel,
	@Column(type = int.class, comment = "角色战斗力")
	Combat,
	@Column(type = int.class, comment = "角色模型sn")
	ModelSn,
	@Column(type = int.class, comment = "角色带的武器sn")
	EquipWeaponSn,
	@Column(type = int.class, comment = "角色带的衣服sn")
	EquipClothesSn,
	@Column(type = int.class, comment = "角色带的时装sn")
	FashionClothesSn,
	@Column(type = boolean.class, comment = "角色是否显示时装")
	IsFashionShow,
	@Column(type = String.class, length = 128, comment = "武将最强阵容简要信息{武将sn，星级sn:1,st:1}")
	genLineupBriefInfo, 
	;
}
