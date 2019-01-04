package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="ActivityData", tableName="activity_data")
public enum EntityActivityData{
	@Column(type = int.class, comment = "活动id", index = true)
	ActivityId,
	@Column(type = int.class, comment = "数据sn")
	Sn,
	@Column(type = long.class, comment = "数值数据", defaults = "0")
	NumValue,
	@Column(type = String.class, comment = "字符数据")
	StrValue,
}
