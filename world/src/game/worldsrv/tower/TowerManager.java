package game.worldsrv.tower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;
import game.msg.Define.DProduce;
import game.msg.Define.DTowerLayerEnemy;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EInformType;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.Define.ETowerDifficulty;
import game.msg.MsgTower.SCTowerBuyLife;
import game.msg.MsgTower.SCTowerEnd;
import game.msg.MsgTower.SCTowerEnter;
import game.msg.MsgTower.SCTowerGoAhead;
import game.msg.MsgTower.SCTowerInfo;
import game.msg.MsgTower.SCTowerIsFight;
import game.msg.MsgTower.SCTowerLayerCount;
import game.msg.MsgTower.SCTowerMultipleAward;
import game.msg.MsgTower.SCTowerOpenCard;
import game.msg.MsgTower.SCTowerOpenRewardBox;
import game.msg.MsgTower.SCTowerResetConditon;
import game.msg.MsgTower.SCTowerSeasonInfo;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.config.ConfTower;
import game.worldsrv.config.ConfTowerMatch;
import game.worldsrv.config.ConfTowerScoreAward;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Tower;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.RankIntType;
import game.worldsrv.enumType.RankType;
import game.worldsrv.fightParam.TowerParam;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.rank.RankData;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectTower;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;

/**
 * @author Neak 爬塔
 */
public class TowerManager extends ManagerBase {
	public static TowerManager inst() {
		return inst(TowerManager.class);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// 1 客户端首次打开爬塔
	// 2 客户端爬塔数据不存在
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 1请求，初始化爬塔数据 2没有数据，客户端再次请求
	 */
	public void _msg_CSTowerModUnlock(HumanObject humanObj) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeTower, humanObj)) {
			Log.tower.error("=== 初始化数据失败，未达到解锁爬塔的等级 ===");
			humanObj.sendSysMsg(540201);
			return;
		}
		// 查询数据库
		DB dbPrx = DB.newInstance(Tower.tableName);
		dbPrx.get(humanObj.getHumanId());
		dbPrx.listenResult(this::_result_msg_CSTowerModUnlock, "humanObj", humanObj);
	}

	/**
	 * 判断是否有数据
	 */
	private void _result_msg_CSTowerModUnlock(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.tower.error("===_result_loadHumanPartner humanObj is null");
			return;
		}
		Record record = results.get();
		// 如果没有玩家的爬塔数据
		if (record == null) {
			// 解锁，初始化爬塔数据
			init_towerUnlock(humanObj);
		} else {
			// 不是登录流程，玩家有爬塔数据
			process_sendSCTowerInfo(record, humanObj, false);
		}
	}

	/**
	 * 爬塔解锁，初始化数据
	 */
	private void init_towerUnlock(HumanObject humanObj) {
		// 实例化主角爬塔信息
		TowerRecord towerRecord = new TowerRecord();
		// 爬塔持久化数据
		Tower tower = towerRecord.getTower();
		tower.setId(humanObj.id);
		tower.setScore(0);
		tower.setRank(ParamManager.rankTopRecordMaxNum[RankIntType.Tower.value()]);
		tower.setSeasonEndTime(0);
		tower.setMatchLv(humanObj.getHuman().getLevel()); // 匹配时的等级
		tower.setMatchCombat(ParamManager.towerInitCombat); // 开放时的基础战力
		tower.setFirstDailyTime(Port.getTime());
		tower.setStayLayer(1);
		tower.setWillFightLayer(1);
		tower.setHaveLifeNum(ParamManager.towerInitHaveLife); // 初始拥有的生命
		tower.setMultiple(1); // 奖励倍率
		tower.setRewardBox("");
		tower.setDiffcultyLv1("");
		tower.setDiffcultyLv2("");
		tower.setDiffcultyLv3("");
		humanObj.towerRecord = towerRecord;
		// 加载匹配相关信息
		init_towerMatch(humanObj, towerRecord);
	}

	/**
	 * 解锁，初始化匹配数据
	 */
	private void init_towerMatch(HumanObject humanObj, TowerRecord towerRecord) {
		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.matchTowerHuman(towerRecord);
		proxy.listenResult(this::_result_init_towerMatch, "humanObj", humanObj);
	}

	/**
	 * 匹配数据完毕，插入数据库，发送爬塔数据
	 */
	private void _result_init_towerMatch(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		boolean can = Utils.getParamValue(results, "result", null);
		if (!can) {
			return;
		}
		TowerRecord towerRecord = Utils.getParamValue(results, "towerRecord", null);
		humanObj.towerRecord = towerRecord;
		// 持久化数据
		towerRecord.getTower().persist();
		// 玩家数据不存在/解锁，下发玩家爬塔信息
		_send_SCTowerInfo(humanObj, false);
	}
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 登录，下发爬塔今天是否挑战过
	 */
	@Listener(EventKey.HumanDataLoadOther2)
	public void _listener_HumanDataLoadOther2(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.tower.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeTower, humanObj)) {
			Log.tower.debug("=== 未达到解锁爬塔的等级 ===");
			return;
		}

		if (humanObj.isDailyFirstLogin) {
			// 设置今日挑战状态
			humanObj.extInfo.setTowerIsFight(false);
		}
		this.sendSCTowerIsFight(humanObj);

		DB dbPrx = DB.newInstance(Tower.tableName);
		dbPrx.get(humanObj.getHumanId());
		dbPrx.listenResult(this::_result_loadTower, "humanObj", humanObj);
		Event.fire(EventKey.HumanDataLoadOther2BeginOne, "humanObj", humanObj);
	}

	/**
	 * 读取数据库数据完毕
	 */
	private void _result_loadTower(Param results, Param context) {
		Record record = results.get();
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.tower.error("===_result_loadHumanPartner humanObj is null");
			return;
		}
		if (record == null) {
			Event.fire(EventKey.HumanDataLoadOther2FinishOne, "humanObj", humanObj);
			return;
		}
		// 登录流程，玩家有爬塔数据
		process_sendSCTowerInfo(record, humanObj, true);
	}

	/**
	 * 玩家有爬塔数据
	 * 
	 * @param isLogin
	 *            是否登录流程 true登录流程，false非登陆流程
	 */
	private void process_sendSCTowerInfo(Record record, HumanObject humanObj, boolean isLogin) {
		// 加载数据
		Tower tower = new Tower(record);
		TowerRecord towerRecord = new TowerRecord();
		towerRecord.init(tower);
		// 初始化时复赋值
		humanObj.towerRecord = towerRecord;

		long tmNow = Port.getTime();
		// 今日凌晨5点重置时间
		long tmDailyReset = Utils.getTimeBeginOfToday(tmNow) + ParamManager.dailyHourReset * Time.HOUR;
		long tmFirstDailyTime = towerRecord.getTower().getFirstDailyTime();
		// 每日首次初始爬塔数据时间
		if (tmFirstDailyTime < tmDailyReset && tmDailyReset <= tmNow) {
			// 重置后，走正常的下发爬梯信息流程
			resetDaily(humanObj, isLogin);
		} else {
			// 下发玩家爬塔信息
			_send_SCTowerInfo(humanObj, isLogin);
		}
	}

	/**
	 * 登录 / 解锁 / 跨天， 查找所有层级的人数后再下发
	 */
	private void _send_SCTowerInfo(HumanObject humanObj, boolean isLogin) {
		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.getLayerInfo();
		proxy.listenResult(this::_result_send_SCTowerInfo, "humanObj", humanObj, "isLogin", isLogin);
	}

	/**
	 * 登录 / 解锁 / 跨天， 下发爬塔数据
	 */
	private void _result_send_SCTowerInfo(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		boolean isLogin = Utils.getParamValue(context, "isLogin", false);
		// 爬塔人数列表
		List<Integer> countList = Utils.getParamValue(results, "countList", null);
		// 爬塔赛季结束时间
		long seasonEndTime = Utils.getParamValue(results, "seasonEndTime", 0l);

		TowerRecord towerRecord = humanObj.towerRecord;
		Tower tower = towerRecord.getTower();

		// 当前时间> 玩家记录赛季结束时间，则意味着是个新赛季
		if (Port.getTime() >= tower.getSeasonEndTime()) {
			this.proess_newSeason(humanObj, seasonEndTime);
		}

		SCTowerInfo.Builder msg = SCTowerInfo.newBuilder();
		msg.setMatchLv(tower.getMatchLv());
		msg.setMatchCombat(tower.getMatchCombat());
		msg.setYestodayMaxLayer(tower.getYestodayMaxLayer());
		msg.setScore(tower.getScore());
		msg.setSeasonEndTime(tower.getSeasonEndTime());
		msg.setTowerRank(tower.getRank());

		msg.setStayLayer(tower.getStayLayer());
		msg.setWillFightLayer(tower.getWillFightLayer());
		msg.setHaveLifeNum(tower.getHaveLifeNum());
		msg.setBuyLifeNum(tower.getBuyLifeNum());
		msg.setMultiple(tower.getMultiple());
		msg.addAllRewardBoxList(towerRecord.createDTowerRewardBoxList());
		msg.addAllLayerCountList(countList);
		msg.setMatchEnemy(towerRecord.createDTowerLayerEnemy(tower.getWillFightLayer()));
		humanObj.sendMsg(msg);
		// 登录流程
		if (isLogin) {
			// 一条二级模块数据加载完成
			Event.fire(EventKey.HumanDataLoadOther2FinishOne, "humanObj", humanObj);
		}
	}

	/**
	 * 爬塔挑战
	 * 
	 * @param humanObj
	 * @param fightLayer
	 *            挑战层数
	 * @param selDiff
	 *            选择的难度
	 */
	public void _msg_CSTowerEnter(HumanObject humanObj, int fightLayer, int selDiff) {

		TowerRecord towerRecord = humanObj.towerRecord;
		Tower tower = towerRecord.getTower();
		// 正在挑战中
		if (towerRecord.isFighting) {
			return;
		}
		// 当前生命
		int haveLife = tower.getHaveLifeNum();
		if (haveLife <= 0) {
			Log.tower.error("=== 当前生命不足，无法挑战 haveLife:{}===", haveLife);
			humanObj.sendSysMsg(541101);
			return;
		}

		// 将要挑战层级
		int willFightLayer = tower.getWillFightLayer();
		if (willFightLayer != fightLayer) {
			Log.tower.error("=== 挑战层级错误，无法挑战 id:{} fightLayer:{}, willFightLayer:{}===", tower.getId(), fightLayer,
					willFightLayer);
			humanObj.sendSysMsg(541102);
			return;
		}
		// 选择的难度
		if (selDiff < ETowerDifficulty.TowerDiffLv1_VALUE || selDiff > ETowerDifficulty.values().length) {
			Log.tower.error("=== 选择难度异常，无法挑战 selDiff:{}===", selDiff);
			humanObj.sendSysMsg(541103);
			return;
		}

		// 判断副本配置是否合法
		int stageSn = ParamManager.towerMapSn;
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.stageCommon.error("===进入爬塔挑战错误： ConfInstStage.sn={}", stageSn);
			return;
		}

		// 获得本次战斗需要挑战对象
		this._get_towerEnemy(humanObj, stageSn, confInstStage.mapSN, fightLayer, selDiff);
	}

	/**
	 * 获得爬塔对手
	 * 
	 * @param humanObj
	 * @param fightLayer
	 *            挑战层级
	 * @param selDiff
	 *            选择难度
	 */
	private void _get_towerEnemy(HumanObject humanObj, int stageSn, int mapSn, int fightLayer, int selDiff) {
		// 根据层级和难度，找到对应层的humanId
		TowerLayerJSON tlJSON = humanObj.towerRecord.getTowerLayerJSON(fightLayer, selDiff);
		if (tlJSON == null) {
			Log.table.error("=== processInstBattle 爬塔数据异常，无法战斗，layer:{} , diff:{} ===", fightLayer, selDiff);
			return;
		}
		long humanId = tlJSON.humanId;
		// 如果id在怪物军团表中有的话，则类型为怪物
		// 类型：怪物
		// if (ConfInstMonster.containsKey((int)humanId)) {
		// // 怪物军队
		// Army enemyArmy =
		// ArmyManager.inst().initArmyTower_monster((int)humanId, tlJSON.combat,
		// BattleDef.Team2Flag, soldierMgr, buffMgr);
		// return;
		// }

		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.getTowerPartner(humanId);
		proxy.listenResult(this::_result_get_towerEnenmy, "humanObj", humanObj, "towerLayerJSON", tlJSON, "fightLayer",
				fightLayer, "selDiff", selDiff, "stageSn", stageSn, "mapSn", mapSn);
	}

	private void _result_get_towerEnenmy(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		TowerLayerJSON tlJSON = Utils.getParamValue(context, "towerLayerJSON", null);
		int stageSn = Utils.getParamValue(context, "stageSn", 0);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int fightLayer = Utils.getParamValue(context, "fightLayer", 0);
		int selDiff = Utils.getParamValue(context, "selDiff", 0);

		// 查找到的对方爬塔镜像数据
		TowerHumanObj towerHumanObj = Utils.getParamValue(results, "towerHumanObj", null);
		if (null == towerHumanObj) {
			// 挑战对象为空
			Log.logGameTest("_result_get_towerEnenmy爬塔挑战对象返回为空id={}", tlJSON.humanId);
			return;
		}
		// 创建爬塔战场
		createTower(humanObj, towerHumanObj, stageSn, mapSn, fightLayer, selDiff);
	}

	/**
	 * 创建爬塔战斗场景
	 * 
	 * @param humanObj
	 * @param stageSn
	 * @param mapSn
	 * @param fightLayer
	 * @param selDiff
	 */
	private void createTower(HumanObject humanObj, TowerHumanObj towerHumanObj, int stageSn, int mapSn, int fightLayer,
			int selDiff) {
		humanObj.setCreateRepTime();// 进入异步前要先设置，避免重复操作

		// 过关条件
		TowerLayerJSON layerJSON = humanObj.towerRecord.getTowerLayerJSON(fightLayer, selDiff);
		List<Integer> conditions = null;
		if (layerJSON != null) {
			conditions = layerJSON.conditions;
		}
		TowerParam towerParam = new TowerParam(new HumanMirrorObject(towerHumanObj, layerJSON.combat), conditions);
		Param param = new Param(HumanMirrorObject.TowerParam, towerParam);

		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageTower(stageSn, mapSn, ECrossFightType.FIGHT_TOWER_VALUE, fightLayer, selDiff, param);
		prx.listenResult(this::_result_createTower, "humanObj", humanObj, "fightLayer", fightLayer, "selDiff", selDiff,
				"stageSn", stageSn, "mapSn", mapSn);

	}

	private void _result_createTower(Param results, Param context) {
		// 创建完毕，用户切换地图
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		int stageSn = Utils.getParamValue(context, "stageSn", 0);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int fightLayer = Utils.getParamValue(context, "fightLayer", 0);
		int selDiff = Utils.getParamValue(context, "selDiff", 0);

		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (stageId < 0 || mapSn < 0 || null == humanObj) {
			Log.game.error("===创建失败：_result_create stageId={}, mapSn={}, humanObj={}", stageId, mapSn, humanObj);
			return;
		}

		Tower tower = humanObj.towerRecord.getTower();
		// 战前先扣一条生命（如果胜利则加回来，失败就不做操作了）
		int haveLife = tower.getHaveLifeNum();
		// 测试时，暂时不扣生命
		tower.setHaveLifeNum(haveLife - 1);
		// 设置正在战斗中
		humanObj.towerRecord.isFighting = true;

		// 判断是否第一次挑战
		if (tower.getFirstFightTime() == 0) {
			long firstTime = Utils.getHourMillis(ParamManager.dailyHourReset);
			if (Port.getTime() < firstTime) {
				firstTime -= Time.DAY;
			}
			// 首次挑战则设置首次挑战时间(都为重置时间)
			tower.setFirstFightTime(firstTime);
			// 增加挑战人数
			TowerServiceProxy proxy = TowerServiceProxy.newInstance();
			proxy.changeLayerCount(fightLayer);
		}

		// 确认进入爬塔后，发送当前生命（先暂时扣一条，赢了加回来）
		SCTowerEnter.Builder towerMsg = SCTowerEnter.newBuilder();
		towerMsg.setHaveLifeNum(tower.getHaveLifeNum());
		humanObj.sendMsg(towerMsg);

		// 派发进入爬塔事件
		Event.fire(EventKey.TowerEnter, "humanObj", humanObj, "stageSn", stageSn, "layer", fightLayer, "diff", selDiff);

		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_TOWER, mapSn, stageId);
	}

	/**
	 * 离开爬塔挑战（逃跑/正常结算后发送）
	 * 
	 * @param humanObj
	 */
	public void _msg_CSTowerLeave(HumanObject humanObj) {
		// 切换地图
		if (!(humanObj.stageObj instanceof StageObjectTower)) {
			return;
		}
		StageObjectTower stageObj = (StageObjectTower) humanObj.stageObj;
		if (stageObj == null) {
			return;
		}

		// 离开爬塔玩法，清除组队ID
		if (humanObj.getTeamId() > 0) {
			TeamManager.inst()._msg_CSTeamLeave(humanObj);
		}

		// 设置爬塔不在战斗中
		humanObj.towerRecord.isFighting = false;
		// 离开爬塔玩法
		// humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}

	/**
	 * 爬塔正常结算
	 * 
	 * @param humanObj
	 * @param isFail
	 *            是否失败
	 */
	public void _msg_CSTowerEnd(HumanObject humanObj, List<Integer> stars, boolean isFail) {
		// 不再爬塔挑战中
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectTower)) {
			Log.tower.error(" === 结算失败，不再爬塔挑战中 ===");
			return;
		}
		// 玩家所在爬塔场景
		StageObjectTower stageObj = (StageObjectTower) (humanObj.stageObj);
		// 爬塔数据管理
		TowerRecord towerRecord = humanObj.towerRecord;
		// 爬塔数据
		Tower tower = towerRecord.getTower();
		// 挑战的层数
		int fightLayer = stageObj.fightLayer;
		// 选择的难度
		int selDiff = stageObj.selDiff;
		// 战斗中是否跨天
		boolean isSpanDay = stageObj.isSpanDay;
		// 设置爬塔不在战斗中
		towerRecord.isFighting = false;

		SCTowerEnd.Builder msg = SCTowerEnd.newBuilder();
		msg.setIsFail(isFail);
		// 爬塔失败，则直接发送以上三条信息给客户端
		if (isFail) {
			humanObj.sendMsg(msg);
			return;
		}

		// 获得配置表
		ConfTower confTower = ConfTower.get(fightLayer);
		// 发放胜利奖励
		int rewardSn = getRewardSn(confTower, selDiff, humanObj.getHuman().getLevel());
		// 每日首次通关掉落
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		if (confRewards == null) {
			Log.table.error("===配置表错误ConfRewards no find sn={}", rewardSn);
		} else {
			int multiple = tower.getMultiple(); // 奖励倍数
			int[] itemSnAry = confRewards.itemSn;
			int[] itemNumAry = confRewards.itemNum;
			int[] totalNumAry = new int[itemNumAry.length];
			// 根据倍率，修改奖励的数量
			for (int i = 0; i < itemNumAry.length; i++) {
				int num = itemNumAry[i] * multiple;
				totalNumAry[i] = num;
			}
			// 奖励，积分也包含在这里面
			ItemChange rewards = RewardHelper.reward(humanObj, itemSnAry, totalNumAry, LogSysModType.TowerPassReward);
			// 添加奖励列表
			msg.addAllItems(rewards.getProduce());
		}
		// 积分情况下发
		msg.setScore(tower.getScore());

		// 跨天情况
		msg.setIsSpanDay(isSpanDay);
		// 没有在战斗中经过跨天
		if (!isSpanDay) {
			if (!humanObj.getHumanExtInfo().isTowerIsFight()) {
				// 设置今日已经挑战过
				humanObj.getHumanExtInfo().setTowerIsFight(true);
				this.sendSCTowerIsFight(humanObj);
			}

			// 胜利且没有跨天
			int haveLifeNum = tower.getHaveLifeNum();
			// 要把扣掉的生命加回来
			tower.setHaveLifeNum(haveLifeNum + 1);
			// 将要挑战的层数+1
			int willFightLayer = tower.getWillFightLayer() + 1;
			tower.setWillFightLayer(willFightLayer);
			// 设置最后过关的难度
			tower.setLastSelDifficulty(selDiff);
			// 设置最后过关的时间
			tower.setLastPassTime(Port.getTime());

			if (confTower != null) {
				int[] dropIds = this.getRewardBoxDropSn(humanObj.getHuman().getLevel(), confTower, selDiff);
				if (dropIds != null) {
					int dropId0 = dropIds[0]; // 普通翻牌
					int dropId1 = dropIds[1]; // 元宝翻牌
					TowerBoxJSON box = new TowerBoxJSON(fightLayer, selDiff, dropId0, dropId1);
					// 插入一个新的box
					towerRecord.addRewardBox(box);
					msg.setRewardBox(box.createDTowerRewardBox());
				}
			}

			msg.setHaveLifeNum(tower.getHaveLifeNum());
			msg.setStayLayer(fightLayer);
			msg.setWillFightLayer(tower.getWillFightLayer());

			// 查询下一层人数
			TowerServiceProxy proxy = TowerServiceProxy.newInstance();
			proxy.changeLayerCount(tower.getWillFightLayer());
			proxy.listenResult(this::_result_msg_CSTowerEnd, "humanObj", humanObj, "msg", msg);

		} else {
			humanObj.sendMsg(msg);
		}
		// 本次挑战的难度层数
		int curFightDiffLayer = this.getDiffLayerValue(fightLayer, selDiff);
		// 记录挑战的层数
		towerRecord.addAlreadyFight(curFightDiffLayer);
		// 发布七日事件
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", tower.getScore(), "type",
				ActivitySevenTypeKey.Type_52);

		// 发送爬塔通过难度和层数
		Event.fire(EventKey.TowerPass, "humanObj", humanObj, "selDiff", selDiff, "layer", fightLayer);

		// 历史最高层数
		int historyLayerDidff = tower.getHistoryMaxLayer();
		if (curFightDiffLayer > historyLayerDidff) {
			// 设置历史最高层数
			tower.setHistoryMaxLayer(curFightDiffLayer);
			// 推送通关跑马灯
			if (fightLayer == ParamManager.towerMaxLayer) {
				int snSysMsg = 999005;
				if (selDiff == ETowerDifficulty.TowerDiffLv2_VALUE) {
					snSysMsg = 999006;
				} else if (selDiff == ETowerDifficulty.TowerDiffLv3_VALUE) {
					snSysMsg = 999007;
				}
				// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
				String content = Utils.createStr("{}|{}|{}", ParamManager.sysMsgMark, snSysMsg,
						humanObj.getHuman().getName());
				InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
			}
		}
		// 今日是否第一个通过该层的玩家，推送第一个通关的跑马灯
		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.processFirstPass(fightLayer, selDiff);
		proxy.listenResult(this::_result_msg_IsFirstPass, "humanObj", humanObj, "selDiff", selDiff, "layer",
				fightLayer);
	}

	private void _result_msg_CSTowerEnd(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		SCTowerEnd.Builder msg = Utils.getParamValue(context, "msg", null);
		// 人数列表
		List<Integer> countList = Utils.getParamValue(results, "countList", null);

		// 更新爬塔排行榜数据
		RankData rankData = this.createRankData(humanObj, humanObj.towerRecord.getTower());
		RankGlobalServiceProxy rankGlobalPxy = RankGlobalServiceProxy.newInstance();
		rankGlobalPxy.addNew(rankData, RankType.RankTower);
		rankGlobalPxy.listenResult(this::_result_msg_CSTowerEnd1, "humanObj", humanObj, "msg", msg, "countList",
				countList);
	}

	private void _result_msg_CSTowerEnd1(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		SCTowerEnd.Builder msg = Utils.getParamValue(context, "msg", null);
		List<Integer> countList = Utils.getParamValue(context, "countList", null);
		int defaultRank = ParamManager.rankTopRecordMaxNum[RankIntType.Tower.value()];
		// 排名
		int ranking = Utils.getParamValue(results, "ranking", defaultRank);
		if (ranking > defaultRank) {
			ranking = defaultRank;
		}
		// 设置数据
		humanObj.towerRecord.getTower().setRank(ranking);

		msg.setTowerRank(ranking);
		msg.addAllLayerCountList(countList);
		humanObj.sendMsg(msg);
	}

	private void _result_msg_IsFirstPass(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		int layer = Utils.getParamValue(context, "layer", 0);
		int selDiff = Utils.getParamValue(context, "selDiff", 0);
		boolean isFirst = Utils.getParamValue(results, "isFirst", false);
		if (!isFirst) {
			return;
		}

		int snSysMsg = 999008;
		if (selDiff == ETowerDifficulty.TowerDiffLv2_VALUE) {
			snSysMsg = 999009;
		} else if (selDiff == ETowerDifficulty.TowerDiffLv3_VALUE) {
			snSysMsg = 999010;
		}
		// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
		String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, snSysMsg,
				humanObj.getHuman().getName(), layer);
		InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
	}

	/**
	 * 进入下一层
	 * 
	 * @param humanObj
	 * @param stayLayer
	 */
	public void _msg_CSTowerGoAhead(HumanObject humanObj, int stayLayer) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}
		Tower tower = towerRecord.getTower();
		// 当前停留层+1，不等于将要挑战层。或者当前停留层超过最大层数则异常
		if (stayLayer + 1 != tower.getWillFightLayer() || stayLayer >= ParamManager.towerMaxLayer) {
			Log.tower.error("=== 进入下一层失败，数据异常 id:{}, reqStayLayer:{}, serverStayLayer:{} WFLayer:{}, maxLayer:{} ===", 
					tower.getId(), stayLayer, tower.getStayLayer(), tower.getWillFightLayer(), ParamManager.towerMaxLayer);
			humanObj.sendSysMsg(541501);
			return;
		}

		// 前进到下一层
		int nextLayer = tower.getWillFightLayer();
		tower.setStayLayer(nextLayer);

		SCTowerGoAhead.Builder msg = SCTowerGoAhead.newBuilder();
		msg.setStayLayer(tower.getStayLayer());
		msg.setFightLayer(tower.getWillFightLayer());
		int willFightLayer = tower.getWillFightLayer();
		// 前进后的停留层数没有超过最大层数，则要下发当前层的匹配敌方数据
		if (willFightLayer <= ParamManager.towerMaxLayer) {
			DTowerLayerEnemy enemy = towerRecord.createDTowerLayerEnemy(willFightLayer);
			if (enemy == null) {
				Log.tower.error("=== 匹配信息异常 ===");
				humanObj.sendSysMsg(541502);
				return;
			}
			msg.setMatchEnemy(enemy);
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 打开宝箱
	 * 
	 * @param humanObj
	 * @param boxLayer
	 *            宝箱所在层数
	 */
	public void _msg_CSTowerOpenRewardBox(HumanObject humanObj, int boxLayer) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}
		TowerBoxJSON boxJSON = towerRecord.getTowerBoxJSON(boxLayer);
		if (boxJSON == null) {
			Log.tower.error("=== 宝箱不存在，无法开启 ===");
			humanObj.sendSysMsg(541701);
			return;
		}

		SCTowerOpenRewardBox.Builder msg = SCTowerOpenRewardBox.newBuilder();
		humanObj.sendMsg(msg);
	}

	/**
	 * 请求翻牌
	 * 
	 * @param humanObj
	 * @param boxLayer
	 *            宝箱所在的层级
	 * @param openIndex
	 *            翻牌的index
	 */
	public void _msg_CSTowerOpenCard(HumanObject humanObj, int boxLayer, int openIndex, boolean isCost) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}
		TowerBoxJSON rewardBox = towerRecord.getTowerBoxJSON(boxLayer);
		if (rewardBox == null) {
			Log.tower.error("=== 宝箱不存在，无法开启 ===");
			humanObj.sendSysMsg(541901);
			return;
		}
		if (rewardBox.isRepeatOpen(openIndex, isCost)) {
			Log.tower.error("=== 翻牌失败，选择的卡牌异常 {},{}===", openIndex, isCost);
			humanObj.sendSysMsg(541902);
			return;
		}
		// 获取已经翻牌的次数
		int openCount = rewardBox.getOpenCount(isCost);
		if (openCount != 0) {
			// FIXME 预留：翻多张牌，需要花费钻石 需要配置支持
			// int costMoney =
			// RewardHelper.getCostGold(ECostGoldType.towerReviveCost,
			// openCount);
			// if (!RewardHelper.checkAndConsume(humanObj,
			// EMoneyType.gold_VALUE, costMoney, LogSysModType.TowerCardOpen)) {
			// Log.tower.error("=== 翻牌失败，货币不足 ===");
			// return;
			// }
			return;
		}
		int money = this.getCostOpenCardMoney(boxLayer, rewardBox.diff, isCost);
		if (money != 0) {
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, money, LogSysModType.TowerCardOpen)) {
				Log.tower.error("=== 翻牌失败，货币不足 ===");
				return;
			}
		}

		// 设置翻牌数据
		rewardBox.setCardOpen(openIndex, isCost);
		towerRecord.modifyRewardBox(rewardBox);

		// 应答
		SCTowerOpenCard.Builder msg = SCTowerOpenCard.newBuilder();
		msg.setRewardBox(rewardBox.createDTowerRewardBox());
		msg.setOpenIndex(openIndex);
		msg.setIsCost(isCost);

		int dropId = rewardBox.dropId;
		if (isCost) {
			dropId = rewardBox.dropId1;
		}
		// 随机出三张牌的奖励
		for (int i = 0; i < TowerBoxJSON.TOWRR_CARD_NUM; i++) {
			DropBag dropBag = DropManager.inst().getItem(humanObj, dropId);
			if (dropBag != null && !dropBag.isEmpty()) {
				DProduce.Builder produce = msg.addRewardItemListBuilder();
				produce.setSn(dropBag.getItemSn()[0]);
				produce.setNum(dropBag.getItemNum()[0]);
				if (i == openIndex) {
					// ItemChange dropRand =
					RewardHelper.reward(humanObj, dropBag.getItemSn(), dropBag.getItemNum(),
							LogSysModType.TowerCardOpen);
				}
			}
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 购买生命
	 * 
	 * @param humanObj
	 */
	public void _msg_CSTowerBuyLife(HumanObject humanObj) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}
		Tower tower = towerRecord.getTower();

		// 当前生命
		int haveNum = tower.getHaveLifeNum();
		if (haveNum > 0) {
			Log.tower.error("=== 生命充足，无法复活  ===");
			humanObj.sendSysMsg(542101);
			return;
		}

		// 获取当前vip等级配置
		ConfVipUpgrade confVip = ConfVipUpgrade.get(humanObj.getHuman().getVipLevel());
		// 已经购买的次数
		int buyNum = tower.getBuyLifeNum();
		// 当前最大的购买复活次数
		int maxReborn = confVip.towerRebornNum;
		if (buyNum >= maxReborn) {
			Log.tower.error("=== 爬塔复活失败，复活已达上限  ===");
			humanObj.sendSysMsg(542102);
			return;
		}

		// 当前购买次数
		int curBuyNum = buyNum + 1;
		int costMoney = RewardHelper.getCostGold(ECostGoldType.towerReviveCost, curBuyNum);
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costMoney, LogSysModType.TowerBuyLife)) {
			Log.tower.error("=== 爬塔复活失败，货币不足 ===");
			return;
		}

		// 购买次数+1
		tower.setBuyLifeNum(buyNum + 1);
		// 生命置成1
		tower.setHaveLifeNum(1);

		// 应答
		SCTowerBuyLife.Builder msg = SCTowerBuyLife.newBuilder();
		msg.setBuyLifeNum(tower.getBuyLifeNum());
		msg.setHaveLifeNum(tower.getHaveLifeNum());
		humanObj.sendMsg(msg);
	}

	/**
	 * 购买结算多倍奖励
	 */
	public void _msg_CSTowerMultipleAward(HumanObject humanObj) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}
		Tower tower = towerRecord.getTower();

		// 获取当前vip等级配置
		ConfVipUpgrade confVip = ConfVipUpgrade.get(humanObj.getHuman().getVipLevel());
		// 倍数
		int multiple = confVip.towerBuyNum / Utils.I10000;
		if (multiple == 0) {
			Log.tower.error("=== 购买多倍奖励失败，VIP等级不足  ===");
			humanObj.sendSysMsg(542301);
			return;
		}

		// 爬塔多倍奖励购买（2倍）
		int costMoney = RewardHelper.getCostGold(ECostGoldType.towerDoubleCost, 1);
		if (multiple >= 3) {
			// 爬塔多倍奖励购买（3倍）
			costMoney = RewardHelper.getCostGold(ECostGoldType.towerTripleCost, 1);
		}
		// 类型后续需要修改
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costMoney, LogSysModType.TowerBuyMutiple)) {
			Log.tower.error("=== 购买多倍奖励失败，钻石不足===");
			return;
		}

		// 设置奖励倍数
		tower.setMultiple(multiple);

		// 应答
		SCTowerMultipleAward.Builder msg = SCTowerMultipleAward.newBuilder();
		msg.setMultiple(multiple);
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取所有层数的信息
	 * 
	 * @param humanObj
	 */
	public void _msg_CSTowerLayerCount(HumanObject humanObj) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}

		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.getLayerInfo();
		proxy.listenResult(this::_result_msg_CSTowerLayerCount, "humanObj", humanObj);
	}

	private void _result_msg_CSTowerLayerCount(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		// 爬塔人数列表
		List<Integer> countList = Utils.getParamValue(results, "countList", null);
		SCTowerLayerCount.Builder msg = SCTowerLayerCount.newBuilder();
		msg.addAllLayerCountList(countList);
		humanObj.sendMsg(msg);
	}

	/**
	 * 重置爬塔过关条件
	 * 
	 * @param humanObj
	 */
	public void _msg_CSTowerResetConditon(HumanObject humanObj) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}

		int[] cost = ParamManager.towerResetting;
		// 消耗判断
		if (!RewardHelper.checkAndConsume(humanObj, cost[0], cost[1], LogSysModType.TowerReset)) {
			return;
		}

		int willFightLayer = towerRecord.getTower().getWillFightLayer();
		// 重置条件
		towerRecord.resetCondition(willFightLayer);

		SCTowerResetConditon.Builder msg = SCTowerResetConditon.newBuilder();
		msg.setMatchEnemy(towerRecord.createDTowerLayerEnemy(willFightLayer));
		humanObj.sendMsg(msg);
	}

	/**
	 * gm重新匹配九重天数据
	 */
	public void _gm_towerMatch(HumanObject humanObj) {
		resetDaily(humanObj);
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
		if (humanObj.towerRecord == null) {
			return;
		}

		long curTime = Port.getTime();
		int hour = Utils.getHourOfDay(curTime);
		if (hour == ParamManager.dailyHourReset) {
			// 跨天的时候如果在战斗
			spcialDaily(humanObj);
			// 每日重置
			resetDaily(humanObj); // 持久化九重天角色数据
		}

		// 时间>=赛季结束时间，则重启新赛季
		if (curTime >= humanObj.towerRecord.getTower().getSeasonEndTime()) {
			restartSeason(humanObj);
		}
	}

	/**
	 * 在线重置每日爬塔信息
	 */
	private void resetDaily(HumanObject humanObj) {
		// 在线每日重置
		resetDaily(humanObj, false); // 持久化九重天角色数据
	}

	/**
	 * 爬塔信息每日重置
	 */
	private void resetDaily(HumanObject humanObj, boolean isLogin) {
		// 是否解锁爬塔
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeTower_VALUE, humanObj)) {
			return;
		}

		// 设置今日挑战状态
		humanObj.extInfo.setTowerIsFight(false);
		this.sendSCTowerIsFight(humanObj);

		if (humanObj.towerRecord == null) {
			return;
		}
		// 实例化主角爬塔信息
		TowerRecord towerRecord = humanObj.towerRecord;
		// 爬塔持久化数据
		Tower tower = towerRecord.getTower();
		tower.setId(humanObj.id);
		// 最后的难度
		int lastDiff = tower.getLastSelDifficulty();
		// 最后通过的层数
		int maxLayer = tower.getWillFightLayer() - 1;
		// 设置昨日通过的最高层数
		tower.setYestodayMaxLayer(this.getDiffLayerValue(maxLayer, lastDiff));
		tower.setMatchLv(humanObj.getHuman().getLevel()); // 匹配时的等级
		tower.setMatchCombat(this.getMatchCombat(humanObj)); // 跨天时的匹配战力
		tower.setFirstDailyTime(Port.getTime()); // 设置今日跨天时间
		tower.setFirstFightTime(0);
		tower.setLastPassTime(0);
		tower.setStayLayer(1);
		tower.setWillFightLayer(1);
		tower.setHaveLifeNum(ParamManager.towerInitHaveLife); // 初始拥有的生命
		tower.setBuyLifeNum(0); // 购买生命次数设置为0
		tower.setMultiple(1); // 奖励倍率
		tower.setRewardBox("");
		tower.setDiffcultyLv1("");
		tower.setDiffcultyLv2("");
		tower.setDiffcultyLv3("");
		// 跨天匹配爬塔数据
		dailyReset_towerMatch(humanObj, towerRecord, isLogin);
	}

	/**
	 * 跨天，重置匹配数据
	 */
	private void dailyReset_towerMatch(HumanObject humanObj, TowerRecord towerRecord, boolean isLogin) {
		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.matchTowerHuman(towerRecord);
		proxy.listenResult(this::_result_dailyReset_towerMatch, "humanObj", humanObj, "isLogin", isLogin);
	}

	/**
	 * 匹配数据完毕，插入数据库，发送爬塔数据
	 */
	private void _result_dailyReset_towerMatch(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		boolean isLogin = Utils.getParamValue(context, "isLogin", false);

		boolean can = Utils.getParamValue(results, "result", null);
		if (!can) {
			return;
		}
		TowerRecord towerRecord = Utils.getParamValue(results, "towerRecord", null);
		humanObj.towerRecord = towerRecord;
		// 下发玩家爬塔信息
		_send_SCTowerInfo(humanObj, isLogin);
	}

	/**
	 * 设置爬塔今日是否挑战过
	 * 
	 * @param humanObj
	 */
	private void sendSCTowerIsFight(HumanObject humanObj) {
		SCTowerIsFight.Builder msg = SCTowerIsFight.newBuilder();
		msg.setTodayIsFight(humanObj.extInfo.isTowerIsFight());
		humanObj.sendMsg(msg);
	}

	/**
	 * 获得匹配战力
	 * 
	 * see 公式：(A*c + B*d ) / 2
	 */
	private int getMatchCombat(HumanObject humanObj) {
		// A:等级修正战力
		int towerCorrect = ConfLevelExp.get(humanObj.getHuman().getLevel()).towerCorrect;
		// c:难度参数c
		int paramC = ParamManager.towerParam1;
		// B:最高战力
		int highestCombat = PartnerManager.inst().getHumanHighestCombat(humanObj);
		// d:难度参数d
		int paramD = ParamManager.towerParam2;
		return (towerCorrect * paramC + highestCombat * paramD) / 2;
	}

	private int getCostOpenCardMoney(int layer, int diff, boolean isCost) {
		if (!isCost) {
			return 0;
		}
		ConfTower conf = ConfTower.get(layer);
		if (conf == null) {
			return 0;
		}
		// 付费宝箱元宝消耗数组
		int[] cost = conf.Cost;
		if (diff < ETowerDifficulty.TowerDiffLv1_VALUE || cost.length < diff - 1) {
			return 0;
		}
		return cost[diff - 1];
	}

	///////////////////////////////////////////////////////////
	/**
	 * 根据战力获取相应的战力偏差表
	 */
	public ConfTowerMatch getConfTowerMatch(int combat) {
		Collection<ConfTowerMatch> confCol = ConfTowerMatch.findAll();
		ConfTowerMatch confTM = null;
		for (ConfTowerMatch conf : confCol) {
			if (combat >= conf.ability) {
				confTM = conf;
			}
		}
		if (confTM == null) {
			List<ConfTowerMatch> list = new ArrayList<>(confCol);
			int randIndex = Utils.randomBetween(0, list.size() - 1);
			confTM = list.get(randIndex);
		}
		return confTM;
	}

	/**
	 * 根据层数，难度，玩家等级，获取爬塔对应的胜利奖励RewardSn
	 * 
	 * @return rewardSn
	 */
	public int getRewardSn(ConfTower confTower, int selDiff, int humanLv) {
		int rewardSn = 0;
		if (confTower == null) {
			return rewardSn;
		}
		// 根据等级获取所在的index
		int index = 0;
		int[] level = confTower.level;
		for (int i = 0; i < level.length; i++) {
			int lv = level[i];
			if (humanLv > lv) {
				index = i;
			} else {
				break;
			}
		}
		// 根据难度和index取的对应的奖励
		switch (selDiff) {
		case ETowerDifficulty.TowerDiffLv1_VALUE:
			rewardSn = confTower.reward1[index];
			break;
		case ETowerDifficulty.TowerDiffLv2_VALUE:
			rewardSn = confTower.reward2[index];
			break;
		case ETowerDifficulty.TowerDiffLv3_VALUE:
			rewardSn = confTower.reward3[index];
			break;
		}
		return rewardSn;
	}

	/**
	 * 获得奖励宝箱的dropSn和元宝翻牌宝箱dropSn
	 * 
	 * @param humanLv
	 * @param confTower
	 *            配置表
	 * @param diff
	 *            挑战的难度
	 * @return 返回数组，index0:普通dropSn，index1：元宝翻牌dropSn
	 */
	public int[] getRewardBoxDropSn(int humanLv, ConfTower confTower, int diff) {
		int[] chest = null;
		int[] costChest = null;
		switch (diff) {
		case ETowerDifficulty.TowerDiffLv1_VALUE:
			chest = confTower.Chest1;
			costChest = confTower.CostChest1;
			break;
		case ETowerDifficulty.TowerDiffLv2_VALUE:
			chest = confTower.Chest2;
			costChest = confTower.CostChest2;
			break;
		case ETowerDifficulty.TowerDiffLv3_VALUE:
			chest = confTower.Chest3;
			costChest = confTower.CostChest3;
			break;
		}
		// 这一层没有奖励
		if (chest == null || costChest == null || chest[0] == 0 || costChest[0] == 0) {
			return null;
		}
		int[] chestLv = confTower.Chestlevel;
		int index = 0;
		// 玩家在哪个等级阶段
		for (int i = chestLv.length - 1; i >= chestLv.length; i--) {
			if (humanLv >= chestLv[i]) {
				index = i;
				break;
			}
		}
		// 奖励数组异常
		if (chest.length - 1 < index || costChest.length - 1 < index) {
			Log.table.error("=== 爬塔奖励宝箱数组异常 ===");
			return null;
		}
		int chestDropId = chest[index];
		int costDropId = costChest[index];
		return new int[] { chestDropId, costDropId };
	}

	/**
	 * 创建排行榜需求数据
	 */
	public RankData createRankData(HumanObject humanObj, Tower tower) {
		Human human = humanObj.getHuman();
		RankData data = new RankData();
		data.humanId = human.getId();
		data.name = human.getName();
		data.level = human.getLevel();
		data.modelSn = human.getDefaultModelSn();
		// 最后的难度
		int lastDiff = tower.getLastSelDifficulty();
		// 最后通过的层数
		int maxLayer = tower.getWillFightLayer() - 1;
		data.maxFloor = maxLayer;
		data.difficultly = lastDiff;
		data.costTime = (int) ((tower.getLastPassTime() - tower.getFirstFightTime()) / Utils.I1000);
		data.towerScore = tower.getScore();

		// 设置extInfo信息
		humanObj.getHumanExtInfo().setTowerPassTime(data.costTime);
		humanObj.getHumanExtInfo().setTowerMaxFloor(maxLayer);
		humanObj.getHumanExtInfo().setTowerSelDiff(lastDiff);
		humanObj.getHumanExtInfo().setTowerScore(data.towerScore);

		return data;
	}

	/**
	 * 是否通过该难度层数
	 * 
	 * @param humanObj
	 * @param recordLayer
	 *            通过的层数记录值（难度*1000+层数
	 * @return true 通过
	 */
	public boolean getAlreadyFight(HumanObject humanObj, int recordLayer) {
		boolean isPass = humanObj.towerRecord.getAlreadyFight(recordLayer);
		return isPass;
	}

	/**
	 * 组合爬塔和难度的数值 eg: 简单难度第4层=1004；普通难度第5层=2005；困难难度第6层=3006
	 */
	public int getDiffLayerValue(int layer, int diff) {
		if (layer == 0) {
			diff = 0;
		}
		return diff * Utils.I1000 + layer;
	}

	/**
	 * 重启赛季
	 * 
	 * @param humanObj
	 */
	private void restartSeason(HumanObject humanObj) {
		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.getSeasonEndTime();
		proxy.listenResult(this::_result_restartSeason, "humanObj", humanObj);
	}

	private void _result_restartSeason(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		// 爬塔赛季结束时间
		long seasonEndTime = Utils.getParamValue(results, "seasonEndTime", 0l);

		this.proess_newSeason(humanObj, seasonEndTime);
	}

	/**
	 * 设置新赛季信息 在线玩家通过 _listener_ResetDailyHour调用 离线玩家，登录时打开爬塔界面时调用
	 */
	private void proess_newSeason(HumanObject humanObj, long seasonEndTime) {
		// 是否解锁爬塔
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeTower_VALUE, humanObj)) {
			return;
		}
		if (humanObj.towerRecord == null) {
			return;
		}
		Tower tower = humanObj.towerRecord.getTower();

		List<DProduce> produceList = null;
		// 不是刚解锁
		if (tower.getSeasonEndTime() != 0) {
			// 获取赛季奖励
			produceList = this.getSeasonAward(humanObj, tower.getScore());
		}

		// 设置新赛季结束
		tower.setScore(0);
		tower.setRank(ParamManager.rankTopRecordMaxNum[RankIntType.Tower.value()]);
		tower.setSeasonEndTime(seasonEndTime);

		// 发放赛季奖励
		SCTowerSeasonInfo.Builder msg = SCTowerSeasonInfo.newBuilder();
		msg.setScore(tower.getScore());
		msg.setSeasonEndTime(tower.getSeasonEndTime());
		if (produceList != null) {
			msg.addAllProduceList(produceList);
		}
		msg.setTowerRank(tower.getRank());
		humanObj.sendMsg(msg);
	}

	/**
	 * 根据玩家等级和积分，获得结算奖励
	 * 
	 * @param score
	 * @return
	 */
	private List<DProduce> getSeasonAward(HumanObject humanObj, int score) {
		int min = 0;
		int max = 0;
		int rewardSn = 0;
		for (ConfTowerScoreAward confTSA : ConfTowerScoreAward.findAll()) {
			min = confTSA.score[0];
			max = confTSA.score[1];
			if (score >= min && score < max) {
				rewardSn = confTSA.rewardSn;
				break;
			}
		}
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		if (confRewards == null) {
			Log.table.error("===配置表错误ConfRewards no find sn={}", rewardSn);
			return null;
		}
		// 奖励
		ItemChange rewards = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum,
				LogSysModType.TowerPassReward);
		return rewards.getProduce();
	}

	/**
	 * 获取赛季轮回起始结束时间
	 */
	public long getSeasonEndTime() {
		// 开服时，如果不在结束当天，则直接取开服的那周
		long endTime = Utils.getTimeOfWeek(Port.getTime(), 7, 5);
		// 正常情况下要偏移到下一周
		if (Utils.getDayOfWeek(Port.getTime()) == ParamManager.towerSeasonEndDay
				&& Utils.getHourOfDay() >= ParamManager.towerSeasonEndTime) {
			// 如果是赛季轮回天数当天，且时间进入下一个赛季的情况下，结束时间修改为下赛季结束时间
			endTime = Utils.getOffDayTime(endTime, 7, 5);
		}
		return endTime;
	}

	/**
	 * 获取玩家的爬塔积分
	 */
	public int getTowerScore(HumanObject humanObj) {
		return humanObj.towerRecord.getTower().getScore();
	}

	/**
	 * 获得爬塔积分道具
	 * 
	 * @param humanObj
	 * @param num
	 */
	public void process_ItemToTowerScore(HumanObject humanObj, int num) {
		TowerRecord towerRecord = humanObj.towerRecord;
		if (towerRecord == null) {
			return;
		}
		Tower tower = towerRecord.getTower();
		if (tower == null) {
			return;
		}
		tower.setScore(tower.getScore() + num);
	}

	/**
	 * 每日重置时，特殊情况的处理
	 * 
	 * @param humanObj
	 */
	private void spcialDaily(HumanObject humanObj) {
		if (humanObj.stageObj != null) {
			if (humanObj.stageObj instanceof StageObjectTower) {
				// 设置爬塔挑战是跨天
				StageObjectTower stageObj = (StageObjectTower) humanObj.stageObj;
				stageObj.setIsSpanDay();
			}
		}
	}

}
