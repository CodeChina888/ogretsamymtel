package game.worldsrv.compete;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DCompeteFighter;
import game.msg.Define.DCompeteHuman;
import game.msg.Define.DCompeteRankInfo;
import game.msg.Define.DCompeteRecord;
import game.msg.Define.DMoney;
import game.msg.Define.DPartnerLineup;
import game.msg.Define.DProduce;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EMapType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EPartnerLineup;
import game.msg.Define.EStanceType;
import game.msg.Define.ETeamType;
import game.msg.Define.EVipBuyType;
import game.msg.MsgCompete.SCCompeteBuyNumResult;
import game.msg.MsgCompete.SCCompeteFightRecord;
import game.msg.MsgCompete.SCCompeteFightResult;
import game.msg.MsgCompete.SCCompeteLogin;
import game.msg.MsgCompete.SCCompeteOpen;
import game.msg.MsgCompete.SCCompeteRank;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.CharacterObject;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfCompeteRobot;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.CompeteHistory;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.CompetePartner;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightParam.CompeteParam;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.human.HumanPlusManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectCompete;
import game.worldsrv.support.D;
import game.worldsrv.support.GlobalConfVal;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.vip.VipManager;

/**
 * 竞技场
 */
public class CompeteManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static CompeteManager inst() {
		return inst(CompeteManager.class);
	}
	
	/**
	 * 登陆时候:玩家其它数据加载开始：加载玩家的武将信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		
		//登陆下发剩余次数
		SCCompeteLogin.Builder msg = SCCompeteLogin.newBuilder();
		msg.setSurplusNum(humanObj.getHumanExtInfo().getSurplusNum());
		humanObj.sendMsg(msg);
		
		CompeteServiceProxy prx = CompeteServiceProxy.newInstance();
		prx.getHumanRank(humanObj.getHumanId());
		prx.listenResult(this::result_load_other, "humanObj", humanObj);
	}
	private void result_load_other(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Human human = humanObj.getHuman(); 
		
		long humanId  = humanObj.getHumanId();
		int rank = results.getInt("rank");
		
		human.setCompeteRank(rank);
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateCompeteRank(humanId, rank);
	}
	
	@Listener(value = EventKey.HumanLogout)
	public void _listener_HumanLogout(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		_msg_CSCompeteLeave(humanObj);
	}
	
	/**
	 * 打开竞技面板
	 * @param humanObj
	 */
	public void _msg_CSCompeteOpen(HumanObject humanObj) {
		CompeteHumanObj cpObj = new CompeteHumanObj(humanObj);
		CompeteServiceProxy prx = CompeteServiceProxy.newInstance();
		prx.competeOpen(cpObj);// 把humanobj转换成competeObj，同步人物信息到CompeteService.humansMap
		prx.listenResult(this::_result_CSCompeteOpen, "humanObj", humanObj);
	}
	private void _result_CSCompeteOpen(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		// 抹去初始化标识
		CompeteHuman cpHuman = results.get("cpHuman");

		/** 排位赛挑战者结构 */
		SCCompeteOpen.Builder msg = SCCompeteOpen.newBuilder();
		DCompeteHuman dCompeteHuman = getDCompeteHuman(cpHuman, humanObj);
		msg.setDCompeteHuman(dCompeteHuman);
		msg.setCompeteToken(humanObj.getHuman().getCompeteToken());
		humanObj.sendMsg(msg);

		/* 敌人列表 */
		List<CompeteHumanObj> cpHumanObjList = results.get("cpObjlist");// 敌人列表sn
		SCCompeteRank.Builder rankMsg = SCCompeteRank.newBuilder();
		for (CompeteHumanObj cpObj : cpHumanObjList) {
			DCompeteRankInfo.Builder dCompeteRankInfo = DCompeteRankInfo.newBuilder();
			CompeteHuman enermy = cpObj.cpHuman;
			dCompeteRankInfo.setRank(cpObj.cpHuman.getRank());
			dCompeteRankInfo.setName(enermy.getName());
			dCompeteRankInfo.setHumanId(enermy.getId());
			//取出Partner的idlist
			String idStr = enermy.getPartnerLineup();
			List<Long>  pidList = Utils.strToLongList(idStr);
			//战斗力显示处理
			int sum = enermy.getCombat();
			if(pidList!=null)
			for (Long id : pidList) {
				CompetePartner cpa = cpObj.partnerMap.get(id);
				if (cpa == null) {
					continue;
				}
				sum+=cpa.getCombat();
			}
			
			dCompeteRankInfo.setCombat(sum);
			dCompeteRankInfo.setLevel(enermy.getLevel());
			dCompeteRankInfo.setModelSn(enermy.getModelSn());
			rankMsg.addDCompeteRankInfo(dCompeteRankInfo);
		}
		humanObj.sendMsg(rankMsg);
	}

	/**
	 * 获取排位赛挑战者结构
	 * 
	 * @param cpHuman
	 * @return
	 */
	private DCompeteHuman getDCompeteHuman(CompeteHuman cpHuman, HumanObject humanObj) {
		DCompeteHuman.Builder dpInfo = DCompeteHuman.newBuilder();
		dpInfo.setHumanID(cpHuman.getId());
		dpInfo.setHeadSn(humanObj.getHuman().getHeadSn());
		dpInfo.setName(cpHuman.getName());
		dpInfo.setRank(cpHuman.getRank());
		if(cpHuman.getRank() == 0){
			Log.game.info("rank error humanId ={}",humanObj.getHumanId());
			dpInfo.setRank(ParamManager.competeRobotNum);
		}
		
		dpInfo.setTopRank(cpHuman.getRankTop());
		HumanExtInfo extInfo = humanObj.getHumanExtInfo();
		dpInfo.setLastBattleTime(extInfo.getLastCompeteTime());
		//今日已经挑战次数 = 总挑战次数 -
		dpInfo.setChallengeNum(extInfo.getChallengeNum());
		//今日购买的挑战次数
		dpInfo.setBuyChallengeNum(extInfo.getCompeteBuyNumAdd());
		dpInfo.setTodayWinNums(extInfo.getTodayWinNums());
		//剩余挑战次数
		dpInfo.setSurplusNum(extInfo.getSurplusNum());
		// 设置阵型
		DPartnerLineup.Builder dpartnerInfo = DPartnerLineup.newBuilder();
		dpartnerInfo.setTypeLineup(EPartnerLineup.LUInst);
		List<Long> partnerLineUp = Utils.strToLongList(cpHuman.getPartnerLineup());
		dpartnerInfo.addAllIdPartner(partnerLineUp);
		int stance = cpHuman.getPartnerStance();
		dpartnerInfo.setTypeStance(EStanceType.valueOf(stance));
		dpInfo.setArmyList(dpartnerInfo);
		
//		dpInfo.setDrop(cpHuman.isDrop());
		return dpInfo.build();
	}
	
	/**
	 * 挑战玩家
	 */
	public void _msg_CSCompeteFight(HumanObject humanObj, int fightRank, long fightId) {
		// 检测挑战次数
		HumanExtInfo extInfo = humanObj.extInfo;
		if(extInfo.getSurplusNum()-1 < 0){
			//挑战次数耗尽
			humanObj.sendSysMsg(430304);
			return;
		}
		
		// 创建竞技场并切换地图进入竞技场
		int stageSn = ParamManager.competeMapSn;
		int mapSn = getCompeteMapSn();
		ConfMap confMap = ConfMap.get(mapSn);
		if (null == confMap) {
			Log.table.error("ConfMap配表错误，no find sn ={} ", mapSn);
			return;
		}
		
		// 判断玩家是否存在
		CompeteServiceProxy proxy = CompeteServiceProxy.newInstance();
		proxy.challenge(fightId);
		proxy.listenResult(this::_result_challenge, "humanObj", humanObj, "fightId", fightId, 
				"stageSn", stageSn, "mapSn", mapSn);
	}
	
	private void _result_challenge(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_result_challenge humanObj is null");
			return;
		}
		long fightId = Utils.getParamValue(context, "fightId", -1L);
		int stageSn = Utils.getParamValue(context, "stageSn", 0);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		boolean result = Utils.getParamValue(results, "result", false);
		if (!result) {
			// 找不到此玩家
			Log.logGameTest("_result_challenge竞技场找不到玩家id={}", fightId);
			return;
		}
		CompeteHumanObj fightCompeteHumanObj = Utils.getParamValue(results, "fightCompeteHumanObj", null);
		if(null == fightCompeteHumanObj){
			// 挑战对象为空
			Log.logGameTest("_result_challenge竞技场挑战返回对象为空id={}", fightId);
			return;
		}
		
		// 创建竞技场副本
		create(humanObj, fightCompeteHumanObj, stageSn, mapSn);
	}

	/**
	 * 创建竞技场
	 */
	private void create(HumanObject humanObj, CompeteHumanObj fightCompeteHumanObj, int stageSn, int mapSn) {
		//进入异步前要先设置，避免重复操作
		if(humanObj.isInCreateRepTime()){
			Log.game.info("正在创建竞技场中");
			return;
		}
		humanObj.setCreateRepTime();
		
		CompeteParam competeParam = new CompeteParam(new HumanMirrorObject(fightCompeteHumanObj));
		Param param = new Param(HumanMirrorObject.CompeteParam, competeParam);
		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageCompete(stageSn, mapSn, ECrossFightType.FIGHT_COMPETE_VALUE, param);
		prx.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
		//发布七日事件
		int num = humanObj.cultureTimes.getCompeteChallenge() +1 ;
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num , "type", ActivitySevenTypeKey.Type_54);
	}
	private void _result_create(Param results, Param context) {
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		int mapSn = Utils.getParamValue(context, "mapSn", -1);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (stageId < 0 || mapSn < 0 || null == humanObj) {
			Log.game.error("===创建失败：_result_create stageId={}, mapSn={}, humanObj={}", 
					stageId, mapSn, humanObj);
			return;
		}
		if (!humanObj.checkHumanSwitchState(results, context, true)) {
			return;
		}
		
		Human human = humanObj.getHuman();
		// 当前挑战次数
		int challengeNum = humanObj.extInfo.getChallengeNum();
		challengeNum++;
		humanObj.extInfo.setChallengeNum(challengeNum);
		
		int surplusNum = humanObj.extInfo.getSurplusNum();
		humanObj.extInfo.setSurplusNum(surplusNum-1);//剩余挑战次数-1
		// 添加次数
		human.setDailyCompeteFightNum(human.getDailyCompeteFightNum() + 1);// 已挑战次数+1
		// 胜利或失败都会增加积分
		human.setDailyCompeteIntegral(human.getDailyCompeteIntegral() + ParamManager.competeIntegral);
		Event.fire(EventKey.CompeteStart, "humanObj", humanObj);
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_COMPETE, mapSn, stageId);
	}
	
	/**
	 * 获取竞技场地图
	 * 
	 * @return
	 */
	public int getCompeteMapSn() {
		int mapSn = 0;
		int stageSn = ParamManager.competeMapSn;
		ConfInstStage conf = ConfInstStage.get(stageSn);
		if (null == conf) {
			Log.table.error("===ConfInstStage配表错误, no find sn={}", stageSn);
		} else {
			mapSn = conf.mapSN;
		}
		return mapSn;
	}
	
	/**
	 * 离开副本 自动回到副本进入前的主地图
	 * 
	 * @param humanObj
	 */
	public void _msg_CSCompeteLeave(HumanObject humanObj) {
		StageObjectCompete stageObj = null;
		if (humanObj.stageObj instanceof StageObjectCompete) {
			stageObj = (StageObjectCompete) humanObj.stageObj;
			if (stageObj == null)
				return;
		} else {
			return;
		}
		
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}
	
	/**
	 * 竞技场结算
	 */
	public void _msg_CSCompeteEnd(HumanObject humanObj, long idDef, boolean isWin, long recordId) {
		StageObjectCompete stageObj = null;
		if (humanObj.stageObj instanceof StageObjectCompete) {
			stageObj = (StageObjectCompete) (humanObj.stageObj);
			if (stageObj == null)
				return;
		} else {
			return;// 不在副本里
		}
		
		long idAtk = humanObj.getHumanId();
		CompeteServiceProxy proxy = CompeteServiceProxy.newInstance();
		proxy.swapRank(idAtk, idDef, isWin, recordId);
		proxy.listenResult(this::result_endCompete, "humanObj", humanObj, "isWin", isWin, "recordId", recordId);
	}
	private void result_endCompete(Param result, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		boolean isWin = Utils.getParamValue(context, "isWin", false);
		long recordId = Utils.getParamValue(context, "recordId", 0L);
		
		CompeteHuman cpHumanAtk = Utils.getParamValue(result, "cpHumanAtk", null);
		CompeteHuman cpHumanDef = Utils.getParamValue(result, "cpHumanDef", null);
		boolean isBreakTopRank = Utils.getParamValue(result, "isBreakTopRank", false);
		int rankOldAtk = Utils.getParamValue(result, "rankOldAtk", 0);
		int rankNewAtk = Utils.getParamValue(result, "rankNewAtk", 0);
		
		// 发放固定结算奖励
		Collection<ConfCompeteRobot> conf = ConfCompeteRobot.findAll();
		int [] itemSn = {};
		int [] itemNum ={};
		for (ConfCompeteRobot c : conf) {
			int min = c.rankMin;
			int max =  c.rankMax;
			int rank = cpHumanAtk.getRank();
			if(min<= rank && max >= rank){
				ConfRewards confR = ConfRewards.get(c.rankingReward);
				itemSn =confR.itemSn;
				itemNum = confR.itemNum;
				RewardHelper.reward(humanObj, confR.itemSn, confR.itemNum,LogSysModType.CompeteFight);//战斗结算奖励
				break;
			}
		}
		
		SCCompeteFightResult.Builder msg = SCCompeteFightResult.newBuilder();
		msg.setIsFail(!isWin);
		msg.setRank(cpHumanAtk.getRank());
		msg.setTopRank(cpHumanAtk.getRankTop());
		msg.setBeChallengerName(cpHumanDef.getName());
		msg.setTodayWinNums(humanObj.getHumanExtInfo().getTodayWinNums());
		// 固定结算奖励
		for (int i = 0; i < itemSn.length; i++) {
			DProduce.Builder d = DProduce.newBuilder();
			d.setSn(itemSn[i]);
			d.setNum(itemNum[i]);
			msg.addReward(d);
		}
		// 突破历史最高奖励
		if (isBreakTopRank) {
			DProduce dp = breakTopReward(humanObj, rankOldAtk, rankNewAtk);
			msg.addBreakReward(dp);
		}
		//msg.setRecordId(recordId);// 回放记录Id
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 突破历史最高奖励
	 * 
	 * @param rankOld
	 * @param rankNew
	 * @return
	 */
	private DProduce breakTopReward(HumanObject humanObj, int rankOld, int rankNew) {
		if (rankOld > ParamManager.competeRankMax) {
			Log.game.info("=== 初始排名超过"+ ParamManager.competeRankMax + " Rank = " + rankOld);
			rankOld = ParamManager.competeRankMax;
		}
		// 突破奖励————黄彬	
		Map<Integer, ConfCompeteRobot> map = GlobalConfVal.getBreakRankRewardMap();
		int sumNum  = 0;
		ConfCompeteRobot conf = null;
		for (Entry<Integer, ConfCompeteRobot> entry : map.entrySet()) {
			conf = entry.getValue();
			if(conf == null){
				continue;
			}
			int rangeMax = conf.rankMax;
			int rangeMin = conf.rankMin;
			// 新排名比当前区间最大排名大，旧排名比区间最小排名小
			if (rankNew > rangeMax || rankOld < rangeMin) {
				continue;
			}
			// 新旧排名在同一区间内
			if(rankNew >= rangeMin && rankNew <= rangeMax && rankOld >= rangeMin && rankOld <= rangeMax){
				sumNum += conf.addMoney * (rankOld - rankNew);
				break;
			}
			// 新排名比当前区间的最小排名小，旧排名比区间最大排名大 old:1500->new:200  区间：1000~501
			else if (rankNew <= rangeMin && rankOld > rangeMax) { 
				sumNum += conf.addMoney * (rangeMax - rangeMin + 1);
			} 
			// 新排名比当前区间的最小排名小，旧排名比区间最大排名小  old:1500->new:200  区间：2000~1001
			else if (rankNew <= rangeMin && rankOld <= rangeMax) {
				sumNum += conf.addMoney * (rankOld - rangeMin);
			}
			// 新排名比当前区间的最小排名大，旧排名比区间最大排名大 old:1500->new:200 区间：200~101
			else if (rankNew > rangeMin && rankOld > rangeMax) {
				sumNum += conf.addMoney * (rangeMax - rankNew + 1);
			}
		}	
		
		sumNum = (int)(sumNum/10);
		RewardHelper.reward(humanObj, DMoney.GOLD_FIELD_NUMBER,sumNum, LogSysModType.CompeteHistoryTop);
		Event.fire(EventKey.CompeteRankHighest, "humanObj", humanObj, "rank", rankNew);
		DProduce.Builder msg = DProduce.newBuilder();
		msg.setSn(DMoney.GOLD_FIELD_NUMBER);
		msg.setIsItem(false);
		msg.setNum(sumNum);
		return msg.build();
	}
	
	/**
	 * 请求竞技场战报
	 * @param humanObj
	 */
	public void _msg_CSCompeteFightRecord(HumanObject humanObj) {
		// 查询战斗记录
		String whereSql = "WHERE `humanIdFight` = ? or `humanIdBeFight` = ? ORDER BY `createdAt` desc limit 10";

		DB dbPrx = DB.newInstance(CompeteHistory.tableName);
		dbPrx.findByQuery(false, whereSql, humanObj.id, humanObj.id);
		dbPrx.listenResult(this::_result_CSCompeteFightRecord, "humanObj", humanObj);
	}
	private void _result_CSCompeteFightRecord(Param results, Param context) {
		List<Record> historys = results.get();
		if (null == historys) {
			Log.game.error("===_result_CSGetRankRecord List<Record>=null");
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_result_CSGetRankRecord humanObj is null");
			return;
		}

		SCCompeteFightRecord.Builder msg = SCCompeteFightRecord.newBuilder();

		// 构建最近战斗记录
		for (Record r : historys) {
			CompeteHistory history = new CompeteHistory(r);
			DCompeteRecord.Builder dBuilder = DCompeteRecord.newBuilder();
			// 挑战者
			DCompeteFighter.Builder drankFigh = DCompeteFighter.newBuilder();
			drankFigh.setHumanID(history.getHumanIdFight());
			drankFigh.setHumanName(history.getHumanNameFight());
			drankFigh.setRank(history.getRank());
			drankFigh.setModelID(history.getFightHeadSn());
			dBuilder.setChallenger(drankFigh);
			// 被挑战者
			DCompeteFighter.Builder drankBeFigh = DCompeteFighter.newBuilder();
			drankBeFigh.setHumanID(history.getHumanIdBeFight());
			drankBeFigh.setHumanName(history.getHumanNameBeFight());
			drankBeFigh.setRank(history.getBeRank());
			drankBeFigh.setModelID(history.getBeFightHeadSn());
			dBuilder.setWinFlag(history.isWin());
			dBuilder.setBattleEndTime(history.getCreatedAt());
			dBuilder.setBeChallenger(drankBeFigh);
			msg.addDCompeteRecord(dBuilder);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 花钱增加进入次数
	 * 
	 * @param humanObj
	 */
	public void CSCompeteBuyNum(HumanObject humanObj) {
		boolean can = canBuyNum(humanObj);
		if (!can) {
			SCCompeteBuyNumResult.Builder msg = SCCompeteBuyNumResult.newBuilder();
			msg.setResultCode(-1);
			humanObj.sendMsg(msg);
			return;
		}
		
		// 剩余购买次数够不够
		Human human = humanObj.getHuman();
		int numBuy = human.getDailyCompeteFightBuyNum();// 已购买次数
		int numVip = HumanManager.inst().getBuyNumVip(humanObj);// 可购买次数
		if (numBuy >= numVip) {
			humanObj.sendSysMsg(430801);// 可购买挑战次数不足！请提升VIP等级
			return ;
		}
		//本次是第几次购买
		int buys = numBuy+1;
		// 钱够不够
		int needGold = RewardHelper.getCostGold(ECostGoldType.competeFightCost, buys);// 获取购买次数需花费的钻石
		// 扣钱
		HumanExtInfo exInfo = humanObj.getHumanExtInfo();
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, needGold, LogSysModType.CompeteBuyNum)) {
			return;
		}

		// 增加次数
		exInfo.setCompeteBuyNumAdd(exInfo.getCompeteBuyNumAdd() + 1);
		exInfo.setSurplusNum(exInfo.getSurplusNum()+1);
		human.setDailyCompeteFightBuyNum(numBuy + 1);
		
		// 返回前端
		SCCompeteBuyNumResult.Builder msg = SCCompeteBuyNumResult.newBuilder();
		msg.setNum(exInfo.getSurplusNum());// 剩余可挑战次数
		msg.setNeedGold(needGold);
		msg.setResultCode(0);

		humanObj.sendMsg(msg);
	}
	
	
	
	/**
	 * 是否可以购买竞技场挑战次数
	 * 
	 * @param humanObj
	 * @return
	 */
	private boolean canBuyNum(HumanObject humanObj) {
		// 次数不够
		HumanExtInfo extInfo = humanObj.getHumanExtInfo();
		int numBuy = extInfo.getCompeteBuyNumAdd();// 已购买次数
		int numVip = VipManager.inst().getConfVipBuyTimes(EVipBuyType.competeFightNum,
				humanObj.getHuman().getVipLevel());
		if (numBuy >= numVip) {
			humanObj.sendSysMsg(430801);// 可购买挑战次数不足！请提升VIP等级
			return false;
		}
		return true;
	}
	
	public Map<Long, CompetePartner> getBotArmyById(CompeteHuman cpHuman,int armyId){
		Map<Long, CompetePartner> tmpMap = new HashMap<>();
		//构建军团数据
		ConfInstMonster confInst = ConfInstMonster.get(armyId);
		if (null == confInst) {
			return tmpMap;
		}
		
		long humanId = cpHuman.getId();
		for(int partnerSn : confInst.monsterIds){
			ConfPartnerProperty confPartner = ConfPartnerProperty.get(partnerSn);
			if(confPartner == null){
				//阵型
				String lineupStr = cpHuman.getPartnerLineup();
				List<Long> pidlist = Utils.strToLongList(lineupStr);
				pidlist.add(0L);
				cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
				continue;
			}
			long partnerId = Port.applyId();
			if(confPartner.roleType == 0){
				//主角
				HumanPlusManager.inst().setMirrorHumanProp(cpHuman, partnerSn);
				cpHuman.setPartnerStance(confInst.lineup);
				
				String lineupStr = cpHuman.getPartnerLineup();
				List<Long> pidlist = Utils.strToLongList(lineupStr);
				pidlist.add(-1L);
				cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
				continue;
			}
			//阵型
			String lineupStr = cpHuman.getPartnerLineup();
			List<Long> pidlist = Utils.strToLongList(lineupStr);
			pidlist.add(partnerId);
			cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
			
			CompetePartner cpartner = new CompetePartner();
			HumanPlusManager.inst().setMirrorPartner(cpartner, humanId, partnerId, partnerSn);
			cpartner.persist();
			tmpMap.put(partnerId, cpartner);
		}
		return tmpMap;
	}
	
	/**
	 * 判断武将是否为空
	 * 
	 * @param mapGen
	 * @return
	 */
	public boolean isMapGenUp(Map<Integer, CharacterObject> mapGen) {
		for (CharacterObject obj : mapGen.values()) {
			if (null != obj) {// 只要有一个存在，不为空
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否进入挑战竞技场
	 * 
	 * @param humanObj
	 * @return
	 */
	public boolean canChallenge(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 功能是否解锁
		// if (!ModunlockManager.inst().isUnlock(ModType.Compete, humanObj)) {
		// humanObj.sendSysMsg(430303);// 玩家等级不足，竞技场未开放！
		// return new ReasonResult(false);
		// }
		HumanExtInfo exinfo = humanObj.getHumanExtInfo();
		// 判断挑战次数
		int challengeNum = exinfo.getCompeteBuyNumAdd() + human.getDailyCompeteFightNum();
		if (challengeNum >= getFightNumMax(humanObj)) {
			humanObj.sendSysMsg(430304);// 剩余挑战次数不足，请先购买！
			return false;
		}

		// 判断挑战冷却时间
		if (getTimeCool(human) > 0) {
			humanObj.sendSysMsg(430305);// 挑战冷却时间未来，不可挑战！
			return false;
		}

		// 非普通场景不能进入(只有主城才能进入竞技场)
		ConfMap confMap = ConfMap.get(humanObj.stageObj.mapSn);
		if (null == confMap) {
			Log.table.error("ConfMap配表错误，no find sn ={} ", humanObj.stageObj.mapSn);
			return false;
		}
		if (!confMap.type.equals(EMapType.common.name())) {
			humanObj.sendSysMsg(430306);// 当前场景地图不可进入挑战！请返回主城地图！
			return false;
		}

		return true;
	}
	
	/**
	 * 竞技场最大可挑战次数
	 * 
	 * @param humanObj
	 * @return
	 */
	private int getFightNumMax(HumanObject humanObj) {
		//vip可挑战次数
//		int numVip = VipManager.inst().getConfVipBuyTimes(EVipBuyType.competeFightNum,humanObj.getHuman().getVipLevel());
		//免费挑战次数
		int freeNum = ParamManager.competeFreeNum;
		//已经购买的挑战次数
		int buyNum = humanObj.getHumanExtInfo().getCompeteBuyNumAdd();
		return freeNum+buyNum;
	}
	
	/**
	 * 获取冷却时间
	 * 
	 * @param human
	 * @return
	 */
	private int getTimeCool(Human human) {
		return 0;
	}
	
	/**
	 * 改名
	 */
	public void rename(HumanObject humanObj) {
		CompeteServiceProxy prx = CompeteServiceProxy.newInstance();
		prx.rename(humanObj.id, humanObj.getHuman().getName());
	}
	
}
