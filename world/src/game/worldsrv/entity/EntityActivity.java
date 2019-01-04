package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="Activity", tableName="activity")
public enum EntityActivity{
	@Column(type = int.class, comment = "活动id", index = true)
	ActivityId,
	@Column(type = long.class, comment = "开始时间")
	BeginTime,
	@Column(type = long.class, comment = "状态", defaults = "0")
	State,
}