package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="AchieveTitle", tableName="achieve_title")
public enum EntityAchieveTitle {
	@Column(type = long.class, comment = "玩家id", index=true)
	HumanId,
	@Column(type=int.class, comment="称号成就类型")
	Type,
	@Column(type=String.class, comment="称号sn（AchieveTitle.sn）")
	TitleSn,
	@Column(type=String.class, comment="当前进度")
	Progress,
	@Column(type=String.class, comment="状态 0进行中 1已获得")
	Status,
	@Column(type=String.class, length=1024, comment="获得时间，-1未获得", defaults = "")
	GainTime,
	@Column(type=String.class, length=1024, comment="到期时间，-1未获得，0为永久", defaults = "")
	LimitTime,
}
