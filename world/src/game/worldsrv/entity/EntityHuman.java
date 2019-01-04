package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
import game.worldsrv.entity.EntityUnit;

@Entity(entityName = "Human", tableName = "human", superEntity = EntityUnit.class)
public enum EntityHuman {
	// GM权限
	@Column(type = int.class, comment = "GM权限：0无任何权限，1有全部权限", defaults = "0")
	GMPrivilege,
	
	/* 客户端调试开关 */
	@Column(type = boolean.class,  comment = "是否开启客户端调试", defaults = "false")
	ClientOpenSRDebug,
	@Column(type = int.class, comment="客户端调试日志级别", defaults = "0")
	ClientLogType,
	
	// 账号信息
	@Column(type = int.class, comment = "平台Id2位")
	PlatformId, 
	@Column(type = int.class, comment = "服务器Id4位", index = true)
	ServerId, 
	@Column(type = int.class, comment = "玩家标识6位（不可改变字段名）")
	HumanDigit,
	@Column(type = int.class, comment = "登录类型ELoginType")
	LoginType,
	@Column(type = String.class, length = 64, comment = "登录账号")
	AccountLogin, 
	@Column(type = String.class, length = 64, comment = "唯一账号ID", index = true)
	AccountId, 
	@Column(type = String.class, length = 16, comment = "渠道", index = true)
	Channel, 
	@Column(type = String.class, length = 16, comment = "设备MAC地址")
	DevMAC, 
	@Column(type = long.class, comment = "设备IMEI号")
	DevIMEI, 
	@Column(type = int.class, comment = "设备类型：0PC，1IOS，2越狱，3安卓")
	DevType, 
	@Column(type = int.class, comment = "在线时间累积秒数")
	TimeSecOnline, 
	@Column(type = long.class, comment = "在线时间")
	TimeOnLine, 
	@Column(type = long.class, comment = "最后一次登录时间")
	TimeLogin, 
	@Column(type = long.class, comment = "最后一次登出时间")
	TimeLogout, 
	@Column(type = long.class, comment = "角色创建时间")
	TimeCreate, 
	@Column(type = long.class, comment = "角色SessionKey", index = true)
	SessionKey, 
	@Column(type = boolean.class, comment = "是否第一次创角")
	FirstCreateHuman,
	@Column(type = int.class, comment = "头像sn")
	HeadSn,
	@Column(type = int.class, comment = "坐骑sn")
	MountSn,
	@Column(type = int.class, comment = "时装sn")
	FashionSn, 
	@Column(type = int.class, comment = "变装sn")
	HenshinSn,
	// 称号信息
	@Column(type = int.class, comment = "称号sn", defaults = "0")
	TitleSn,
	@Column(type = boolean.class, comment = "是否显示称号")
	TitleShow,
	
	//TODO 补偿邮件相关 (记录补偿邮件的id,用逗号分割，在登录的时候检查，如果Id过期了进行维护)
	@Column(type = String.class, comment = "补偿邮件相关", defaults = "[]", length=1024)
	fillMailJson,

	// VIP充值相关
	@Column(type = int.class, comment = "VIP等级", defaults = "0")
	VipLevel,
	@Column(type = long.class, comment="累计充值金额")
	ChargeGold,
	@Column(type = long.class, comment="今日充值金额",defaults = "0")
	TodayChargeGold,
	@Column(type = String.class, comment="充值信息", length=512)
	PayCharge,
	/** 按天返还元宝的充值信息 **/
	@Column(type = String.class, comment="可按天返还的充值信息{商品sn, 充值次数, 返还结束时间}", defaults = "[]" , length=1024)
	ChargeInfo,
	
	// 货币信息开始 14lootScore记录在抢夺本信息里，15VIP经验通过充值实际金额来替代
	@Column(type = long.class, comment = "1 铜币")
	Coin, 
	@Column(type = long.class, comment = "2 元宝")
	Gold, 
	@Column(type = long.class, comment = "3 体力值")
	Act, 
	@Column(type = long.class, comment = "4 经验值")
	Exp,
	@Column(type = long.class, comment = "5 威望")
	CompeteToken,
	@Column(type = long.class, comment = "6 元魂")
	SoulToken, 
	@Column(type = long.class, comment = "7 远征积分")
	TowerToken, 
	@Column(type = long.class, comment = "8 将魂")
	GeneralToken, 
	@Column(type = long.class, comment = "9 招募代币")
	SummonToken,
	@Column(type = long.class, comment = "10 伙伴经验池")
	ParnterExp,
	@Column(type = long.class, comment = "11 纹石碎片")
	RuneToken,
	@Column(type = long.class, comment = "12 抢夺本单人代币")
	LootSingle,
	@Column(type = long.class, comment = "13 抢夺本多人代币")
	LootMultiple,
	@Column(type = long.class, comment = "16 招募赠送代币（仙缘）")
	SummonPresent,
	@Column(type = long.class, comment = "17 高级招募令")
	SummonHigher,
	@Column(type = long.class, comment = "18 刷新令")
	RefreshToken,
	@Column(type = long.class, comment = "19 阅历（技能修炼）")
	SkillExperience,
	@Column(type = long.class, comment = "20 精炼石")
	RefineToken,
	@Column(type = long.class, comment = "21 招募获得积分")
	SummonScore,
	@Column(type = long.class, comment = "22 法宝灵气")
	CimeliaToken,
	@Column(type = long.class, comment = "23 开采令")
	DevelopmentToken,
	@Column(type = long.class, comment = "24 抢夺令")
	SnatchToken,
	@Column(type = long.class, comment = "25 修炼重置石")
	ResetStone,
	@Column(type = long.class, comment = "26 仙盟币")
	GuildCoin,
	
	// 货币信息结束
	@Column(type = int.class, comment = "最大体力值")
	ActMax,
	
	// 地图
	@Column(type = String.class, length = 512, comment = "地图位置信息{{id,sn,x,y,common},{}}")
	StageHistory,

	// 装备和时装记录
	@Column(type = boolean.class, comment = "是否显示时装,true显示", defaults = "true")
	FashionShow, 
	@Column(type = int.class, comment = "时装武器sn")
	FashionWeaponSn, 
	@Column(type = String.class, length = 1024, comment = "主公装备槽", defaults = "")
	EquipPosJSON,
	
	// 背包
	@Column(type = int.class, comment = "当前背包最大格子数")
	ItemBagNumMax,
	
	/* 队伍及活动副本相关 */
	@Column(type = int.class, comment = "所属队伍ID")
	TeamId, 
	
	@Column(type = String.class, length = 128, comment = "活动副本之魔王梦境最后通关记录{type2:difficulty}")
	InstActMHXKWarLastPass, 
	@Column(type = String.class, length = 128, comment = "活动副本之经典战役最后通关记录{type2:difficulty}")
	InstActStoryLastPass, 
	@Column(type = String.class, length = 128, comment = "活动副本之经典战役领奖记录{type2:EAwardType}")
	InstActStoryAward, 
	
//	/* 竞技场相关 */
//	@Column(type = int.class, comment = "竞技场历史最高排名", defaults = "0")
//	CompeteRankTop, 
//	@Column(type = long.class, comment = "竞技场玩家上次挑战时间")
//	CompeteFightTime, 
//	@Column(type = int.class, comment = "竞技场历史当前排名", defaults = "0")
//	CompeteRankNow, 
//	@Column(type = boolean.class, comment = "竞技场完成后领奖标识")
//	CompeteLottery,
//	@Column(type = String.class, length = 512, comment = "竞技场已经领取的历史最高奖励sn", defaults = "")
//	CompeteHistoryTopAward,
//	
	/* 抽奖相关 */
	@Column(type = String.class, length = 128, comment = "抽卡的各个栏位冷却时间, {{1,12312312321},{},{}}", defaults = "{}")
	LottoryCooldown, 
	@Column(type = String.class, length = 32, comment = "已经抽过的宝箱类型信息 11|21|31|", defaults = "")
	LottorySelectedCount,
	@Column(type = int.class, comment = "免费抽奖次数", defaults = "0")
	LottoryFreeCount,

	/* 占星相关 */
	@Column(type = String.class, length = 128, comment = "占星的各个栏位冷却时间, {{1,12312312321},{},{}}", defaults = "{}")
	HoroCooldown, 
	@Column(type = String.class, length = 32, comment = "第一，二栏位，免费抽奖情况{pos1,status;pos2,status}sta:0或1", defaults = "{}")
	HoroIsFirst,
	@Column(type = String.class, length = 32, comment = "免费抽奖次数,结构是1,1,1")
	HoroFreeCount,
	@Column(type = String.class, length = 32, comment = "极品间隔次数,结构是1,1,1")
	HoroJPGap, 
	@Column(type = String.class, length = 32, comment = "极品次数累计,结构是1,1,1")
	HoroJPCount,

	// 任务及活跃度相关
	@Column(type = boolean.class, comment = "是否已经过了改名任务", defaults = "false")
	QuestNameChangePassed,
	
	/* 新手引导 */
	@Column(type = int.class, comment = "创角后的第一个新手剧情")
	FirstStory,
	@Column(type = boolean.class, comment = "玩家新手引导是否关闭,true 关闭，false 打开")
	GuideClose,
	@Column(type = String.class, length = 64, comment = "玩家新手引导ID")
	GuideIds, 

	// 技能设置
	@Column(type = String.class, length = 64, comment = "技能设置1")
	SkillSet1, 
	@Column(type = long.class, comment = "技能升级材料总消耗", defaults = "0")
	SkillStuffCost,
	
	/* 玩家每日零点重置的信息 */
	// 玩家每日零点重置的信息-消耗元宝购买次数
	@Column(type = int.class, comment = "今日已购买体力次数 ")
	DailyActBuyNum, 
	@Column(type = int.class, comment = "今日已购买铜币次数 ")
	DailyCoinBuyNum, 
	@Column(type = int.class, comment = "今日已购买竞技场挑战次数 ")
	DailyCompeteFightBuyNum, 
	@Column(type = int.class, comment = "今日已购买抢夺本复活次数", defaults = "0")
	DailyLootMapRevivalBuyNum,
	@Column(type = int.class, comment = "今日已发送魔法表情次数", defaults = "0")
	DailySendWinksNum,
	@Column(type = int.class, comment = "今日已主城切磋胜利次数", defaults = "0")
	DailyFightWinNum,
	
	//TODO 玩家每日零点重置的信息-其他零碎信息
	@Column(type = long.class, comment = "今日在线时间", defaults = "0")
	DailyOnlineTime,
	@Column(type=int.class, comment="每日签到：0未签到,1已签到", defaults = "0")
	DailySignFlag,
	@Column(type = int.class, comment = "每日竞技场已挑战次数")
	DailyCompeteFightNum, 
	@Column(type = int.class, comment = "每日竞技场积分")
	DailyCompeteIntegral,
	@Column(type = String.class, length = 512, comment = "每日竞技场已经领取的积分sn", defaults = "")
	DailyCompeteIntegralAward,
	@Column(type = String.class, length = 1024, comment = "每日活动副本已完成次数{sn:1,...}", defaults = "{}")
	DailyInstFinishNum,
	@Column(type = String.class, length = 128, comment = "每日排行榜膜拜次数{0:1,2:1,3:1,4:1}")
	DailyRankWorship, 
	@Column(type = int.class, comment = "每日任务活跃度，需每日清0")
	DailyQuestLiveness, 
	@Column(type = String.class, length = 128, comment = "每日活跃度奖励已领取记录，需每日清空", defaults = "")
	DailyLivenessReward,
	/* 玩家每日零点重置的信息 */
	
	/* 玩家每周零点重置的信息 */
	@Column(type = int.class, comment = "每周任务活跃度，需每周清0")
	WeeklyQuestLiveness, 
	@Column(type = String.class, length = 128, comment = "每周活跃度奖励已领取记录，需每周清空", defaults = "")
	WeeklyLivenessReward,
	/* 玩家每周零点重置的信息 */
	
	// 体力恢复
	@Column(type = long.class, comment = "恢复满体力的时间点")
	ActFullTime,
	
	/* activity补签 */
	@Column(type = int.class, comment = "补签次数")
	Resigntimes,
	
	@Column(type=long.class, comment="福利号上次领取福利时间，一星期领取一次", defaults = "0")
	TimeFuli,
	@Column(type=boolean.class, comment="是否是福利号")
	Fuli,
	@Column(type=boolean.class, comment="是否是GS")
	Gs,
	@Column(type=int.class, comment="GS号的虚拟人民币数量", defaults = "0")
	GsRmb,
	
	@Column(type = long.class, comment = "禁言的玩家结束时间", defaults = "0")
	SilenceEndTime,
	@Column(type = long.class, comment = "封号的玩家结束时间", defaults = "0")
	SealEndTime,
	@Column(type = int.class, comment = "是否是已删除的玩家，0 未删除，1 已删除", defaults = "0")
	DeleteRole, 
	@Column(type = int.class, comment = "已改名次数", defaults = "0")
	RenameNum,
	
	/* 已经开启的功能模块sn */
	@Column(type = String.class, length = 128, comment = "已解锁的功能模块sn(只存任务类型的功能模块sn)", defaults = "")
	ModUnlock,
	/* 功能开放领取奖励记录 */
	@Column(type = String.class, length = 128, comment = "功能开放领取奖励", defaults = "")
	ModUnlockReward,

	/* 排行榜膜拜信息 */
	@Column(type = int.class, comment = "副本排行星星数", defaults = "0")
	RankInstStars,
	@Column(type = int.class, comment = "竞技场最高排名", defaults = "0")
	CompeteRank,
		
	/* 战力*/
	@Column(type = int.class, comment = "总战力", defaults = "0")
	SumCombat,
	@Column(type = int.class, comment = "伙伴总战力", defaults = "0")
	GeneralSumCombat,
	@Column(type = int.class, comment = "伙伴最强阵容总战力", defaults = "0")
	GeneralMostCombat,
	
	/*帮会*/
	@Column(type = long.class, comment = "帮会贡献", defaults = "0")
	Contribute,
	@Column(type = long.class, comment = "帮会ID", defaults = "0")
	GuildId,
	@Column(type = long.class, comment = "上次离开帮会时间", defaults = "0")
	GuildLeaveTime,
	@Column(type = int.class, comment = "工会等级", defaults = "0")
	GuildLevel,
	@Column(type = boolean.class, comment = "是否是会长")
	GuildCDR,
	@Column(type = String.class, comment = "宝箱领取")
	GuildImmoSnList,
	@Column(type = String.class, comment = "公会技能")
	GuildSkills,
	@Column(type = String.class, comment = "公会名称")
	GuildName,
	
	//活动积分
	@Column(type = int.class, comment = "活动积分", defaults = "0")
	ActivityIntegral,
	@Column(type = String.class, comment = "活动积分已领取宝箱sn", defaults = "")
	IntegralBoxList,
	/**/
	@Column(type = long.class, comment = "购买开采令次数")
	BuyDevelopmentTokenCount,
	@Column(type = long.class, comment = "购买抢夺令次数")
	BuySnatchTokeCountn,
	@Column(type = int.class, comment = "已登陆天数",defaults="0")
	LoginNum,
	@Column(type = int.class, comment = "7日登陆当前奖励天数",defaults="0")
	LoginNumAwardStatus,
	@Column(type = long.class, comment = "领取登陆奖励时间",defaults="0")
	GetloginAwardTime,
    @Column(type = int.class, comment = "公会副本挑战次数",defaults="0")
    GuildInstChallengeTimes,
    @Column(type = long.class, comment = "公会副本挑战次数添加时间",defaults="0")
    GuildInstChallengeTimesAddTime,
    @Column(type = String.class, length = 1024, comment = "公会副本章节奖励",defaults="")
    GuildInstChapterReward,
    @Column(type = String.class, length = 1024, comment = "公会副本关卡奖励",defaults="")
    GuildInstStageReward,
    @Column(type = long.class, comment = "公会副本重置时间",defaults="0")
    GuildInstResetTime,
    @Column(type = int.class, comment = "公会副本已购买挑战次数",defaults="0")
    GuildInstBuyChallengeTimes,
}