package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "GuildImmoLog", tableName = "guild_immo_log")
public enum EntityGuildImmo {
	@Column(type = long.class, comment = "工会id", defaults = "1")
	GuildId,
	@Column(type = int.class, comment = "1献祭，2挑战，3日常", defaults = "1")
	logTypeKey,
	@Column(type = long.class, comment = "人物id", defaults = "1")
	HumanId,
	@Column(type = int.class, comment = "资质", defaults = "1")
	AptitudeKey,
	@Column(type = int.class, comment = "1进入公会，2退出公会，3任命会长，4任命副会长[// 日常操作日志]", defaults = "1")
	Handle,
	@Column(type = long.class, comment = "献祭时间", defaults = "1")
	Time,
	@Column(type = String.class, comment = "人物名字", defaults = "1")
	HumanName,
	@Column(type = int.class, comment = "日志信息[/ 挑战日志]", defaults = "1")
	Content,
	@Column(type = int.class, comment = "记录献祭消耗类型[/ 公会建设日志（献祭）]", defaults = "1")
	Immotype,
	;
}