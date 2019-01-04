package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="ActivityHumanData", tableName="activity_humandata")
public enum EntityActivityHumanData{
	@Column(type = long.class, comment = "玩家id", index = true)
	HumanId,
	@Column(type = int.class, comment = "活动id", index = true)
	ActivityId,
	@Column(type = int.class, comment = "数据sn")
	Sn,
	@Column(type = int.class, comment = "活动编号,如果活动编号为0,则存储一条数据就够了")
	Aid,
	@Column(type = long.class, comment = "数值数据", defaults = "0")
	NumValue,
	@Column(type = String.class, comment = "字符数据",length=4096)
	StrValue,
}