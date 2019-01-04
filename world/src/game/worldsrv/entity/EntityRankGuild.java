package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "RankGuild", tableName = "rank_guild")
public enum EntityRankGuild {
	@Column(type = int.class, comment = "排名")
	Rank,
	@Column(type = long.class, comment = "最近一次更新排行值的时间")
	RankTime,
	// 角色特殊字段
	@Column(type = long.class, comment = "会长角色Id")
	HumanId,
	@Column(type = String.class, length = 32, comment = "角色名字")
	Name,
	@Column(type = int.class, comment = "角色等级")
	Level, 
	@Column(type = int.class, comment = "角色VIP等级")
	VipLevel,
	@Column(type = int.class, comment = "角色战斗力")
	Combat,
	@Column(type = int.class, comment = "角色模型sn")
	ModelSn,
	@Column(type = int.class, comment = "角色带的武器sn")
	EquipWeaponSn,
	@Column(type = int.class, comment = "角色带的衣服sn")
	EquipClothesSn,
	@Column(type = int.class, comment = "角色带的时装sn")
	FashionClothesSn,
	@Column(type = boolean.class, comment = "角色是否显示时装")
	IsFashionShow,
	// 公会特殊字段
	@Column(type = long.class, comment = "公会Id", index = true)
	GuildId,
	@Column(type = String.class, length = 16, comment = "公会名字")
	GuildName,
	@Column(type = int.class, comment = "公会等级",defaults="1")
	GuildLevel,
	@Column(type = long.class, comment = "会长id")
	GuildLeaderId, 
	@Column(type = int.class, comment = "工会经验",defaults="0")
	GuildExp,
	@Column(type = String.class, length = 32, comment = "会长名字")
	GuildLeaderName, 
	@Column(type = int.class, comment = "工会现有人数")
	GuildMember, 
	@Column(type = int.class, comment = "工会图标")
	GuildIcon,
	;
}
