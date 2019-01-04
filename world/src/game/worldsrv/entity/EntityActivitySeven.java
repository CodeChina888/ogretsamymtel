package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="ActivitySeven", tableName="activity_seven")
public enum EntityActivitySeven {
	@Column(type=long.class, comment="所属的ID")
	humanID,
	@Column(type = String.class, length = 32, comment = "人物名字")
	humanName, 
	@Column(type = int.class, comment = "活动类型")
	type, 
	@Column(type = String.class, length = 1024, comment = "活动ID")
	actId,
	@Column(type = String.class, length = 1024, comment = "活动状态")
	actStatus,
	@Column(type = String.class, length = 1024, comment = "活动进度")
	actProgress,
	@Column(type = int.class, comment = "活动进度")
	actIng,
	;
}
