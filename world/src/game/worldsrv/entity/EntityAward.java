package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Award", tableName = "award")
public enum EntityAward {
	@Column(type = String.class, length = 128, comment = "等级竞技奖励{奖励sn:1,排行rk:1,领取dr:2,...}")
	ActLevelRace,
	// 领取 0未领取 1已经领取 2不可领取
	@Column(type = String.class, length = 128, comment = "战力竞技奖励{奖励sn:1,排行rk:1,领取dr:2,...}")
	ActCombatRace, ;
}
