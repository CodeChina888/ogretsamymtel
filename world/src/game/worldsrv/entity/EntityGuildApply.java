package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
/**
 * 工会申请信息
 * @author songy
 *
 */
@Entity(entityName = "GuildApply", tableName = "guild_apply")
public enum EntityGuildApply {

	@Column(type = long.class, comment = "会员ID[HumanId]", defaults = "1")
	GuildLevel,
	@Column(type = long.class, comment = "公会id", defaults = "1")
	GuildId,
	@Column(type = String.class, comment = "会员名字", defaults = "")
	HumanName,
	@Column(type = int.class, comment = "当前等级", defaults = "1")
	GuilLv,
	@Column(type = int.class, comment = "战斗力", defaults = "1")
	GuilCombat,
	@Column(type = long.class, comment = "申请时间", defaults = "1")
	ApplyTime,
	@Column(type = int.class, comment = "人物sn", defaults = "1")
	HumanSn,
	@Column(type = int.class, comment = "资质", defaults = "1")
	AptitudeKey,
	@Column(type = long.class, comment = "申请人id", defaults = "1")
	HumanId,
}