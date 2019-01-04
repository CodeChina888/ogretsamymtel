package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
import game.worldsrv.entity.EntityUnit;
/**
 * 玩家简要数据,全局离线服务的基础数据,只提供给前端展示使用，不可以做其他任何操作
 * @author songy
 *
 */
@Entity(entityName = "HumanSimple", tableName = "human_simple")
public enum EntityHumanSimple {
	@Column(type = String.class,length=4096, comment = "  玩家最后一次登陆时间")
	Content,
//	@Column(type = long.class, comment = "  玩家最后一次登陆时间")
//	TimeLogin,
//	@Column(type = String.class,length = 32, comment = " 姓名")
//	HumanName,
//	@Column(type = int.class,comment = "  职业")
//	Profession,
//	@Column(type = int.class, comment = "性别")
//	Sex,
//	@Column(type = int.class, comment = "等级")
//	Level,
//	@Column(type = int.class, comment = "VIP等级")
//	VipLv,
//	@Column(type = int.class, comment = "战斗力")
//	Combat,
//	@Column(type = int.class, comment = "头像sn")
//	HeadSn,
//	@Column(type = int.class, comment = "玩家模型")
//	ModelSn,
//	@Column(type = int.class, comment = "玩家称号")
//	TitleSn,
//	@Column(type = long.class, comment = "guildId")
//	GuildId,
//	@Column(type = String.class,length = 32, comment = "公会名字")
//	GuildName,
//	@Column(type = int.class, comment = "角色Sn")
//	Sn,
//	@Column(type = int.class, comment = "竞技场排名")
//	competeRank,
//	@Column(type = int.class, comment = "副本总星数")
//	instStar,
//	
//	@Column(type = String.class,length = 1024, comment = "伙伴信息")
//	PartnerInfo,
//	@Column(type = String.class,length = 1024, comment = "上阵技能信息")
//	SkillInfo,
//	@Column(type = String.class,length = 1024, comment = "神兽信息")
//	Godbeast,
}