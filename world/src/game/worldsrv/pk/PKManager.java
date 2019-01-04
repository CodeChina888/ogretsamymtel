package game.worldsrv.pk;

import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgPk.SCPKHumanEnd;
import game.msg.MsgPk.SCPKMirrorEnd;
import game.turnbasedsrv.combat.CombatObject;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightParam.CompeteParam;
import game.worldsrv.fightParam.PKHumanParam;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectPKHuman;
import game.worldsrv.stage.types.StageObjectPKMirror;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class PKManager extends ManagerBase {
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static PKManager inst() {
		return inst(PKManager.class);
	}
	
	/**
	 * 切磋镜像玩家
	 * @param humanObj
	 * @param beFightId
	 */
	public void _msg_CSPKMirrorFight(HumanObject humanObj, long beFightId) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(beFightId);
		prx.listenResult(this::_result_getInfo, "humanObj", humanObj, "fightType", ECrossFightType.FIGHT_PK_MIRROR_VALUE);
	}
	private void _result_getInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		HumanGlobalInfo info = results.get();
		if (info == null || info.unit==null) {
			Log.game.error("=== 玩家已经离线，无法查询 ===");
			humanObj.sendSysMsg(120);
			return;
		}
		int fightType = context.get("fightType");
		
		if (fightType == ECrossFightType.FIGHT_PK_MIRROR_VALUE) {
			// 判断副本配置是否合法
			int stageSn = ParamManager.pkMirrorMapSn; 
			ConfInstStage confInstStage = ConfInstStage.get(stageSn);
			if (confInstStage == null) {
				Log.stageCommon.error("===进入发起切磋错误： ConfInstStage.sn={}", stageSn);
				return;
			}
			// 创建切磋镜像玩家副本
			this.createPKMirror(humanObj, confInstStage.sn, confInstStage.mapSN, info);
			// 发送事件：切磋镜像玩家次数改变
			Event.fire(EventKey.PKMirrorFightNum, "humanObj", humanObj);
		} else if (fightType == ECrossFightType.FIGHT_PK_HUMAN_VALUE) {
			// 判断副本配置是否合法
			int stageSn = ParamManager.pkHumanMapSn; 
			ConfInstStage confInstStage = ConfInstStage.get(stageSn);
			if (confInstStage == null) {
				Log.stageCommon.error("===进入发起切磋错误： ConfInstStage.sn={}", stageSn);
				return;
			}
			int mapSn = confInstStage.mapSN;
			// 创建切磋真人玩家副本
			this.createPKHuman(humanObj, stageSn, mapSn, info);
			// 发送事件：切磋真人玩家次数改变
			Event.fire(EventKey.PKHumanFightNum, "humanObj", humanObj);
		}
	}
	
	/**
	 * 创建切磋镜像玩家副本
	 * @param humanObj
	 */
	private void createPKMirror(HumanObject humanObj, int stageSn, int mapSn, HumanGlobalInfo hgInfo) {
		humanObj.setCreateRepTime();//进入异步前要先设置，避免重复操作
		
		CompeteParam competeParam = new CompeteParam(new HumanMirrorObject(hgInfo));
		Param param = new Param(HumanMirrorObject.CompeteParam, competeParam);
		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStagePKMirror(stageSn, mapSn, ECrossFightType.FIGHT_PK_MIRROR_VALUE, param);
		prx.listenResult(this::_result_createPKMirror, "humanObj", humanObj, "mapSn", mapSn);
	}
	private void _result_createPKMirror(Param results, Param context) {
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
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_PK_MIRROR, mapSn, stageId);
	}

	/**
	 * 离开切磋镜像玩家副本
	 * @param humanObj
	 */
	public void _msg_CSPKMirrorLeave(HumanObject humanObj) {
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectPKMirror)) {
			Log.game.error(" ===离开失败，不再切磋中 ===");
			return;
		}
		
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}

	/**
	 * 返回结算切磋镜像玩家副本
	 * @param humanObj
	 */
	public void _msg_CSPKMirrorEnd(HumanObject humanObj, boolean isWin) {
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectPKMirror)) {
			Log.game.error(" ===结算失败，不再切磋中 ===");
			return;
		}
		
		StageObjectPKMirror stageObj = (StageObjectPKMirror) humanObj.stageObj;
		// 胜利次数
		int winCount = humanObj.getHuman().getDailyFightWinNum();
		// 获取单个敌方玩家的战斗对象数据
		HumanMirrorObject objMir = stageObj.getCombatObj(humanObj.getHumanId()).getHumanMirrorObject(humanObj.getHumanId());
		
		SCPKMirrorEnd.Builder msg = SCPKMirrorEnd.newBuilder();
		msg.setIsFail(!isWin);
		msg.setBeFightId(objMir.getHumanId());
		msg.setBeFightName(objMir.getHumanName());
		// 胜利，且在可以获得奖励的次数内
		if (isWin && winCount < ParamManager.pkMirrorRewardCount) {
			
			humanObj.getHuman().setDailyFightWinNum(winCount+1);
			
			int index = winCount;
			if (index > ParamManager.pkMirrorReward.length) {
				index = ParamManager.pkMirrorReward.length - 1;
			}
			int rewardSn = ParamManager.pkMirrorReward[index];
			ConfRewards confReward = ConfRewards.get(rewardSn);
			if (confReward != null) {
				ItemChange itemChange = RewardHelper.reward(humanObj, confReward.itemSn, confReward.itemNum, LogSysModType.PKMirrorWin);
				msg.addAllItems(itemChange.getProduce());
			}
		}
		msg.setWinCount(humanObj.getHuman().getDailyFightWinNum());
		humanObj.sendMsg(msg);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 切磋真人玩家
	 * @param humanObj
	 * @param beFightId
	 */
	public void _msg_CSPKHumanFight(HumanObject humanObj, long beFightId) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(beFightId);
		prx.listenResult(this::_result_getInfo, "humanObj", humanObj, "fightType", ECrossFightType.FIGHT_PK_HUMAN_VALUE);
	}
	
	/**
	 * 创建切磋真人玩家副本
	 * @param humanObj
	 */
	private void createPKHuman(HumanObject humanObj, int stageSn, int mapSn, HumanGlobalInfo hgInfo) {
		humanObj.setCreateRepTime();//进入异步前要先设置，避免重复操作
		
		PKHumanInfo pkHumanInfo1 = new PKHumanInfo(humanObj.getHumanId());
		PKHumanInfo pkHumanInfo2 = new PKHumanInfo(hgInfo.id);
		// 本场PVP的参数信息
		PKHumanParam pkHumanParam = new PKHumanParam(pkHumanInfo1, pkHumanInfo2);
		Param param = new Param(HumanMirrorObject.PKHumanParam, pkHumanParam);
		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStagePKHuman(stageSn, mapSn, ECrossFightType.FIGHT_PK_HUMAN_VALUE, param);
		prx.listenResult(this::_result_createPKHuman, "humanObj", humanObj, "mapSn", mapSn, "fightHumanId", hgInfo.id);
	}
	private void _result_createPKHuman(Param results, Param context) {
		long fightHumanId = Utils.getParamValue(context, "fightHumanId", -1L);
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		int mapSn = Utils.getParamValue(context, "mapSn", -1);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (fightHumanId < 0 || stageId < 0 || mapSn < 0 || null == humanObj) {
			Log.game.error("===创建失败：_result_create stageId={},mapSn={},humanObj={},fightHumanId={}", 
					stageId, mapSn, humanObj, fightHumanId);
			return;
		}
		if (!humanObj.checkHumanSwitchState(results, context, true)) {
			return;
		}
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_PK_HUMAN, mapSn, stageId);
		// 发送给对手
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendFightInfo(fightHumanId, ETeamType.Team2, ECrossFightType.FIGHT_PK_HUMAN, mapSn, stageId);
	}

	/**
	 * 离开切磋真人玩家副本
	 * @param humanObj
	 */
	public void _msg_CSPKHumanLeave(HumanObject humanObj) {
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectPKHuman)) {
			Log.game.error(" ===离开失败，不再切磋中 ===");
			return;
		}
		
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}

	/**
	 * 返回结算切磋真人玩家副本
	 * @param humanObj
	 */
	public void _msg_CSPKHumanEnd(HumanObject humanObj, boolean isWin) {
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectPKHuman)) {
			Log.game.error(" ===结算失败，不再切磋中 ===");
			return;
		}
		
		StageObjectPKHuman stageObj = (StageObjectPKHuman) humanObj.stageObj;
		// 胜利次数
		int winCount = humanObj.getHuman().getDailyFightWinNum();
		CombatObject combatObj = stageObj.getCombatObj(humanObj.getHumanId());
		// 获取单个敌方玩家的战斗对象数据
		HumanMirrorObject objMir = combatObj.getHumanMirrorObject(humanObj.getHumanId());
		
		SCPKHumanEnd.Builder msg = SCPKHumanEnd.newBuilder();
		msg.setIsFail(!isWin);
		msg.setBeFightId(objMir.getHumanId());
		msg.setBeFightName(objMir.getHumanName());
		// 胜利，且在可以获得奖励的次数内
		if (isWin && winCount < ParamManager.pkHumanRewardCount) {
			
			humanObj.getHuman().setDailyFightWinNum(winCount+1);
			msg.setWinCount(winCount+1);
			
			int index = winCount;
			if (index > ParamManager.pkHumanReward.length) {
				index = ParamManager.pkHumanReward.length - 1;
			}
			int rewardSn = ParamManager.pkHumanReward[index];
			ConfRewards confReward = ConfRewards.get(rewardSn);
			if (confReward != null) {
				ItemChange itemChange = RewardHelper.reward(humanObj, confReward.itemSn, confReward.itemNum, LogSysModType.PKHumanWin);
				msg.addAllItems(itemChange.getProduce());
			}
		}
		humanObj.sendMsg(msg);
	}
}
