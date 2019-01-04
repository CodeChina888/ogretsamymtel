package game.worldsrv.support.observer;

public final class EventKey {
	// 事件：每个整点执行一次
	public static final int ResetDailyHour = 10001; // 每个整点执行一次
	// 每分钟执行一次
	public static final int EVERY_MIN = 0x0001; // 每分钟执行一次
	
	// 事件：游戏启动及每日重置
	public static final int GameStartUpBefore = 0x1001; // 游戏启动前
	public static final int GameStartUpFinish = 0x1002; // 游戏启动完毕
	
	public static final int MergeOption = 0x1003; // 合服后同步版本
	
	// 事件：数据加载及待办事件
	public static final int HumanDataLoadBaseBeginOne = 0x1021;    // 玩家基础数据加载开始一个
	public static final int HumanDataLoadBaseFinishOne = 0x1022;   // 玩家基础数据加载完成一个
	public static final int HumanDataLoadOtherBeginOne = 0x1023;   // 玩家第一级别模块数据加载开始一个
	public static final int HumanDataLoadOtherFinishOne = 0x1024;  // 玩家第一级别模块数据加载完成一个
	public static final int HumanDataLoadOther2BeginOne = 0x1025;  // 玩家第二级别模块数据加载开始一个
	public static final int HumanDataLoadOther2FinishOne = 0x1026; // 玩家第二级别模块数据加载完成一个
	
	public static final int HumanDataLoadExtInfo = 0x1031;	 // 玩家数据加载扩展信息
	public static final int HumanDataLoadUnitProp = 0x1032;  // 玩家数据加载属性信息
	public static final int HumanDataLoadBuff = 0x1033; 	 // 玩家数据加载Buff信息
	public static final int HumanDataLoadBacklog = 0x1034; 	 // 玩家数据加载待办事项
	public static final int HumanDataLoadOther = 0x1035; 	 // 玩家优先级别模块数据加载开始
	public static final int HumanDataLoadOther2 = 0x1036;	 // 玩家第一级别模块数据加在开始
	public static final int HumanDataLoadAllFinish = 0x1037; // 玩家所有数据加载完成
	public static final int PartnerLoadLineUp = 0x1038; 	 // 加载武将上阵信息
	
	// 事件：场景地图相关
	public static final int StageRegister = 0x2001; // 场景被注册
	public static final int StageCancel = 0x2002; // 场景被注销
	public static final int StageInstStart = 0x2003; // 场景副本启动
	public static final int StageHumanEnterBefore = 0x2004; // 场景角色进入前（切换地图时会触发）
	public static final int StageHumanEnter = 0x2005; // 场景角色进入（切换地图时会触发）
	public static final int StageHumanRegister = 0x2006; // 场景角色注册到场景中的时候
	public static final int StageHumanShow = 0x2007; // 场景角色显示出来
	
	// 事件：战斗及技能相关
	public static final int UnitMoveStart = 0x2011; // 可移动单元每次开始移动
	public static final int UnitHpLoss = 0x2012; // 战斗单元受到伤害
	public static final int UnitAttack = 0x2013; // 战斗单元攻击
	public static final int UnitAction = 0x2014; // 地图单元有动作，移动，攻击，施法等
	public static final int UnitBeAttacked = 0x2015; // 战斗单元受攻击
	public static final int UnitBeKilled = 0x2016; // 战斗单元死亡
	public static final int UnitCastSkill = 0x2017; // 释放技能
	
	public static final int SkillPassiveCheck = 0x2021; // 被动技能检查
	public static final int GenSkillUp = 0x2022; // 武将技能升级
		
	// 事件：角色相关
	public static final int HumanCreate = 0x3001; // 创建角色
	public static final int HumanFirstLogin = 0x3002; // 创角后首次登陆
	public static final int HumanLogin = 0x3003; // 角色登录
	public static final int HumanLoginFinish = 0x3004; // 角色登录结束，可以开始接收消息。
	public static final int HumanLoginFinishFirstToday = 0x3005; // 角色今日首次登录结束，可以开始接收消息。
	public static final int HumanLogout = 0x3006; // 角色登出游戏
	
	public static final int HumanMoveStartBefore = 0x3011; // 角色每次开始移动 之前抛出
	public static final int HumanMoveStart = 0x3012; // 角色每次开始移动
	public static final int HumanMoveFinish = 0x3013; // 角色停止移动
	public static final int HumanHpLoss = 0x3014; // 角色受到伤害
	public static final int HumanAttack = 0x3015; // 角色攻击
	public static final int HumanActionBefore = 0x3016; // 角色有动作，移动，攻击，施法等 之前抛出
	public static final int HumanAction = 0x3017; // 角色有动作，移动，攻击，施法等
	public static final int HumanBeAttacked = 0x3018; // 角色受攻击
	public static final int HumanRevive = 0x3019; // 角色复活
	
	public static final int HumanLvUp = 0x3021; // 角色升级，每次升级抛出一次，传递开始和结束等级
	public static final int HumanNameChange = 0x3022; // 角色改名
	public static final int HumanActValueConsume = 0x3023;// 角色体力累计消耗
	public static final int HumanBuyMonthCard = 0x3024; // 角色购买月卡
	public static final int HumanCombatChange = 0x3025; // 角色战斗力变化
	public static final int HumanMoneyChange = 0x3026; // 角色货币数据改变
	public static final int HumanMoneyAdd = 0x3027; // 角色货币数据增加
	public static final int HumanMoneyReduce = 0x3028; // 角色货币数据减少
	public static final int HumanSkillUp = 0x3029; // 角色技能升级
	public static final int HumanSkillTrain = 0x3030; // 角色技能修炼
	public static final int HumanSkillTrainSave = 0x3031; // 角色技能修炼保存
	
	public static final int HumanTotalCombatChange = 0x3041; // 角色总战力发生变化
	
	// 事件：武将相关
	public static final int GeneralCreate = 0x4001; // 创建武将
	public static final int GeneralLvUp = 0x4002; // 武将升级
	public static final int GeneralBeAttacked = 0x4003; // 武将被攻击
	public static final int PartnerUnlocked = 0x4004;//伙伴解锁
	public static final int PartnerLvUp = 0x4005;//伙伴升级
	public static final int PartnerAdvanced = 0x4006;//伙伴突破
	public static final int PartnerStartUp = 0x4007;//伙伴升星
	
	
	// 事件：怪物相关
	public static final int MonsterMoveStart = 0x5001; // 怪物每次开始移动
	public static final int MonsterHpLoss = 0x5002; // 怪物受到伤害
	public static final int MonsterHpLossByNoHuman = 0x5003; // 怪物受到伤害
	public static final int MonsterAttack = 0x5004; // 怪物攻击
	public static final int MonsterBeKilledBefore = 0x5005; // 怪物被击杀前一刻
	public static final int MonsterBeKilled = 0x5006; // 怪物被击杀
	public static final int MonsterBeAttacked = 0x5007; // 怪物被攻击
	public static final int MonsterAction = 0x5008; // 怪物有动作，移动，攻击，施法等
	public static final int MonsterEatObject = 0x5009; // 怪物吞噬了其他怪
	public static final int MonsterBorn = 0x5010; // 怪物出生
	public static final int MonsterCastSkill = 0x5011; // 怪物施放技能
	
	// 事件：物品相关
	public static final int ItemChange = 0x6001; // 物品变动
	public static final int ItemChangeAdd = 0x6002; // 物品增加
	public static final int ItemChangeDel = 0x6003; // 物品删除
	public static final int ItemChangeMod = 0x6004; // 物品修改
	public static final int ItemCreate = 0x6005; // 物品创建
	public static final int ItemUse = 0x6006; // 物品使用
	public static final int ItemUseSuccess = 0x6007; // 物品使用成功
	
	// 事件：装备相关
	public static final int EquipIntensify = 0x6011; // 主公装备强化
	public static final int EquipAdvanced = 0x6012; // 主公装备进阶
	public static final int EquipPutOn = 0x6013; // 物品穿戴
	public static final int EquipRefine = 0x6014; // 主角装备精炼
	public static final int EquipRefineUp = 0x6015; // 主角装备精炼升级

	// 事件：副本相关
	public static final int InstEnter = 0x7001; // 副本进入
	public static final int InstNormalPass = 0x7002; // 普通副本通关
	public static final int InstHardPass = 0x7003; // 精英副本通关
	public static final int InstAutoPass = 0x7004; // 副本扫荡
	public static final int InstAnyPass = 0x7005;  // 任意副本通关
	public static final int InstFirstPass = 0x7006; // 副本首次通过，触发解锁
	
	public static final int InstResPass = 0x7011; // 资源本过关
	public static final int InstResAutoPass = 0x7012; // 资源本扫荡
	
	public static final int CompeteFight = 0x7021; // 竞技场通过
	public static final int CompeteRankUp = 0x7022; // 竞技场排名上升
	public static final int TowerEnter = 0x7023; // 爬塔进入
	public static final int TowerPass = 0x7024; // 爬塔通过当前层
	public static final int CompeteStart = 0x7025; // 竞技场开始
	public static final int CompeteRankHighest = 0x7026; // 竞技场排名最高
	
	// 事件：竞技场相关
	public static final int CompeteRankChange = 0x8001; // 离线竞技排行变化-机器人
	public static final int CompeteRewardSend = 0x8002; // 竞技场奖励发放
	public static final int CompeteRankChangeUser = 0x8003; // 离线竞技排行变化-角色
	
	//工会事件
	public static final int GuildAddExp = 0x8004;//工会加经验事件
	
	
	// 事件：活动副本相关
	public static final int ActInstTeamPVEEnter = 0x8011; // 活动副本之进入组队PVE
	public static final int ActInstTeamPVPEnter = 0x8012; // 活动副本之进入实时竞技
	public static final int ActInstWorldBossEnter = 0x8013; // 活动副本之进入世界Boss
	public static final int ActInstWorldBossHarmRank = 0x8014; // 活动副本之世界Boss伤害排行
	public static final int ActInstWorldBossKiller = 0x8015; // 活动副本之世界Boss击杀
	public static final int ActInstWorldBossHumanReborn = 0x8016; // 活动副本之世界Boss玩家重生
	
	// 事件：抢夺本相关
	public static final int LootMapEnter = 0x8100;// 进入抢夺本
	
	// 事件：活动事件
	public static final int ActSevenQuestStatusCheck = 0x9001; 	// 活动之开服七天任务状态检查
	public static final int ActShopBuy = 0x9002; 				// 活动之购买商品事件
	public static final int ActConsumeGold = 0x9003; 			// 活动之玩家消费元宝
	public static final int ActConsumeCoin = 0x9004; 			// 活动之玩家消费铜币
    public static final int UpdateActivitySeven = 0x9004;		//  更新7天开服活动进度
    public static final int ActResetDailyHour = 0x9005;			// 活动之5点更新或重置
	
	// 事件：Vip事件
	public static final int VipLvChange = 0x10001; // VIP等级发生改变
	public static final int VipBuyGold = 0x10002; // VIP充值钻石
	
	// 事件：每日消耗元宝购买事件
	public static final int DailyActBuy = 0x10011; // 每日购买体力
	public static final int DailyCoinBuy = 0x10012; // 每日购买铜币
	public static final int DailyCompeteFightBuy = 0x10013; // 每日购买竞技场挑战
	public static final int DailyTowerReviveBuy = 0x10014; // 每日购买爬塔复活
	
	// 事件：其它小事件
	public static final int HoroscopeDraw = 0x11001; // 占星抽卡
	public static final int FriendAdd = 0x11003; // 加好友
	
	// 事件：GM
	public static final int GM = 0x12001;//gm命令
	
	// 事件：充值
	public static final int Pay = 0x13001;//平台发来的安卓充值信息
	public static final int PayNotify = 0x150003;//游戏内用的充值通知事件
	public static final int PayIOS = 0x150005;//平台发来的IOS充值信息
	public static final int PayNotifyHttps = 0x150006;//后台发来的充值成功信息
	// 事件：抽卡
	public static final int DrawCard = 0x14001; //抽卡
	public static final int DrawCard_GoldTen = 0x14002; //元宝10连抽
	// 事件：成就
	public static final int AchievementComplete = 0x16001; //成就达成
	
	// 事件：神兽相关
	public static final int GodsUnlock = 0x17001; //神兽解锁
	public static final int GodsLvUp  = 0x17002;  //神兽升级
	public static final int GodsStarUp = 0x17003; //神兽升星
	public static final int GodsLvPractice = 0x17004; //神兽升级培养
	
	// 事件：主城
	public static final int MainCityCastellan = 0x18001; // 成为城主
	
	// 事件：切磋
	public static final int PKMirrorFightNum = 0x19001; // 切磋镜像玩家次数
	public static final int PKHumanFightNum = 0x19001; // 切磋真人玩家次数
	
	
	
	// 以下为跨服服务事件，非跨服事件请加在上面去//////////////////////////////////////////////////////////
	public static final int CrossStartupBefore = 0x310001;			// 跨服游戏启动前
	public static final int CrossStartupFinish = 0x310002;			// 跨服游戏启动完毕
	public static final int CombatantLogout = 0x310003;				// 玩家登出游戏
	public static final int CombatantStageEnterBefore = 0x310004;	// 玩家进入地图之前一点点（切换地图时会触发）
	public static final int CombatantStageEnter = 0x310005;			// 玩家进入地图（切换地图时会触发）
	public static final int CombatantLogin = 0x310006;				// 玩家登录
	public static final int CombatantStageLeave = 0x310005;			// 玩家离开地图（切换地图时会触发）
	public static final int HumanCultureTimes = 0x310006;//加载玩家培养次数信息
	/////////////////////////////////////////////////////////////////////////////////////////////
	
}