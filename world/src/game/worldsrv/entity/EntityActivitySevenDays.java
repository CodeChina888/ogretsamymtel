package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="ActivitySevenDays", tableName="activity_sevenDays")
public enum EntityActivitySevenDays {
	@Column(type=long.class, comment="所属的ID")
	HumanID,
	@Column(type = String.class, length = 32, comment = "人物名字")
	HumanName, 
	@Column(type = int.class, comment = "活动类型")
	Type, 
	@Column(type = String.class, length = 1024, comment = "活动ID")
	ActId,
	@Column(type = String.class, length = 1024, comment = "活动状态")
	ActStatus,
	@Column(type = String.class, length = 1024, comment = "活动进度")
	ActProgress,
	@Column(type = int.class, comment = "活动进度")
	ActIng,
	;
}
