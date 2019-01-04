package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "RankSumCombat", tableName = "rank_sum_combat")
public enum EntityRankSumCombat {
	@Column(type = int.class, comment = "排名")
	Rank,
	@Column(type = long.class, comment = "最近一次更新排行值的时间")
	RankTime,
	// 角色特殊字段
	@Column(type = long.class, comment = "角色Id", index = true)
	HumanId,
	@Column(type = String.class, length = 32, comment = "角色名字")
	Name,
	@Column(type = int.class, comment = "总战斗力")
	SumCombat,
	@Column(type = int.class, comment = "头像sn")
	headSn,
	@Column(type = int.class, comment = "等级")
	lv,
	@Column(type = int.class, comment = "角色战斗力")
	Combat,
	@Column(type = int.class, comment = "角色模型sn")
	ModelSn,
	;
}
