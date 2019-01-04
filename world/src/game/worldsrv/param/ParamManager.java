package game.worldsrv.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.ManagerBase;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfParam;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.C;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class ParamManager extends ManagerBase {
	public static boolean whileListStatus = false;	// 是否开启白名单，默认关闭白名单
	
	// 特殊功能配置
	public static boolean openCheckVersion = true;// 开启检查版本号（1：开启，0：关闭）
	public static long newlyVersion = 0;// 最新版本号（该值必须和客户端登录时提交的验证版本号一致才允许登录）
	public static int loginMaxOnline = 10000;		// 最大同时在线人数(5000机器人开10G内存给world就够用)
	public static int loginMaxQueue = 10;			// 最大同时登录人数
	public static int loginTimeFull = 1;			// 服务器在线人数满时预计每人次等待时间
	public static int loginIntervalTips = 10;		// 登录队列中角色的提示间隔(秒)
	public static int loginIntervalClear = 30;		// 登录队列信息维护清理间隔(秒)
	public static int closeDelaySec = 300;// 玩家下线延时时间即断线重连秒数：默认300秒
	
	public static boolean openReloadData = true;// 开启热加载数据文件（1：开启，0：关闭）
	public static boolean openReloadClass = true;// 开启热加载类文件（1：开启，0：关闭）
	public static boolean openGM = true;// 开启GM命令（1：开启，0：关闭）
	
	public static boolean openShowService = true;// 开启联系客服（1：开启，0：关闭）
	public static boolean openActiveKey = true;// 开启激活码（1：开启，0：关闭）
	public static boolean openGiftKey = true;// 开启礼包激活码（1：开启，0：关闭）
	public static boolean openChatUpload = true;// 开启聊天上传到聊天服务器（1：开启，0：关闭）
	public static int[] chatMonitor = {20,1,3,90,19};// 监测聊天规则：等级,VIP等级,最大发言数,重置秒数,最大字数
	public static int dropCountRepeatNum = 100; // 循环最大次数
	
	// 每日重置相关配置
	public static int dailyHourReset = 5;// 每日重置时间点
	public static int dailyCoinBuyValue = 5000;// 快速购买 - 每次购买铜币的数量
	public static int dailyActBuyValue = 120;// 快速购买 - 每次购买体力的数量
	
	// 人物新手配置
	public static int guideClose = 0;// 是否关闭新手引导：1关闭，0开启
	public static int firstMapSn = 1;// 创角后的第一个场景地图SN
	public static int newbieMapSn = 0;//99999;// 创角后的新手引导战斗副本的地图SN
	public static int newbieStageSn = 0;//99999;// 创角后的新手引导战斗副本的关卡SN

	// 人物初始配置
	public static int maxHumanLevel = 80;// 最大人物等级
	public static int initHumanLevel = 1;// 初始人物等级
	public static int initHumanSpeed = 80;// 初始人物移动速度
	public static int cdHumanAct = 6;// 体力恢复倒计时时间（分钟）
	public static int initHumanAct = 60; // 初始体力值（=初始最大体力值）
	public static int initHumanCoin = 5000; // 初始人物铜币
	public static int initHumanGold = 100; // 初始人物元宝
	
	public static int maxItemBagNum = 999;		//初始背包格子数
	public static int[] initHumanEquip = {};	//初始人物携带装备sn
	public static int initHumanEquipLv;			//初始人物携带装备强化等级
	public static int fashionInitialSuitId;	//默认创角套装
	
	// 人物伙伴相关
	public static String initPartnerSn = "";// 初始携带伙伴sn（数组方式填写-1表示主角）
	public static int initPartnerStance = 1;// 初始伙伴站位阵型（0-W型"前3后2"；1-M型"前2后3"）
	public static int[] recruitAutoLineUpLvRange = {1,2}; // 自动上阵的等级区间
	public static int[] arrayUnlock = {4,16,0,0,0}; // 布阵-1~5各阵位解锁等级（W型"前3后2"；M型"前2后3"）
	public static int partnerMaxNum = 100;		//允许伙伴的最大数量
	public static int cardGetGodFate = 10;	// 招募一次获得的仙缘
	public static int[] cardOnceNormalScore = {1,10}; // 普通招募一次获得的积分
	public static int[] cardOnceHigherScore = {10,50}; // 高级招募一次获得的积分
	public static int cardExchangeMaxRound = 10; // 招募积分兑换最大轮数
	
	// 人物战斗配置
	// TODO 战斗相关补充
	public static int hitParam = 1000;// 命中参数
	public static int hitParamA = 100; // 命中参数a
	public static int hitParamB = 100; // 命中参数e
	public static int hitParamMin = 5000; // 命中概率下限
	public static int critAddParamD = 2; // 暴击概率参数d
	public static int critAddParamE = 1000; // 暴击概率参数e
	public static double critAddParamF = 0.1f; // 暴击概率参数f
	public static int critAddParam = 1000;// 暴击倍率参数
	public static int critAddParamMax = 5000; // 暴击概率上限
	public static int critAddParamMin = 500; // 暴击概率下限
	public static int controlParamG = 2; // 控制概率参数d
	public static int controlParamH = 1000; // 控制概率参数e
	public static double controlParamI = 0.1f; // 控制概率参数f
	public static int controlParamMax = 9000; // 控制概率上限
	public static int controlParamMin = 1000; // 控制概率下限
	public static int battleBlockParam = 1500;// 格挡减免伤害比例
	public static int battleMpRecovery = 2;// 基础能量回复
	public static int battleAngerInit = 1;// 爆点初始怒气
	public static int battleAngerMax = 4;// 爆点怒气上限
    public static int battleAngerRecoverySkill = 1;// 主角使用技能增加爆点怒气值
    public static int battleAngerRecoveryMonsterDie = 0;// 敌方怪物死亡增加怒气值
    public static int battleAngerRecoveryEnemyDie = 0;// 敌方主角/伙伴死亡增加怒气值
    public static int battleAngerRecoveryOurDie = 0;// 我方伙伴死亡增加怒气值
    public static int battleAngerRecoveryPveRound = 0;// PVE战斗每回合回复怒气值
    public static int battleAngerRecoveryPvpRound = 0;// PVP战斗每回合回复怒气值
    public static int battleAngerRecoveryGvGRound = 0;// GVG战斗每回合回复怒气值
    public static int battlePveRoundLimit = 20;// PVE通用 - 战斗最大回合数限制
    public static int battlePvpRoundLimit = 10;// PVP通用 - 战斗最大回合数限制
    public static int battleAttackLimit = 3;// PVP通用 - 主角出手回合时间限制（秒）
    public static int battleTreatmentCritParam = 500;// 回复技能暴击系数（千分比值）
    public static int battleSkillCostHpParam = 10;// 战中释放技能HP消耗保底值（千分比值）
    public static int PVEHumanCastTime = 300;	// PVE战斗中玩家出手最长等待时间，最后几秒倒计时后自动使用随机技能出手
    public static int PVPHumanCastTime = 30;	// PVP战斗中玩家出手最长等待时间，最后几秒倒计时后自动使用随机技能出手
    public static List<Integer> combatCapabilityRepressio = new ArrayList<>(); // 战力压制影响的玩法数组 
    


	// 人物战力转化参数
//	public static double combatHpMax				= 0; 			// 战斗力-最大生命值系数
//	public static double combatMpMax				= 0; 			// 战斗力-最大魔法值系数
//	public static double combatAtkPhy				= 0; 			// 战斗力-物理攻击系数
//	public static double combatDefPhy				= 0; 			// 战斗力-物理防御系数
//	public static double combatAtkMag				= 0; 			// 战斗力-法术攻击系数
//	public static double combatDefMag				= 0; 			// 战斗力-法术防御系数
//	public static double combatHit					= 0; 			// 战斗力-命中系数
//	public static double combatDodge				= 0; 			// 战斗力-闪避系数
//	public static double combatBlock				= 0; 			// 战斗力-格挡系数
//	public static double combatCrit					= 0; 			// 战斗力-暴击系数
//	public static double combatCritAdd				= 0; 			// 战斗力-必杀（暴击倍率）系数
//	public static double combatAntiCrit				= 0; 			// 战斗力-坚韧（抵抗暴击）系数
//	public static double combatControl				= 0;			// 战斗力-控制系数
//	public static double combatAntiControl			= 0;			// 战斗力-抗控（抵抗控制）系数
//	
//	public static double combatHpRecovery			= 0; 			// 战斗力-生命回复系数
//	public static double combatHurtDeep				= 0; 			// 战斗力-伤害加深系数
//	public static double combatHurtDrop				= 0; 			// 战斗力-伤害减免系数
   
    public static double combatHpMax       = 0;     // 战斗力-最大怒气系数(Float型）  
    public static double combatRageMax     = 0;     // 战斗力-最大怒气系数(Float型）          
    public static double combatAtk         = 0;     // 战斗力-攻击系数(Float型）
    public static double combatDefPhy      = 0;     // 战斗力-物防系数(Float型）              
    public static double combatDefMag      = 0;     // 战斗力-法防系数(Float型）              
    public static double combatHit         = 0;     // 战斗力-命中系数(Float型）            
    public static double combatDodge       = 0;     // 战斗力-闪避系数(Float型）              
    public static double combatCrit        = 0;     // 战斗力-暴击系数(Float型）            
    public static double combatAntiCrit    = 0;     // 战斗力-坚韧系数(Float型）                
    public static double combatBlock       = 0;     // 战斗力-格挡系数(Float型）              
    public static double combatAntiBlock   = 0;     // 战斗力-破挡系数(Float型）  
    public static double combatDamAdd      = 0;     // 战斗力-伤害加深(Float型） 
    public static double combatDamAddEx    = 0;     // 战斗力-最终增伤(Float型）
    public static double combatDamRed      = 0;     // 战斗力-伤害减免(Float型）              
    public static double combatDamRedEx    = 0;     // 战斗力-最终减伤(Float型）                
    public static double combatPenePhy     = 0;     // 战斗力-物理穿透(Float型）                
    public static double combatPeneMag     = 0;     // 战斗力-法术穿透(Float型）                
    public static double combatCureAdd     = 0;     // 战斗力-治疗强度(Float型）                
    public static double combatCureAddEx   = 0;     // 战斗力-最终治疗(Float型）                  
    public static double combatHealAdd     = 0;     // 战斗力-受疗效果(Float型）                
    public static double combatCritAdd     = 0;     // 战斗力-致命一击(Float型）                
    public static double combatAntiCritAdd = 0;     // 战斗力-致命抵抗(Float型）
	
	// 人物其他配置
	public static int[] reviveAddBuff = {};// 人物复活加buff
	public static int changeNameCostGold = 100;// 改名第一次免费，以后每次100元宝
	public static int maxVipLv = 15;// 最大VIP等级
	public static int maxHumanNameLength = 8;// 最大人物名字长度
	
	// 资源本配置
	public static int instResChallengeTimes = 3; // 资源本挑战次数
	
	// 技能及技能点配置
	public static int rageMax = 1000;// 技能之怒气上限值
	public static int backDisMax = 5;// 技能之最大击退距离
	public static int[] skillSlotUnlock = {1,1,3,5,9}; //技能解锁等级最后一个为爆点技能
	public static int skillMaxInstall = 4; //最大上阵技能数
	public static int skillLevelLimit = 80; // 技能等级上限
	public static int skillUpgradingTime = 3600; // 符文重置按钮刷新时间
	public static int skillGodsAddLevelQm = 10; // 爆点附加技能解锁等级
	public static int[] skillTrainCost = {8,100}; //培养抽奖一次需要的资源和数量
	public static int skillTrainDouble = 100; //两个相同额外产出值
	public static int skillTrainTriple = 300; //三个相同额外产出只
	public static int[] skillTrainReset = {8,100}; //技能培养重置消耗
	public static String[] skillTrainCostRate = {}; // 技能培养消耗倍率相关  
	public static int[] skillTrainType = {1,2,3,4}; // 技能神通类型
	
	// 排行榜配置
	// 对应顺序按RankIntType：
	public static int[] rankTopShowNum = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100};// 各个排行榜下发与显示的数量
	public static int[] rankTopRecordFilter = {10, 5000, 1, 10, 10, 10, 10, 10, 10, 10};// 各排行榜的入榜条件 TODO
	public static int[] rankTopRecordMaxNum = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};// 各排行榜的记录上限
	public static String rankTopRewardSn = ""; // 奖励Sn
	public static String rankTopRewardNum = ""; // 奖励数量
	public static int rankWorshipTimes = 1;// 排行榜可以膜拜的次数；
	
	// 竞技场配置
	public static int competeRankMax = 2000;// 竞技场排名最大值(初始值)（该值不配到param表，因为修改该值得重启服务器）
	public static int competeGradeRange = 30; // 首次匹配对手的排名和最低名次的名次差值
	public static int competeRobotNum = 2000; // 竞技场机器人数量
	public static int competeRobotAI = 6666;// 竞技场AI编号
	public static int competeRankTop = 50;// 竞技场排名榜可查看排名前多少名
	public static int competeFreeNum = 10; // 竞技场每日免费次数
	public static int competeBuyNumAdd = 5;// 竞技场 - 每天可购买的挑战次数
	public static int[] competeBuyNumVip = {};// 竞技场VIP对应可购买次数（从VIP0开始）
	// public static int competeRewardStartHour = 21;//竞技场奖励发放开始时间（小时）
	// public static int competeRewardLastMinute = 30;//竞技场奖励发放持续时间（分钟）
	public static int competeMapSn = 8;// 竞技场 RepStage表sn
	public static int competeFightTime = 300;// 竞技场挑战冷却时间（单位：秒）
	//public static int competeFightAwardItemSn = 8;// 竞技场挑战获得固定奖励道具Sn
	//public static int competeFightAwardItemNumSucc = 2;// 竞技场挑战胜利获得固定奖励道具数量
	//public static int competeFightAwardItemNumFail = 1;// 竞技场挑战失败获得固定奖励道具数量
	public static int competeIntegral = 1;// 竞技场参与后获得积分
	public static int competeParamA = 55;//竞技场匹配参数A
	public static int competeParamB = 1;//竞技场匹配参数B
	public static int [] competeRobotSn ={3001,3002,3003};//竞技场首次随机机器人军团ID
	
	// 爬塔配置
	public static int towerMapSn = 7;// 爬塔对应的instStageSn和mapSn
	public static int towerMaxLayer = 9;// 爬塔的最高层
	public static int towerPartnerMinLv = 1;// 爬塔伙伴最低等级限制
	public static int towerInitHaveLife = 3;// 爬塔初始生命数量
	public static int towerInitCombat = 19800;// 爬塔初始战力
	public static int towerJoinMultiple = 10000; // 参与人数倍率万份比
	public static int towerBaseJoinNum = 10; // 保底参与人数
	public static int[] towerResetting = {3,50}; // 爬塔重置条件消耗
	public static int towerParam1 = 1; // 平衡系数C
	public static int towerParam2 = 1; // 平衡系数D
	public static int towerSeasonEndDay = 7; // 赛季轮回 开始/结束：周几
	public static int towerSeasonEndTime = 5; // 赛季轮回 开始/结束当天的时间hour
	
	
	// 主城相关
	public static int mainCityFightMapSn = 411; // 主城切磋场景
	public static int mainCityRewardCount = 5; // 主城挑战可以领奖次数
	public static int[] mainCityReward = {8001,8001,8001,8001,8001}; // 对应次数的奖励sn
	
	// 邮件配置
	public static int mailMax = 30;// 邮件最大存储个数
	public static String mailMark = "$&#";// 邮件特殊标志
	public static String sysMsgMark = "$#&";// 系统提示特殊标志
	
	// 切磋相关
	public static int pkMirrorMapSn = 411; // 切磋镜像玩家 - 战斗场景sn
	public static int pkMirrorRewardCount = 5; // 切磋镜像玩家 - 奖励次数
	public static int[] pkMirrorReward = {0,0,0,0,0}; // 切磋镜像玩家 - 对应次数奖励
	public static int pkHumanMapSn = 412; // 切磋真人玩家 - 战斗场景sn
	public static int pkHumanRewardCount = 5; // 切磋真人玩家 - 奖励次数
	public static int[] pkHumanReward = {0,0,0,0,0}; // 切磋真人玩家 - 对应次数奖励
	
	// 聊天配置
	public static int chatLengthMax = 40;// 聊天框发言长度限制
	public static int chatCDTime = 10;// 聊天发言的时间限制（秒）
	
	// 世界boss
	public static int worldBossShowMaxNum = 30;// 世界BOSS - 同屏显示最大人数
	public static int worldBossTopShowNum = 50;// 世界BOSS - 排行下发与显示数量
	public static int worldBossTopRankNum = 100;// 世界BOSS - 排行记录上限
	public static int worldBossFightCD = 30;// 世界BOSS - 复活秒数，玩家死亡后复活的固定秒数
	public static int worldBossInspireCD = 150;// 世界BOSS - 每次鼓舞增加的CD，单位为秒
	public static int worldBossInspireFreeNum = 1;// 世界BOSS - 初始活动时玩家可免费鼓舞次数
	public static int worldBossInspireMaxNum = 20;// 世界BOSS - 鼓舞的伤害加成最大次数
	public static int worldBossInspireAddAtk = 500;// 世界BOSS - 每次鼓舞提升的攻击加成万分比
	public static int worldBossRebornAddAtk = 2000;// 世界BOSS - 涅槃重生提升的攻击加成万分比，不收鼓舞上限影响，但是只作用于本次战斗
	public static int worldBossDyingRatio = 2000;// 世界BOSS - 濒死血量万分比，低于该数值则隐藏血条
	public static int worldBossReadySecond = 60;// 世界BOSS - 几秒后才可以打BOSS
	public static int worldBossKillSecond = 400;// 世界BOSS - 几秒内击杀BOSS，则BOSS会升级
	public static int worldBossDieSecond = 2;// 世界BOSS - 在boss死亡后，几秒内的伤害输出有效
		
	//好友相关
	public static int friendsRefresh = 10000;			// 好友推荐列表的手动刷新CD（毫秒）
	public static int friendsUpperLimit = 50;			// 好友的数量上限
	public static int friendsApplyLimit = 50;			// 好友的申请数量上限
	public static int friendsGiveNumber = 6;				// 好友互赠体力的体力数量
	public static int friendsReceiveTimes = 5;				// 好友间每天可领取体力的次数
	public static int friendsGiveTimes = 5;				// 好友间每天可赠送体力的次数
	public static int friendsMessageTime = 7;				// 好友申请信息的最长存在时间（天）
	public static int friendsBlackUpperLimit = 100;				// 黑名单上限
	public static int friendsApplyUpperLimit = 50;				// 好友申请上限

	//命格相关
	public static int[] divinationLatticeUnlockLvArr = {10,28,33,35,40,45,50,55}; 	//10,28,33,35,40,45,50,55  命格装备位置开放等级
	public static int[] divinationCountArr = {}; 	        //0,0,0,10,10  命格计数间隔，白,绿,蓝,紫,橙，0为无需计数
	public static int divinationBagNum = 60; 	            //命格背包容量（用于召唤时限制）
	public static int divinationBagActualNum = 200;			//命格实际可装最大数量（额外领取或者卸下时的判断）
	
	//抢夺本
	public static int pvpMapSn = 3; // pvp战斗map.sn
	public static int lootMapPvpRoleSkillLv = 5; // 抢夺本PVP战斗中，技能的平衡等级
	public static int lootMapPvpGodSkillLv = 5; // 抢夺本PVP战斗中，爆点的平衡等级
	public static int[] lootMapPvpRoleCombatData = {8004,8004,8004}; // 抢夺本PVP战斗中，主角替换模板
	public static int[] lootMapPvpPartnerCombatData = {8001,8002,8003}; // 抢夺本PVP战斗中，伙伴替换模板
	public static int[] lootMapSingleSize; // 单人本尺寸
	public static int[] lootMapMultipleSize; // 多人本尺寸
	public static int lootMapLootScale = 1; // 抢夺比例
	public static int lootMapKillScale = 1; // 人头比例
	public static int lootMapBattleHpCost = 1; // 抢夺花费Hp
	public static int lootMapBattleWinHpChange = 1; // 战后胜利hp改变
	public static int lootMapBattleFailHpChange = 1; // 战后失败hp改变
	public static int[] lootMapTimeModeTime = {0,200,400}; // 时间模式 时间
	public static int[] lootMapTimeModeType = {0,4,3}; // 时间模式 类型
	public static int[] lootMapScore = {2000,3000,5000}; // 积分奖励 积分
	public static int[] lootMapScoreReward = {149001,149002,149003}; // 积分奖励 奖励
	public static int lootMapSignUpQueueTime = 1; // 报名队列超时时间
	public static int lootMapMultipleTime = 1; // 多人玩法整体时间
	public static int lootMapSignUpMinHumanNumber = 1; // 多人报名需求最少人数 超时
	public static int lootMapSignUpMaxHumanNumber = 1; // 多人报名需求最多人数
	public static int lootMapEggLevelTime = 60; // 彩蛋关卡时间
	public static int lootMapRoomTime = 3600; // 抢夺本一个房间最多时间
	public static int lootMapHumanMinAttack = 1; // 抢夺本玩家最低攻击力
	public static int lootMapHumanMaxAttack = 10; // 抢夺本玩家最高攻击力
	public static int lootMapHumanMaxHp = 100; // 抢夺本玩家最高生命值
	public static int lootMapWaitRevivalTime = 30; // 抢夺本复活等待时间 
	public static int lootMapStartPortectedTime = 10; // 抢夺本开始保护时间
	public static int lootMapBattleWintPortectedTime = 5; // 抢夺本战胜保护时间
	public static int lootMapBattleFailtPortectedTime = 10; // 抢夺本失败保护时间
	public static int[] lootMapGreenItem = {100,10,35}; // pk结束 抢夺到绿色的内容
	public static int[] lootMapBlueItem = {100,10,40}; // pk结束 抢夺到蓝色的内容
	public static int[] lootMapPurpleItem = {100,10,30}; // pk结束 抢夺到紫色的内容
	public static int[] lootMapOrangeItem = {100,10,20}; // pk结束 抢夺到橙色的内容
	public static int[] lootMapRedItem = {100,10,10}; // pk结束 抢夺到橙色的内容
	public static int lootMapIntoPkTime = 5;// 进入pk倒计时
	public static int lootMapMoveStepTime = 266; // 每一步移动时间毫秒
	public static int[] lootMapCombatFrequencyLimit = {10,10}; // 抢夺与被抢夺次数
	public static int initHumanLootMapSingleKey = 4; // 初始化抢夺本钥匙
	public static int initHumanLootMapMultipleKey = 2; // 初始化抢夺本钥匙
	public static int initLootMapStartScore = 1000; // 初始化抢夺本多人积分
	public static int lootMapStartPos = 5; // 抢夺本门与人互斥范围(人不能出现在这个范围内)
	public static int[] lootMapPvpStartCombatDate ={10,100}; // 多人初始攻击血量

	/*工会部分*/
	public static int applyLimitTime = 1;//加入公会的CD时间
	public static int guildImmoLogMax = 100;//最大日志数量
	public static int guildExpendGlod = 1;//创建公会所需钻石
	public static int createGuildLevel = 1;//创建公会所需等级
	public static int guildNameLength = 50;//工会名字长度限制
	public static int guildRename = 1;//工会改名消耗钻石数量
	public static int offlineTimeLimit = 14;//会长离线时间
	public static int addGuildLimitTime = 1;//退会后冷却时间
	public static String guildDeclare;//默认工会宣告
	public static String guildNotice;//默认内部公告
	public static int selectGuildPageSize = 10;//工会每页第几天数据
	public static int guildMemberLimit = 20;//申请次数上限
	/*仙域相关*/
	public static int caveMapSn = 9;//instStage表sn
	public static int domainUnitIncome = 60;//结算分钟数
	public static int domainGrabRatio = 500;//仙域之争 - 抢夺比例，万分比
	public static int domaincaveNumLimit = 2;//仙域最高占领个数
	public static int lootMapMultipleJoinCount = 10;
	public static int domainBeGrabLimit	= 3;//被抢夺次数上限
	/*普通占领消耗*/
	public static List<ProduceVo> domainOccupyBase = new ArrayList<ProduceVo>();
	/*普通抢夺消耗*/
	public static List<ProduceVo> domainGrabBase = new ArrayList<ProduceVo>();
	/*高级占领消耗*/
	public static List<ProduceVo> domainOccupyHigh = new ArrayList<ProduceVo>();
	/*高级抢夺消耗*/
	public static List<ProduceVo> domainGrabHigh = new ArrayList<ProduceVo>();
	
	public static int domainDailyOccupyReset = 1;//仙域之争 - 每日获得的开采令
	public static int domainDailyGrabReset = 1;//仙域之争 - 每日获得的抢夺令
	public static Map<Integer,Integer> domainGuildAddRatio = new HashMap<>();//仙域之争 - 仙盟加成比例
	
	// 道具相关
	public static List<Integer> universalPartnerList = new ArrayList<>(); // 可使用万能神将碎片的资质
	public static List<Integer> universalPartnerConversion = new ArrayList<>(); // 万能神将碎片与伙伴碎片的换算比例,针对上行记录的资质
	public static List<Integer> universalGodConversion = new ArrayList<>(); // 万能灵兽碎片与灵兽碎片的换算比例（朱雀,青龙,白虎,玄武）
	public static int universalPartnerItem = 133013; // 万能神将碎片SN
	public static int universalGodItem  = 133014; // 万能灵兽碎片SN


//	public static int []  costItemSn = {2,2};//额外奖励延长消耗物品Num
//	public static int [] costItemNum = {10,10};//额外奖励延长消耗物品Num
	public static ParamManager inst() {
		return inst(ParamManager.class);
	}
		
	/**
	 * 重新加载所有参数配置
	 */
	public static void reloadAllParam() {
		reloadParamSpecials();// 重新加载参数：特殊功能配置
		reloadParamDailyHourReset();// 重新加载参数：每日重置相关配置
		reloadParamHumanNewbie();// 重新加载参数：人物新手配置
		reloadParamHumanInit();// 重新加载参数：人物初始配置
		reloadParamHumanCombat();// 重新加载参数：人物战斗配置
		reloadParamHumanOther();// 重新加载参数：人物其他配置
		reloadParamSkill();// 重新加载参数：技能及技能点配置
		reloadParamRank();// 重新加载参数：排行榜配置
		reloadParamCompete();// 重新加载参数：竞技场配置
		reloadParamWorldBoss();//重新加载参数：世界boss配置
		reloadParamTower();// 重新加载参数：爬塔配置
		reloadParamMainCity();// 重新加载参数：主城相关配置
		reloadParamMail();// 重新加载参数：邮件配置
		reloadParamChat();// 重新加载参数：聊天配置
		reloadParamFriends();//重新加载参数：好友相关配置
		reloadParamRune();//重新加载参数：命格
		reloadParamLootMap(); // 重新加载参数：抢夺本
		reloadParamGuild();//重新加载参数：工会
		reloadParamItem();// 重新加载参数：道具
	}
	/**
	 * 重新加载参数：工会
	 */
	private static void reloadParamGuild() {
		if (ConfParam.containsKey("guildExpendGlod")) {
			guildExpendGlod = Utils.intValue(ConfParam.get("guildExpendGlod").value);
		}
		if (ConfParam.containsKey("createGuildLevel")) {
			createGuildLevel = Utils.intValue(ConfParam.get("createGuildLevel").value);
		}
		if (ConfParam.containsKey("guildMemberLimit")) {
			guildMemberLimit = Utils.intValue(ConfParam.get("guildMemberLimit").value);
		}
		if(ConfParam.containsKey("domainOccupyConsume")){
			String str = ConfParam.get("domainOccupyConsume").value;
			String [] str_arr = str.split(",");
			//基础消耗
			int baseSn = Integer.valueOf(str_arr[0].split("\\|")[0]);
			int baseItem = Integer.valueOf(str_arr[0].split("\\|")[1]);
			domainOccupyBase.clear();
			domainOccupyBase.add(new ProduceVo(baseSn,baseItem));
			//高级消耗
			int highSn = Integer.valueOf(str_arr[1].split("\\|")[0]);
			int highNum = Integer.valueOf(str_arr[1].split("\\|")[1]);
			domainOccupyHigh.clear();
			domainOccupyHigh.add(new ProduceVo(baseSn,baseItem));
			domainOccupyHigh.add(new ProduceVo(highSn,highNum));
		}
		if(ConfParam.containsKey("domainGrabConsume")){
			String str = ConfParam.get("domainGrabConsume").value;
			String [] str_arr = str.split(",");
			//基础消耗
			int baseSn = Integer.valueOf(str_arr[0].split("\\|")[0]);
			int baseItem = Integer.valueOf(str_arr[0].split("\\|")[1]);
			domainGrabBase.clear();
			domainGrabBase.add(new ProduceVo(baseSn,baseItem));
			//高级消耗
			int highSn = Integer.valueOf(str_arr[1].split("\\|")[0]);
			int highNum = Integer.valueOf(str_arr[1].split("\\|")[1]);
			domainGrabHigh.clear();
			domainGrabHigh.add(new ProduceVo(baseSn,baseItem));
			domainGrabHigh.add(new ProduceVo(highSn,highNum));
		}
		// 仙盟加成比例
		if(ConfParam.containsKey("domainGuildAddRatio")){
			String str = ConfParam.get("domainGuildAddRatio").value;
			String [] str_arr = str.split(",");
			
			for(String s:str_arr) {
				int humanNum = Integer.valueOf(s.split("\\|")[0]);
				int addNum = Integer.valueOf(s.split("\\|")[1]);
				domainGuildAddRatio.put(humanNum, addNum);
			}
			
		}
//		}
//		if(ConfParam.containsKey("domainOccupyConsumeItemSn")){
//			domainOccupyConsumeItemSn = Utils.strToIntArray(ConfParam.get("domainOccupyConsumeItemSn").value);
//		}
//		if(ConfParam.containsKey("domainOccupyConsumeItemNum")){
//			domainOccupyConsumeItemNum = Utils.strToIntArray(ConfParam.get("domainOccupyConsumeItemNum").value);
//		}
//		if(ConfParam.containsKey("domainGrabConsumeItemSn")){
//			domainGrabConsumeItemSn = Utils.strToIntArray(ConfParam.get("domainGrabConsumeItemSn").value);
//		}
		if(ConfParam.containsKey("guildLeaderTurnExecuteDay")){
			offlineTimeLimit = Utils.intValue(ConfParam.get("guildLeaderTurnExecuteDay").value);
		}
		if (ConfParam.containsKey("domainDailyOccupyReset")) {
			domainDailyOccupyReset = Utils.intValue(ConfParam.get("domainDailyOccupyReset").value);
		}
		if (ConfParam.containsKey("domainDailyGrabReset")) {
			domainDailyGrabReset = Utils.intValue(ConfParam.get("domainDailyGrabReset").value);
		}
		if (ConfParam.containsKey("guildAddLimitTime")) {
			addGuildLimitTime = Utils.intValue(ConfParam.get("guildAddLimitTime").value);
		}
		
//		if(ConfParam.containsKey("costItemSn")){
//			costItemSn = Utils.strToIntArray(ConfParam.get("costItemSn").value);
//		}
//		if(ConfParam.containsKey("costItemNum")){
//			costItemNum = Utils.strToIntArray(ConfParam.get("costItemNum").value);
//		}
	}
	/**
	 * 重新加载参数：特殊功能配置
	 */
	private static void reloadParamSpecials() {
		if (ConfParam.containsKey("openCheckVersion")) {
			int open = Utils.intValue(ConfParam.get("openCheckVersion").value);
			openCheckVersion = (open==1)?true:false;// 开启检查版本号（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("newlyVersion")) {
			String version = ConfParam.get("newlyVersion").value;
			newlyVersion = Utils.getVersionNum(version);
		}
		if (ConfParam.containsKey("loginMaxOnline")) {
			loginMaxOnline = Utils.intValue(ConfParam.get("loginMaxOnline").value);
		}
		if (ConfParam.containsKey("loginMaxQueue")) {
			loginMaxQueue = Utils.intValue(ConfParam.get("loginMaxQueue").value);
		}
		if (ConfParam.containsKey("loginTimeFull")) {
			loginTimeFull = Utils.intValue(ConfParam.get("loginTimeFull").value);
		}
		if (ConfParam.containsKey("loginIntervalTips")) {
			loginIntervalTips = Utils.intValue(ConfParam.get("loginIntervalTips").value);
		}
		if (ConfParam.containsKey("loginIntervalClear")) {
			loginIntervalClear = Utils.intValue(ConfParam.get("loginIntervalClear").value);
		}
		if (ConfParam.containsKey("closeDelaySec")) {
			closeDelaySec = Utils.intValue(ConfParam.get("closeDelaySec").value);
			// 断线重连秒数：默认300秒（限制范围[30,600],不在范围内则置为默认值300秒）
			if (closeDelaySec < 30 || closeDelaySec > 600) {
				closeDelaySec = 300;
			}
			//System.out.println("reloadParamSpecials() closeDelaySec="+closeDelaySec);
		}
		
		if (ConfParam.containsKey("openReloadData")) {
			int open = Utils.intValue(ConfParam.get("openReloadData").value);
			openReloadData = (open==1)?true:false;// 开启热加载数据文件（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("openReloadClass")) {
			int open = Utils.intValue(ConfParam.get("openReloadClass").value);
			openReloadClass = (open==1)?true:false;// 开启热加载类文件（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("openGM")) {
			int open = Utils.intValue(ConfParam.get("openGM").value);
			openGM = (open==1)?true:false;// 开启GM命令（1：开启，0：关闭）
		}
		
		if (ConfParam.containsKey("openShowService")) {
			int open = Utils.intValue(ConfParam.get("openShowService").value);
			openShowService = (open==1)?true:false;// 开启联系客服（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("openActiveKey")) {
			int open = Utils.intValue(ConfParam.get("openActiveKey").value);
			openActiveKey = (open==1)?true:false;// 开启激活码（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("openGiftKey")) {
			int open = Utils.intValue(ConfParam.get("openGiftKey").value);
			openGiftKey = (open==1)?true:false;// 开启礼包激活码（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("openChatUpload")) {
			int open = Utils.intValue(ConfParam.get("openChatUpload").value);
			openChatUpload = (open==1)?true:false;// 开启聊天上传到聊天服务器（1：开启，0：关闭）
		}
		if (ConfParam.containsKey("chatMonitor")) {
			// 监测聊天规则：等级,VIP等级,最大发言数,重置秒数,最大字数
			chatMonitor = Utils.arrayStrToInt(ConfParam.get("chatMonitor").value);
		}
		if (ConfParam.containsKey("dropCountRepeatNum")) {
			dropCountRepeatNum = Utils.intValue(ConfParam.get("dropCountRepeatNum").value);
		}
	}
	
	/**
	 * 每日重置相关配置
	 */
	private static void reloadParamDailyHourReset() {
		if (ConfParam.containsKey("dailyHourReset")) {
			dailyHourReset = Utils.intValue(ConfParam.get("dailyHourReset").value);
		}
		if (ConfParam.containsKey("dailyCoinBuyValue")) {
			dailyCoinBuyValue = Utils.intValue(ConfParam.get("dailyCoinBuyValue").value);
		}
		if (ConfParam.containsKey("dailyActBuyValue")) {
			dailyActBuyValue = Utils.intValue(ConfParam.get("dailyActBuyValue").value);
		}
	}

	/**
	 * 重新加载参数：人物新手配置
	 */
	private static void reloadParamHumanNewbie() {
		if (ConfParam.containsKey("guideClose")) {
			guideClose = Utils.intValue(ConfParam.get("guideClose").value);
		}
		if (ConfParam.containsKey("firstMapSn")) {
			firstMapSn = Utils.intValue(ConfParam.get("firstMapSn").value);
		}
		if (ConfParam.containsKey("newbieMapSn")) {
			newbieMapSn = Utils.intValue(ConfParam.get("newbieMapSn").value);
		}
		if (ConfParam.containsKey("newbieStageSn")) {
			newbieStageSn = Utils.intValue(ConfParam.get("newbieStageSn").value);
		}
	}

	/**
	 * 重新加载参数：人物初始配置
	 */
	private static void reloadParamHumanInit() {
		// 最大人物等级
		if (ConfParam.containsKey("maxHumanLevel")) {
			maxHumanLevel = Utils.intValue(ConfParam.get("maxHumanLevel").value);
			if (null == ConfLevelExp.get(maxHumanLevel)) { 
				maxHumanLevel = getMaxLevelExp();
			}
		} else {
			maxHumanLevel = getMaxLevelExp();
		}
		// 初始人物等级
		if (ConfParam.containsKey("initHumanLevel")) {
			initHumanLevel = Utils.intValue(ConfParam.get("initHumanLevel").value);
		}
		if (ConfParam.containsKey("lootMapMultipleJoinCount")) {
			lootMapMultipleJoinCount = Utils.intValue(ConfParam.get("lootMapMultipleJoinCount").value);
		}
		
		if (ConfParam.containsKey("initHumanSpeed")) {
			initHumanSpeed = Utils.intValue(ConfParam.get("initHumanSpeed").value);
		}
		if (ConfParam.containsKey("cdHumanAct")) {
			cdHumanAct = Utils.intValue(ConfParam.get("cdHumanAct").value);
		}
		if (ConfParam.containsKey("initHumanAct")) {
			initHumanAct = Utils.intValue(ConfParam.get("initHumanAct").value);
		}
		if (ConfParam.containsKey("initHumanCoin")) {
			initHumanCoin = Utils.intValue(ConfParam.get("initHumanCoin").value);
		}
		if (ConfParam.containsKey("initHumanGold")) {
			initHumanGold = Utils.intValue(ConfParam.get("initHumanGold").value);
		}
		// 初始背包格子数
		if (ConfParam.containsKey("maxItemBagNum")) {
			maxItemBagNum = Utils.intValue(ConfParam.get("maxItemBagNum").value);
		}
		// 初始人物携带装备sn
		if (ConfParam.containsKey("initHumanEquip")) {
			initHumanEquip = Utils.strToIntArray(ConfParam.get("initHumanEquip").value);
		}
		// 初始人物携带装备强化等级
		if (ConfParam.containsKey("initHumanEquipLv")) {
			initHumanEquipLv = Utils.intValue(ConfParam.get("initHumanEquipLv").value);
		}
		// 初始人物携带套装sn 
		if (ConfParam.containsKey("fashionInitialSuitId")) {
			fashionInitialSuitId = Utils.intValue(ConfParam.get("fashionInitialSuitId").value);
		}
		// 初始携带伙伴sn（数组方式填写-1表示主角）
		if (ConfParam.containsKey("initPartnerSn")) {
			initPartnerSn = ConfParam.get("initPartnerSn").value;
		}
		//允许伙伴最大的数量
		if (ConfParam.containsKey("partnerMaxNum")) {
			partnerMaxNum = Utils.intValue(ConfParam.get("partnerMaxNum").value);
		}
		// 初始伙伴站位阵型（0-W型"前3后2"；1-M型"前2后3"）
		if (ConfParam.containsKey("initPartnerStance")) {
			initPartnerStance = Utils.intValue(ConfParam.get("initPartnerStance").value);
		}
		// 自动上阵的等级区间
		if (ConfParam.containsKey("cardRecruitEmbattlePartner")) {
			recruitAutoLineUpLvRange = Utils.strToIntArray(ConfParam.get("cardRecruitEmbattlePartner").value);
		}
		// 布阵-1~5各阵位解锁等级（W型"前3后2"；M型"前2后3"）
		if (ConfParam.containsKey("arrayUnlock")) {
			arrayUnlock = Utils.strToIntArray(ConfParam.get("arrayUnlock").value);
		}
		// 高级招募一次获得的仙缘
		if (ConfParam.containsKey("cardGetGodFate")) {
			cardGetGodFate = Utils.intValue(ConfParam.get("cardGetGodFate").value);
		}
		// 普通招募一次获得的积分
		if (ConfParam.containsKey("cardOnceNormalScore")) {
			cardOnceNormalScore = Utils.strToIntArray(ConfParam.get("cardOnceNormalScore").value);
		}
		// 高级招募一次获得的积分
		if (ConfParam.containsKey("cardOnceHigherScore")) {
			cardOnceHigherScore = Utils.strToIntArray(ConfParam.get("cardOnceHigherScore").value);
		}
		// 招募积分兑换最大轮数
		if (ConfParam.containsKey("cardExchangeMaxRound")) {
			cardExchangeMaxRound = Utils.intValue(ConfParam.get("cardExchangeMaxRound").value);
		}
		
	}

	/**
	 * 重新加载参数：人物战斗配置
	 */
	private static void reloadParamHumanCombat() {
		// 战斗相关配置
		if (ConfParam.containsKey("hitParam")) {
			hitParam = Utils.intValue(ConfParam.get("hitParam").value);
		}
		if (ConfParam.containsKey("hitParamA")) {
			hitParamA = Utils.intValue(ConfParam.get("hitParamA").value);
		}
		if (ConfParam.containsKey("hitParamB")) {
			hitParamB = Utils.intValue(ConfParam.get("hitParamB").value);
		}
		if (ConfParam.containsKey("hitParamMin")) {
			hitParamMin = Utils.intValue(ConfParam.get("hitParamMin").value);
		}
		
		if (ConfParam.containsKey("critAddParamD")) {
			critAddParamD = Utils.intValue(ConfParam.get("critAddParamD").value);
		}
		if (ConfParam.containsKey("critAddParamE")) {
			critAddParamE = Utils.intValue(ConfParam.get("critAddParamE").value);
		}
		if (ConfParam.containsKey("critAddParamF")) {
			critAddParamF = Utils.floatValue(ConfParam.get("critAddParamF").value);
		}
		if (ConfParam.containsKey("critAddParam")) {
			critAddParam = Utils.intValue(ConfParam.get("critAddParam").value);
		}
		if (ConfParam.containsKey("critAddParamMax")) {
			critAddParamMax = Utils.intValue(ConfParam.get("critAddParamMax").value);
		}
		if (ConfParam.containsKey("critAddParamMin")) {
			critAddParamMin = Utils.intValue(ConfParam.get("critAddParamMin").value);
		}
		
		if (ConfParam.containsKey("controlParamG")) {
			controlParamG = Utils.intValue(ConfParam.get("controlParamG").value);
		}
		if (ConfParam.containsKey("controlParamH")) {
			controlParamH = Utils.intValue(ConfParam.get("controlParamH").value);
		}
		if (ConfParam.containsKey("controlParamI")) {
			controlParamI = Utils.doubleValue(ConfParam.get("controlParamI").value);
		}
		if (ConfParam.containsKey("controlParamMax")) {
			controlParamMax = Utils.intValue(ConfParam.get("controlParamMax").value);
		}
		if (ConfParam.containsKey("controlParamMin")) {
			controlParamMin = Utils.intValue(ConfParam.get("controlParamMin").value);
		}
		
		if (ConfParam.containsKey("battleParryParam")) {
		    battleBlockParam = Utils.intValue(ConfParam.get("battleParryParam").value);
		}
		if (ConfParam.containsKey("battleEnergyReply")) {
		    battleMpRecovery = Utils.intValue(ConfParam.get("battleEnergyReply").value);
		}
		if (ConfParam.containsKey("battleAngerParam")) {
		    String[] value = Utils.strToStrArray(ConfParam.get("battleAngerParam").value);
		    if (value.length >= 2) {
		        battleAngerInit = Utils.intValue(value[0]);
		        battleAngerMax = Utils.intValue(value[1]);
		    }
		}
		if (ConfParam.containsKey("battleAngerReply1")) {
		    battleAngerRecoverySkill = Utils.intValue(ConfParam.get("battleAngerReply1").value);
		}
		if (ConfParam.containsKey("battleAngerReply2")) {
		    battleAngerRecoveryMonsterDie = Utils.intValue(ConfParam.get("battleAngerReply2").value);
		}
		if (ConfParam.containsKey("battleAngerReply3")) {
		    battleAngerRecoveryEnemyDie = Utils.intValue(ConfParam.get("battleAngerReply3").value);
		}
		if (ConfParam.containsKey("battleAngerReply4")) {
		    battleAngerRecoveryOurDie = Utils.intValue(ConfParam.get("battleAngerReply4").value);
		}
		if (ConfParam.containsKey("battleAngerReply5")) {
		    battleAngerRecoveryPveRound = Utils.intValue(ConfParam.get("battleAngerReply5").value);
		}
		if (ConfParam.containsKey("battleAngerReply6")) {
		    battleAngerRecoveryPvpRound = Utils.intValue(ConfParam.get("battleAngerReply6").value);
		}
		if (ConfParam.containsKey("battleAngerReply7")) {
		    battleAngerRecoveryGvGRound = Utils.intValue(ConfParam.get("battleAngerReply7").value);
		}
		if (ConfParam.containsKey("battlePveRoundLimit")) {
		    battlePveRoundLimit = Utils.intValue(ConfParam.get("battlePveRoundLimit").value);
		}
		if (ConfParam.containsKey("battlePvpRoundLimit")) {
		    battlePvpRoundLimit = Utils.intValue(ConfParam.get("battlePvpRoundLimit").value);
		}
		if (ConfParam.containsKey("battleAttackLimit")) {
		    battleAttackLimit = Utils.intValue(ConfParam.get("battleAttackLimit").value);
		}
		if (ConfParam.containsKey("battleTreatmentCritParam")) {
		    battleTreatmentCritParam = Utils.intValue(ConfParam.get("battleTreatmentCritParam").value);
		}
		if (ConfParam.containsKey("battleSkillCostHpParam")) {
		    battleSkillCostHpParam = Utils.intValue(ConfParam.get("battleSkillCostHpParam").value);
		}
		
		if (ConfParam.containsKey("PVEHumanCastTime")) {
			PVEHumanCastTime = Utils.intValue(ConfParam.get("PVEHumanCastTime").value);
		}
		if (ConfParam.containsKey("PVPHumanCastTime")) {
			PVPHumanCastTime = Utils.intValue(ConfParam.get("PVPHumanCastTime").value);
		}
		if (ConfParam.containsKey("combatCapabilityRepressio")) {
			combatCapabilityRepressio = Utils.strToIntList(ConfParam.get("combatCapabilityRepressio").value);
		}
		
		// 属性转化战力参数配置
		if (ConfParam.containsKey("combatHpMax")) {
	      combatHpMax = Utils.doubleValue(ConfParam.get("combatHpMax").value);
	    }
	    if (ConfParam.containsKey("combatRageMax")) {
	      combatRageMax = Utils.doubleValue(ConfParam.get("combatRageMax").value);
	    }
	    if (ConfParam.containsKey("combatAtk")) {
	      combatAtk = Utils.doubleValue(ConfParam.get("combatAtk").value);
	    }
	    if (ConfParam.containsKey("combatDefPhy")) {
	      combatDefPhy = Utils.doubleValue(ConfParam.get("combatDefPhy").value);
	    }
	    if (ConfParam.containsKey("combatDefMag")) {
	      combatDefMag = Utils.doubleValue(ConfParam.get("combatDefMag").value);
	    }
	    if (ConfParam.containsKey("combatHit")) {
	      combatHit = Utils.doubleValue(ConfParam.get("combatHit").value);
	    }
	    if (ConfParam.containsKey("combatDodge")) {
	      combatDodge = Utils.doubleValue(ConfParam.get("combatDodge").value);
	    }
	    if (ConfParam.containsKey("combatCrit")) {
	      combatCrit = Utils.doubleValue(ConfParam.get("combatCrit").value);
	    }
	    if (ConfParam.containsKey("combatAntiCrit")) {
	      combatAntiCrit = Utils.doubleValue(ConfParam.get("combatAntiCrit").value);
	    }
	    if (ConfParam.containsKey("combatBlock")) {
	      combatBlock = Utils.doubleValue(ConfParam.get("combatBlock").value);
	    }
	    if (ConfParam.containsKey("combatAntiBlock")) {
	      combatAntiBlock = Utils.doubleValue(ConfParam.get("combatAntiBlock").value);
	    }
	    if (ConfParam.containsKey("combatDamAdd")) {
	      combatDamAdd = Utils.doubleValue(ConfParam.get("combatDamAdd").value);
	    }
	    if (ConfParam.containsKey("combatDamAddEx")) {
	      combatDamAddEx = Utils.doubleValue(ConfParam.get("combatDamAddEx").value);
	    }
	    if (ConfParam.containsKey("combatDamRed")) {
	      combatDamRed = Utils.doubleValue(ConfParam.get("combatDamRed").value);
	    }
	    if (ConfParam.containsKey("combatDamRedEx")) {
	      combatDamRedEx = Utils.doubleValue(ConfParam.get("combatDamRedEx").value);
	    }
	    if (ConfParam.containsKey("combatPenePhy")) {
	      combatPenePhy = Utils.doubleValue(ConfParam.get("combatPenePhy").value);
	    }
	    if (ConfParam.containsKey("combatPeneMag")) {
	      combatPeneMag = Utils.doubleValue(ConfParam.get("combatPeneMag").value);
	    }
	    if (ConfParam.containsKey("combatCureAdd")) {
	      combatCureAdd = Utils.doubleValue(ConfParam.get("combatCureAdd").value);
	    }
	    if (ConfParam.containsKey("combatCureAddEx")) {
	      combatCureAddEx = Utils.doubleValue(ConfParam.get("combatCureAddEx").value);
	    }
	    if (ConfParam.containsKey("combatHealAdd")) {
	      combatHealAdd = Utils.doubleValue(ConfParam.get("combatHealAdd").value);
	    }
	    if (ConfParam.containsKey("combatCritAdd")) {
	      combatCritAdd = Utils.doubleValue(ConfParam.get("combatCritAdd").value);
	    }
	    if (ConfParam.containsKey("combatAntiCritAdd")) {
	      combatAntiCritAdd = Utils.doubleValue(ConfParam.get("combatAntiCritAdd").value);
	    }
	}
	
	/**
	 * 重新加载参数：人物其他配置
	 */
	private static void reloadParamHumanOther() {
		if (ConfParam.containsKey("reviveAddBuff")) {
			reviveAddBuff = Utils.strToIntArray(ConfParam.get("reviveAddBuff").value);
		}
		if (ConfParam.containsKey("changeNameCostGold")) {
			changeNameCostGold = Utils.intValue(ConfParam.get("changeNameCostGold").value);
		}
		if (ConfParam.containsKey("maxVipLv")) {
			maxVipLv = Utils.intValue(ConfParam.get("maxVipLv").value);
		}
		if (ConfParam.containsKey("maxHumanNameLength")) {
			maxHumanNameLength = Utils.intValue(ConfParam.get("maxHumanNameLength").value);
		}
		if (ConfParam.containsKey("instResChallengeTimes")) {
			instResChallengeTimes = Utils.intValue(ConfParam.get("instResChallengeTimes").value);
		}
	}
	
	/**
	 * 重新加载参数配置：技能及技能点配置
	 */
	private static void reloadParamSkill() {
		if (ConfParam.containsKey("rageMax")) {
			rageMax = Utils.intValue(ConfParam.get("rageMax").value);
		}
		if (ConfParam.containsKey("backDisMax")) {
			backDisMax = Utils.intValue(ConfParam.get("backDisMax").value);
		}
		if (ConfParam.containsKey("skillSlotUnlock")) {
			skillSlotUnlock = Utils.strToIntArray(ConfParam.get("skillSlotUnlock").value);
			skillMaxInstall = skillSlotUnlock.length - 1;
		}
		if (ConfParam.containsKey("skillLevelLimit")) {
			skillLevelLimit = Utils.intValue(ConfParam.get("skillLevelLimit").value);
		}
		if (ConfParam.containsKey("skillUpgradingTime")) {
			skillUpgradingTime = Utils.intValue(ConfParam.get("skillUpgradingTime").value);
		}
		if (ConfParam.containsKey("godSkillLevelLimit")) {
			skillGodsAddLevelQm = Utils.intValue(ConfParam.get("godSkillLevelLimit").value);
		}
		if (ConfParam.containsKey("skillTrainCost")) {
			skillTrainCost = Utils.strToIntArray(ConfParam.get("skillTrainCost").value);
		} 
		if (ConfParam.containsKey("skillTrainDouble")) {
			skillTrainDouble = Utils.intValue(ConfParam.get("skillTrainDouble").value);
		} 
		if (ConfParam.containsKey("skillTrainTriple")) {
			skillTrainTriple = Utils.intValue(ConfParam.get("skillTrainTriple").value);
		} 
		if (ConfParam.containsKey("skillTrainReset")) {
			skillTrainReset = Utils.strToIntArray(ConfParam.get("skillTrainReset").value);
		} 
		if (ConfParam.containsKey("skillTrainCostRate")) {
			skillTrainCostRate = Utils.strToStrArray(ConfParam.get("skillTrainCostRate").value);
		} 
		if (ConfParam.containsKey("skillTrainType")) {
			skillTrainType = Utils.strToIntArray(ConfParam.get("skillTrainType").value);
		} 
	}
	
	/**
	 * 重新加载参数配置：排行榜配置
	 */
	private static void reloadParamRank() {
		if (ConfParam.containsKey("rankTopShowNum")) {
			rankTopShowNum = Utils.strToIntArray(ConfParam.get("rankTopShowNum").value);
		}
		if (ConfParam.containsKey("rankTopRecordFilter")) {
			rankTopRecordFilter = Utils.strToIntArray(ConfParam.get("rankTopRecordFilter").value);
		}
		if (ConfParam.containsKey("rankTopRecordMaxNum")) {
			rankTopRecordMaxNum = Utils.strToIntArray(ConfParam.get("rankTopRecordMaxNum").value);
		}
		if (ConfParam.containsKey("rankTopRewardSn")) {
			rankTopRewardSn = ConfParam.get("rankTopRewardSn").value;
		}
		if (ConfParam.containsKey("rankTopRewardNum")) {
			rankTopRewardNum = ConfParam.get("rankTopRewardNum").value;
		}
		if (ConfParam.containsKey("rankWorshipTimes")) {
			rankWorshipTimes = Utils.intValue(ConfParam.get("rankWorshipTimes").value);
		}
	}

	/**
	 * 重新加载参数配置：竞技场配置
	 */
	private static void reloadParamCompete() {
		if (ConfParam.containsKey("competeRankMax")) {
			competeRankMax = Utils.intValue(ConfParam.get("competeRankMax").value);
		}
		if (ConfParam.containsKey("competeGradeRange")) {
			competeGradeRange = Utils.intValue(ConfParam.get("competeGradeRange").value);
		}
		if (ConfParam.containsKey("competeRobotNum")) {
			competeRobotNum = Utils.intValue(ConfParam.get("competeRobotNum").value);
		}
		if (ConfParam.containsKey("competeRobotAI")) {
			competeRobotAI = Utils.intValue(ConfParam.get("competeRobotAI").value);
		}
		if (ConfParam.containsKey("competeRankTop")) {
			competeRankTop = Utils.intValue(ConfParam.get("competeRankTop").value);
		}
		if (ConfParam.containsKey("competeFreeNum")) {
			competeFreeNum = Utils.intValue(ConfParam.get("competeFreeNum").value);
		}
		if (ConfParam.containsKey("competeBuyNumAdd")) {
			competeBuyNumAdd = Utils.intValue(ConfParam.get("competeBuyNumAdd").value);
		}
		if (ConfParam.containsKey("competeBuyNumVip")) {
			competeBuyNumVip = Utils.strToIntArray(ConfParam.get("competeBuyNumVip").value);
		}
		if(ConfParam.containsKey("competePickFormula")){
			String[] value = Utils.strToStrArray(ConfParam.get("competePickFormula").value);
			competeParamA = Integer.parseInt(value[0]);
			competeParamB = Integer.parseInt(value[1]);
		}
		// if(ConfParam.containsKey("competeRewardStartHour")) {
		// competeRewardStartHour =
		// Utils.intValue(ConfParam.get("competeRewardStartHour").value);
		// }
		// if(ConfParam.containsKey("competeRewardLastMinute")) {
		// competeRewardLastMinute =
		// Utils.intValue(ConfParam.get("competeRewardLastMinute").value);
		// }
		if (ConfParam.containsKey("competeMapSn")) {
			competeMapSn = Utils.intValue(ConfParam.get("competeMapSn").value);
		}
		if (ConfParam.containsKey("caveMapSn")) {
			competeMapSn = Utils.intValue(ConfParam.get("caveMapSn").value);
		}
		
		if (ConfParam.containsKey("competeFightTime")) {
			competeFightTime = Utils.intValue(ConfParam.get("competeFightTime").value);
		}
//		if (ConfParam.containsKey("competeFightAwardItemSn")) {
//			competeFightAwardItemSn = Utils.intValue(ConfParam.get("competeFightAwardItemSn").value);
//		}
//		if (ConfParam.containsKey("competeFightAwardItemNumSucc")) {
//			competeFightAwardItemNumSucc = Utils.intValue(ConfParam.get("competeFightAwardItemNumSucc").value);
//		}
//		if (ConfParam.containsKey("competeFightAwardItemNumFail")) {
//			competeFightAwardItemNumFail = Utils.intValue(ConfParam.get("competeFightAwardItemNumFail").value);
//		}
		if(ConfParam.containsKey("competeIntegral")){
			competeIntegral = Utils.intValue(ConfParam.get("competeIntegral").value);
		}
	}

	/**
	 * 重新加载参数配置：爬塔配置
	 */
	private static void reloadParamTower() {
		if (ConfParam.containsKey("towerMapSn")) {
			towerMapSn = Utils.intValue(ConfParam.get("towerMapSn").value);
		}
		if (ConfParam.containsKey("towerMaxLayer")) {
			towerMaxLayer = Utils.intValue(ConfParam.get("towerMaxLayer").value);
		}
		if (ConfParam.containsKey("towerPartnerMinLv")) {
			towerPartnerMinLv = Utils.intValue(ConfParam.get("towerPartnerMinLv").value);
		}
		if (ConfParam.containsKey("towerInitHaveLife")) {
			towerInitHaveLife = Utils.intValue(ConfParam.get("towerInitHaveLife").value);
		}
		if (ConfParam.containsKey("towerInitCombat")) {
			towerInitCombat = Utils.intValue(ConfParam.get("towerInitCombat").value);
		}
		if (ConfParam.containsKey("towerJoinMultiple")) {
			towerJoinMultiple = Utils.intValue(ConfParam.get("towerJoinMultiple").value);
		}
		if (ConfParam.containsKey("towerBaseJoinNum")) {
			towerBaseJoinNum = Utils.intValue(ConfParam.get("towerBaseJoinNum").value);
		}
		if (ConfParam.containsKey("towerResetting")) {
			towerResetting = Utils.strToIntArray(ConfParam.get("towerResetting").value);
		}
		if (ConfParam.containsKey("towerParam1")) {
			towerParam1 = Utils.intValue(ConfParam.get("towerParam1").value);
		}
		if (ConfParam.containsKey("towerParam2")) {
			towerParam2 = Utils.intValue(ConfParam.get("towerParam2").value);
		}
		if (ConfParam.containsKey("towerSeasonEndDay")) {
			towerSeasonEndDay = Utils.intValue(ConfParam.get("towerSeasonEndDay").value);
		}
		if (ConfParam.containsKey("towerSeasonEndTime")) {
			towerSeasonEndTime = Utils.intValue(ConfParam.get("towerSeasonEndTime").value);
		}
	}
	
	/**
	 * 重新加载参数配置：主城相关配置
	 */
	private static void reloadParamMainCity() {
		if (ConfParam.containsKey("mainCityFightMapSn")) {
			mainCityFightMapSn = Utils.intValue(ConfParam.get("mainCityFightMapSn").value);
		}
		if (ConfParam.containsKey("mainCityRewardCount")) {
			mainCityRewardCount = Utils.intValue(ConfParam.get("mainCityRewardCount").value);
		}
		if (ConfParam.containsKey("mainCityReward")) {
			mainCityReward = Utils.strToIntArray(ConfParam.get("mainCityReward").value);
		}
		if (ConfParam.containsKey("pkMirrorMapSn")) {
			pkMirrorMapSn = Utils.intValue(ConfParam.get("pkMirrorMapSn").value);
		}
		if (ConfParam.containsKey("pkMirrorRewardCount")) {
			pkMirrorRewardCount = Utils.intValue(ConfParam.get("pkMirrorRewardCount").value);
		}
		if (ConfParam.containsKey("pkMirrorReward")) {
			pkMirrorReward = Utils.strToIntArray(ConfParam.get("pkMirrorReward").value);
		}
		
		if (ConfParam.containsKey("pkHumanMapSn")) {
			pkHumanMapSn = Utils.intValue(ConfParam.get("pkHumanMapSn").value);
		}
		if (ConfParam.containsKey("pkHumanRewardCount")) {
			pkHumanRewardCount = Utils.intValue(ConfParam.get("pkHumanRewardCount").value);
		}
		if (ConfParam.containsKey("pkHumanReward")) {
			pkHumanReward = Utils.strToIntArray(ConfParam.get("pkHumanReward").value);
		}
		
	}
	
	/**
	 * 重新加载参数：邮件配置
	 */
	private static void reloadParamMail() {
		if (ConfParam.containsKey("mailMax")) {
			mailMax = Utils.intValue(ConfParam.get("mailMax").value);
		}
		if (ConfParam.containsKey("mailMark")) {
			mailMark = ConfParam.get("mailMark").value;
		}
		if (ConfParam.containsKey("sysMsgMark")) {
			sysMsgMark = ConfParam.get("sysMsgMark").value;
		}
	}
	
	/**
	 * 重新加载参数：聊天配置
	 */
	private static void reloadParamChat() {
		if (ConfParam.containsKey("chatLengthMax")) {
			chatLengthMax = Utils.intValue(ConfParam.get("chatLengthMax").value);
		}
		if (ConfParam.containsKey("chatCDTime")) {
			chatCDTime = Utils.intValue(ConfParam.get("chatCDTime").value);
		}
	}
		
	/**
	 * 重新加载参数：世界boss配置
	 */
	private static void reloadParamWorldBoss() {
		if (ConfParam.containsKey("worldBossShowMaxNum")) {
			worldBossShowMaxNum = Utils.intValue(ConfParam.get("worldBossShowMaxNum").value);
		}
		if (ConfParam.containsKey("worldBossTopShowNum")) {
			worldBossTopShowNum = Utils.intValue(ConfParam.get("worldBossTopShowNum").value);
		}
		if (ConfParam.containsKey("worldBossTopRankNum")) {
			worldBossTopRankNum = Utils.intValue(ConfParam.get("worldBossTopRankNum").value);
		}
		if (ConfParam.containsKey("worldBossFightCD")) {
			worldBossFightCD = Utils.intValue(ConfParam.get("worldBossFightCD").value);
		}
		if (ConfParam.containsKey("worldBossInspireCD")) {
			worldBossInspireCD = Utils.intValue(ConfParam.get("worldBossInspireCD").value);
		}
		if (ConfParam.containsKey("worldBossInspireFreeNum")) {
			worldBossInspireFreeNum = Utils.intValue(ConfParam.get("worldBossInspireFreeNum").value);
		}
		if (ConfParam.containsKey("worldBossInspireMaxNum")) {
			worldBossInspireMaxNum = Utils.intValue(ConfParam.get("worldBossInspireMaxNum").value);
		}
		if (ConfParam.containsKey("worldBossInspireAddAtk")) {
			worldBossInspireAddAtk = Utils.intValue(ConfParam.get("worldBossInspireAddAtk").value);
		}
		if (ConfParam.containsKey("worldBossRebornAddAtk")) {
			worldBossRebornAddAtk = Utils.intValue(ConfParam.get("worldBossRebornAddAtk").value);
		}
		if (ConfParam.containsKey("worldBossDyingRatio")) {
			worldBossDyingRatio = Utils.intValue(ConfParam.get("worldBossDyingRatio").value);
		}
		if (ConfParam.containsKey("worldBossReadySecond")) {
			worldBossReadySecond = Utils.intValue(ConfParam.get("worldBossReadySecond").value);
		}
		if (ConfParam.containsKey("worldBossUpgradeSecond")) {
			worldBossKillSecond = Utils.intValue(ConfParam.get("worldBossUpgradeSecond").value);
		}
		if (ConfParam.containsKey("worldBossDieSecond")) {
			worldBossDieSecond = Utils.intValue(ConfParam.get("worldBossDieSecond").value);
		}
		
	}
	
	/**
	 * 重新加载参数：好友相关配置
	 */
	private static void reloadParamFriends() {
		if (ConfParam.containsKey("friendsRefresh")) {
			friendsRefresh = Utils.intValue(ConfParam.get("friendsRefresh").value);
		}		
		if (ConfParam.containsKey("friendsUpperLimit")) {
			friendsUpperLimit = Utils.intValue(ConfParam.get("friendsUpperLimit").value);
		}		
		if (ConfParam.containsKey("friendsApplyLimit")) {
			friendsApplyLimit = Utils.intValue(ConfParam.get("friendsApplyLimit").value);
		}
		if (ConfParam.containsKey("friendsGiveNumber")) {
			friendsGiveNumber = Utils.intValue(ConfParam.get("friendsGiveNumber").value);
		}
		if (ConfParam.containsKey("friendsReceiveTimes")) {
			friendsReceiveTimes = Utils.intValue(ConfParam.get("friendsReceiveTimes").value);
		}
		if (ConfParam.containsKey("friendsGiveTimes")) {
			friendsGiveTimes = Utils.intValue(ConfParam.get("friendsGiveTimes").value);
		}
		if (ConfParam.containsKey("friendsMessageTime")) {
			friendsMessageTime = Utils.intValue(ConfParam.get("friendsMessageTime").value);
		}
		if (ConfParam.containsKey("friendsBlackUpperLimit")) {
			friendsBlackUpperLimit = Utils.intValue(ConfParam.get("friendsBlackUpperLimit").value);
		}
		if (ConfParam.containsKey("friendsApplyUpperLimit")) {
			friendsApplyUpperLimit = Utils.intValue(ConfParam.get("friendsApplyUpperLimit").value);
		}
	}
	/**
	 * 重新加载参数：命格参数配置
	 */
	private static void reloadParamRune() {
		if (ConfParam.containsKey("divinationLatticeUnlockLv")) {
			divinationLatticeUnlockLvArr = Utils.strToIntArray(ConfParam.get("divinationLatticeUnlockLv").value);
		}
		if (ConfParam.containsKey("divinationCount")) {
			divinationCountArr = Utils.strToIntArray(ConfParam.get("divinationCount").value);
		}
		if (ConfParam.containsKey("divinationBagNum")) {
			divinationBagNum = Utils.intValue(ConfParam.get("divinationBagNum").value);
		}
		if (ConfParam.containsKey("divinationBagActualNum")) {
			divinationBagActualNum = Utils.intValue(ConfParam.get("divinationBagActualNum").value);
		}
	}
	
	/**
	 * 重新加载参数：抢夺本参数配置
	 */
	private static void reloadParamLootMap(){
		if (ConfParam.containsKey("pvpMapSn")) {
			pvpMapSn = Utils.intValue(ConfParam.get("pvpMapSn").value);
		}
		if (ConfParam.containsKey("lootMapPvpRoleSkillLv")) {
			lootMapPvpRoleSkillLv = Utils.intValue(ConfParam.get("lootMapPvpRoleSkillLv").value);
		}
		if (ConfParam.containsKey("lootMapPvpGodSkillLv")) {
			lootMapPvpGodSkillLv = Utils.intValue(ConfParam.get("lootMapPvpGodSkillLv").value);
		}
		if (ConfParam.containsKey("lootMapPvpRoleCombatData")) {
			lootMapPvpRoleCombatData = Utils.strToIntArray(ConfParam.get("lootMapPvpRoleCombatData").value);
		}
		if (ConfParam.containsKey("lootMapPvpPartnerCombatData")) {
			lootMapPvpPartnerCombatData = Utils.strToIntArray(ConfParam.get("lootMapPvpPartnerCombatData").value);
		}
		
		if (ConfParam.containsKey("lootMapSingleSize")) {
			lootMapSingleSize = Utils.strToIntArray(ConfParam.get("lootMapSingleSize").value);
		}
		
		if (ConfParam.containsKey("lootMapMultipleSize")) {
			lootMapMultipleSize = Utils.strToIntArray(ConfParam.get("lootMapMultipleSize").value);
		}
		if (ConfParam.containsKey("lootMapLootScale")) {
			lootMapLootScale = Utils.intValue(ConfParam.get("lootMapLootScale").value);
		}
		if (ConfParam.containsKey("lootMapKillScale")) {
			lootMapKillScale = Utils.intValue(ConfParam.get("lootMapKillScale").value);
		}
		if (ConfParam.containsKey("domainUnitIncome")) {
			lootMapKillScale = Utils.intValue(ConfParam.get("domainUnitIncome").value);
		}
		if (ConfParam.containsKey("lootMapLootScale")) {
			lootMapLootScale = Utils.intValue(ConfParam.get("lootMapLootScale").value);
		}
		
		if (ConfParam.containsKey("lootMapPvpBattleEndHPChange")) {
			int[] hpChange = Utils.strToIntArray(ConfParam.get("lootMapPvpBattleEndHPChange").value);
			lootMapBattleWinHpChange= hpChange[0];
			lootMapBattleFailHpChange = hpChange[1];
		}
		
		if (ConfParam.containsKey("lootMapBattleHpCost")) {
			lootMapBattleHpCost = Utils.intValue(ConfParam.get("lootMapBattleHpCost").value);
		}
		
		if (ConfParam.containsKey("lootMapTimeModTime")) {
			lootMapTimeModeTime = Utils.strToIntArray(ConfParam.get("lootMapTimeModTime").value);
		}
		
		if (ConfParam.containsKey("lootMapTimeModType")) {
			lootMapTimeModeType = Utils.strToIntArray(ConfParam.get("lootMapTimeModType").value);
		}
		
		if (ConfParam.containsKey("lootMapRewardScore")) {
			lootMapScore = Utils.strToIntArray(ConfParam.get("lootMapRewardScore").value);
		}

		if (ConfParam.containsKey("lootMapScoreReward")) {
			lootMapScoreReward = Utils.strToIntArray(ConfParam.get("lootMapScoreReward").value);
		}

		if (ConfParam.containsKey("lootMapRoomLimitTime")) {
			String[] strArray = Utils.strToStrArray(ConfParam.get("lootMapRoomLimitTime").value);
			lootMapSignUpQueueTime = Utils.intValue(strArray[0]);
			lootMapMultipleTime = Utils.intValue(strArray[1]);
		}
		if (ConfParam.containsKey("lootMapRoomSignUpNumber")) {
			String[] strArray = Utils.strToStrArray(ConfParam.get("lootMapRoomSignUpNumber").value);
			lootMapSignUpMinHumanNumber = Utils.intValue(strArray[0]);
			lootMapSignUpMaxHumanNumber = Utils.intValue(strArray[1]);
		}
		if (ConfParam.containsKey("lootMapEggRoomTime")) {
			lootMapEggLevelTime = Utils.intValue(ConfParam.get("lootMapEggRoomTime").value);
		}
		if (ConfParam.containsKey("lootMapRoomTime")) {
			lootMapRoomTime = Utils.intValue(ConfParam.get("lootMapRoomTime").value);
		}
		if(ConfParam.containsKey("lootMapPvpRoleMinAtk")){
			lootMapHumanMinAttack = Utils.intValue(ConfParam.get("lootMapPvpRoleMinAtk").value);
		}
		if(ConfParam.containsKey("lootMapPvpRoleMinAtk")){
			lootMapHumanMaxAttack = Utils.intValue(ConfParam.get("lootMapPvpRoleMaxAtk").value);
		}
		if(ConfParam.containsKey("lootMapPvpRoleMaxHp")){
			lootMapHumanMaxHp = Utils.intValue(ConfParam.get("lootMapPvpRoleMaxHp").value);
		}
		if(ConfParam.containsKey("lootMapPvpResurrectionTime")){
			lootMapWaitRevivalTime = Utils.intValue(ConfParam.get("lootMapPvpResurrectionTime").value);
		}
		if(ConfParam.containsKey("lootMapPvpResurrectionTime")){
			lootMapWaitRevivalTime = Utils.intValue(ConfParam.get("lootMapPvpResurrectionTime").value);
		}
		
		if(ConfParam.containsKey("lootMapProtectedTime")){
			int[] protectedTime = Utils.strToIntArray(ConfParam.get("lootMapProtectedTime").value);
			if(protectedTime.length >= 1){
				lootMapStartPortectedTime = protectedTime[0];
			}
			if(protectedTime.length >= 2){
				lootMapBattleWintPortectedTime = protectedTime[1];
			}
			if(protectedTime.length >= 3){
				lootMapBattleFailtPortectedTime = protectedTime[2];
			}
		}
		if(ConfParam.containsKey("lootMapGreenItem")){
			lootMapGreenItem = Utils.strToIntArray(ConfParam.get("lootMapGreenItem").value);
		}
		if(ConfParam.containsKey("lootMapBlueItem")){
			lootMapBlueItem = Utils.strToIntArray(ConfParam.get("lootMapBlueItem").value);
		}
		if(ConfParam.containsKey("lootMapPurpleItem")){
			lootMapPurpleItem = Utils.strToIntArray(ConfParam.get("lootMapPurpleItem").value);
		}
		if(ConfParam.containsKey("lootMapOrangeItem")){
			lootMapOrangeItem = Utils.strToIntArray(ConfParam.get("lootMapOrangeItem").value);
		}
		if(ConfParam.containsKey("lootMapRedItem")){
			lootMapRedItem = Utils.strToIntArray(ConfParam.get("lootMapRedItem").value);
		}
		if(ConfParam.containsKey("lootMapPvpBattleTime")){
			lootMapIntoPkTime = Utils.intValue(ConfParam.get("lootMapPvpBattleTime").value);
		}
		if(ConfParam.containsKey("lootMapMoveStepTime")){
			lootMapMoveStepTime = Utils.intValue(ConfParam.get("lootMapMoveStepTime").value);
		}
		if(ConfParam.containsKey("lootMapCombatFrequencyLimit")){
			lootMapCombatFrequencyLimit = Utils.strToIntArray(ConfParam.get("lootMapCombatFrequencyLimit").value);
		}
		if(ConfParam.containsKey("initHumanSingleKey")){
			initHumanLootMapSingleKey = Utils.intValue(ConfParam.get("initHumanSingleKey").value);
		}
		if(ConfParam.containsKey("initHumanMultipleKey")){
			initHumanLootMapMultipleKey = Utils.intValue(ConfParam.get("initHumanMultipleKey").value);
		}
		if(ConfParam.containsKey("lootMapPvpStartIntegral")){
			initLootMapStartScore = Utils.intValue(ConfParam.get("lootMapPvpStartIntegral").value);
		}
		if(ConfParam.containsKey("lootMapStartPos")){
			lootMapStartPos = Utils.intValue(ConfParam.get("lootMapStartPos").value);
		}
		if(ConfParam.containsKey("lootMapPvpStartCombatDate")){
			lootMapPvpStartCombatDate = Utils.strToIntArray(ConfParam.get("lootMapPvpStartCombatDate").value);
		}
		if(ConfParam.containsKey("competeRobotSn")){
			competeRobotSn = Utils.strToIntArray(ConfParam.get("competeRobotSn").value);
		}
		//仙府相关
		if(ConfParam.containsKey("domainGrabRatio")){
			domainGrabRatio = Utils.intValue(ConfParam.get("domainGrabRatio").value);
		}
		if(ConfParam.containsKey("domaincaveNumLimit")){
			domaincaveNumLimit = Utils.intValue(ConfParam.get("domaincaveNumLimit").value);
		}
		if(ConfParam.containsKey("domainBeGrabLimit")){
			domainBeGrabLimit = Utils.intValue(ConfParam.get("domainBeGrabLimit").value);
		}
		
	}
	
	private static void reloadParamItem() {
		if(ConfParam.containsKey("universalPartnerList")){
			universalPartnerList = Utils.strToIntList(ConfParam.get("universalPartnerList").value);
		}
		if(ConfParam.containsKey("universalPartnerConversion")){
			universalPartnerConversion = Utils.strToIntList(ConfParam.get("universalPartnerConversion").value);
		}
		if(ConfParam.containsKey("universalGodConversion")){
			universalGodConversion = Utils.strToIntList(ConfParam.get("universalGodConversion").value);
		}
		if(ConfParam.containsKey("universalPartnerItem")){
			universalPartnerItem = Utils.intValue(ConfParam.get("universalPartnerItem").value);
		}
		if(ConfParam.containsKey("universalGodItem")){
			universalGodItem = Utils.intValue(ConfParam.get("universalGodItem").value);
		}
	}
	
	/**
	 * 获取LevelExp配表的最大等级
	 * @return
	 */
	private static int getMaxLevelExp() {
		int lvMax = 0;
		for (ConfLevelExp conf : ConfLevelExp.findAll()) {
			if (conf != null && conf.sn > lvMax) {
				lvMax = conf.sn;
			}
		}
		if (lvMax <= 0) {
			Log.human.error("===人物最大等级<=0?请检查配表ConfLevelExp！");
			lvMax = 1;
		}
		return lvMax;
	}
	
	/** 合服后服务器id列表**/
	public static List<Integer> serverIds = new ArrayList<>();
	
	/**
	 * @param serverId
	 * @return true:为本服服务器id
	 */
	public static boolean isServerIdValid(int serverId) {
		if (serverId == C.GAME_SERVER_ID)
			return true;
		if (serverIds!=null && !serverIds.isEmpty())
			return serverIds.contains(serverId);
		return false;
	}
}
