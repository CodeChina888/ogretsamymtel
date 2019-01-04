package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * @author Neak
 * @see :玩家爬塔数据
 */
@Entity(entityName = "Tower", tableName = "tower")
public enum EntityTower {
	@Column(type = int.class, comment = "赛季积分")
	Score,
	@Column(type = int.class, comment = "赛季积分排名")
	Rank,
	@Column(type = long.class, comment = "赛季结束时间")
	SeasonEndTime,
	@Column(type = int.class, comment = "历史挑战过的最高层数（difficulty * 1000 + layer）")
	HistoryMaxLayer,
	@Column(type = int.class, comment = "昨日挑战过的最高层数（difficulty * 1000 + layer）")
	YestodayMaxLayer,
	@Column(type = int.class, comment = "更新匹配当日爬塔数据时的等级")
	MatchLv,
	@Column(type = int.class, comment = "更新匹配当日爬塔数据时的战斗力")
	MatchCombat,
	@Column(type = long.class, comment = "每日首次打开爬塔的时间戳")
	FirstDailyTime,
	@Column(type = long.class, comment = "每日挑战首层的时间戳")
	FirstFightTime,
	@Column(type = long.class, comment = "最后一次过关的时间戳(通过首次和最后一次时间戳，作为排行榜的时间参数)")
	LastPassTime,
	@Column(type = int.class, comment = "当前停留层数")
	StayLayer,
	@Column(type = int.class, comment = "将要挑战的层数")
	WillFightLayer,
	@Column(type = int.class, comment = "最后挑战层数选择的难度")
	LastSelDifficulty,
	@Column(type = int.class, comment = "当前拥有的生命数")
	HaveLifeNum,
	@Column(type = int.class, comment = "已经购买的生命数")
	BuyLifeNum,
	@Column(type = int.class, comment = "爬塔挑战胜利奖励倍数", defaults = "1")
	Multiple,
	// sn1元宝翻牌的dropId,c1元宝翻牌的当前翻盘情况
	@Column(type = String.class, length = 2048,comment = "(l=layer,sn=dropId,c=cardStates,sn1=dropId1,c1=cardStates1)奖励宝箱{l:1,d:10,c:0,1,0,c1:0,1,0},..", defaults = "{}") 
	RewardBox,
	@Column(type = String.class, length = 512,comment = "已经打过的难度层数列表 1001,1002,1003,...,2001,2002..3001", defaults = "") 
	AlreadyFight,
	
	// 今日匹配到的九层数据 json {"layer":1,"id":"1000","c":"100"}, {"layer":1,"id":"1001","c":"200","cd":"1,1"}, ...}
	@Column(type = String.class, length = 1024, comment = "(l=layer,id:=humanId,c=combat,cd=condition)难度1：[{l:1,id:1000,c:100,cd:1,1},...}]", defaults = "{}")
	DiffcultyLv1,
	@Column(type = String.class, length = 1024, comment = "(l=layer,id:=humanId,c=combat,cd=condition)难度2：[{l:1,id:1000,c:100,cd:1,1},...}]", defaults = "{}")
	DiffcultyLv2,
	@Column(type = String.class, length = 1024, comment = "(l=layer,id:=humanId,c=combat,cd=condition)难度3：[{l:1,id:1000,c:100,cd:1,1},...}]", defaults = "{}")
	DiffcultyLv3,
	;
}
