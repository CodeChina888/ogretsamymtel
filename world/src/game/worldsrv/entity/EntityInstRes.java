package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 资源本信息记录
 * @author Neak
 */
@Entity(entityName = "InstRes", tableName = "inst_resource")
public enum EntityInstRes {
	@Column(type = String.class, length = 1024, comment = "记录星数{sn:starNum...}", defaults = "")
	StarInfo,
	@Column(type = String.class, length = 2048, comment = "记录资源副本信息{type:{diff:num,...}}", defaults = "{}")
	ResInfo, 
	;
}
