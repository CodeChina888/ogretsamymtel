package game.worldsrv.enumType;

import java.util.HashMap;
import java.util.Map;

import game.msg.Define.EMoneyType;
import game.worldsrv.config.ConfItem;

/**
 * 记录系统模块类型枚举
 * @author shenjh （0-99）特殊用途 （1000-8999）功能模块 （9000-9999）未定义模块
 */
public enum LogSysModType {

	/** ======（0-99）特殊用途====== **/
	Unknown(0), // 未设置
	GmTest(1), // Gm测试

	/** ======（1000-8999）功能模块====== **/
	// ======竞技场======
	Compete(1000), // 竞技场
	CompeteBuyNum(1001), // 竞技场购买挑战次数
	CompeteCDClear(1002), // 竞技场清除冷却时间
	CompeteFight(1003), // 竞技场挑战
	CompeteIntegralAward(1004),//竞技场积分领奖
	CompeteHistoryTop(1005),//竞技场历史最高奖励

	// ======商店======
	ShopGold(1301),//元宝商店
	ShopGeneral(1302),//将魂商店
	ShopArena(1303),//竞技场商店
	ShopTower(1304),//远征商店
	ShopSeven(1305),//七日商店
	ShopGuild(1306),//公会商店
	ShopGoldRef(1311),//元宝商店刷新
	ShopGeneralRef(1312),//将魂商店刷新
	ShopArenaRef(1313),//竞技场商店刷新
	ShopTowerRef(1314),//远征商店刷新
	ShopSevenRef(1315),//七日商店刷新
	ShopGuildRef(1316),//公会商店刷新
	// ======装备======
	Forge(1400), // 锻造系统
	EquipIntensify(1401), // 装备强化
	EquipEvolution(1402), // 装备进化
	EquipRefine(1403),	// 装备精炼
	FashionUnlock(1410),// 解锁时装
	FashionHenshinUseItem(1411), // 使用变身时装道具
	FashionHenshinBuyNew(1412), // 购买新的变身装

	// ======战斗相关======
	AutoCombat(1700), // 自动战斗
	PauseBtn(1701), // 暂停按钮

	// ======在线======
	OnlineAward(2101),// 在线奖励

	// ======开服七日======
	KFSeven(2200), // 开服七日
	KFSevenLoginAward(2201), // 开服七天登陆奖励
	KFSevenQuestAward(2202), // 开服七天任务奖励
	KFSevenQuestBuy(2203), // 开服七天任务半价抢购

	// ======活动======
	Activity(2300), // 活动系统
	ActDailySignIn(2301), // 活动之每日签到
	ActReSign(2302), // 活动之补签
	ActGift(2303), // 活动之礼包
	ActGrowAward(2304), // 活动之成长计划奖励
	ActHalfPrice(2305),// 活动之半价购买
	ActDailyPayGift(2306),// 活动之每日充值大返还
	ActActTimeUpBack(2307),// 活动之分期返利
	ActForeverGrowUpFund(2308),// 活动之成长基金
	ActGiftCode(2309),// 礼包激活码
	ActExpRecover(2310),// 活动经验追回
	ActLoginSpecial(2311),// 活动特殊登录
	ActLvUpGift(2312), // 活动之等级礼包
	ActOnlineGift(2312), // 活动之在线礼包
	ActSevenDaysSignIn(2313), // 活动之七日签到
	ActItemExchange(2314), // 活动之物品收集兑换
	ActWishing(2315), // 活动之许愿池
	ActDisCount(2316), // 活动之限时折扣
	ActLoginNumSendGold(2317),//活动之10W元宝
	ActSeekImmortal(2318),//活动之寻仙有礼
	ActSevenDaysEnjoy(2319),//活动之7日尊享
	ActServerGiftBag(2320),//活动之新服礼包
	ActPayWelfare(2321),// 活动之充值福利
	ActTimeLimitExchange(2322), // 活动之限时兑换
	
	// ======每日任务/成就系统======
	QuestDaily(2400), // 每日任务
	QuestDailyLiveness(2401), // 每日任务活跃度
	Achievement(2450), // 成就系统

	// ======排行榜功能======
	Rank(2500), // 排行榜
	RankWorship(2501), // 排行榜膜拜

	// ======公会======
	Guild(2600), // 公会系统
	GuildCreate(2601), // 创建公会消耗钻石
	GuildRename(2602), // 公会改名消耗钻石
	GuildImmo(2603), // 公会献祭
	GuildProperty(2604), // 公会属性
	GuildDrawReset(2605), //公会抽奖重置
	GuildPrize(2606),// 公会抽奖奖励
	//==========仙域
	GIVETokenEVERYDAY(2607),//每日给抢夺令
    GuildSkillUpgrade(2608),// 公会技能升级
    GuildInstChapter(2609),// 公会副本章节奖励
    GuildInstStage(2610),// 公会副本关卡奖励
    GuildInstChallenge(2611),// 公会副本挑战奖励
	/**
	 * 购买开采令/强夺令
	 */
	CaveBuy(2607),
	CaveBattle(2608),//仙府战斗
	CaveDelay(2609),//延时
	// ======技能======
	Skill(2700), // 技能系统功能
	SkillUpgrade(2701), // 技能升级
	SkillOneKeyUpgrade(2702), // 技能一键升级
	SkillRuneUnlock(2710), //技能符文激活
	SkillRunePractice(2711), //技能符文洗练
	SkillGodsLvUp(2720), //技能爆点升级
	SkillGodsUnlock(2721), //技能爆点解锁
	SkillGodsStarUp(2722), //技能爆点升星
	SkillTrain(2723), //技能培养抽奖
	SkillTrainReset(2723), //技能重置培养
	skillStageUp(2724), // 技能升阶

	// ======背包======
	Bag(2800), // 背包系统
	BagEnlarge(2801), // 背包扩大
	BagSellItem(2802), // 背包出售物品
	BagItemUse(2803), // 背包物品使用
	BagItemAutoUse(2804), // 背包物品自动使用
	BagItemCompose(2805), // 背包物品合成
	BagSelectPackageItem(2806), // 背包选择礼包道具
	
	// ======伙伴======
	Partner(2900), // 伙伴系统
	PartnerRecuit(2901), // 伙伴招募
	PartnerAddLevel(2902), // 伙伴升级
	PartnerAddStar(2903), // 伙伴升星
	PartnerAddCont(2907),// 伙伴进阶
	PartnerResolve(2909),//伙伴分解
	DrawCard(2920), // 抽卡
	ScoreCardExchange(2921), // 招募积分兑换
	ServantClean(2930),
	CimeliaAddStar(2931),
	CimeliaLvUp(2932),		
	CimeliaAdvantUp(2933),
	// ======命格======
	RuneSummon(3100),//命格占卜
	RuneExchange(3101),//命格兑换
	
	// ======邮件======
	Mail(5000), // 邮件系统
	MailSend(5001), // 邮件发送
	MailPickup(5002), // 邮件取附件

	// ======副本======
	Inst(5100), // 副本系统
	InstDropFirst(5101), // 副本首次掉落
	InstDropRand(5102), // 副本随机掉落
	InstAuto(5103), // 副本自动扫荡
	InstResetFightNum(5104), // 副本重置挑战次数
	InstLottery(5105), // 副本通关抽奖
	InstStarBox(5106), // 副本星数宝箱
	InstFightRevive(5107), // 副本战斗中死亡复活
	InstRes(5108), // 资源本挑战
	InstResDrop(5109), // 资源本掉落
	InstResAuto(5110), // 资源本扫荡
	

	// ======活动副本======
	ActInst(5300), // 活动副本系统
	
	// ======世界BOSS======
	WDBossRevive(5501), // 世界Boss立即复活
	WDBossReborn(5502), // 世界Boss涅槃重生
	WDBossInspireCD(5503), // 世界Boss消除鼓舞cd
	WDBossHarmCoin(5504), // 世界Boss伤害奖励
		
	// ======爬塔======
	TowerReset(6000), // 爬塔重置
	TowerCardOpen(6001), //爬塔宝箱翻牌
	TowerBuyLife(6002), //购买生命
	TowerPassReward(6003), //爬塔过关奖励
	TowerBuyMutiple(6004), //爬塔购买多倍奖励
	
	/** ======（7000-7999）角色相关模块====== **/
	Human(7000), // 角色相关
	HumanCreate(7001), // 角色首次创建
	HumanRename(7002), // 角色改名
	HumanAddLevel(7003), // 角色升级（当前会按比例获得体力）
	HumanActBuy(7004), // 体力-购买
	ActValueRecovery(7005),	// 体力-系统自动恢复
	ActValueLevelUp(7006), // 体力-升级获得
	ActValueLogin(7007), // 体力-登陆计算
	ActValueCost(7008), // 体力-消耗
	ActValueGet(7009), // 体力-领取
	DailyCoinBuy(8202), // 每日购买铜币
	
	/** ======（8000-8999）VIP及付费相关模块====== **/
	Vip(8000), // VIP相关
	VipPay(8001), // VIP充值
	VipPayBack(8002), // VIP充值返还
	VipDraw(8003), // VIP领取
	VipDailySignIn(8004), // VIP每日签到
	VipReSign(8005), // VIP补签
	VipInvestmentBack(8006),// VIP投资返还
	VipMonthCard(8007),// 月卡
	VipBuy(8008),// VIP购买次数
	VipLvGift(8009),// VIP等级礼包
	VipAward(8009),// VIP特殊奖励
	VipGiftPayBack(8010),// VIP充值返利礼包
	VipFirstChargeReward(8011),// VIP充值返利礼包
	
	ChargePresent(8101), // 充值送红包
	
	
	/** ======（9000-9999）未定义模块====== **/
	OfflineReimburse(9010), // 离线补偿领取
	
	TeamHireRobot(9020), // 组队副本雇佣机器人
	
	HornInform(9030), // 喇叭喊话消耗
	ModUnlockReward(9031), //功能开放奖励
	
	FriendSendAct(9040), // 好友赠送体力
	
	RaffleLuckTurntable(9050), // 抽奖-幸运转盘
	
	LootMapRevival(9060), // 抢夺本死亡复活
	
	MainCityBuy(9070), // 城主购买红包次数
	
	PKMirrorWin(9080), // 切磋镜像玩家胜利
	PKHumanWin(9081), // 切磋真人玩家胜利
	
	;
	
	/** 映射表 **/
	private static final Map<Integer, String> mapEnums = new HashMap<>();
	static {
		for (LogSysModType type : values()) {
			mapEnums.put(type.value(), type.name());
		}
	}

	private int value;

	private LogSysModType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
	
	/**
	 * 获取指定枚举值的枚举
	 * 
	 * @param value
	 * @return
	 */
	public static LogSysModType getByValue(int value) {
		LogSysModType type = null;
		String name = mapEnums.get(value);
		if (name != null) {
			type = valueOf(name);
		}
		return type;
	}
	
	
	/**
	 * 获取货币名称
	 * @param resName 消费类型
	 * @return
	 */
	public static String getResName(int resName) {
		String str = "";
		EMoneyType type = EMoneyType.valueOf(resName);
		if (type == null) {
			//获取道具名称
			ConfItem conf = ConfItem.get(resName);
			if (conf != null) {
				str = String.valueOf(conf.name);
			}
		} else {
			str = getMoneyName(type);
		}
		return str;
	}
	
	public static String getMoneyName(EMoneyType moneyType) {
		String str = "";
		switch (moneyType) {
		case act :			str = "体力";		break;
		case exp :			str = "玩家经验";		break;
		case coin :			str = "金币"; 		break;
		case gold :			str = "钻石";		break;
		case competeToken :	str = "威望";		break;
		case soulToken :	str = "元魂";		break;
		case towerToken :	str = "远征积分";		break;
		case generalToken :	str = "将魂";		break;
		case summonToken :	str = "招募代币";		break;
		case parnterExp :	str = "伙伴经验池";	break;
		case runeToken :	str = "纹石碎片";		break;
		case lootSingle :	str = "抢夺本单人钥匙";	break;
		case lootMultiple :	str = "抢夺本多人钥匙";	break;
		case lootScore:		str = "抢夺本积分";	break;
		default:			str = "无该资源";		break;
		}
		return str;
	}
	
	public static String getName(LogSysModType type){
		String name = "未定义";
		if (type == null) {
			return name;
		}
		switch (type) {
			case Unknown:					name = "未设置";						break;// 未设置
			case GmTest:					name = "Gm测试";						break;// gm测试 
			
			case Compete: 					name = "竞技场"; 					break;// 竞技场
			case CompeteFight:				name = "竞技场挑战";					break;// 竞技场挑战
			case CompeteBuyNum: 			name = "竞技场购买挑战次数"; 			break;// 竞技场购买挑战次数
			case CompeteHistoryTop: 		name = "竞技场历史最高奖励";			break;// 竞技场历史最高奖励
			
			case ShopGold:					name = "元宝商店";					break; //元宝商店
			case ShopGeneral:				name = "将魂商店";					break; //将魂商店
			case ShopArena:					name = "竞技场商店";					break; //竞技场商店
			case ShopTower:					name = "远征商店";					break; //远征商店
			case ShopSeven:					name = "七日商店";					break; //七日商店
			case ShopGoldRef:				name = "元宝商店刷新";				break; //元宝商店刷新
			case ShopGeneralRef:			name = "将魂商店刷新";				break; //将魂商店刷新
			case ShopArenaRef:				name = "竞技场商店刷新";				break; //竞技场商店刷新
			case ShopTowerRef:				name = "远征商店刷新";				break; //远征商店刷新
			case ShopSevenRef:				name = "七日商店刷新";				break; //七日商店刷新
			case ShopGuildRef:					name = "公会商店刷新";				break; //公会商店刷新
			
			case EquipIntensify: 			name = "装备强化"; 					break;// 装备强化
			case EquipEvolution: 			name = "装备进化"; 					break;// 装备进化
			case FashionUnlock:				name = "时装解锁";					break;// 时装解锁
			case FashionHenshinUseItem:		name = "使用变身";					break;// 使用变身
			case FashionHenshinBuyNew:		name = "购买新的变身装"; 				break;// 购买新的变身装 

			case AutoCombat: 				name = "自动战斗";					break;// 自动战斗
			case PauseBtn:					name = "暂停按钮";					break;// 暂停按钮
			case OnlineAward:				name = "在线";						break;// 在线
			
			case KFSeven: 					name = "开服七日";					break;// 开服七日
			case KFSevenLoginAward: 		name = "开服七天登陆奖励";				break;// 开服七天登陆奖励
			case KFSevenQuestAward: 		name = "开服七天任务奖励";				break;// 开服七天任务奖励
			case KFSevenQuestBuy: 			name = "开服七天任务半价抢购";			break;// 开服七天任务半价抢购
			
			case Activity:					name = "活动系统";					break;// 活动系统
			case ActDailySignIn:			name = "活动之每日签到";				break;// 活动之每日签到
			case ActReSign:					name = "活动之补签";					break;// 活动之补签
			case ActGift:					name = "活动之礼包";					break;// 活动之礼包
			case ActGrowAward:				name = "活动之成长计划奖励";			break;// 活动之成长计划奖励
			case ActHalfPrice:				name = "动之半价购买";				break;// 活动之半价购买
			case ActDailyPayGift:			name = "动之每日充值大返还";			break;// 活动之每日充值大返还
			case ActActTimeUpBack:			name = "动之分期返利";				break;// 活动之分期返利
			case ActForeverGrowUpFund:		name = "动之成长基金";				break;// 活动之成长基金
			case ActGiftCode:				name = "包激活码";					break;// 礼包激活码
			case ActExpRecover:				name = "动经验追回";					break;// 活动经验追回
			case ActLoginSpecial:			name = "动特殊登录";					break;// 活动特殊登录
			case ActLvUpGift:				name = "活动之等级礼包";				break;// 活动之等级礼包
			case ActOnlineGift:				name = "活动之在线礼包";				break;// 活动之在线礼包
			case ActSevenDaysSignIn:		name = "活动之七日签到";				break;// 活动之七日签到
			case ActItemExchange:			name = "活动之物品收集兑换";			break;// 活动之物品收集兑换
			case ActWishing:				name = "活动之许愿池";				break;// 活动之许愿池
			
			case QuestDaily: 				name = "每日任务";					break;// 每日任务
			case QuestDailyLiveness: 		name = "每日任务活跃度";				break;// 每日任务活跃度
			case Achievement: 				name = "成就系统";					break;// 成就系统
			
			case Rank: 				   		name = "排行榜";						break;// 排行榜
			case RankWorship: 				name = "排行榜膜拜";					break;// 排行榜膜拜
			
			case Guild: 					name = "公会系统";					break;// 公会系统
			case GuildCreate: 				name = "创建公会消耗钻石";				break;// 创建公会消耗钻石
			case GuildRename: 				name = "公会改名消耗钻石";				break;// 公会改名消耗钻石
			case GuildImmo: 				name = "公会献祭";					break;// 公会献祭
			case GuildProperty: 			name = "公会属性";					break;// 公会属性
			case GuildDrawReset: 			name = "公会抽奖重置";				break;// 公会抽奖重置
			case GuildPrize:				name = "公会抽奖奖励";				break;// 公会抽奖奖励
			
			case Skill: 					name = "技能系统功能";				break;// 技能系统功能
			case SkillUpgrade: 				name = "技能升级";					break;// 技能升级
			case SkillOneKeyUpgrade:		name = "技能一键升级";				break;// 技能一键升级
			case SkillRuneUnlock:			name = "技能符文激活"; 				break;// 技能符文激活
			case SkillRunePractice: 		name = "技能符文洗练";				break;// 技能符文激活
			case SkillGodsLvUp: 			name = "技能爆点升级";				break;// 技能爆点升级
			case SkillGodsUnlock:			name = "技能爆点解锁";				break;// 技能爆点解锁
			case SkillGodsStarUp:			name = "技能爆点升星";				break;// 技能爆点升星
			case SkillTrain:				name = "技能培养抽奖";				break;// 技能培养抽奖
			case SkillTrainReset:			name = "技能培养重置";				break;// 技能培养重置
			case skillStageUp:				name = "技能升阶";					break;// 技能升阶
			
			case Bag: 						name = "背包系统";					break;// 背包系统
			case BagEnlarge: 				name = "背包扩大";					break;// 背包扩大
			case BagSellItem: 				name = "背包出售物品";				break;// 背包出售物品
			case BagItemUse: 				name = "背包物品使用";				break;// 背包物品使用
			case BagItemAutoUse: 			name = "背包物品自动使用";				break;// 背包物品自动使用
			case BagItemCompose:			name = "背包物品合成";				break;// 背包物品合成

			case Partner: 					name = "伙伴系统";					break;// 伙伴系统
			case PartnerRecuit: 		 	name = "伙伴卡片使用";				break;// 伙伴卡片使用
			case PartnerAddLevel: 		 	name = "伙伴升级";					break;// 伙伴升级
			case PartnerAddStar: 		 	name = "伙伴升星";					break;// 伙伴升星
			case PartnerAddCont:			name = "符文进阶";					break;// 符文进阶
			
			case CimeliaAddStar:			name = "法宝升星"; 					break;
			case CimeliaLvUp:				name = "法宝升级"; 					break;
			case CimeliaAdvantUp:			name = "法宝突破"; 					break;
			
			case DrawCard: 					name = "抽卡";						break;// 抽卡
			case ScoreCardExchange:			name = "招募积分兑换";				break;// 招募积分兑换
			case ServantClean: 				name=  "解锁护法";					break;// 解锁护法
			case RuneSummon:				name = "命格占卜";					break;// 命格占卜
			case RuneExchange:				name = "命格兑换";					break;// 命格兑换

			case Mail: 						name = "邮件系统";					break;// 邮件系统
			case MailSend: 					name = "邮件发送";					break;// 邮件发送
			case MailPickup: 				name = "邮件取附件";					break;// 邮件取附件

			case Inst: 						name = "副本系统";					break;// 副本系统
			case InstAuto: 					name = "副本自动扫荡";				break;// 副本自动扫荡
			case InstResetFightNum: 		name = "副本购买次数";				break;// 副本购买次数
			case InstLottery: 				name = "副本通关抽奖";				break;// 副本通关抽奖
			case InstStarBox: 				name = "副本星数宝箱";				break;// 副本星数宝箱
			case InstDropFirst:				name = "副本首次过关";				break;// 副本首次过关
			case InstDropRand: 				name = "副本掉落";					break;// 副本掉落
			case InstFightRevive: 			name = "副本战斗中死亡复活";			break;// 副本战斗中死亡复活
			case InstRes:					name = "资源本挑战";					break;// 资源本挑战
			case InstResDrop:				name = "资源本掉落";					break;// 资源本掉落
			case InstResAuto:				name = "资源本扫荡";					break;// 资源本扫荡

			case ActInst: 					name = "活动副本系统";				break;// 活动副本系统
			
			case WDBossRevive:				name = "世界Boss立即复活";			break;// 世界Boss立即复活
			case WDBossReborn:				name = "世界Boss涅槃重生";			break;// 世界Boss涅槃重生
			case WDBossInspireCD:			name = "世界Boss消除鼓舞cd";			break;// 世界Boss消除鼓舞cd
			case WDBossHarmCoin:			name = "世界Boss伤害奖励";			break;// 世界Boss伤害奖励

			case TowerReset: 				name = "爬塔重置";					break;// 爬塔重置
			case TowerCardOpen: 			name = "爬塔宝箱翻牌";				break;// 爬塔宝箱翻牌
			case TowerBuyLife:				name = "爬塔购买生命";				break;// 爬塔购买生命
			case TowerPassReward:			name = "爬塔过关奖励";				break;// 爬塔过关奖励
			case TowerBuyMutiple:			name = "爬塔购买多倍奖励";				break;// 爬塔购买多倍奖励

			case Human: 					name = "角色相关";					break;// 角色相关
			case HumanCreate: 				name = "角色首次创建";				break;// 角色首次创建
			case HumanRename: 				name = "角色改名";					break;// 角色改名
			case HumanAddLevel: 			name = "角色升级(当前会按比例获得体力)";break;// 角色升级（当前会按比例获得体力）
			
			case HumanActBuy: 				name = "体力-购买";					break;// 体力-购买
			case ActValueRecovery: 			name = "体力-系统自动恢复";			break;// 体力-系统自动恢复
			case ActValueLevelUp:  			name = "体力-升级获得";				break;// 体力-升级获得
			case ActValueLogin: 			name = "体力-登陆计算";				break;// 体力-登陆计算
			case ActValueCost: 				name = "角色体力消耗";				break;// 角色体力消耗
			case ActValueGet:				name = "体力-领取";					break;// 体力-领取
			case DailyCoinBuy:				name = "购买金币";					break;// 购买金币

			case Vip: 						name = "VIP";						break;// VIP
			case VipPay: 					name = "VIP充值";					break;// VIP充值
			case VipPayBack: 				name = "VIP充值返还";					break;// VIP充值返还
			case VipDraw: 					name = "VIP领取";					break;// VIP领取
			case VipDailySignIn: 			name = "VIP每日签到";					break;// VIP每日签到
			case VipReSign: 				name = "VIP补签";					break;// VIP补签
			case VipGiftPayBack:			name = "VIP充值返利礼包";				break;// VIP充值返利礼包
			case VipInvestmentBack:			name = "VIP 投资返还";				break;// VIP 投资返还
			case VipMonthCard:				name = "月卡";						break;// 月卡
			case VipBuy:					name = "VIP购买次数";					break;// VIP购买次数
			case VipLvGift:					name = "VIP等级礼包";					break;// VIP等级礼包
			case VipAward:					name = "VIP特殊奖励";					break;// VIP特殊奖励
			case VipFirstChargeReward:		name = "VIP充值返利礼包";				break;// VIP充值返利礼包
			
			case ChargePresent:				name = "充值送红包";					break;// 充值送红包
			
			case OfflineReimburse: 			name = "离线补偿领取";				break;// 离线补偿领取
			case TeamHireRobot: 			name = "组队副本雇佣机器人";			break;// 组队副本雇佣机器人
			case HornInform:				name = "喇叭喊话消耗";				break;// 喇叭喊话消耗
			case ModUnlockReward:			name = "功能开放奖励";				break;// 功能开放奖励
				
			case FriendSendAct: 			name = "好友赠送体力";             	break;// 好友赠送体力
			case RaffleLuckTurntable:		name = "抽奖-幸运转盘";				break;// 抽奖-幸运转盘
			
			case PKMirrorWin:				name = "切磋镜像玩家胜利";				break;// 切磋镜像玩家胜利
			case PKHumanWin:				name = "切磋真人玩家胜利";				break;// 切磋真人玩家胜利
			case CaveBuy:					name = "购买开采令/强夺令";			break;// 
			
			default:						name = "未补充类型"; 					break;// 没有补充
			}
		
		return name;
	}
}