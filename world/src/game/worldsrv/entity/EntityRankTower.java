package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "RankTower", tableName = "rank_tower")
public enum EntityRankTower {
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
	@Column(type = int.class, comment = "头像")
	Icon,
	// 爬塔特殊字段
	@Column(type = int.class, comment = "爬塔的最大层数")
	MaxFloor,
	@Column(type = int.class, comment = "爬塔耗时(秒)")
	CostTime,
	@Column(type = int.class, comment = "难度")
	DIFFICULTY, 
	@Column(type = int.class, comment = "新：积分，旧：综合评分(排序依据,层数*10,000,000+难度*1000,000+耗时秒数)")
	Grade,
	;
}
