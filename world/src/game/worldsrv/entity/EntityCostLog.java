package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 稀有物品日志
 */
@Entity(entityName = "CostLog", tableName="cost_log")
public enum EntityCostLog {
	@Column(type = long.class, comment = "玩家ID", index = true)
	HumanId,
	@Column(type=String.class, comment="名字")
	Name,
	@Column(type = int.class, comment = "消费类型")
	Type,
	@Column(type = long.class, comment = "消费前")
	OldMoney,
	@Column(type = long.class, comment = "数量")
	Num,
	@Column(type = long.class, comment = "消费后")
	NewMoney,
	@Column(type=String.class, comment="操作")
	Operate,
	@Column(type=String.class, comment="时间")
	Time,
}
