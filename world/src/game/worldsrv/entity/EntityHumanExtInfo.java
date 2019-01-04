package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;


@Entity(entityName="HumanExtInfo", tableName="human_ext_info")
public enum EntityHumanExtInfo {
	/* 每日签到 */
	@Column(type=int.class, comment="签到次数", defaults = "0")
	SignIndex,
	@Column(type=int.class, comment="签到组", defaults = "1")
	SignGroup,
	@Column(type=int.class, comment="签到总数", defaults = "0")
	SignCount,
	
	/* vip购买次数 */
	@Column(type=String.class, comment="vip各项购买次数  {type：buytimes}", defaults = "[]")
	VipBuyTimes,
	@Column(type=int.class, comment="领奖记录")
	VipAwardId,
	@Column(type=String.class, comment="已购买过vip等级礼包 id,id,id", defaults = "[]")
	VipBuyLvs,
	@Column(type=int.class, comment="是否领取首充2 已领取 1 可领取；0否", defaults = "0")
	FirstChargeRewardState,
	
	@Column(type = String.class, length=1024, comment = "商城商品购买信息", defaults = "[]")
	MallJson,
	
	@Column(type = int.class, comment="人气", defaults="0")
	Popularity,
	@Column(type = String.class, length=1024, comment="点赞玩家id", defaults="[]")
	Likelist,
	
	/* 伙伴相关*/
	@Column(type = String.class, length = 256, comment = "伙伴阵容(id列表) ", defaults = "")
	PartnerLineup,
	@Column(type = int.class, comment = "伙伴站位(0-W型；1-M型)")
	PartnerStance,
	/**
	 * {ServantLock表的sn}
	 */
	@Column(type = int.class, comment = "当前护法解锁个数 ", defaults = "0")
	ServantLock,	

	
	
	
	@Column(type = String.class,length = 1024, comment = "已经<领取>的图鉴id,领取物品的时候用的", defaults ="")
	ActivityReword,
	@Column(type = String.class,length = 1024, comment = "已经<激活>,可领取未领取的图鉴id,领取物品的时候用的", defaults ="")
	AlreadyGetReword,
	@Column(type = String.class,length = 1024, comment = "激活过的伙伴", defaults ="")
	ActivityHand,
	//竞技场
	@Column(type = boolean.class,  comment = "是否第一次初始化竞技场",defaults="true")
	FirstInCompete, 
	@Column(type = long.class,  comment = "上次挑战时间")
	lastCompeteTime,
	@Column(type = int.class,  comment = "每天已购买挑战次数",defaults ="0")
	competeBuyNumAdd,
	@Column(type = long.class,  comment = "上次购买时间",defaults ="0")
	lastcompeteBuyTime,
	@Column(type = int.class,  comment = "今日已挑战次数",defaults ="0")
	challengeNum,
	@Column(type = int.class,  comment = "剩余挑战次数",defaults ="0")
	surplusNum,
	@Column(type = int.class,  comment = "今日胜利次数",defaults ="0")
	todayWinNums,
	
	
	/* 等级礼包 */
	@Column(type = String.class, length = 256,comment = "已领取的等级礼包",defaults = "")
	LevelPackage,
	
	/* 世界BOSS */
	@Column(type = long.class, comment = "上次参与活动时间戳", defaults = "0")
	WDBossJoinTime,
	@Column(type = long.class, comment = "下次挑战时间戳", defaults = "0")
	WDBossNextFightTime,
	@Column(type = long.class, comment = "下次鼓舞时间戳", defaults = "0")
	WDBossNextInspireTime,
	@Column(type = int.class, comment = "本次活动已鼓舞次数", defaults = "0")
	WDBossInspireNum,
	@Column(type = int.class, comment = "本次活动已涅槃重生次数", defaults = "0")
	WDBossRebornNum,
	
	/* 远征副本(九层妖塔) */
	@Column(type = boolean.class, comment = "爬塔今日是否已经挑战过", defaults = "false")
	TowerIsFight,
	@Column(type = int.class, comment = "爬塔耗时（秒）", defaults = "0")
	TowerPassTime,
	@Column(type = int.class, comment = "爬塔最大层数", defaults = "0")
	TowerMaxFloor,
	@Column(type = int.class, comment = "爬塔最后过关难度", defaults = "0")
	TowerSelDiff,
	@Column(type = int.class, comment = "爬塔积分", defaults = "0")
	TowerScore,
	
	/* 符文命格信息 */
	@Column(type = int.class, comment = "符文召唤当前的sn（RuneSummonSn）", defaults = "0")
	RuneSummonSn, 
	@Column(type = String.class, length = 128, comment = "符文占卜计数列表", defaults = "0,0,0,0,0")
	RuneSummonCount, 
	
	/* 好友体力  */
	@Column(type = int.class, comment = "赠送好友体力次数", defaults = "0")
	FriendGiveTimes,
	@Column(type = int.class, comment = "接受好友赠送体力次数", defaults = "0")
	FriendReceiveTimes,
	/* 整点体力领取时间*/
	@Column(type = long.class, comment = "整点体力领取时间")
	ActivityGetTime,
	
	/* 抢夺本相关 */
	@Column(type = long.class, comment = "多人抢夺本最近参与时间")
	LootMapMultipleJoinTime,
	@Column(type = int.class, comment = "多人抢夺本最近参与时间内累计参加次数")
	LootMapMultipleJoinCount,
	@Column(type = int.class, comment = "多人抢夺本今日最高")
	LootMapMultipleTodayScore,
	@Column(type = long.class, comment = "多人抢夺本累计积分")
	LootMapMultipleScore,
	
	/* 公会相关 */
//	@Column(type = long.class, comment = "所属公会")
//	GuildBelong,

	
//	@Column(type = boolean.class, comment = "是否是会长，true是会长，false不是会长")
//	GuildCDR, 
//	@Column(type = int.class, comment = "当前公会等级")
//	GuildLevel,
//	@Column(type = int.class, comment = "对当前公会贡献值")
//	GuildContribute,
//	@Column(type = String.class, length = 512, comment = "公会所有属性加成{属性SN:属性level,...}", defaults = "")
//	GuildPropJSON, 
//	@Column(type = long.class, comment = "主动退会时间")
//	GuildLeaveTime,
//	@Column(type = String.class, length = 512, comment = "公会中奖状态{位置sn(1-6):是否中奖(0未中奖,1中奖),...}", defaults = "")
//	GuildDrawJSON, 
//	@Column(type = int.class, comment = "公会抽奖领取状态（0不可领奖1可领奖）")
//	GuildDrawState,

}