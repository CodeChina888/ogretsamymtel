package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "DropInfo", tableName = "dropInfo")
public enum EntityDropInfo {
	@Column(type = long.class, comment = "主角ID")
	HumanId, 
	@Column(type = String.class, length = 4096, comment = "计数类型 {1:0,2:0,3:0......}")
	CountType,
}