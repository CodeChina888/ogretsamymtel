package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "ActServerCompetition", tableName = "ActServerCompetition")
public enum EntityActServerCompetition {
	@Column(type = int.class, comment = "排行类型")
	Type,
	@Column(type = String.class, length = 2048, comment = "参数类型")
	ParamJSON,
}