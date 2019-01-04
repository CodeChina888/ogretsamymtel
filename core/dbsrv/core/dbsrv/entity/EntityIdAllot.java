package core.dbsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "IdAllot", tableName = "core_id_allot")
public enum EntityIdAllot {
	@Column(type = long.class, comment = "当前已分配的ID最大值", index = true)
	MaxID, 
	@Column(type = long.class, comment = "当前已分配的玩家标识最大值", index = true)
	HumanDigit, 
	;
}