package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "GodsWarHuman", tableName = "godswar_human")
public enum EntityGodsWarHuman {
	@Column(type = long.class, comment = "角色的id")
	HumanId,
	@Column(type = long.class, comment = "记录列表时间")
	RecordTime,
	@Column(type = boolean.class, comment = "是否机器人")
	IsRobot,
	@Column(type = String.class, comment = "姓名")
	Name,
	@Column(type = int.class, comment = "职业")
	Profession, 
	@Column(type = int.class, comment = "性别")
	Sex, 
	@Column(type = int.class, comment = "等级")
	Level,
	
	// 战斗力及排名等
	@Column(type = int.class, comment = "战斗力")
	Combat,
	@Column(type = int.class, comment = "排名")
	Rank,
	@Column(type = int.class, comment = "历史最高排名")
	RankTop, 
	@Column(type = int.class, comment = "每日最高排名")
	RankDaily, 
	@Column(type = long.class, comment = "每日最高排名奖励发放时间")
	TimeRankAwardDaily, 
	@Column(type = long.class, comment = "玩家同步时间")
	TimeSync,
	
	// 其它信息
	@Column(type = int.class, comment = "模型sn")
	ModelSn,
	@Column(type = int.class, comment = "称号SN")
	TitleSN,
	@Column(type = boolean.class, comment = "是否显示称号")
	TitleShow,
	/* 武将 */ 
	// 第1只武将的数据 
	@Column(type = long.class, comment = "上阵武将id1(真实玩家才有值)")
	GenId1, 
	@Column(type = int.class, comment = "武将1sn")
	GenSn1, 
	@Column(type = int.class, comment = "武将1等级")
	GenLv1, 
	@Column(type = int.class, comment = "武将1星级")
	GenStar1,
	@Column(type = int.class, comment = "武将1阶级")
	GenAdv1,
	// 武将所有技能sn
	@Column(type = String.class, length = 128, comment = "武将1所有技能sn", defaults = "")
	GenSkillAllSn1,
	// 武将属性
	@Column(type = String.class, length = 2048, comment = "武将1属性JSON", defaults = "")
	GenPropJSON1,

	// 第2只武将的数据
	@Column(type = long.class, comment = "上阵武将id2(真实玩家才有值)")
	GenId2,
	@Column(type = int.class, comment = "武将2sn")
	GenSn2, 
	@Column(type = int.class, comment = "武将2等级")
	GenLv2, 
	@Column(type = int.class, comment = "武将2星级")
	GenStar2,
	@Column(type = int.class, comment = "武将2阶级")
	GenAdv2,
	// 武将所有技能sn
	@Column(type = String.class, length = 128, comment = "武将2所有技能sn", defaults = "")
	GenSkillAllSn2,
	// 武将属性
	@Column(type = String.class, length = 2048, comment = "武将2属性JSON", defaults = "")
	GenPropJSON2,


	// 第3只武将的数据
	@Column(type = long.class, comment = "上阵武将id3(真实玩家才有值)")
	GenId3,
	@Column(type = int.class, comment = "武将3sn")
	GenSn3, 
	@Column(type = int.class, comment = "武将3等级")
	GenLv3, 
	@Column(type = int.class, comment = "武将3星级")
	GenStar3,
	@Column(type = int.class, comment = "武将3阶级")
	GenAdv3,
	// 武将所有技能sn
	@Column(type = String.class, length = 128, comment = "武将3所有技能sn", defaults = "")
	GenSkillAllSn3,
	// 武将属性
	@Column(type = String.class, length = 2048, comment = "武将3属性JSON", defaults = "")
	GenPropJSON3,
	@Column(type = long.class, comment = "从competehuman表拷贝过来的时间")
	TimeRecord, 
	
	;
}
