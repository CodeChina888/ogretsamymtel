package game.worldsrv.human;

import game.msg.Define.DDailyCostBuy;
import game.msg.Define.DDailyReset;
import game.msg.Define.DGuideInfo;
import game.msg.Define.DHuman;
import game.msg.Define.DMoney;
import game.msg.Define.DProp;
import game.msg.Define.DShowInfo;
import game.msg.Define.DSkillGroup;
import game.msg.Define.DVipPayment;
import game.msg.Define.DWeeklyReset;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ELoginType;
import game.msg.Define.EMapType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EPropChangeType;
import game.msg.Define.EReviveType;
import game.msg.Define.ESignType;
import game.msg.Define.EVipBuyType;
import game.msg.MsgAccount.SCAccountBind;
import game.msg.MsgCommon.SCActFullTimeChange;
import game.msg.MsgCommon.SCDailyActBuy;
import game.msg.MsgCommon.SCDailyCoinBuy;
import game.msg.MsgCommon.SCDailyCostBuyChange;
import game.msg.MsgCommon.SCDailyResetChange;
import game.msg.MsgCommon.SCDebugClient;
import game.msg.MsgCommon.SCHumanData;
import game.msg.MsgCommon.SCInitData;
import game.msg.MsgCommon.SCLevelChange;
import game.msg.MsgCommon.SCPropInfoChange;
import game.msg.MsgCommon.SCTeamBundleIDChange;
import game.msg.MsgFight.SCFightRevive;
import game.msg.MsgFight.SCRecordFightInfo;
import game.platform.DistrPF;
import game.platform.login.LoginServiceProxy;
import game.seam.account.AccountObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitDataPersistance;
import game.worldsrv.character.UnitManager;
import game.worldsrv.character.UnitObject;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfRoleModel;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.entity.Backlog;
import game.worldsrv.entity.Buff;
import game.worldsrv.entity.CultureTimes;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.enumType.BacklogType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightrecord.RecordInfo;
import game.worldsrv.guild.GuildManager;
import game.worldsrv.immortalCave.CaveManager;
import game.worldsrv.integration.PF_PAY_Manager;
import game.worldsrv.item.Item;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.item.ItemPack;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.payment.PaymentManager;
import game.worldsrv.rank.RankManager;
import game.worldsrv.stage.StageBattleManager;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectReplay;
import game.worldsrv.support.C;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.MathUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;
import game.worldsrv.vip.VipManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;

public class HumanManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static HumanManager inst() {
		return inst(HumanManager.class);
	}

	public void onScheduleEvent(HumanObject humanObj, int key, long timeNow) {
	    Log.game.info("---------------- onScheduleEvent {} {}", key, timeNow);
		Event.fire(key, "humanObj", humanObj, "timeLoginLast", timeNow);
	}
	
	
	/**
	 * 绑定账号
	 */
	public void bindAccount(HumanObject humanObj, ELoginType loginType, String accountId, String password) {
		// 原账号为电脑或游客登录类型，才可绑定账号
		if (humanObj.getHuman().getLoginType() != ELoginType.PC.getNumber()) {
			sendSCAccountBind(humanObj, 11301);// 11301非游客帐号不允许绑定
			return;
		}
		// 不能绑定到电脑或游客登录类型
		if (loginType == ELoginType.PC) {
			sendSCAccountBind(humanObj, 11302);// 11302不允许绑定为游客
			return;
		}
		// 连接一个随机的验证服务
		String portLogin = D.PORT_PLATFORM_LOGIN_PREFIX + new Random().nextInt(D.PORT_WORLD_STARTUP_PLATFORM_LOGIN);
		LoginServiceProxy loginServ = LoginServiceProxy.newInstance(DistrPF.NODE_ID, portLogin, DistrPF.SERV_LOGIN);
		loginServ.check(loginType.getNumber(), accountId, password,"",Port.getTime()+"");
		loginServ.listenResult(this::_result_msg_CSAccountBind, "humanObj", humanObj, "accountId", accountId);
	}

	private void _result_msg_CSAccountBind(Param results, Param context) {
		String accountId = Utils.getParamValue(context, "accountId", "");
		if (accountId.isEmpty()) {
			Log.game.error("===HumanManager._result_msg_CSAccountBind accountId.isEmpty()");
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===HumanManager._result_msg_CSAccountBind humanObj is null");
			return;
		}
		// 登录验证结果
		int resultCode = results.getInt();// 0成功，非0错误码
		if (resultCode != 0) {// 平台服登录验证失败
			sendSCAccountBind(humanObj, 100);// 100账号验证失败，请重新登录
			Log.game.info("===登陆失败：平台服登陆验证失败！_result_msg_CSAccountBind accountId={}", accountId);
			return;
		}
		// 查询数据account是否已经绑定
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.countBy(false, "accountId", accountId);
		dbPrx.listenResult(this::_result_isAccountExist, "humanObj", humanObj, "accountId", accountId);
	}

	/**
	 * 查询是否有多个帐号
	 * 
	 * @param results
	 * @param context
	 */
	private void _result_isAccountExist(Param results, Param context) {
		int count = results.get();
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===HumanManager._result_isAccountExist humanObj is null");
			return;
		}
		if (count > 0) {
			sendSCAccountBind(humanObj, 11303);// 11303该帐号已经被其他人绑定，请用其他帐号
			return;
		}
		String accountId = Utils.getParamValue(context, "accountId", "");
		if (accountId.isEmpty()) {
			Log.game.error("===HumanManager._result_isAccountExist accountId.isEmpty()");
			return;
		}
		// 更改账号
		humanObj.getHuman().setAccountId(accountId);
		sendSCAccountBind(humanObj, 0);
	}

	/**
	 * 发送绑定账号返回结果
	 * 
	 * @param humanObj
	 * @param resultCode
	 *            返回结果编号：0成功，非0失败（即sysMsg中的sn）
	 */
	private void sendSCAccountBind(HumanObject humanObj, int resultCode) {
		SCAccountBind.Builder msg = SCAccountBind.newBuilder();
		msg.setResultCode(resultCode);
		humanObj.sendMsg(msg);
	}

	/**
	 * 创建玩家
	 * 
	 * @param id 玩家ID
	 * @param accObj 帐号对象
	 * @param name 名字
	 * @param profession 职业
	 * @return
	 */
	public Human create(long id, AccountObject accObj, String name, int profession) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(profession);
		if (null == conf) {
			Log.table.error("===创角失败：ConfPartnerProperty配表错误，no find sn={} ", profession);
			return null;
		}
		ConfMap confMap = ConfMap.get(ParamManager.firstMapSn);
		if (confMap == null) {
			Log.table.error("===创角失败：ConfMap配表错误，no find sn={} ", ParamManager.firstMapSn);
			return null;
		}
		// 获取创角后的第一个场景地图的信息
		Vector2D pos = StageManager.inst().getHumanPos(ParamManager.firstMapSn);
		Vector2D posRand = StageBattleManager.inst().randomPosInCircle(pos, 0, 3);
		List<?> temp = Utils.ofList(confMap.sn, confMap.sn, Utils.round(posRand.x), Utils.round(posRand.y), confMap.type);
		List<?> stageInfo = Utils.ofList(temp);
		
		int serverId = accObj.getServerId(); 
		// 初始化玩家信息
		Human human = new Human();
		human.setId(id); // 玩家唯一id
		human.setPlatformId(C.GAME_PLATFORM_ID); // 平台Id2位
		human.setServerId(serverId); // 服务器Id4位
		if (0 == serverId && ParamManager.openGM) {
			// 0为测试服，且配置为开启GM命令，则创号就拥有GM权限
			human.setGMPrivilege(1);
		} else {// 默认关闭GM权限 TODO 正式服关闭 
			human.setGMPrivilege(0);
		}
		String humanDigit = Long.toString(id);
		humanDigit = humanDigit.substring(humanDigit.length() - 6);
		human.setHumanDigit(Utils.intValue(humanDigit)); // 玩家标识6位
		human.setFirstCreateHuman(true); // 标记为第一次创角
		human.setAccountLogin(accObj.getAccount()); // 登录帐号
		human.setAccountId(accObj.getAccountId()); // 唯一帐号ID
		human.setChannel(accObj.getChannel()); // 渠道
		human.setName(name); // 角色昵称
		human.setProfession(conf.type); // 职业
		human.setLevel(ParamManager.initHumanLevel); // 初始等级
		human.setTimeCreate(Port.getTime()); // 角色创建时间
		human.setTimeLogin(0L); // 最后登录时间
		human.setTimeLogout(0L); // 最后下线时间
		human.setItemBagNumMax(ParamManager.maxItemBagNum); // 背包最大格子数
		
		// 根据属性配表设置属性信息
		UnitManager.inst().initProperty(human, conf.sn);
		
		
		// 其他初始信息
		human.setAct(ParamManager.initHumanAct); // 初始体力
		human.setCoin(ParamManager.initHumanCoin); // 初始金币
		human.setGold(ParamManager.initHumanGold); // 初始钻石
		human.setSpeed(ParamManager.initHumanSpeed); // 初始化移动速度
		human.setLootSingle(ParamManager.initHumanLootMapSingleKey); // 初始化单人钥匙
		human.setLootMultiple(ParamManager.initHumanLootMapMultipleKey); //初始化多人钥匙
		human.setDevelopmentToken(ParamManager.domainDailyOccupyReset);//开采令（
		human.setSnatchToken(ParamManager.domainDailyGrabReset);//抢夺令
		// 设置体力
		ConfLevelExp confLv = ConfLevelExp.get(ParamManager.initHumanLevel);
		if (confLv == null) {
			Log.table.error("ConfLevelExp配表错误，no find sn={} ", ParamManager.initHumanLevel);
			return null;
		} else {
			human.setAct(confLv.staminaMax);
			human.setActMax(confLv.staminaMax); // 设置玩家体力上限
			human.setActFullTime(Port.getTime()); // 体力满值为创建角色时系统时间
		}

		// 设置地图信息
		human.setStageHistory(JSON.toJSONString(stageInfo));

		/* 在时装模块中初始化
		// 设置模型
		int modelSn = getHumanModelSn(profession);
		// 默认对应时装的模型sn
		human.setDefaultModelSn(modelSn);
		// 当前使用模型
		human.setModelSn(modelSn);
		human.setHeadSn(modelSn);
		*/
		
		// 初始化经验
		human.setExp(0);

		// 生成默认适用的武将阵容是副本阵容
//		human.setGenUseLineup(EGeneralLineup.LURep_VALUE);

		// 重置排行榜膜拜次数
//		RankManager.resetRankWorship(human, 0, 0, 0, 0);
		
		// 持久化
		human.persist();
		
		//培养统计计数表
		CultureTimes cultureTime = new CultureTimes();
		cultureTime.setId(human.getId());
		cultureTime.setHumanId(human.getId());
		cultureTime.persist();
		
		// 扩展表
		HumanExtInfo extInfo = new HumanExtInfo();
		extInfo.setId(id);
		// 设置竞技场相关
		//初始化剩余挑战次数
		extInfo.setSurplusNum(ParamManager.competeFreeNum);
		//伙伴相关初始化
		PartnerManager.inst().initLineup(extInfo);
		extInfo.persist();// 持久化

		// ***属性设置
		
		/* 创建好友信息 */
		//FriendManager.inst().createFriendList(id, name);
		/* 创建任务系统信息 */
		//QuestManager.inst().createQuest(human);
		
		human.setLoginType(accObj.getLoginType().getNumber());
		if (!accObj.getAccountBind().isEmpty()) {// 玩家账号绑定游客
			human.setAccountId(accObj.getAccountBind());
		}
		human.setDevMAC(accObj.getDevMAC());
		human.setDevIMEI(accObj.getDevIMEI());
		human.setDevType(accObj.getDevType());

		logOPHumanCreate(human, accObj.getClientIP());// 记录创角日志，包括：初始货币及注册
		
		// 通知创角
		Event.fire(EventKey.HumanCreate, "human", human);

		return human;
	}
	
	/**
	 * 记录创角日志，包括：初始货币及注册
	 * 
	 * @param human
	 */
	private void logOPHumanCreate(Human human, String clientIP) {
		LogSysModType log = LogSysModType.HumanCreate;
		if (human.getAct() > 0) {// 初始体力
			LogOpUtils.LogGain(human, log, EMoneyType.act_VALUE, human.getAct());
		}
		if (human.getExp() > 0) {// 初始exp
			LogOpUtils.LogGain(human, log, EMoneyType.exp_VALUE, human.getExp());
		}
		if (human.getCoin() > 0) {// 初始金币
			LogOpUtils.LogGain(human, log, EMoneyType.coin_VALUE, human.getCoin());
		}
		if (human.getGold() > 0) {// 初始钻石
			LogOpUtils.LogGain(human, log, EMoneyType.gold_VALUE, human.getGold());
		}
		if (human.getCompeteToken() > 0) {// 初始威望
			LogOpUtils.LogGain(human, log, EMoneyType.competeToken_VALUE, human.getCompeteToken());
		}
		if (human.getSoulToken() > 0) {// 初始元魂
			LogOpUtils.LogGain(human, log, EMoneyType.soulToken_VALUE, human.getSoulToken());
		}
		if (human.getSummonToken() > 0) {// 初始招募代币
			LogOpUtils.LogGain(human, log, EMoneyType.summonToken_VALUE, human.getSummonToken());
		}
		if (human.getParnterExp() > 0) {// 伙伴经验池
			LogOpUtils.LogGain(human, log, EMoneyType.parnterExp_VALUE, human.getParnterExp());
		}
		if (human.getRuneToken() > 0) {// 纹石碎片
			LogOpUtils.LogGain(human, log, EMoneyType.runeToken_VALUE, human.getRuneToken());
		}
		if (human.getLootSingle() > 0) {// 抢夺本单人
			LogOpUtils.LogGain(human, log, EMoneyType.lootSingle_VALUE, human.getLootSingle());
		}
		if (human.getLootMultiple() > 0) {// 抢夺本多人
			LogOpUtils.LogGain(human, log, EMoneyType.lootMultiple_VALUE, human.getLootMultiple());
		}
		LogOpUtils.LogRegister(human, clientIP);
	}

	/**
	 * 玩家登录游戏后，加载玩家的数据
	 */
	public void loadHumanAllData(HumanObject humanObj) {
		// 获取当前请求编号
		long pid = Port.getCurrent().createReturnAsync();
		humanObj.loadingPID = pid;

		// 先将human主数据加载好
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.get(humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanAllData, "humanObj", humanObj);
	}

	private void _result_loadHumanAllData(Param results, Param context) {
		// 玩家
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanAllData humanObj is null");
			return;
		}

		Record record = results.get();
		if (record == null) {
			Log.game.error("===_result_loadHumanDataMain record=null");
			return;
		}

		// 处理返回数据
		UnitDataPersistance data = humanObj.dataPers;
		Human human = new Human(record);
		data.unit = human;
		humanObj.modelSn = data.unit.getModelSn();
		humanObj.sn = data.unit.getSn();
		// 加载玩家扩展数据
		DB dbPrx = DB.newInstance(HumanExtInfo.tableName);
		dbPrx.get(humanObj.id);
		dbPrx.listenResult(this::_result_HumanDataLoadExtInfo, "humanObj", humanObj);
	}
	
	/**
	 * 玩家数据加载属性信息
	 */
	@Listener(EventKey.HumanDataLoadExtInfo)
	public void _listener_HumanDataLoadExtInfo(Param param) {
//		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
//		if (humanObj == null) {
//			Log.game.error("===_listener_HumanDataLoadExtInfo humanObj is null");
//			return;
//		}
//		DB dbPrx = DB.newInstance(HumanExtInfo.tableName);
//		dbPrx.get(humanObj.id);
//		dbPrx.listenResult(this::_result_HumanDataLoadExtInfo, "humanObj", humanObj);
//		// 玩家数据加载开始一个
//		Event.fire(EventKey.HumanDataLoadBaseBeginOne, "humanObj", humanObj);
	}

	private void _result_HumanDataLoadExtInfo(Param results, Param context) {
		Record record = results.get();
		if (record == null) {
			Log.game.error("===_result_HumanDataLoadExtInfo record=null");
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_HumanDataLoadExtInfo humanObj is null");
			return;
		}
		// 加载数据
		humanObj.extInfo = new HumanExtInfo(record);
		List<Long> battlePartners = Utils.strToLongList(humanObj.extInfo.getPartnerLineup());
		if (battlePartners!=null && battlePartners.isEmpty()==false) {
			int partnerNum = 0;
			boolean flag = false;
			for (int i = 0; i < battlePartners.size(); i ++) {
				long partnerId = battlePartners.get(i);
				if (partnerId>0 && ++partnerNum>=5) {
					battlePartners.set(i, 0l);
					flag = true;
				}
			}
			if (flag) {
				//玩家上阵伙伴超过限制处理
				humanObj.extInfo.setPartnerLineup(Utils.ListLongToStr(battlePartners));
			}
		}
		// 发布事件：玩家数据加载属性信息
		Event.fire(EventKey.HumanDataLoadUnitProp, "humanObj", humanObj);
		// 发布事件：玩家数据加载Buff信息
		Event.fire(EventKey.HumanDataLoadBuff, "humanObj", humanObj);
		// 发布事件：玩家数据加载待办事项
		Event.fire(EventKey.HumanDataLoadBacklog, "humanObj", humanObj);
		// 发布事件:加载玩家培养次数信息
		Event.fire(EventKey.HumanCultureTimes, "humanObj", humanObj);
//		// 玩家数据加载完成一个
//		Event.fire(EventKey.HumanDataLoadBaseFinishOne, "humanObj", humanObj);
	}

	/**
	 * 玩家数据加载属性信息
	 */
	@Listener(EventKey.HumanDataLoadUnitProp)
	public void _listener_HumanDataLoadUnitProp(Param param) {
//		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
//		if (humanObj == null) {
//			Log.game.error("===_listener_HumanDataLoadUnitProp humanObj is null");
//			return;
//		}
//		DB dbPrx = DB.newInstance(UnitPropPlus.tableName);
//		dbPrx.get(humanObj.id);
//		dbPrx.listenResult(this::_result_HumanDataLoadUnitProp, "humanObj", humanObj);
//		// 玩家数据加载开始一个
//		Event.fire(EventKey.HumanDataLoadBaseBeginOne, "humanObj", humanObj);
	}

//	private void _result_HumanDataLoadUnitProp(Param results, Param context) {
//		Record record = results.get();
//		if (record == null) {
//			Log.game.error("===_result_HumanDataLoadUnitProp record=null");
//			return;
//		}
//		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
//		if (humanObj == null) {
//			Log.game.error("===_result_HumanDataLoadUnitProp humanObj is null");
//			return;
//		}
//		// 加载数据
//		UnitPropPlus unitPropPlus = new UnitPropPlus(record);
//		humanObj.dataPers.unitPropPlus.init(unitPropPlus);
//		// 玩家数据加载完成一个
//		Event.fire(EventKey.HumanDataLoadBaseFinishOne, "humanObj", humanObj);
//	}

	/**
	 * 玩家数据加载Buff信息
	 */
	@Listener(EventKey.HumanDataLoadBuff)
	public void _listener_HumanDataLoadBuff(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadBuff humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Buff.tableName);
		dbPrx.findBy(false, Buff.K.IdAffect, humanObj.id);
		dbPrx.listenResult(this::_result_HumanDataLoadBuff, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadBaseBeginOne, "humanObj", humanObj);
	}

	private void _result_HumanDataLoadBuff(Param results, Param context) {
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_HumanDataLoadBuff records=null");
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_HumanDataLoadBuff humanObj is null");
			return;
		}
		// 加载数据
		for (Record record : records) {
			Buff buff = new Buff(record);
			humanObj.dataPers.buffs.put(buff.getType(), buff);
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadBaseFinishOne, "humanObj", humanObj);
	}

	/**
	 * 玩家数据加载待办事项
	 */
	@Listener(EventKey.HumanDataLoadBacklog)
	public void _listener_HumanDataLoadBacklog(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadBacklog humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Backlog.tableName);
		dbPrx.findBy(false, Backlog.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_HumanDataLoadBacklog, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadBaseBeginOne, "humanObj", humanObj);
	}

	private void _result_HumanDataLoadBacklog(Param results, Param context) {
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_HumanDataLoadBacklog records=null");
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_HumanDataLoadBacklog humanObj is null");
			return;
		}
		// 加载数据
		for (Record record : records) {
			Backlog backlog = new Backlog(record);
			String type = backlog.getType();
			if (type.equals(BacklogType.Pay.name())) {
				// 部分记录在human表的可直接处理
				PF_PAY_Manager.inst().backlogToPay(humanObj, backlog);
				backlog.remove();// 删除数据库中待办事项
			} else {
				// 非记录在human表的保存起来，后面再处理
				if (humanObj.backlogMap.containsKey(type)) {
					List<Backlog> backlogList = humanObj.backlogMap.get(type);
					backlogList.add(backlog);
				} else {
					List<Backlog> backlogList = new ArrayList<>();
					backlogList.add(backlog);
					humanObj.backlogMap.put(type, backlogList);
				}
			}
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadBaseFinishOne, "humanObj", humanObj);
	}
	
	@Listener(EventKey.HumanCultureTimes)
	public void _listener_LoadHumanCultureTimes(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadExtInfo humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(CultureTimes.tableName);
		dbPrx.get(humanObj.id);
		dbPrx.listenResult(this::_result_LoadHumanCultureTimes, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadBaseBeginOne, "humanObj", humanObj);
	}

	private void _result_LoadHumanCultureTimes(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_HumanDataLoadExtInfo humanObj is null");
			return;
		}
		
		Record record = results.get();
		if (record == null) {
			Log.game.error("===_result_HumanDataLoadExtInfo record=null");
			Event.fire(EventKey.HumanDataLoadBaseFinishOne, "humanObj", humanObj);
			return;
		}
		
		// 加载数据
		humanObj.cultureTimes = new CultureTimes(record);
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadBaseFinishOne, "humanObj", humanObj);
	}

	/**
	 * 玩家登录游戏后，开始加载一条玩家基础数据
	 */
	@Listener(EventKey.HumanDataLoadBaseBeginOne)
	public void _listener_HumanDataLoadBeginOne(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadBeginOne humanObj is null");
			return;
		}
		humanObj.loadingNum++;
	}

	/**
	 * 玩家登录游戏后，完成加载一条玩家基础数据
	 */
	@Listener(EventKey.HumanDataLoadBaseFinishOne)
	public void _listener_HumanDataLoadFinishOne(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadFinishOne humanObj is null");
			return;
		}
		humanObj.loadingNum--;
		// 玩家基础数据全部加载完毕 可以正式进行登录了
		if (humanObj.loadingNum <= 0) {
			// 模块数据加载也要使用到该变量
			humanObj.loadingNum = 0;
			
			// 发送玩家相关数据
			login(humanObj);
			// 发布事件：玩家最高级别模块数据加载开始
			Event.fire(EventKey.HumanDataLoadOther, "humanObj", humanObj);
		}
	}
	
	/**
	 * 玩家登录游戏后，开始加载一条玩家第一级别模块数据
	 */
	@Listener(EventKey.HumanDataLoadOtherBeginOne)
	public void _listener_HumanDataLoadOtherBegin(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOtherBegin humanObj is null");
			return;
		}
		humanObj.loadingNum++;
	}
	/**
	 * 玩家登录游戏后，完成加载一条玩家第一级别模块数据
	 */
	@Listener(EventKey.HumanDataLoadOtherFinishOne)
	public void _listener_HumanDataLoadOtherFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOtherFinish humanObj is null");
			return;
		}
		humanObj.loadingNum--;
		// 玩家第一级别模块数据全部加载完毕，开始加载第一级别模块数据
		if (humanObj.loadingNum <= 0) {
			humanObj.loadingNum = 0;
			// 初始化主角与伙伴，护法属性
			UnitManager.inst().humanPropsInit(humanObj);
			// 所有一级数据加载完毕，开始加载玩家二级模块数据
			Event.fire(EventKey.HumanDataLoadOther2, "humanObj", humanObj);
		}
	}
	
	/**
	 * 玩家登录游戏后，开始加载一条第二级别玩家模块数据
	 */
	@Listener(EventKey.HumanDataLoadOther2BeginOne)
	public void _listener_HumanDataLoadOther2Begin(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOtherBegin humanObj is null");
			return;
		}
		humanObj.loadingNum++;
		Log.game.info("loading num = {}", humanObj.loadingNum);
	}
	/**
	 * 玩家登录游戏后，完成加载一条第二级别玩家模块数据
	 */
	@Listener(EventKey.HumanDataLoadOther2FinishOne)
	public void _listener_HumanDataLoadOther2Finish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOtherFinish humanObj is null");
			return;
		}
		humanObj.loadingNum--;
		Log.game.info("loading num = {}", humanObj.loadingNum);
		// 玩家第二级别模块数据全部加载完毕，发送所有数据加载完毕
		if (humanObj.loadingNum <= 0) {
			// 发送所有数据加载完毕事件
			Event.fire(EventKey.HumanDataLoadAllFinish, "humanObj", humanObj);
			
			// 玩家数据加载完毕后，登录游戏最终消息
			SCInitData.Builder msg = SCInitData.newBuilder();
			// 场景判空
			if(humanObj.stageObj == null){
				Log.stageCommon.error(" humanObj.stageObj == null ,humanId={},humanName={}",humanObj.getHumanId(), humanObj.getHuman().getName());
				return;
			}
			game.msg.Define.DStage dStage = StageManager.inst().getDStage(humanObj);
			if (dStage == null) {
				Log.stageCommon.error(" getDStage error ,humanId={},humanName={}",humanObj.getHumanId(),humanObj.getHuman().getName());
				return;
			}
			msg.setStage(dStage);// 地图信息
			humanObj.sendMsg(msg);
			GuildManager.inst()._msg_CSGuildSkillList(humanObj);//下发公会技能信息
			// 如果是第一次创角
			Port port = Port.getCurrent();
			port.returnsAsync(humanObj.loadingPID, "nodeId", port.getNode().getId(), "portId", port.getId());
		}
	}

	/**
	 * 玩家数据加载完毕后 登录游戏
	 * @param humanObj
	 */
	private void login(HumanObject humanObj) {
	    Log.game.info("------------- login: {}", humanObj);
		// 注册到地图
		StageObject stageObj = humanObj.stageObj;
		int mapSn = stageObj.mapSn;
		// 获取玩家在该地图的历史坐标
		Vector2D vector = humanObj.getStagePos(stageObj.LineId);
		// Vector3D result = HeightFinding.posHeight(stageObj.stageSn, vector);
		if (vector.isZero() || vector.isWrongPos()) {
			// 没找到历史坐标，打回出生点
			humanObj.posNow = StageManager.inst().getHumanPos(mapSn);
			humanObj.dirNow = MathUtils.getDir(humanObj.posNow, StageManager.inst().getHumanDir(mapSn));
		} else {
			// 历史路径
			humanObj.posNow = vector;
			humanObj.dirNow = MathUtils.getDir(humanObj.posNow, StageManager.inst().getHumanDir(mapSn));
		}
		// 将玩家注册进地图 暂不显示
		if (!humanObj.stageRegister(stageObj)) {
			Log.game.error("===将玩家注册进地图失败！！！");
			return;
		}
		// 登陆时清除组队
		humanObj.setTeam(null);
		
		// 发送登录初始化信息至客户端
		this._send_SCHumanData(humanObj);

		/* 初始化玩家信息 */
		Human human = humanObj.getHuman();
		// 修正vip数据
		PaymentManager.inst().onPayChange(humanObj, human.getChargeGold());
		// 计算人物属性
		// UnitManager.inst().propCalc(humanObj);
		// 武将的登陆
		// GeneralManager.inst().propCalcAll(humanObj);

		humanObj.name = human.getName();
		// 刷新进游戏，如果人物死亡，设为满血复活
		if (humanObj.isDie()) {
			human.setHpCur(human.getHpMax());
		}

		// 当前时间
		long timeNow = Port.getTime();
		// 上次最后登录时间
		long timeLast = human.getTimeLogin();
		// 设置登陆状态
		if (Utils.isSameDay(timeLast, timeNow)) {
			humanObj.loginStageState = 1;
		} else {
			humanObj.loginStageState = 2;
		}
		// 发布玩家登录事件
		Event.fire(EventKey.HumanLogin, "humanObj", humanObj, "timeLoginLast", timeLast);

		// 添加玩家全局信息
		HumanGlobalInfo hgInfo = new HumanGlobalInfo();
		hgInfo.connPoint = humanObj.connPoint;// 玩家连接ID
		hgInfo.nodeId = stageObj.getPort().getNodeId();// Node名称
		hgInfo.portId = stageObj.getPort().getId();// Port名称
		hgInfo.channel = human.getChannel();// 渠道
		hgInfo.serverId = human.getServerId();// serverId

		hgInfo.timeSync = Port.getTime();// 同步时间
		hgInfo.timeLogin = human.getTimeLogin();// 玩家最后一次登陆时间
		hgInfo.timeSchedule = timeNow;// 玩家最后一次执行调度的时间

		hgInfo.id = humanObj.id;// 玩家ID
		hgInfo.digit = human.getHumanDigit(); // 玩家标识
		hgInfo.sn = human.getSn(); // 角色sn
		hgInfo.account = human.getAccountLogin();// 登录账号
		hgInfo.accountId = human.getAccountId();// 唯一账号Id
		hgInfo.name = human.getName();// 玩家名称
		hgInfo.stageId = stageObj.stageId;// 所在地图ID
		hgInfo.stageName = stageObj.mapName;// 所在地图名
		// humanGlobalInfo.countryId = 0;// 国家
		// humanGlobalInfo.unionId = 0;// 联盟
		hgInfo.teamId = human.getTeamId();// 队伍
		hgInfo.profession = human.getProfession();// 职业
		hgInfo.sex = human.getSex();// 性别
		hgInfo.level = human.getLevel();// 等级
		hgInfo.vipLv = human.getVipLevel();// VIP等级
		hgInfo.combat = getLineUpCombat(humanObj);// 战斗力
		hgInfo.headSn = human.getHeadSn();// 头像
		
		hgInfo.modelSn = human.getModelSn();// 玩家模型
		hgInfo.defaultModelSn = human.getDefaultModelSn(); // 默认模型
//		hgInfo.titleSn = human.getTitleSN();// 玩家称号
		hgInfo.guildId = human.getGuildId();// 所属公会id
//		hgInfo.partnerSnList = PartnerManager.inst().getPartnerList(humanObj);
		// 记录玩家全局信息
		humanObj.humanGlobalInfo = hgInfo;
		// 登记玩家数据
        Log.game.info("------------- register: {} ...", humanObj);
		HumanGlobalServiceProxy prxShs = HumanGlobalServiceProxy.newInstance();
		prxShs.register(hgInfo);

		// 玩家地图人数+1
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		proxy.stageHumanNumAdd(humanObj.stageObj.stageId);

		stageHistoryRepair(humanObj);// 修复由于登陆的时候造成的一些地图问题
		openHumanActValue(humanObj);// 登录时开启玩家体力规则
		openHumanLifeEquip(humanObj);// 登录时开启玩家限时装备规则
		openHumanGeneralSkill(humanObj);// 登录时开启玩家武将技能点规则
		sendLoginOtherMsg(humanObj);// 玩家上线时自动下发数据：玩家相关的数据
		checkBacklog(humanObj);// 待办事件,先处理human身上
		// 发送登录日志
		LogOpUtils.sendLoginLog(humanObj);
	}
	/**
	 * 处理待办事件，先处理存在human身上的
	 * @param humanObj
	 */
	private void checkBacklog(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		JSONObject jo = new JSONObject();
		long guildId = 0;
		for (String key : humanObj.backlogMap.keySet()) {
			BacklogType type = BacklogType.valueOf(key);
			List<Backlog> backlogList = humanObj.backlogMap.get(key);
			switch (type) {
			case GuildJoin://加入公会
				for (int i = 0; i < backlogList.size(); i++) {
					Backlog backlog = backlogList.get(i);
					jo = Utils.toJSONObject(backlog.getParamJSON());
					guildId = Utils.longValue(jo.getString(GuildManager.inst().GuildIdKey));
					if(human.getGuildId() == 0){
						human.setGuildId(guildId);
						
						int guildLv = Utils.intValue(jo.getString(Human.K.GuildLevel));
						String guildName = jo.getString(GuildManager.inst().GuildNameKey);
						human.setGuildName(guildName);
						human.setGuildLevel(guildLv);
						backlog.remove();//删除数据库中待办事项
						human.update(true);
						HumanGlobalInfo hgInfo = humanObj.humanGlobalInfo;
						HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId, hgInfo.portId, hgInfo.id);
						prxHumanObj.guildJoin(true, guildId, guildLv, guildName, 1);// 通知加入了公会
					}
				}
				break;
			case GuildLeave://退出公会
				for (int i = 0; i < backlogList.size(); i++) {
					Backlog backlog = backlogList.get(i);
					jo = Utils.toJSONObject(backlog.getParamJSON());
					guildId = Utils.longValue(jo.getString(GuildManager.inst().GuildIdKey));
					if(human.getGuildId() == guildId){
						human.setGuildId(0);
						if(human.isGuildCDR()){
							human.setGuildCDR(false);
						}
						backlog.remove();//删除数据库中待办事项
						human.update(true);
						HumanGlobalInfo hgInfo = humanObj.humanGlobalInfo;
						HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId, hgInfo.portId, hgInfo.id);
						prxHumanObj.guildLeave(true, 1);// 通知玩家被踢出公会
					}
				}
				break;
			case GuildCDR://是否是会长状态改变
				for (int i = 0; i < backlogList.size(); i++) {
					Backlog backlog = backlogList.get(i);
					jo = Utils.toJSONObject(backlog.getParamJSON());
					boolean isCDR = jo.getBooleanValue(Human.K.GuildCDR);
					human.setGuildCDR(isCDR);
					backlog.remove();//删除数据库中待办事项
					human.update(true);
				}
				break;
			default:
				break;
			}
		}
		// 删除内存数据
		humanObj.backlogMap.remove(BacklogType.GuildJoin);
		humanObj.backlogMap.remove(BacklogType.GuildLeave);
		humanObj.backlogMap.remove(BacklogType.GuildCDR);
		
	}

	/**
	 * 发送登录初始化信息至客户端
	 */
	private void _send_SCHumanData(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long tmNow = Port.getTime();
		long tmDailyReset = Utils.getTimeBeginOfToday(tmNow) + ParamManager.dailyHourReset * Time.HOUR;
		long tmLastLogin = human.getTimeLogin();
		if (tmLastLogin == 0L) {
			Event.fire(EventKey.HumanFirstLogin, "humanObj", humanObj);// 创角后首次登陆
		}
		// 根据上下线时间判断是否每日首次登录，是则需执行每日重置
		if (tmLastLogin < tmDailyReset && tmDailyReset <= tmNow) {
			humanObj.isDailyFirstLogin = true;
			Event.fire(EventKey.HumanLoginFinishFirstToday, "humanObj", humanObj);
			resetDaily(humanObj);// 每日重置--登陆判断重置的入口
		} else {
			humanObj.isDailyFirstLogin = false;
		}
		// 更新角色上线时间
//		human.setTimeLogin(tmNow);

		// dHuman基本信息开始////////////////////////////////////////////////////////
		DHuman.Builder dHuman = DHuman.newBuilder();
		dHuman.setId(human.getId());// 角色唯一ID19位
		dHuman.setPlatformId(human.getPlatformId());// 平台ID2位
		dHuman.setServerId(human.getServerId());// 服务器ID4位
		dHuman.setHumanDigit(human.getHumanDigit());// 角色标识6位
		dHuman.setName(human.getName());// 角色名
		dHuman.setProfession(human.getProfession());// 职业
		dHuman.setSex(human.getSex());// 性别
		dHuman.setSn(human.getSn());// 配置表sn
		dHuman.setModelSn(human.getModelSn());// 模型sn
		dHuman.setHeadSn(human.getHeadSn());// 头像sn
		dHuman.setMountSn(human.getMountSn());// 坐骑sn
		dHuman.setLevel(human.getLevel()); // 人物等级
		dHuman.setCombat(human.getCombat()); // 战斗力
		dHuman.setSumCombat(human.getSumCombat());// 人物+武将总战力
		dHuman.setGeneralMostCombat(human.getGeneralMostCombat()); // 最强阵容战力
		dHuman.setGuildId(human.getGuildId());
		dHuman.setCreatHumanTime(human.getTimeCreate());// 玩家创角时间
		
		// 属性信息
		DProp dProp = UnitManager.inst().getDProp(human);
		dHuman.setProp(dProp);// 设置属性信息

		// 货币信息
		DMoney dMoney = RewardHelper.getDMoney(human);
		dHuman.setMoney(dMoney);// 货币信息
		
		// 技能信息
		DSkillGroup.Builder dskill = DSkillGroup.newBuilder();
		List<Integer> skillset1 = Utils.strToIntList(human.getSkillSet1());
		dskill.addAllSkillSet(skillset1);
		dHuman.setSkillGroup(dskill);
		
		// 符文穿戴信息
		dHuman.addAllRuneIds(Utils.strToLongList(human.getRuneInfo()));
		
		// 玩家每日零点重置的信息-消耗元宝购买次数
		dHuman.setDDailyCostBuy(getDDailyCostBuy(human));
		// 玩家每日零点重置的信息-其他零碎信息
		dHuman.setDDailyReset(getDDailyReset(human));
		// 玩家每周零点重置的信息
		dHuman.setDWeeklyReset(getDWeeklyReset(human));
		
		// 玩家新手引导相关信息
		dHuman.setDGuideInfo(getDGuideInfo(human));
		// 玩家VIP充值相关信息
		dHuman.setDVipPayment(getDVipPayment(human));
		// 玩家显示装备和时装相关信息
		dHuman.setDShowInfo(getDShowInfo(human));

		List<Integer> modUnlockSnList = Utils.strToIntList(human.getModUnlock());
		dHuman.addAllModUnlockSnList(modUnlockSnList);// 所有已经激活的功能sn集合
		// 功能开放领取状态
		dHuman.addAllModUnlockReward(Utils.strToIntList(human.getModUnlockReward()));
		dHuman.setRenameNum(human.getRenameNum());// 已经改名次数
		dHuman.setActFullTime(human.getActFullTime()); // 当前体力恢复满的时间戳
		
		//int levelStopTime = ParamManager.rankLevelShopTime;
		//dHuman.setLevelRankActivityStopTime(levelStopTime);// 等级排行活动结束时间，单位：天
		//int combatStopTime = ParamManager.rankCombatShopTime;
		//dHuman.setCombatRankActivityStopTime(combatStopTime);// 战力排行活动结束时间，单位：天

		//dHuman.setTitleShow(0);// 是否显示称号
		//dHuman.setTitleSN(0);// 称号SN
		//dHuman.setGuildId(0);// 公会id
		//dHuman.setGuildContribute(0);// 公会贡献
		// dHuman基本信息结束////////////////////////////////////////////////////////
		
		// 最终消息
		SCHumanData.Builder msg = SCHumanData.newBuilder();
		msg.setHuman(dHuman);// 玩家基础信息
		humanObj.sendMsg(msg);
		
		// 发送客户端调试状态
		sendSCDebugClient(humanObj);
	}
	/**
	 * 发送客户端调试状态
	 * @param humanObj
	 */
	public void sendSCDebugClient(HumanObject humanObj) {
		SCDebugClient.Builder msg = SCDebugClient.newBuilder();
		msg.setOpenSRDebug(humanObj.getHuman().isClientOpenSRDebug());
		msg.setLogType(humanObj.getHuman().getClientLogType());
		humanObj.sendMsg(msg);
	}

	/**
	 * 每个整点执行一次
	 * 
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == 0) {
			// 发送登录信息（次留需要）
			LogOpUtils.sendLoginLog(humanObj); 
		}
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			resetDaily(humanObj);
		}
	}

	/**
	 * 每日重置：重置记录在human上的记录
	 */
	public void resetDaily(HumanObject humanObj) {
		/* 玩家每日需重置的记录 */
		Human human = humanObj.getHuman();
		
		// 累积登陆天数
		int loginNum = human.getLoginNum();
		human.setLoginNum(loginNum+1);
		
		// 玩家每日消耗元宝购买次数记录
		human.setDailyActBuyNum(0);// 今日已购买体力次数
		human.setDailyCoinBuyNum(0);// 今日已购买铜币次数
		human.setDailyCompeteFightBuyNum(0);// 今日已购买竞技场挑战次数
		
		human.setDailyLootMapRevivalBuyNum(0);// 今日已购买抢夺本复活次数
		human.setDailySendWinksNum(0); // 今日发送魔法表情次数
		human.setDailyFightWinNum(0); // 今日切磋胜利次数
		
		if(human.getLootSingle() < ParamManager.initHumanLootMapSingleKey){ // 重置抢夺本单人密钥
			human.setLootSingle(ParamManager.initHumanLootMapSingleKey);
		}
		if(human.getLootMultiple() < ParamManager.initHumanLootMapMultipleKey){ // 重置多人本多人密钥
			human.setLootMultiple(ParamManager.initHumanLootMapMultipleKey);
		}
		
		// 下发通知
		sendSCDailyCostBuyChange(humanObj);
		
		//TODO 其他需每日重置的记录
		human.setTimeLogin(Port.getTime()); //设置最后上线时间为跨天时间
		human.setDailyOnlineTime(0L);// 重置今日在线时间
		human.setDailySignFlag(ESignType.signNot_VALUE); // 重置每日签到
		
		human.setDailyCompeteFightNum(0);// 重置竞技场已挑战次数
		human.setDailyCompeteIntegral(0);// 重置竞技场积分
		human.setDailyCompeteIntegralAward("{}");// 重置竞技场积分领取记录
		
		human.setDailyInstFinishNum("{}");// 重置每日活动副本已完成次数
		human.setDailyRankWorship("{}");// 重置每日排行榜已膜拜次数
		
		human.setDailyQuestLiveness(0);// 重置每日任务活跃度
		human.setDailyLivenessReward("{}");// 重置每日活跃度奖励已领取记录
		human.setTodayChargeGold(0);//充值今日充值
		// 下发通知
		sendSCDailyResetChange(humanObj);
		
		HumanExtInfo extInfo = humanObj.getHumanExtInfo();
		if(extInfo == null) return;
		extInfo.setLootMapMultipleTodayScore(0); // 抢夺本今日最高
		extInfo.setSurplusNum(ParamManager.competeFreeNum);//竞技场剩余次数次数重置
		
		extInfo.setCompeteBuyNumAdd(0);//今日竞技场购买次数
		extInfo.setChallengeNum(0);//今日已挑战次数
		//重置仙府信息
		CaveManager.inst().resetDaily(humanObj);
		//重置宝箱领取
		human.setGuildImmoSnList("");
		// 重置爬塔记录
//		extInfo.setTowerPassTime(0);
//		extInfo.setTowerMaxFloor(0);
//		extInfo.setTowerSelDiff(0);
	}
	
	@Listener(EventKey.HumanLogout)
	public void _listener_HumanLogout(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("HumanManager._listener_HumanLogout humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();

		TeamManager.inst()._msg_CSTeamLeave(humanObj);// 掉线离队

		saveStageHistory(humanObj);// 保存玩家场景位置记录

		human.setTimeLogout(Port.getTime());// 更新角色下线时间

//		long timeLast = Port.getTime() - human.getTimeLogin();// 本次登录的时长
//		human.setTimeOnLine(timeLast + human.getTimeOnLine());// 累加在线时间
//		if(timeLast + human.getDailyOnlineTime() > 0){
//			human.setDailyOnlineTime(timeLast + human.getDailyOnlineTime()); // 本日在线时间
//		}
		
		// 移除地图
		humanObj.stageLeave();

		// 在线奖励时间计时器关掉
		if (humanObj.ttNowAward.isStarted()) {
			humanObj.ttNowAward.stop();
		}
		
		//关闭体力计时器
		if(humanObj.ttActValue.isStarted()){
			humanObj.ttActValue.stop();
		}

		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		Log.game.info("===important - infomation===" + humanObj.getHumanId());
		// 地图玩家数-1
		proxy.stageHumanNumReduce(humanObj.stageObj.stageId);
	}
	
	/**
	 * 保存玩家场景位置记录
	 */
	public void saveStageHistory(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 同步玩家位置
		JSONArray ja = Utils.toJSONArray(human.getStageHistory());
		if (ja.isEmpty()) {
			Log.game.error("HumanManager._listener_HumanLogout human.getStageHistory={}", human.getStageHistory());
			return;
		}
		JSONArray jaTemp = Utils.toJSONArray(ja.getString(0));
		if (jaTemp.isEmpty()) {
			Log.game.error("HumanManager._listener_HumanLogout ja.getString(0)={}", ja.getString(0));
			return;
		}
		ConfMap confMap = ConfMap.get(Utils.intValue(jaTemp.getString(1)));
		if (null == confMap) {
			Log.table.error("ConfMap配表错误，no find sn={} ", Utils.intValue(jaTemp.getString(1)));
			return;
		}
		// 第1场战斗 是直接发送的没有 地图的记录
		if (humanObj.stageObj.mapSn == confMap.sn) {
			jaTemp.set(2, Utils.round(humanObj.posNow.x));
			jaTemp.set(3, Utils.round(humanObj.posNow.y));
			ja.set(0, jaTemp);
			human.setStageHistory(JSON.toJSONString(ja));
		}
	}

	/**
	 * 玩家完成加载登录到地图中时进行操作
	 * 
	 * @param params
	 */
	@Listener(EventKey.StageHumanEnter)
	public void _listener_StageHumanEnter(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_StageHumanEnter humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();

		// 设置状态 代表客户端已经准备好了
		humanObj.isClientStageReady = true;
		// 如果是创角后的第一个新手剧情
		if (human.getFirstStory() == 0 && humanObj.stageObj != null && humanObj.stageObj.confMap.type.equals(EMapType.common.name())) {
			human.setFirstStory(1);
		}

		// 补满血
		human.setHpCur(human.getHpMax());
		humanObj.confModel = ConfRoleModel.get(human.getModelSn());
	}

	/**
	 * 玩家上线时自动下发数据：玩家相关的数据
	 * 
	 * @param humanObj
	 */
	private void sendLoginOtherMsg(HumanObject humanObj) {
		//TeamManager.inst()._msg_CSTeamRepInfo(humanObj);// 请求活动副本信息
	}

	@Listener(EventKey.MonsterHpLoss)
	public void _listener_MonsterHpLoss(Param param) {
		UnitObject atker = (UnitObject) Utils.getParamValue(param, "atker", null);
		int hpLost = Utils.getParamValue(param, "hpLost", 0);
		if (atker != null && atker instanceof HumanObject) {
			HumanObject humanObj = (HumanObject) atker;
			if (humanObj.dPVEHarm != null && hpLost > 0)
				humanObj.dPVEHarm.setHarm(humanObj.dPVEHarm.getHarm() + hpLost);
		}
	}

	/*@Listener(value = EventKey.ItemUse, subInt = { ItemTypeKey.useForAttributes, ItemTypeKey.useForItem })
	public void _listener_ItemUse(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_listener_ItemUse humanObj is null");
			return;
		}

		ItemBag itemBag = Utils.getParamValue(param, "itemBag", null);
		ConfItem confItem = Utils.getParamValue(param, "confItem", null);
		if (null == confItem) {
			Log.table.error("===HumanManager._listener_ItemUse, ConfItem=null");
			return;
		}
		if (null == confItem.param) {
			Log.table.error("===ConfItem配表错误，confItem.param == null，sn={}", confItem.sn);
			return;
		}
		int num = Utils.getParamValue(param, "num", 0);

		List<ProduceVo> result = new ArrayList<ProduceVo>();

		// 获得所有物品
		result = getProduceVoList(confItem.param, num);

		// 判断是否可以给
		ReasonResult rr = ProduceManager.inst().canGiveProduceItem(humanObj, result);
		if (!rr.success) {
			return;
		}
		// 叠加物品
		result = ProduceManager.inst().mergeProduce(result);

		// 实际给物品
		ProduceManager.inst().giveProduceItem(humanObj, result, LogSysModType.BagItemUse);

		// 删掉使用的物品
		ItemBagManager.inst().remove(humanObj, itemBag, num);

		// 发送消息
		SCItemUse.Builder msg = SCItemUse.newBuilder();
		for (ProduceVo produceVo : result) {
			msg.addProduces(produceVo.toDProduce());
		}
		//msg.setIsTip(!confItem.autoUse);
		msg.setIsTip(confItem.autoUse == 0);
		humanObj.sendMsg(msg);
	}*/

	/**
	 * 开启玩家限时装备规则
	 * 
	 * @param humanObj
	 */
	public void openHumanLifeEquip(HumanObject humanObj) {

		long time = Port.getTime();// 当前时间
		ItemPack bag = ItemBodyManager.inst().getPack(humanObj);
		long minTime = 0;
		boolean ret = false;// 默认没有过时装备
		for (Item itb : bag.findAll()) {
			long life = itb.getLife();
			// 身上有限时装备
			if (life > 0) {
				if (life > 0 && life <= time) {
					ItemBodyManager.inst().calc_equipProp(humanObj);
					if (!ret) {
						humanObj.ttLifeEquipMin.stop();
					}
				} else {
					long intervalTime = life - time;
					if (minTime == 0 || minTime > intervalTime) {
						minTime = intervalTime;
						ret = true;// 有过时装备
					}
				}
			}
		}
		if (ret) {// 有过时装备
			humanObj.ttLifeEquipMin.stop();// 先关闭后开启计时器
			humanObj.ttLifeEquipMin.start(minTime);// 开启计时器
		}
	}

	/**
	 * 开启武将技能点计时器
	 * 
	 * @param humanObj
	 */
	private void openHumanGeneralSkill(HumanObject humanObj) {
		// Human human = humanObj.getHuman();
		// long timeNow = Port.getTime();
		// int generalSkillPointMax = ParamManager.generalSkillPointMax;//
		// 武将最大技能点限制
		// long skillPointAllTime = human.getGenSkillPointAllTime();
		// // 如果计时器还开着就关闭 重新计算时间
		// if (humanObj.ttGenSkillPoint.isStarted()) {
		// humanObj.ttGenSkillPoint.stop();
		// }
		// if (humanObj.ttGenSPNext.isStarted()) {
		// humanObj.ttGenSPNext.stop();
		// }
		// if (skillPointAllTime <= timeNow) {// 记录最终恢复满的时间小于当前时间 恢复满技能点
		// human.setGenSkillPoint(generalSkillPointMax);
		// } else {
		// int genSkillPointRecover = ParamManager.generalSkillPointRecover;
		// long gapTime = skillPointAllTime - timeNow;
		// // 剩下的值 = （向上取整） （总时间 - 当前时间）/ 冷却时间 整形除以整形容易出现0的情况
		// int remain = (int) Math.ceil((double) (gapTime * 1.0f /
		// (genSkillPointRecover * Time.SEC)));
		// // 当前的技能点 = 总技能点 - 剩下的技能点
		// int skillpoint = generalSkillPointMax - remain;
		// if (skillpoint > generalSkillPointMax) {
		// skillpoint = generalSkillPointMax;
		// }
		// if (skillpoint < 0) {
		// skillpoint = 0;
		// }
		// human.setGenSkillPoint(skillpoint);
		// // 距离回满技能点最大时间
		// long time = gapTime;
		// long gapTimeMax = remain * genSkillPointRecover * Time.SEC;
		// if (gapTime <= gapTimeMax) {
		// time = gapTime - (remain - 1) * genSkillPointRecover * Time.SEC;//
		// 因为remain是向上取整
		// // 所以要减1
		// }
		// // 如果计时器是关闭的就打开
		// if (!humanObj.ttGenSPNext.isStarted()) {
		// humanObj.ttGenSPNext.start(time);
		// }
		// PartnerPlusManager.inst().sendSCGeneralSkillPoint(humanObj);
		// }
	}

	/**
	 * 开启玩家体力规则
	 */
	private void openHumanActValue(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		int humanLv = human.getLevel();
		ConfLevelExp confLevelExp = ConfLevelExp.get(humanLv);
		if (confLevelExp == null) {
			Log.table.error("开启玩家体力规则错误：openHumanActValue no find humanLv={}", humanLv);
			return;
		}
		
		//RewardHelper.LogDebugAct(humanObj,"openHumanActValue");
		
		long humanActMax = confLevelExp.staminaMax;// 角色当前等级最高体力
		humanObj.ttActValue.start(Port.getTime(), ParamManager.cdHumanAct * Time.MIN); //开始计时器
		
		long humanActNow = human.getAct();// 角色数据库中的体力
		if (humanActNow < humanActMax) {
			if (human.getActFullTime() <= Port.getTime()) {
				// 已过满体时间，体力达到最大值
				RewardHelper.reward(humanObj, EMoneyType.act_VALUE, (int)(humanActMax-humanActNow), LogSysModType.ActValueLogin);
			} else {
				long tmRemain = human.getActFullTime() - Port.getTime(); // 回复剩余时间 毫秒
				long tmOneAct = ParamManager.cdHumanAct * Time.MIN;// 回复一点体力的毫秒数
				int actAdd = (int)(tmRemain / tmOneAct);// 还需要多少体才满体
				long interval = tmRemain - actAdd * tmOneAct;
				if (interval > 0) {
					actAdd += 1;// 6分30秒 算 2点体
					humanObj.ttActValue.setTimeNext(Port.getTime()+interval); // 设置下次回复时间
				}
				int actNeed = (int)(humanActMax - humanActNow);// 还差多少体才满体
				if (actAdd < actNeed) {
					RewardHelper.reward(humanObj, EMoneyType.act_VALUE, (actNeed - actAdd), LogSysModType.ActValueLogin);
				}
			}
		}
	}

	/**
	 * 记录人物路径 只在切换地图的时候调用
	 * 
	 * @param humanObj
	 * @param stageId
	 * @param stageSn
	 * @param stageType
	 * @param vectorOld
	 */
	public void recordStage(HumanObject humanObj, long stageId, int stageSn, String stageType, Vector2D vectorOld) {
	}

	/**
	 * 从地图历史信息中找到普通地图的坐标信息
	 * 
	 * @param humanObj
	 * @return
	 */
	public Vector2D stageHistoryCommon(HumanObject humanObj) {
		Vector2D result = new Vector2D();

		Human human = humanObj.getHuman();
		JSONArray ja = Utils.toJSONArray(human.getStageHistory());
		if (ja.isEmpty()) {
			return result;
		}
		Iterator<Object> iter = ja.iterator();
		// 循环遍历查找地图
		while (iter.hasNext()) {
			Object next = iter.next();
			JSONArray jaNext = Utils.toJSONArray(next.toString());
			if (jaNext.isEmpty()) {
				continue;
			}
			String stageType = jaNext.getString(4);

			if (stageType.equals(EMapType.common.name())) {
				result.x = jaNext.getDoubleValue(2);
				result.y = jaNext.getDoubleValue(3);
				break;
			}
		}
		return result;
	}

	public int stageHistoryCommonSn(HumanObject humanObj) {
		int result = -1;

		Human human = humanObj.getHuman();
		JSONArray ja = Utils.toJSONArray(human.getStageHistory());
		if (ja.isEmpty()) {
			return result;
		}
		Iterator<Object> iter = ja.iterator();
		// 循环遍历查找地图
		while (iter.hasNext()) {
			Object next = iter.next();
			JSONArray jaNext = Utils.toJSONArray(next.toString());
			if (jaNext.isEmpty()) {
				continue;
			}
			String stageType = jaNext.getString(4);

			if (stageType.equals(EMapType.common.name())) {
				result = jaNext.getIntValue(1);
				break;
			}
		}
		return result;
	}

	/**
	 * 修复由于登陆的时候造成的一些地图问题
	 * 
	 * @param humanObj
	 */
	private void stageHistoryRepair(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 删除所有不是common的
		JSONArray ja = Utils.toJSONArray(human.getStageHistory());
		if (ja.isEmpty()) {
			return;
		}
		Iterator<Object> iter = ja.iterator();
		// 循环遍历查找上一张地图
		while (iter.hasNext()) {
			Object next = iter.next();
			JSONArray jaNext = Utils.toJSONArray(next.toString());
			if (jaNext.isEmpty()) {
				continue;
			}
			String stageType = jaNext.getString(4);
			if (stageType.equals(EMapType.common.name()))
				break;

			iter.remove();
		}
		human.setStageHistory(JSON.toJSONString(ja));
	}

	/**
	 * 获得正在出战的武将ID
	 * 
	 * @param humanObj
	 * @return
	 */
	public List<Long> getAttGenIdList(HumanObject humanObj) {
		List<Long> result = new ArrayList<Long>();
		/*
		 * for (UnitObject uo : humanObj.salvesAttingList) {wgz if (uo == null)
		 * { continue; } result.add(uo.id); }
		 */
		return result;
	}

	/**
	 * 复活
	 * @param humanObj
	 * @param type 复活类型：EReviveType
	 * @param stageSn 关卡Sn
	 * @param actInstSn 活动副本sn
	 */
	public void revive(HumanObject humanObj, EReviveType type, int stageSn, int actInstSn) {
		if (stageSn > 0) {
			ConfInstStage confInstStage = ConfInstStage.get(stageSn);
			if (confInstStage == null) {
				// 复活失败，提示：131101副本配置不存在！
				sendSCFightRevive(humanObj, 131101);
				Log.table.error("ConfInstStage配表错误，no find sn={} ", stageSn);
				return;
			} else {
				if (confInstStage.chapterSN > 0) {
					// 章节副本玩家死亡，服务端不知道要先死才能复活
					humanObj.die(humanObj, new Param("skillSn", 0));
				}
			}
		}

		Human human = humanObj.getHuman();
		// human.setHpCur(0);
		// if (!humanObj.isDie()) {
		// return new ReasonResult(false, "您当前未死亡不需要复活");
		// }

		int result = 0;// 0成功，非0失败（即sysMsg中的sn）
		switch (type) {
		case ReviveBirth: {// 复活点复活
			// 玩家复活点复活
			human.setHpCur(human.getHpMax());
			StageManager.inst().pullToRevive(humanObj, humanObj.birthPos, humanObj.birthDir);
		}	break;
		case ReviveBuffNone: {// 复活无buff
			// 玩家原地复活
			human.setHpCur(human.getHpMax());
			humanObj.stageShowRevive();
		}	break;
		case ReviveBuffOne:
		case ReviveBuffTwo:
		case ReviveBuffThree:
		case ReviveBuffFour: {// 复活带buff
			result = reviveBuff(humanObj, stageSn, type.getNumber());
			if (result != 0) {
				sendSCFightRevive(humanObj, result);// 返回复活失败
				return;
			}
			// 玩家原地复活
			human.setHpCur(human.getHpMax());
			humanObj.stageShowRevive();
		}	break;
		default:
			break;
		}

		sendSCFightRevive(humanObj, 0);// 返回复活成功
		// 人物复活事件
		Event.fire(EventKey.HumanRevive, "humanObj", humanObj);
	}

	private int reviveBuff(HumanObject humanObj, int stageSn, int type) {
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={} ", stageSn);
			return 131101;// 131101副本配置不存在！
		}
		//TODO 有复活的副本：世界BOSS，等级BOSS，爬塔，读取CostGold表配置
		int reviveCost = 0;//confInstStage.reviveCost;// 复活花费钻石
		int[] reviveBuff = {0};//confInstStage.reviveBuff;// 复活带BUFF
		if (type > reviveBuff.length || type - 1 < 0) {
			return 131102;// 131102副本配置的复活buff错误！
		}

		// 记录复活BuffSn
		humanObj.reviveBuffSn = reviveBuff[type - 1];
		int reviveCount = humanObj.stageObj.getReviveCount(humanObj.id);
		if (reviveCount <= 0) {
			return 131103;// 131103副本的复活次数不足！
		} else if (reviveCost > 0) {
			// 扣元宝
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, reviveCost, LogSysModType.InstFightRevive)) {
				return 0;// 复活失败
			}
			// 扣次数
			humanObj.stageObj.setReviveCount(humanObj.id, reviveCount - 1);
		}
		return 0;
	}

	/**
	 * 发送复活结果
	 * @param humanObj
	 * @param result 0成功，非0失败（即sysMsg中的sn）
	 */
	private void sendSCFightRevive(HumanObject humanObj, int result) {
		SCFightRevive.Builder msg = SCFightRevive.newBuilder();
		msg.setResult(result);
		if (result == 0) {// 0成功，加入玩家ID并广播复活
			msg.setId(humanObj.id);
			StageManager.inst().sendMsgToArea(msg, humanObj.stageObj, humanObj.posNow);
			// Log.human.info("===广播复活成功：id={},pos={}", humanObj.id,
			// humanObj.posNow);
		} else {// 非0失败
			humanObj.sendMsg(msg);
		}
	}
	
	/**
	 * 获取玩家职业：模型SN=职业*10+性别
	 * @param modelSn
	 * @return
	 */
	private int getHumanProfession(int modelSn) {
		int profession = modelSn / 10;
		return profession;
	}

	/**
	 * 获取玩家性别：模型SN=职业*10+性别
	 * 
	 * @param modelSn
	 * @return
	 */
	private int getHumanSex(int modelSn) {
		int sex = modelSn - getHumanProfession(modelSn) * 10;
		return sex;
	}

	/**
	 * 是否女性玩家
	 * 
	 * @param modelSn
	 * @return
	 */
	public boolean isHumanFemale(int modelSn) {
		int sex = getHumanSex(modelSn);
		boolean isFemale = (sex == 1 ? true : false);// 0男，1女
		return isFemale;
	}

	/**
	 * 发送恢复满体力时间改变
	 * 
	 * @param humanObj
	 */
	public void sendSCActValueFullTimeChange(HumanObject humanObj) {
		SCActFullTimeChange.Builder msg = SCActFullTimeChange.newBuilder();
		msg.setActFullTime(humanObj.getHuman().getActFullTime());
		humanObj.sendMsg(msg);
	}

	/**
	 * 等级变化
	 * 
	 * @param humanObj
	 */
	public void sendSCLevelChange(HumanObject humanObj) {
		SCLevelChange.Builder msg = SCLevelChange.newBuilder();
		msg.setLevel(humanObj.getHuman().getLevel());
		msg.setActMax(humanObj.getHuman().getActMax());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获取param中的物品或金币
	 * 
	 * @param param
	 * @param num
	 * @return
	 */
	/*public List<ProduceVo> getProduceVoList(String[] param, int num) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if (null == param || 0 == num) {
			Log.game.error("===HumanManager.getProduceVoList, param={}, num={}", param, num);
			return result;
		} else {
			for (int i = 0; i < num; i++) {
				for (int j = 0; j < param.length; j++) {
					int[] itemParam = Utils.strToIntArray(param[j], "\\|");
					int type = Utils.intValue(itemParam[0]);// 类型 <100:货币类型
															// 后面跟着是值, =100:物品礼包
															// 后面跟着是Produce表的sn
					int value = Utils.intValue(itemParam[1]);// 值或Produce表的sn
					// TODO 判断类型
					if (type > EMoneyType.minMoney_VALUE && type < EMoneyType.maxMoney_VALUE) { // 货币类型
						result.add(new ProduceVo(type, value));
					} else if (type == ItemParamType.GiftBag.value()) {// 物品包类型
						result.addAll(ProduceManager.inst().produceItem(value));
					}
				}
			}
		}
		return result;
	}*/

	/**
	 * 返回技能信息
	 * 
	 * @param humanObj
	 * @param skillSn
	 */
	public void sendMsgGenSKillUpdate(HumanObject humanObj, int skillSn) {
	}

	/**
	 * 玩家每日购买体力
	 */
	public void _msg_CSDailyActBuy(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		int vipLv = human.getVipLevel();
		// 判断VIP每日购买体力次数
		int numBuyed = human.getDailyActBuyNum();
		if(!VipManager.inst().checkVipBuyNum(EVipBuyType.actBuyNum, numBuyed, vipLv)){
			humanObj.sendSysMsg(14);// 今日可使用次数已用完！
			return;
		}
		
		// 获取花费元宝数
		int goldCost = RewardHelper.getCostGold(ECostGoldType.actBuyCost, numBuyed+1);
		int actGain = ParamManager.dailyActBuyValue;
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, goldCost, LogSysModType.HumanActBuy)) {
			return;
		}
		
		// 增加体力
		RewardHelper.reward(humanObj, EMoneyType.act_VALUE, actGain, LogSysModType.HumanActBuy);

		// 改变次数
		numBuyed++;
		human.setDailyActBuyNum(numBuyed);
		// 返回结果
		SCDailyActBuy.Builder msg = SCDailyActBuy.newBuilder();
		msg.setGoldCost(goldCost);
		msg.setActGain(actGain);
		msg.setNumBuyed(numBuyed);
		humanObj.sendMsg(msg);
		
		// 触发任务
		Event.fire(EventKey.DailyActBuy , "humanObj", humanObj);
	}
	
	/**
	 * 玩家每日购买铜币[招财进宝 摇钱树]
	 */
	public void _msg_CSDailyCoinBuy(HumanObject humanObj, int num) {
		Human human = humanObj.getHuman();
		int vipLv = human.getVipLevel();
		// 判断VIP每日购买体力次数
		int numBuyed = human.getDailyCoinBuyNum();
		int numMax = VipManager.inst().getConfVipBuyTimes(EVipBuyType.coinBuyNum, vipLv);
		if (numBuyed >= numMax) {
			humanObj.sendSysMsg(14);// 今日可使用次数已用完！
			return;
		}
		if (numBuyed + num > numMax) {
			num = numMax - numBuyed;
		}
		
		// 获取花费元宝数
		int goldCost = 0;
		for (int i = 1; i <= num; i++) {
			goldCost += RewardHelper.getCostGold(ECostGoldType.coinBuyCost, numBuyed+i);
		}
		int coinGain = 0;
		ConfLevelExp conf = ConfLevelExp.get(human.getLevel());
		if( conf!= null) {
			coinGain = conf.dailyCoinBuyValue* num;
		}
				
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, goldCost, LogSysModType.DailyCoinBuy)) {
			return;
		}
		
		// 增加铜币
		RewardHelper.reward(humanObj, EMoneyType.coin_VALUE, coinGain, LogSysModType.DailyCoinBuy);

		// 改变次数
		numBuyed += num;
		human.setDailyCoinBuyNum(numBuyed);
		// 返回结果
		SCDailyCoinBuy.Builder msg = SCDailyCoinBuy.newBuilder();
		msg.setGoldCost(goldCost);
		msg.setCoinGain(coinGain);
		msg.setNumBuyed(numBuyed);
		humanObj.sendMsg(msg);
		
		// 触发任务
		Event.fire(EventKey.DailyCoinBuy , "humanObj", humanObj);
		
	}
	
	/**
	 * 发送人物属性变化
	 */
	public void sendSCPropInfoChange(HumanObject humanObj, EPropChangeType type) {
		SCPropInfoChange.Builder msg = SCPropInfoChange.newBuilder();
		DProp dProp = UnitManager.inst().getDProp(humanObj.getHuman());
		msg.setType(type);
		msg.setProp(dProp);
		msg.setCombat(humanObj.getHuman().getCombat());
		humanObj.sendMsg(msg);
	}

	/**
	 * 发送敌我标识变化
	 * 
	 * @param humanObj
	 */
	public void sendSCTeamBundleIDChange(HumanObject humanObj) {
		SCTeamBundleIDChange.Builder msg = SCTeamBundleIDChange.newBuilder();
		msg.setTeamBundleID(humanObj.teamBundleID);
		humanObj.sendMsg(msg);
	}

	/**
	 * 体力变化发送协议
	 * 
	 */
	@Listener(EventKey.HumanMoneyChange)
	public void _listener_HumanMoneyChange(Param param) {
		// TODO 体力变化
//		 HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
//		 MoneyType type = Utils.getParamValue(param, "type", null);
//		 if (humanObj == null) {
//		 Log.game.error("===_listener_HumanMoneyChange humanObj is null");
//		 return;
//		 }
//		 if(type.equals(MoneyType.ActValue)){
//		 sendSCActValueFullTimeChange(humanObj);//发送恢复满体力时间改变
//		 //System.out.println("当前时间："+Utils.formatTime(Port.getTime(),
//		 "yyyy-MM-dd HH:mm:ss:SS"));
//		 //System.out.println("满体时间："+Utils.formatTime(humanObj.getHuman().getActValueFullTime(),
//		 "yyyy-MM-dd HH:mm:ss:SS"));
//		 }
	}

	
	/**
	 * 保存一条待办事项
	 */
	public void saveOneBacklog(long humanId, BacklogType type, JSONObject jo) {
		Backlog pocketList = new Backlog();
		pocketList.setId(Port.applyId());
		pocketList.setHumanId(humanId);
		pocketList.setType(type.name());
		pocketList.setParamJSON(jo.toJSONString());
		pocketList.persist();
	}

	/**
	 * 每日签到
	 * 
	 * @param humanObject
	 */
	public void _msg_CSActivitySign(HumanObject humanObject) {
		// 废弃
	}

	/**
	 * 等级礼包
	 * 
	 * @param humanObject
	 * @param lvPackage
	 */
	public void _msg_CSActivityLvPackage(HumanObject humanObject, int lvPackage) {
		// 废弃
	}
	
	/**
	 * 下发玩家每日零点重置通知-其他零碎信息
	 * @param humanObj
	 */
	public void sendSCDailyResetChange(HumanObject humanObj) {
		SCDailyResetChange.Builder msg = SCDailyResetChange.newBuilder();
		msg.setDDailyReset(getDDailyReset(humanObj.getHuman()));
		humanObj.sendMsg(msg);
	}
	/**
	 * 下发玩家每日零点重置通知-消耗元宝购买次数
	 * @param humanObj
	 */
	public void sendSCDailyCostBuyChange(HumanObject humanObj) {
		SCDailyCostBuyChange.Builder msg = SCDailyCostBuyChange.newBuilder();
		msg.setDDailyCostBuy(getDDailyCostBuy(humanObj.getHuman()));
		humanObj.sendMsg(msg);
	}
	/**
	 * 获取玩家每日零点重置的信息-消耗元宝购买次数
	 * @return
	 */
	public DDailyCostBuy getDDailyCostBuy(Human human) {
		DDailyCostBuy.Builder dDailyCostBuy = DDailyCostBuy.newBuilder();
		dDailyCostBuy.setDailyActBuyNum(human.getDailyActBuyNum());//今日已购买体力次数
		dDailyCostBuy.setDailyCoinBuyNum(human.getDailyCoinBuyNum());//今日已购买铜币次数
		dDailyCostBuy.setDailyCompeteFightBuyNum(human.getDailyCompeteFightBuyNum());//今日已购买竞技场挑战次数
		dDailyCostBuy.setDailyLootMapReviveBuyNum(human.getDailyLootMapRevivalBuyNum());//今日已购买抢夺本复活次数
		ConfVipUpgrade confVip = ConfVipUpgrade.get(human.getVipLevel());
		// 获得免费发送魔法表情次数
		int canFreeNum = confVip.winksNum;
		int sendNum = human.getDailySendWinksNum();
		if (sendNum >= canFreeNum) {
			canFreeNum = 0;
		} else {
			canFreeNum -= sendNum;
		}
		dDailyCostBuy.setDailyFreeWinksNum(canFreeNum);
		return dDailyCostBuy.build();
	}
	
	/**
	 * 获取玩家每日零点重置通知-其他零碎信息
	 * @return
	 */
	public DDailyReset getDDailyReset(Human human) {
		DDailyReset.Builder dDailyReset = DDailyReset.newBuilder();
		dDailyReset.setDailyOnlineTime(human.getDailyOnlineTime());
		dDailyReset.setDailySignFlag(ESignType.valueOf(human.getDailySignFlag()));
		dDailyReset.setDailyCompeteFightNum(human.getDailyCompeteFightNum());
		dDailyReset.setDailyCompeteIntegral(human.getDailyCompeteIntegral());
		dDailyReset.addAllDFinishNum(TeamManager.inst().getActInstFinishNum(human));;
		dDailyReset.addAllDRankWorship(RankManager.inst().getRankWorship(human));
		dDailyReset.setDailyQuestLiveness(human.getDailyQuestLiveness());// 每日任务活跃度
		return dDailyReset.build();
	}
	
	/**
	 * 获取玩家VIP充值相关信息
	 * @return
	 */
	public DWeeklyReset getDWeeklyReset(Human human) {
		DWeeklyReset.Builder dWeeklyReset = DWeeklyReset.newBuilder();
		return dWeeklyReset.build();
	}
	
	/**
	 * 获取玩家新手引导相关信息
	 * @return
	 */
	public DGuideInfo getDGuideInfo(Human human) {
		DGuideInfo.Builder dGuideInfo = DGuideInfo.newBuilder();
		if (!human.getGuideIds().isEmpty()) {
			// 把引导信息取出来
			List<Integer> guiedIds = Utils.strToIntList(human.getGuideIds());
			if (guiedIds.size() >= 3) {
				dGuideInfo.setOrderIndex(guiedIds.get(0));
				dGuideInfo.setCurFunctionId(guiedIds.get(1));
				dGuideInfo.setFuncIndex(guiedIds.get(2));
			}
		}
		return dGuideInfo.build();
	}
	
	/**
	 * 获取玩家VIP充值相关信息
	 * @return
	 */
	public DVipPayment getDVipPayment(Human human) {
		DVipPayment.Builder dVipPayment = DVipPayment.newBuilder();
		dVipPayment.setVipLevel(human.getVipLevel());//VIP等级
		dVipPayment.setChargeGold(human.getChargeGold());//VIP经验，即累积充值元宝
		return dVipPayment.build();
	}
	
	/**
	 * 获取玩家显示装备和时装相关信息
	 * @return
	 */
	public DShowInfo getDShowInfo(Human human) {
		DShowInfo.Builder dShowInfo = DShowInfo.newBuilder();
		dShowInfo.setFashionShow(human.isFashionShow());// 是否显示时装
		dShowInfo.setFashionWeaponSn(human.getFashionWeaponSn());// 时装武器
		dShowInfo.setFashionClothesSn(human.getFashionSn());// 时装衣服
		return dShowInfo.build();
	}
	
	/**
	 * 获取玩家当前阵容总战力
	 */
	public int getLineUpCombat(HumanObject humanObj) {
		// 当前上阵的伙伴
		List<PartnerObject> partnerList = PartnerManager.inst().getPartnerList(humanObj);
		int combat = humanObj.getHuman().getCombat();
		for (PartnerObject partner : partnerList) {
			combat += partner.getPartner().getCombat();
		}
		return combat;
	}
	
	/**
	 * 创建录像副本场景
	 * @param humanObj
	 */
	public void createReplayStage(HumanObject humanObj, int result, int fightType, String token, 
			String crossIp, int crossPort, long stageId, int stageSn, int mapSn, RecordInfo record) {
		if(result != 0) {
			SCRecordFightInfo.Builder msg = SCRecordFightInfo.newBuilder();
			msg.setFightType(ECrossFightType.valueOf(fightType));
			msg.setResult(result);
			msg.setMapSn(mapSn);
			msg.setToken(token);
			msg.setIp(crossIp);
			msg.setPort(crossPort);
			humanObj.sendMsg(msg.build());
			return;
		}
		
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageReplay(record, stageSn, mapSn);
		prx.listenResult(this::_result_create, 
				HumanObject.paramKey, humanObj, 
				"stageSn", stageSn, 
				"fightType", fightType, 
				"mapSn", mapSn,
				"token", token,
				"crossIp", crossIp,
				"crossPort", crossPort,
				"record", record);
	}
	private void _result_create(Param results, Param context) {
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int fightType = Utils.getParamValue(context, "fightType", 0);
		String token = Utils.getParamValue(context, "token", "");
		String crossIp = Utils.getParamValue(context, "crossIp", "");
		int crossPort = Utils.getParamValue(context, "crossPort", 0);
		long stageId = Utils.getParamValue(results, "stageId", 0L);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if(humanObj == null) {
			Log.game.error("===_result_create humanObj==null");
			return;
		}
		SCRecordFightInfo.Builder msg = SCRecordFightInfo.newBuilder();
		String switchKey = mapSn + ":" + stageId;
		msg.setSwitchKey(switchKey);
		msg.setFightType(ECrossFightType.valueOf(fightType));
		msg.setResult(0);
		msg.setMapSn(mapSn);
		msg.setToken(token);
		msg.setIp(crossIp);
		msg.setPort(crossPort);
		humanObj.sendMsg(msg.build());	
	}
	/**
	 * 离开副本 自动回到副本进入前的主地图
	 * @param humanObj
	 */
	public void _msg_CSReplayLeave(HumanObject humanObj) {
		StageObjectReplay stageObj = (StageObjectReplay) humanObj.stageObj;
		if (stageObj == null) {
			return;
		}
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}
	
	/**
	 * 根据玩家vip获取可购买次数
	 * 
	 * @param humanObj
	 * @return
	 */
	public int getBuyNumVip(HumanObject humanObj) {
		int vipLv = humanObj.getHuman().getVipLevel();
		ConfVipUpgrade conf = ConfVipUpgrade.get(vipLv);
		if (conf == null) {
			Log.table.info("==getBuyNumVip 获取购买竞技场次数错误 ConfVipUpgrade sn={}", vipLv);
			return 0;
		}
		return conf.competeFightNum;
	}
}