package game.worldsrv.entity;

import org.apache.commons.lang3.exception.ExceptionUtils;

import core.db.DBConsts;
import core.dbsrv.DB;
import core.Chunk;
import core.Port;
import core.Record;
import core.support.BufferPool;
import core.support.SysException;
import core.support.log.LogCore;
import game.worldsrv.entity.Unit;
import core.gen.GofGenFile;

@GofGenFile
public final class Human extends Unit {
	public static final String tableName = "human";

	/**
	 * 属性关键字
	 */
	public static final class K extends SuperK {
		public static final String id = "id";	//id
		public static final String GMPrivilege = "GMPrivilege";	//GM权限：0无任何权限，1有全部权限
		public static final String ClientOpenSRDebug = "ClientOpenSRDebug";	//是否开启客户端调试
		public static final String ClientLogType = "ClientLogType";	//客户端调试日志级别
		public static final String PlatformId = "PlatformId";	//平台Id2位
		public static final String ServerId = "ServerId";	//服务器Id4位
		public static final String HumanDigit = "HumanDigit";	//玩家标识6位（不可改变字段名）
		public static final String LoginType = "LoginType";	//登录类型ELoginType
		public static final String AccountLogin = "AccountLogin";	//登录账号
		public static final String AccountId = "AccountId";	//唯一账号ID
		public static final String Channel = "Channel";	//渠道
		public static final String DevMAC = "DevMAC";	//设备MAC地址
		public static final String DevIMEI = "DevIMEI";	//设备IMEI号
		public static final String DevType = "DevType";	//设备类型：0PC，1IOS，2越狱，3安卓
		public static final String TimeSecOnline = "TimeSecOnline";	//在线时间累积秒数
		public static final String TimeOnLine = "TimeOnLine";	//在线时间
		public static final String TimeLogin = "TimeLogin";	//最后一次登录时间
		public static final String TimeLogout = "TimeLogout";	//最后一次登出时间
		public static final String TimeCreate = "TimeCreate";	//角色创建时间
		public static final String SessionKey = "SessionKey";	//角色SessionKey
		public static final String FirstCreateHuman = "FirstCreateHuman";	//是否第一次创角
		public static final String HeadSn = "HeadSn";	//头像sn
		public static final String MountSn = "MountSn";	//坐骑sn
		public static final String FashionSn = "FashionSn";	//时装sn
		public static final String HenshinSn = "HenshinSn";	//变装sn
		public static final String TitleSn = "TitleSn";	//称号sn
		public static final String TitleShow = "TitleShow";	//是否显示称号
		public static final String fillMailJson = "fillMailJson";	//补偿邮件相关
		public static final String VipLevel = "VipLevel";	//VIP等级
		public static final String ChargeGold = "ChargeGold";	//累计充值金额
		public static final String TodayChargeGold = "TodayChargeGold";	//今日充值金额
		public static final String PayCharge = "PayCharge";	//充值信息
		public static final String ChargeInfo = "ChargeInfo";	//可按天返还的充值信息{商品sn, 充值次数, 返还结束时间}
		public static final String Coin = "Coin";	//1 铜币
		public static final String Gold = "Gold";	//2 元宝
		public static final String Act = "Act";	//3 体力值
		public static final String Exp = "Exp";	//4 经验值
		public static final String CompeteToken = "CompeteToken";	//5 威望
		public static final String SoulToken = "SoulToken";	//6 元魂
		public static final String TowerToken = "TowerToken";	//7 远征积分
		public static final String GeneralToken = "GeneralToken";	//8 将魂
		public static final String SummonToken = "SummonToken";	//9 招募代币
		public static final String ParnterExp = "ParnterExp";	//10 伙伴经验池
		public static final String RuneToken = "RuneToken";	//11 纹石碎片
		public static final String LootSingle = "LootSingle";	//12 抢夺本单人代币
		public static final String LootMultiple = "LootMultiple";	//13 抢夺本多人代币
		public static final String SummonPresent = "SummonPresent";	//16 招募赠送代币（仙缘）
		public static final String SummonHigher = "SummonHigher";	//17 高级招募令
		public static final String RefreshToken = "RefreshToken";	//18 刷新令
		public static final String SkillExperience = "SkillExperience";	//19 阅历（技能修炼）
		public static final String RefineToken = "RefineToken";	//20 精炼石
		public static final String SummonScore = "SummonScore";	//21 招募获得积分
		public static final String CimeliaToken = "CimeliaToken";	//22 法宝灵气
		public static final String DevelopmentToken = "DevelopmentToken";	//23 开采令
		public static final String SnatchToken = "SnatchToken";	//24 抢夺令
		public static final String ResetStone = "ResetStone";	//25 修炼重置石
		public static final String GuildCoin = "GuildCoin";	//26 仙盟币
		public static final String ActMax = "ActMax";	//最大体力值
		public static final String StageHistory = "StageHistory";	//地图位置信息{{id,sn,x,y,common},{}}
		public static final String FashionShow = "FashionShow";	//是否显示时装,true显示
		public static final String FashionWeaponSn = "FashionWeaponSn";	//时装武器sn
		public static final String EquipPosJSON = "EquipPosJSON";	//主公装备槽
		public static final String ItemBagNumMax = "ItemBagNumMax";	//当前背包最大格子数
		public static final String TeamId = "TeamId";	//所属队伍ID
		public static final String InstActMHXKWarLastPass = "InstActMHXKWarLastPass";	//活动副本之魔王梦境最后通关记录{type2:difficulty}
		public static final String InstActStoryLastPass = "InstActStoryLastPass";	//活动副本之经典战役最后通关记录{type2:difficulty}
		public static final String InstActStoryAward = "InstActStoryAward";	//活动副本之经典战役领奖记录{type2:EAwardType}
		public static final String LottoryCooldown = "LottoryCooldown";	//抽卡的各个栏位冷却时间, {{1,12312312321},{},{}}
		public static final String LottorySelectedCount = "LottorySelectedCount";	//已经抽过的宝箱类型信息 11|21|31|
		public static final String LottoryFreeCount = "LottoryFreeCount";	//免费抽奖次数
		public static final String HoroCooldown = "HoroCooldown";	//占星的各个栏位冷却时间, {{1,12312312321},{},{}}
		public static final String HoroIsFirst = "HoroIsFirst";	//第一，二栏位，免费抽奖情况{pos1,status;pos2,status}sta:0或1
		public static final String HoroFreeCount = "HoroFreeCount";	//免费抽奖次数,结构是1,1,1
		public static final String HoroJPGap = "HoroJPGap";	//极品间隔次数,结构是1,1,1
		public static final String HoroJPCount = "HoroJPCount";	//极品次数累计,结构是1,1,1
		public static final String QuestNameChangePassed = "QuestNameChangePassed";	//是否已经过了改名任务
		public static final String FirstStory = "FirstStory";	//创角后的第一个新手剧情
		public static final String GuideClose = "GuideClose";	//玩家新手引导是否关闭,true 关闭，false 打开
		public static final String GuideIds = "GuideIds";	//玩家新手引导ID
		public static final String SkillSet1 = "SkillSet1";	//技能设置1
		public static final String SkillStuffCost = "SkillStuffCost";	//技能升级材料总消耗
		public static final String DailyActBuyNum = "DailyActBuyNum";	//今日已购买体力次数 
		public static final String DailyCoinBuyNum = "DailyCoinBuyNum";	//今日已购买铜币次数 
		public static final String DailyCompeteFightBuyNum = "DailyCompeteFightBuyNum";	//今日已购买竞技场挑战次数 
		public static final String DailyLootMapRevivalBuyNum = "DailyLootMapRevivalBuyNum";	//今日已购买抢夺本复活次数
		public static final String DailySendWinksNum = "DailySendWinksNum";	//今日已发送魔法表情次数
		public static final String DailyFightWinNum = "DailyFightWinNum";	//今日已主城切磋胜利次数
		public static final String DailyOnlineTime = "DailyOnlineTime";	//今日在线时间
		public static final String DailySignFlag = "DailySignFlag";	//每日签到：0未签到,1已签到
		public static final String DailyCompeteFightNum = "DailyCompeteFightNum";	//每日竞技场已挑战次数
		public static final String DailyCompeteIntegral = "DailyCompeteIntegral";	//每日竞技场积分
		public static final String DailyCompeteIntegralAward = "DailyCompeteIntegralAward";	//每日竞技场已经领取的积分sn
		public static final String DailyInstFinishNum = "DailyInstFinishNum";	//每日活动副本已完成次数{sn:1,...}
		public static final String DailyRankWorship = "DailyRankWorship";	//每日排行榜膜拜次数{0:1,2:1,3:1,4:1}
		public static final String DailyQuestLiveness = "DailyQuestLiveness";	//每日任务活跃度，需每日清0
		public static final String DailyLivenessReward = "DailyLivenessReward";	//每日活跃度奖励已领取记录，需每日清空
		public static final String WeeklyQuestLiveness = "WeeklyQuestLiveness";	//每周任务活跃度，需每周清0
		public static final String WeeklyLivenessReward = "WeeklyLivenessReward";	//每周活跃度奖励已领取记录，需每周清空
		public static final String ActFullTime = "ActFullTime";	//恢复满体力的时间点
		public static final String Resigntimes = "Resigntimes";	//补签次数
		public static final String TimeFuli = "TimeFuli";	//福利号上次领取福利时间，一星期领取一次
		public static final String Fuli = "Fuli";	//是否是福利号
		public static final String Gs = "Gs";	//是否是GS
		public static final String GsRmb = "GsRmb";	//GS号的虚拟人民币数量
		public static final String SilenceEndTime = "SilenceEndTime";	//禁言的玩家结束时间
		public static final String SealEndTime = "SealEndTime";	//封号的玩家结束时间
		public static final String DeleteRole = "DeleteRole";	//是否是已删除的玩家，0 未删除，1 已删除
		public static final String RenameNum = "RenameNum";	//已改名次数
		public static final String ModUnlock = "ModUnlock";	//已解锁的功能模块sn(只存任务类型的功能模块sn)
		public static final String ModUnlockReward = "ModUnlockReward";	//功能开放领取奖励
		public static final String RankInstStars = "RankInstStars";	//副本排行星星数
		public static final String CompeteRank = "CompeteRank";	//竞技场最高排名
		public static final String SumCombat = "SumCombat";	//总战力
		public static final String GeneralSumCombat = "GeneralSumCombat";	//伙伴总战力
		public static final String GeneralMostCombat = "GeneralMostCombat";	//伙伴最强阵容总战力
		public static final String Contribute = "Contribute";	//帮会贡献
		public static final String GuildId = "GuildId";	//帮会ID
		public static final String GuildLeaveTime = "GuildLeaveTime";	//上次离开帮会时间
		public static final String GuildLevel = "GuildLevel";	//工会等级
		public static final String GuildCDR = "GuildCDR";	//是否是会长
		public static final String GuildImmoSnList = "GuildImmoSnList";	//宝箱领取
		public static final String GuildSkills = "GuildSkills";	//公会技能
		public static final String GuildName = "GuildName";	//公会名称
		public static final String ActivityIntegral = "ActivityIntegral";	//活动积分
		public static final String IntegralBoxList = "IntegralBoxList";	//活动积分已领取宝箱sn
		public static final String BuyDevelopmentTokenCount = "BuyDevelopmentTokenCount";	//购买开采令次数
		public static final String BuySnatchTokeCountn = "BuySnatchTokeCountn";	//购买抢夺令次数
		public static final String LoginNum = "LoginNum";	//已登陆天数
		public static final String LoginNumAwardStatus = "LoginNumAwardStatus";	//7日登陆当前奖励天数
		public static final String GetloginAwardTime = "GetloginAwardTime";	//领取登陆奖励时间
		public static final String GuildInstChallengeTimes = "GuildInstChallengeTimes";	//公会副本挑战次数
		public static final String GuildInstChallengeTimesAddTime = "GuildInstChallengeTimesAddTime";	//公会副本挑战次数添加时间
		public static final String GuildInstChapterReward = "GuildInstChapterReward";	//公会副本章节奖励
		public static final String GuildInstStageReward = "GuildInstStageReward";	//公会副本关卡奖励
		public static final String GuildInstResetTime = "GuildInstResetTime";	//公会副本重置时间
		public static final String GuildInstBuyChallengeTimes = "GuildInstBuyChallengeTimes";	//公会副本已购买挑战次数
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Human() {
		super();
		setGMPrivilege(0);
		setClientOpenSRDebug(false);
		setClientLogType(0);
		setTitleSn(0);
		setFillMailJson("[]");
		setVipLevel(0);
		setTodayChargeGold(0);
		setChargeInfo("[]");
		setFashionShow(true);
		setLottoryCooldown("{}");
		setLottoryFreeCount(0);
		setHoroCooldown("{}");
		setHoroIsFirst("{}");
		setQuestNameChangePassed(false);
		setSkillStuffCost(0);
		setDailyLootMapRevivalBuyNum(0);
		setDailySendWinksNum(0);
		setDailyFightWinNum(0);
		setDailyOnlineTime(0);
		setDailySignFlag(0);
		setDailyInstFinishNum("{}");
		setTimeFuli(0);
		setGsRmb(0);
		setSilenceEndTime(0);
		setSealEndTime(0);
		setDeleteRole(0);
		setRenameNum(0);
		setRankInstStars(0);
		setCompeteRank(0);
		setSumCombat(0);
		setGeneralSumCombat(0);
		setGeneralMostCombat(0);
		setContribute(0);
		setGuildId(0);
		setGuildLeaveTime(0);
		setGuildLevel(0);
		setActivityIntegral(0);
		setLoginNum(0);
		setLoginNumAwardStatus(0);
		setGetloginAwardTime(0);
		setGuildInstChallengeTimes(0);
		setGuildInstChallengeTimesAddTime(0);
		setGuildInstResetTime(0);
		setGuildInstBuyChallengeTimes(0);
	}

	public Human(Record record) {
		super(record);
	}

	
	/**
	 * 新增数据
	 */
	@Override
	public void persist() {
		//状态错误
		if(record.getStatus() != DBConsts.RECORD_STATUS_NEW) {
			LogCore.db.error("只有新增包能调用persist函数，请确认状态：data={}, stackTrace={}", this, ExceptionUtils.getStackTrace(new Throwable()));
			return;
		}
		
		DB prx = DB.newInstance(getTableName());
		prx.insert(record);
		
		//重置状态
		record.resetStatus();
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * 默认不立即持久化到数据库
	 */
	@Override
	public void update() {
		update(false);
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * @param sync 是否立即同持久化到数据库
	 */
	@Override
	public void update(boolean sync) {
		//新增包不能直接调用update函数 请先调用persist
		if(record.getStatus() == DBConsts.RECORD_STATUS_NEW) {
			throw new SysException("新增包不能直接调用update函数，请先调用persist：data={}", this);
		}
		
		//升级包
		Chunk path = record.pathUpdateGen();
		if(path == null || path.length == 0) return;

		//将升级包同步至DB服务器
		DB prx = DB.newInstance(getTableName());
		prx.update(getId(), path, sync);
		
		//回收缓冲包
		BufferPool.deallocate(path.buffer);
		
		//重置状态
		record.resetStatus();
	}

	/**
	 * 删除数据
	 */
	@Override
	public void remove() {
		DB prx = DB.newInstance(getTableName());
		prx.delete(getId());
	}

	/**
	 * id
	 */
	public long getId() {
		return record.get("id");
	}

	public void setId(final long id) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("id", id);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * GM权限：0无任何权限，1有全部权限
	 */
	public int getGMPrivilege() {
		return record.get("GMPrivilege");
	}

	public void setGMPrivilege(final int GMPrivilege) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GMPrivilege", GMPrivilege);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否开启客户端调试
	 */
	public boolean isClientOpenSRDebug() {
		return record.<Integer>get("ClientOpenSRDebug") == 1;
	}

	public void setClientOpenSRDebug(boolean ClientOpenSRDebug) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ClientOpenSRDebug", ClientOpenSRDebug ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 客户端调试日志级别
	 */
	public int getClientLogType() {
		return record.get("ClientLogType");
	}

	public void setClientLogType(final int ClientLogType) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ClientLogType", ClientLogType);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 平台Id2位
	 */
	public int getPlatformId() {
		return record.get("PlatformId");
	}

	public void setPlatformId(final int PlatformId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("PlatformId", PlatformId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 服务器Id4位
	 */
	public int getServerId() {
		return record.get("ServerId");
	}

	public void setServerId(final int ServerId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ServerId", ServerId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 玩家标识6位（不可改变字段名）
	 */
	public int getHumanDigit() {
		return record.get("HumanDigit");
	}

	public void setHumanDigit(final int HumanDigit) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HumanDigit", HumanDigit);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 登录类型ELoginType
	 */
	public int getLoginType() {
		return record.get("LoginType");
	}

	public void setLoginType(final int LoginType) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LoginType", LoginType);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 登录账号
	 */
	public String getAccountLogin() {
		return record.get("AccountLogin");
	}

	public void setAccountLogin(final String AccountLogin) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("AccountLogin", AccountLogin);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 唯一账号ID
	 */
	public String getAccountId() {
		return record.get("AccountId");
	}

	public void setAccountId(final String AccountId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("AccountId", AccountId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 渠道
	 */
	public String getChannel() {
		return record.get("Channel");
	}

	public void setChannel(final String Channel) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Channel", Channel);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 设备MAC地址
	 */
	public String getDevMAC() {
		return record.get("DevMAC");
	}

	public void setDevMAC(final String DevMAC) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DevMAC", DevMAC);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 设备IMEI号
	 */
	public long getDevIMEI() {
		return record.get("DevIMEI");
	}

	public void setDevIMEI(final long DevIMEI) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DevIMEI", DevIMEI);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 设备类型：0PC，1IOS，2越狱，3安卓
	 */
	public int getDevType() {
		return record.get("DevType");
	}

	public void setDevType(final int DevType) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DevType", DevType);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 在线时间累积秒数
	 */
	public int getTimeSecOnline() {
		return record.get("TimeSecOnline");
	}

	public void setTimeSecOnline(final int TimeSecOnline) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TimeSecOnline", TimeSecOnline);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 在线时间
	 */
	public long getTimeOnLine() {
		return record.get("TimeOnLine");
	}

	public void setTimeOnLine(final long TimeOnLine) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TimeOnLine", TimeOnLine);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 最后一次登录时间
	 */
	public long getTimeLogin() {
		return record.get("TimeLogin");
	}

	public void setTimeLogin(final long TimeLogin) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TimeLogin", TimeLogin);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 最后一次登出时间
	 */
	public long getTimeLogout() {
		return record.get("TimeLogout");
	}

	public void setTimeLogout(final long TimeLogout) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TimeLogout", TimeLogout);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 角色创建时间
	 */
	public long getTimeCreate() {
		return record.get("TimeCreate");
	}

	public void setTimeCreate(final long TimeCreate) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TimeCreate", TimeCreate);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 角色SessionKey
	 */
	public long getSessionKey() {
		return record.get("SessionKey");
	}

	public void setSessionKey(final long SessionKey) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SessionKey", SessionKey);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否第一次创角
	 */
	public boolean isFirstCreateHuman() {
		return record.<Integer>get("FirstCreateHuman") == 1;
	}

	public void setFirstCreateHuman(boolean FirstCreateHuman) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FirstCreateHuman", FirstCreateHuman ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 头像sn
	 */
	public int getHeadSn() {
		return record.get("HeadSn");
	}

	public void setHeadSn(final int HeadSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HeadSn", HeadSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 坐骑sn
	 */
	public int getMountSn() {
		return record.get("MountSn");
	}

	public void setMountSn(final int MountSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("MountSn", MountSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 时装sn
	 */
	public int getFashionSn() {
		return record.get("FashionSn");
	}

	public void setFashionSn(final int FashionSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FashionSn", FashionSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 变装sn
	 */
	public int getHenshinSn() {
		return record.get("HenshinSn");
	}

	public void setHenshinSn(final int HenshinSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HenshinSn", HenshinSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 称号sn
	 */
	public int getTitleSn() {
		return record.get("TitleSn");
	}

	public void setTitleSn(final int TitleSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TitleSn", TitleSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否显示称号
	 */
	public boolean isTitleShow() {
		return record.<Integer>get("TitleShow") == 1;
	}

	public void setTitleShow(boolean TitleShow) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TitleShow", TitleShow ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 补偿邮件相关
	 */
	public String getFillMailJson() {
		return record.get("fillMailJson");
	}

	public void setFillMailJson(final String fillMailJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("fillMailJson", fillMailJson);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * VIP等级
	 */
	public int getVipLevel() {
		return record.get("VipLevel");
	}

	public void setVipLevel(final int VipLevel) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("VipLevel", VipLevel);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 累计充值金额
	 */
	public long getChargeGold() {
		return record.get("ChargeGold");
	}

	public void setChargeGold(final long ChargeGold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ChargeGold", ChargeGold);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日充值金额
	 */
	public long getTodayChargeGold() {
		return record.get("TodayChargeGold");
	}

	public void setTodayChargeGold(final long TodayChargeGold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TodayChargeGold", TodayChargeGold);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 充值信息
	 */
	public String getPayCharge() {
		return record.get("PayCharge");
	}

	public void setPayCharge(final String PayCharge) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("PayCharge", PayCharge);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 可按天返还的充值信息{商品sn, 充值次数, 返还结束时间}
	 */
	public String getChargeInfo() {
		return record.get("ChargeInfo");
	}

	public void setChargeInfo(final String ChargeInfo) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ChargeInfo", ChargeInfo);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 1 铜币
	 */
	public long getCoin() {
		return record.get("Coin");
	}

	public void setCoin(final long Coin) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Coin", Coin);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 2 元宝
	 */
	public long getGold() {
		return record.get("Gold");
	}

	public void setGold(final long Gold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Gold", Gold);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 3 体力值
	 */
	public long getAct() {
		return record.get("Act");
	}

	public void setAct(final long Act) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Act", Act);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 4 经验值
	 */
	public long getExp() {
		return record.get("Exp");
	}

	public void setExp(final long Exp) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Exp", Exp);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 5 威望
	 */
	public long getCompeteToken() {
		return record.get("CompeteToken");
	}

	public void setCompeteToken(final long CompeteToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("CompeteToken", CompeteToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 6 元魂
	 */
	public long getSoulToken() {
		return record.get("SoulToken");
	}

	public void setSoulToken(final long SoulToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SoulToken", SoulToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 7 远征积分
	 */
	public long getTowerToken() {
		return record.get("TowerToken");
	}

	public void setTowerToken(final long TowerToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TowerToken", TowerToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 8 将魂
	 */
	public long getGeneralToken() {
		return record.get("GeneralToken");
	}

	public void setGeneralToken(final long GeneralToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GeneralToken", GeneralToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 9 招募代币
	 */
	public long getSummonToken() {
		return record.get("SummonToken");
	}

	public void setSummonToken(final long SummonToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SummonToken", SummonToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 10 伙伴经验池
	 */
	public long getParnterExp() {
		return record.get("ParnterExp");
	}

	public void setParnterExp(final long ParnterExp) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ParnterExp", ParnterExp);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 11 纹石碎片
	 */
	public long getRuneToken() {
		return record.get("RuneToken");
	}

	public void setRuneToken(final long RuneToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RuneToken", RuneToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 12 抢夺本单人代币
	 */
	public long getLootSingle() {
		return record.get("LootSingle");
	}

	public void setLootSingle(final long LootSingle) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LootSingle", LootSingle);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 13 抢夺本多人代币
	 */
	public long getLootMultiple() {
		return record.get("LootMultiple");
	}

	public void setLootMultiple(final long LootMultiple) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LootMultiple", LootMultiple);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 16 招募赠送代币（仙缘）
	 */
	public long getSummonPresent() {
		return record.get("SummonPresent");
	}

	public void setSummonPresent(final long SummonPresent) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SummonPresent", SummonPresent);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 17 高级招募令
	 */
	public long getSummonHigher() {
		return record.get("SummonHigher");
	}

	public void setSummonHigher(final long SummonHigher) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SummonHigher", SummonHigher);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 18 刷新令
	 */
	public long getRefreshToken() {
		return record.get("RefreshToken");
	}

	public void setRefreshToken(final long RefreshToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RefreshToken", RefreshToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 19 阅历（技能修炼）
	 */
	public long getSkillExperience() {
		return record.get("SkillExperience");
	}

	public void setSkillExperience(final long SkillExperience) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SkillExperience", SkillExperience);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 20 精炼石
	 */
	public long getRefineToken() {
		return record.get("RefineToken");
	}

	public void setRefineToken(final long RefineToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RefineToken", RefineToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 21 招募获得积分
	 */
	public long getSummonScore() {
		return record.get("SummonScore");
	}

	public void setSummonScore(final long SummonScore) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SummonScore", SummonScore);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 22 法宝灵气
	 */
	public long getCimeliaToken() {
		return record.get("CimeliaToken");
	}

	public void setCimeliaToken(final long CimeliaToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("CimeliaToken", CimeliaToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 23 开采令
	 */
	public long getDevelopmentToken() {
		return record.get("DevelopmentToken");
	}

	public void setDevelopmentToken(final long DevelopmentToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DevelopmentToken", DevelopmentToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 24 抢夺令
	 */
	public long getSnatchToken() {
		return record.get("SnatchToken");
	}

	public void setSnatchToken(final long SnatchToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SnatchToken", SnatchToken);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 25 修炼重置石
	 */
	public long getResetStone() {
		return record.get("ResetStone");
	}

	public void setResetStone(final long ResetStone) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ResetStone", ResetStone);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 26 仙盟币
	 */
	public long getGuildCoin() {
		return record.get("GuildCoin");
	}

	public void setGuildCoin(final long GuildCoin) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildCoin", GuildCoin);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 最大体力值
	 */
	public int getActMax() {
		return record.get("ActMax");
	}

	public void setActMax(final int ActMax) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ActMax", ActMax);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 地图位置信息{{id,sn,x,y,common},{}}
	 */
	public String getStageHistory() {
		return record.get("StageHistory");
	}

	public void setStageHistory(final String StageHistory) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("StageHistory", StageHistory);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否显示时装,true显示
	 */
	public boolean isFashionShow() {
		return record.<Integer>get("FashionShow") == 1;
	}

	public void setFashionShow(boolean FashionShow) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FashionShow", FashionShow ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 时装武器sn
	 */
	public int getFashionWeaponSn() {
		return record.get("FashionWeaponSn");
	}

	public void setFashionWeaponSn(final int FashionWeaponSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FashionWeaponSn", FashionWeaponSn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 主公装备槽
	 */
	public String getEquipPosJSON() {
		return record.get("EquipPosJSON");
	}

	public void setEquipPosJSON(final String EquipPosJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("EquipPosJSON", EquipPosJSON);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 当前背包最大格子数
	 */
	public int getItemBagNumMax() {
		return record.get("ItemBagNumMax");
	}

	public void setItemBagNumMax(final int ItemBagNumMax) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ItemBagNumMax", ItemBagNumMax);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 所属队伍ID
	 */
	public int getTeamId() {
		return record.get("TeamId");
	}

	public void setTeamId(final int TeamId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TeamId", TeamId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 活动副本之魔王梦境最后通关记录{type2:difficulty}
	 */
	public String getInstActMHXKWarLastPass() {
		return record.get("InstActMHXKWarLastPass");
	}

	public void setInstActMHXKWarLastPass(final String InstActMHXKWarLastPass) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("InstActMHXKWarLastPass", InstActMHXKWarLastPass);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 活动副本之经典战役最后通关记录{type2:difficulty}
	 */
	public String getInstActStoryLastPass() {
		return record.get("InstActStoryLastPass");
	}

	public void setInstActStoryLastPass(final String InstActStoryLastPass) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("InstActStoryLastPass", InstActStoryLastPass);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 活动副本之经典战役领奖记录{type2:EAwardType}
	 */
	public String getInstActStoryAward() {
		return record.get("InstActStoryAward");
	}

	public void setInstActStoryAward(final String InstActStoryAward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("InstActStoryAward", InstActStoryAward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 抽卡的各个栏位冷却时间, {{1,12312312321},{},{}}
	 */
	public String getLottoryCooldown() {
		return record.get("LottoryCooldown");
	}

	public void setLottoryCooldown(final String LottoryCooldown) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LottoryCooldown", LottoryCooldown);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 已经抽过的宝箱类型信息 11|21|31|
	 */
	public String getLottorySelectedCount() {
		return record.get("LottorySelectedCount");
	}

	public void setLottorySelectedCount(final String LottorySelectedCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LottorySelectedCount", LottorySelectedCount);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 免费抽奖次数
	 */
	public int getLottoryFreeCount() {
		return record.get("LottoryFreeCount");
	}

	public void setLottoryFreeCount(final int LottoryFreeCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LottoryFreeCount", LottoryFreeCount);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 占星的各个栏位冷却时间, {{1,12312312321},{},{}}
	 */
	public String getHoroCooldown() {
		return record.get("HoroCooldown");
	}

	public void setHoroCooldown(final String HoroCooldown) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HoroCooldown", HoroCooldown);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 第一，二栏位，免费抽奖情况{pos1,status;pos2,status}sta:0或1
	 */
	public String getHoroIsFirst() {
		return record.get("HoroIsFirst");
	}

	public void setHoroIsFirst(final String HoroIsFirst) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HoroIsFirst", HoroIsFirst);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 免费抽奖次数,结构是1,1,1
	 */
	public String getHoroFreeCount() {
		return record.get("HoroFreeCount");
	}

	public void setHoroFreeCount(final String HoroFreeCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HoroFreeCount", HoroFreeCount);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 极品间隔次数,结构是1,1,1
	 */
	public String getHoroJPGap() {
		return record.get("HoroJPGap");
	}

	public void setHoroJPGap(final String HoroJPGap) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HoroJPGap", HoroJPGap);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 极品次数累计,结构是1,1,1
	 */
	public String getHoroJPCount() {
		return record.get("HoroJPCount");
	}

	public void setHoroJPCount(final String HoroJPCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HoroJPCount", HoroJPCount);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否已经过了改名任务
	 */
	public boolean isQuestNameChangePassed() {
		return record.<Integer>get("QuestNameChangePassed") == 1;
	}

	public void setQuestNameChangePassed(boolean QuestNameChangePassed) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("QuestNameChangePassed", QuestNameChangePassed ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 创角后的第一个新手剧情
	 */
	public int getFirstStory() {
		return record.get("FirstStory");
	}

	public void setFirstStory(final int FirstStory) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FirstStory", FirstStory);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 玩家新手引导是否关闭,true 关闭，false 打开
	 */
	public boolean isGuideClose() {
		return record.<Integer>get("GuideClose") == 1;
	}

	public void setGuideClose(boolean GuideClose) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuideClose", GuideClose ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 玩家新手引导ID
	 */
	public String getGuideIds() {
		return record.get("GuideIds");
	}

	public void setGuideIds(final String GuideIds) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuideIds", GuideIds);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 技能设置1
	 */
	public String getSkillSet1() {
		return record.get("SkillSet1");
	}

	public void setSkillSet1(final String SkillSet1) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SkillSet1", SkillSet1);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 技能升级材料总消耗
	 */
	public long getSkillStuffCost() {
		return record.get("SkillStuffCost");
	}

	public void setSkillStuffCost(final long SkillStuffCost) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SkillStuffCost", SkillStuffCost);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日已购买体力次数 
	 */
	public int getDailyActBuyNum() {
		return record.get("DailyActBuyNum");
	}

	public void setDailyActBuyNum(final int DailyActBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyActBuyNum", DailyActBuyNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日已购买铜币次数 
	 */
	public int getDailyCoinBuyNum() {
		return record.get("DailyCoinBuyNum");
	}

	public void setDailyCoinBuyNum(final int DailyCoinBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyCoinBuyNum", DailyCoinBuyNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日已购买竞技场挑战次数 
	 */
	public int getDailyCompeteFightBuyNum() {
		return record.get("DailyCompeteFightBuyNum");
	}

	public void setDailyCompeteFightBuyNum(final int DailyCompeteFightBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyCompeteFightBuyNum", DailyCompeteFightBuyNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日已购买抢夺本复活次数
	 */
	public int getDailyLootMapRevivalBuyNum() {
		return record.get("DailyLootMapRevivalBuyNum");
	}

	public void setDailyLootMapRevivalBuyNum(final int DailyLootMapRevivalBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyLootMapRevivalBuyNum", DailyLootMapRevivalBuyNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日已发送魔法表情次数
	 */
	public int getDailySendWinksNum() {
		return record.get("DailySendWinksNum");
	}

	public void setDailySendWinksNum(final int DailySendWinksNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailySendWinksNum", DailySendWinksNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日已主城切磋胜利次数
	 */
	public int getDailyFightWinNum() {
		return record.get("DailyFightWinNum");
	}

	public void setDailyFightWinNum(final int DailyFightWinNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyFightWinNum", DailyFightWinNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 今日在线时间
	 */
	public long getDailyOnlineTime() {
		return record.get("DailyOnlineTime");
	}

	public void setDailyOnlineTime(final long DailyOnlineTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyOnlineTime", DailyOnlineTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日签到：0未签到,1已签到
	 */
	public int getDailySignFlag() {
		return record.get("DailySignFlag");
	}

	public void setDailySignFlag(final int DailySignFlag) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailySignFlag", DailySignFlag);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日竞技场已挑战次数
	 */
	public int getDailyCompeteFightNum() {
		return record.get("DailyCompeteFightNum");
	}

	public void setDailyCompeteFightNum(final int DailyCompeteFightNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyCompeteFightNum", DailyCompeteFightNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日竞技场积分
	 */
	public int getDailyCompeteIntegral() {
		return record.get("DailyCompeteIntegral");
	}

	public void setDailyCompeteIntegral(final int DailyCompeteIntegral) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyCompeteIntegral", DailyCompeteIntegral);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日竞技场已经领取的积分sn
	 */
	public String getDailyCompeteIntegralAward() {
		return record.get("DailyCompeteIntegralAward");
	}

	public void setDailyCompeteIntegralAward(final String DailyCompeteIntegralAward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyCompeteIntegralAward", DailyCompeteIntegralAward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日活动副本已完成次数{sn:1,...}
	 */
	public String getDailyInstFinishNum() {
		return record.get("DailyInstFinishNum");
	}

	public void setDailyInstFinishNum(final String DailyInstFinishNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyInstFinishNum", DailyInstFinishNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日排行榜膜拜次数{0:1,2:1,3:1,4:1}
	 */
	public String getDailyRankWorship() {
		return record.get("DailyRankWorship");
	}

	public void setDailyRankWorship(final String DailyRankWorship) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyRankWorship", DailyRankWorship);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日任务活跃度，需每日清0
	 */
	public int getDailyQuestLiveness() {
		return record.get("DailyQuestLiveness");
	}

	public void setDailyQuestLiveness(final int DailyQuestLiveness) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyQuestLiveness", DailyQuestLiveness);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日活跃度奖励已领取记录，需每日清空
	 */
	public String getDailyLivenessReward() {
		return record.get("DailyLivenessReward");
	}

	public void setDailyLivenessReward(final String DailyLivenessReward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DailyLivenessReward", DailyLivenessReward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每周任务活跃度，需每周清0
	 */
	public int getWeeklyQuestLiveness() {
		return record.get("WeeklyQuestLiveness");
	}

	public void setWeeklyQuestLiveness(final int WeeklyQuestLiveness) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WeeklyQuestLiveness", WeeklyQuestLiveness);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每周活跃度奖励已领取记录，需每周清空
	 */
	public String getWeeklyLivenessReward() {
		return record.get("WeeklyLivenessReward");
	}

	public void setWeeklyLivenessReward(final String WeeklyLivenessReward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WeeklyLivenessReward", WeeklyLivenessReward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 恢复满体力的时间点
	 */
	public long getActFullTime() {
		return record.get("ActFullTime");
	}

	public void setActFullTime(final long ActFullTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ActFullTime", ActFullTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 补签次数
	 */
	public int getResigntimes() {
		return record.get("Resigntimes");
	}

	public void setResigntimes(final int Resigntimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Resigntimes", Resigntimes);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 福利号上次领取福利时间，一星期领取一次
	 */
	public long getTimeFuli() {
		return record.get("TimeFuli");
	}

	public void setTimeFuli(final long TimeFuli) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TimeFuli", TimeFuli);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否是福利号
	 */
	public boolean isFuli() {
		return record.<Integer>get("Fuli") == 1;
	}

	public void setFuli(boolean Fuli) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Fuli", Fuli ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否是GS
	 */
	public boolean isGs() {
		return record.<Integer>get("Gs") == 1;
	}

	public void setGs(boolean Gs) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Gs", Gs ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * GS号的虚拟人民币数量
	 */
	public int getGsRmb() {
		return record.get("GsRmb");
	}

	public void setGsRmb(final int GsRmb) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GsRmb", GsRmb);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 禁言的玩家结束时间
	 */
	public long getSilenceEndTime() {
		return record.get("SilenceEndTime");
	}

	public void setSilenceEndTime(final long SilenceEndTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SilenceEndTime", SilenceEndTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 封号的玩家结束时间
	 */
	public long getSealEndTime() {
		return record.get("SealEndTime");
	}

	public void setSealEndTime(final long SealEndTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SealEndTime", SealEndTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否是已删除的玩家，0 未删除，1 已删除
	 */
	public int getDeleteRole() {
		return record.get("DeleteRole");
	}

	public void setDeleteRole(final int DeleteRole) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DeleteRole", DeleteRole);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 已改名次数
	 */
	public int getRenameNum() {
		return record.get("RenameNum");
	}

	public void setRenameNum(final int RenameNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RenameNum", RenameNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 已解锁的功能模块sn(只存任务类型的功能模块sn)
	 */
	public String getModUnlock() {
		return record.get("ModUnlock");
	}

	public void setModUnlock(final String ModUnlock) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ModUnlock", ModUnlock);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 功能开放领取奖励
	 */
	public String getModUnlockReward() {
		return record.get("ModUnlockReward");
	}

	public void setModUnlockReward(final String ModUnlockReward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ModUnlockReward", ModUnlockReward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 副本排行星星数
	 */
	public int getRankInstStars() {
		return record.get("RankInstStars");
	}

	public void setRankInstStars(final int RankInstStars) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RankInstStars", RankInstStars);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 竞技场最高排名
	 */
	public int getCompeteRank() {
		return record.get("CompeteRank");
	}

	public void setCompeteRank(final int CompeteRank) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("CompeteRank", CompeteRank);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 总战力
	 */
	public int getSumCombat() {
		return record.get("SumCombat");
	}

	public void setSumCombat(final int SumCombat) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SumCombat", SumCombat);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 伙伴总战力
	 */
	public int getGeneralSumCombat() {
		return record.get("GeneralSumCombat");
	}

	public void setGeneralSumCombat(final int GeneralSumCombat) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GeneralSumCombat", GeneralSumCombat);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 伙伴最强阵容总战力
	 */
	public int getGeneralMostCombat() {
		return record.get("GeneralMostCombat");
	}

	public void setGeneralMostCombat(final int GeneralMostCombat) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GeneralMostCombat", GeneralMostCombat);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 帮会贡献
	 */
	public long getContribute() {
		return record.get("Contribute");
	}

	public void setContribute(final long Contribute) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Contribute", Contribute);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 帮会ID
	 */
	public long getGuildId() {
		return record.get("GuildId");
	}

	public void setGuildId(final long GuildId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildId", GuildId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 上次离开帮会时间
	 */
	public long getGuildLeaveTime() {
		return record.get("GuildLeaveTime");
	}

	public void setGuildLeaveTime(final long GuildLeaveTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildLeaveTime", GuildLeaveTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 工会等级
	 */
	public int getGuildLevel() {
		return record.get("GuildLevel");
	}

	public void setGuildLevel(final int GuildLevel) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildLevel", GuildLevel);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 是否是会长
	 */
	public boolean isGuildCDR() {
		return record.<Integer>get("GuildCDR") == 1;
	}

	public void setGuildCDR(boolean GuildCDR) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildCDR", GuildCDR ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 宝箱领取
	 */
	public String getGuildImmoSnList() {
		return record.get("GuildImmoSnList");
	}

	public void setGuildImmoSnList(final String GuildImmoSnList) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildImmoSnList", GuildImmoSnList);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会技能
	 */
	public String getGuildSkills() {
		return record.get("GuildSkills");
	}

	public void setGuildSkills(final String GuildSkills) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildSkills", GuildSkills);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会名称
	 */
	public String getGuildName() {
		return record.get("GuildName");
	}

	public void setGuildName(final String GuildName) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildName", GuildName);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 活动积分
	 */
	public int getActivityIntegral() {
		return record.get("ActivityIntegral");
	}

	public void setActivityIntegral(final int ActivityIntegral) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ActivityIntegral", ActivityIntegral);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 活动积分已领取宝箱sn
	 */
	public String getIntegralBoxList() {
		return record.get("IntegralBoxList");
	}

	public void setIntegralBoxList(final String IntegralBoxList) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("IntegralBoxList", IntegralBoxList);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 购买开采令次数
	 */
	public long getBuyDevelopmentTokenCount() {
		return record.get("BuyDevelopmentTokenCount");
	}

	public void setBuyDevelopmentTokenCount(final long BuyDevelopmentTokenCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("BuyDevelopmentTokenCount", BuyDevelopmentTokenCount);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 购买抢夺令次数
	 */
	public long getBuySnatchTokeCountn() {
		return record.get("BuySnatchTokeCountn");
	}

	public void setBuySnatchTokeCountn(final long BuySnatchTokeCountn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("BuySnatchTokeCountn", BuySnatchTokeCountn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 已登陆天数
	 */
	public int getLoginNum() {
		return record.get("LoginNum");
	}

	public void setLoginNum(final int LoginNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LoginNum", LoginNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 7日登陆当前奖励天数
	 */
	public int getLoginNumAwardStatus() {
		return record.get("LoginNumAwardStatus");
	}

	public void setLoginNumAwardStatus(final int LoginNumAwardStatus) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LoginNumAwardStatus", LoginNumAwardStatus);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 领取登陆奖励时间
	 */
	public long getGetloginAwardTime() {
		return record.get("GetloginAwardTime");
	}

	public void setGetloginAwardTime(final long GetloginAwardTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GetloginAwardTime", GetloginAwardTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本挑战次数
	 */
	public int getGuildInstChallengeTimes() {
		return record.get("GuildInstChallengeTimes");
	}

	public void setGuildInstChallengeTimes(final int GuildInstChallengeTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInstChallengeTimes", GuildInstChallengeTimes);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本挑战次数添加时间
	 */
	public long getGuildInstChallengeTimesAddTime() {
		return record.get("GuildInstChallengeTimesAddTime");
	}

	public void setGuildInstChallengeTimesAddTime(final long GuildInstChallengeTimesAddTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInstChallengeTimesAddTime", GuildInstChallengeTimesAddTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本章节奖励
	 */
	public String getGuildInstChapterReward() {
		return record.get("GuildInstChapterReward");
	}

	public void setGuildInstChapterReward(final String GuildInstChapterReward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInstChapterReward", GuildInstChapterReward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本关卡奖励
	 */
	public String getGuildInstStageReward() {
		return record.get("GuildInstStageReward");
	}

	public void setGuildInstStageReward(final String GuildInstStageReward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInstStageReward", GuildInstStageReward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本重置时间
	 */
	public long getGuildInstResetTime() {
		return record.get("GuildInstResetTime");
	}

	public void setGuildInstResetTime(final long GuildInstResetTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInstResetTime", GuildInstResetTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本已购买挑战次数
	 */
	public int getGuildInstBuyChallengeTimes() {
		return record.get("GuildInstBuyChallengeTimes");
	}

	public void setGuildInstBuyChallengeTimes(final int GuildInstBuyChallengeTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInstBuyChallengeTimes", GuildInstBuyChallengeTimes);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}

}