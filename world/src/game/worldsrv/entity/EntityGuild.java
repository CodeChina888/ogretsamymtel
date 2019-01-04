package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Guild", tableName = "guild")
public enum EntityGuild {

	@Column(type = int.class, comment = "公会等级", defaults = "1")
	GuildLevel,
	@Column(type = int.class, comment = "公会图标", defaults = "1")
	GuildIcon,
	@Column(type = String.class, length = 32, comment = "公会名字")
	GuildName,
	@Column(type = int.class, comment = "QQ群", defaults = "0")
	QQ,
	@Column(type = int.class, comment = "入会最低等级", defaults = "0")
	InitiationMinLevel,
	@Column(type = int.class, comment = "公会活跃度", defaults = "0")
	GuildLiveness,
	@Column(type = int.class, comment = "公会经验", defaults = "0")
	GuildExp,
	@Column(type = int.class, comment = "公会进度", defaults = "0")
	GuildPlan,
	@Column(type = int.class, comment = "公会祭祀人数", defaults = "0")
	GuildImmoNum,
	@Column(type = long.class, comment = "公会总贡献值", defaults = "0")
	GuildTotalContribute,
	@Column(type = long.class, comment = "会长id", defaults = "0")
	GuildLeaderId,
	@Column(type = String.class, length = 32, comment = "会长名字")
	GuildLeaderName, 
	@Column(type = String.class, length = 128, comment = "有官职的会员{id, post}")
	GuildPostMember, 
	@Column(type = int.class, comment = "公会拥有会员总人数", defaults = "1")
	GuildOwnNum, 
	@Column(type = int.class, comment = "公会状态 1 可加入，2 需申请", defaults = "1")
	GuildStatus, 
	@Column(type = String.class, length = 128, comment = "公会宣告")
	GuildDeclare,
	@Column(type = String.class, length = 128, comment = "公会内部宣告")
	GuildNotice,
	// 公会成员信息,格式={ id:1,名字:2,等级:3,职业:4,帮会贡献:5,离线时间:6,战力:7,离线状态:8,职位,9}
	@Column(type = String.class, length = 5120, comment = "公会成员信息{id:?,name:?,lv:?,...}")
	GuildHuman,
	// 申请入会成员信息， 格式={ id:1,名字:2,等级:3,职业:4,战力:5,申请时间:6}
	@Column(type = String.class, length = 5120, comment = "申请入会成员信息{id:1,name:2,lv:3,pr:4}")
	GuildApplyHuman,
	@Column(type = int.class, comment = "申请表条数", defaults = "0")
	GuildApplyNum,
	//记录凌晨4点更新时间 来判断今天是否执行过
	@Column(type = long.class, comment = "记录凌晨4点更新时间", defaults = "0")
	GuildUpdateTime,
	@Column(type = int.class, comment = "副本已打到的最大章节", defaults = "0")
	GuildChapterMax,
	@Column(type = int.class, comment = "团长使用全军出击次数", defaults = "0")
	GuildEncourageTimes,
	@Column(type = int.class, comment = "团长已经重置副本次数", defaults = "0")
	GuildChapterRestTimes,
	@Column(type = boolean.class, comment = "副本是否自动重置", defaults = "false")
	GuildChapterAutoReset,
	@Column(type = long.class, comment = "军团战斗力", defaults = "0")
	GuildCombat,
    @Column(type = String.class, length = 1024, comment = "公会副本", defaults = "")
    GuildInst,
    @Column(type = String.class, length = 1024*1024, comment = "公会副本领奖情况", defaults = "")
    GuildReward,
	;
}