package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="MergeVersion", tableName="merge_version")
public enum EntityMergeVersion {
	@Column(type=String.class,comment="版本日期")
	version,
	@Column(type=String.class,comment="当前已经执行同步版本日期")
	updatedVersion,
	;
}
