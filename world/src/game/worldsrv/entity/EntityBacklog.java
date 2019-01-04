package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Backlog", tableName = "backlog")
public enum EntityBacklog {
	@Column(type = long.class, comment = "玩家Id", index = true)
	HumanId,
	@Column(type = String.class, length = 64, comment = "待办事项类型")
	Type,
	@Column(type = String.class, length = 2048, comment = "待办事项参数JSON")
	ParamJSON,
}