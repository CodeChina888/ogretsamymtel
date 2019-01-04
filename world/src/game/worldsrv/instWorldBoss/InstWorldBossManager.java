package game.worldsrv.instWorldBoss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;
import game.msg.Define.DHumanInfo;
import game.msg.Define.DPVEHarm;
import game.msg.Define.DWBKillInfo;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EInformType;
import game.msg.Define.EMailType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.MsgWorldBoss.SCWorldBossEnd;
import game.msg.MsgWorldBoss.SCWorldBossEnterFight;
import game.msg.MsgWorldBoss.SCWorldBossFightInfo;
import game.msg.MsgWorldBoss.SCWorldBossHarm;
import game.msg.MsgWorldBoss.SCWorldBossInfo;
import game.msg.MsgWorldBoss.SCWorldBossInspireCDClean;
import game.msg.MsgWorldBoss.SCWorldBossInstSn;
import game.msg.MsgWorldBoss.SCWorldBossOtherHuman;
import game.msg.MsgWorldBoss.SCWorldBossRank;
import game.msg.MsgWorldBoss.SCWorldBossRankFinal;
import game.msg.MsgWorldBoss.SCWorldBossReborn;
import game.msg.MsgWorldBoss.SCWorldBossRevive;
import game.msg.MsgWorldBoss.SCWorldBossUponTop;
import game.turnbasedsrv.combat.types.CombatObjectWorldBoss;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.config.ConfWorldBoss;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.WorldBoss;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightParam.WorldBossParam;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectInstWorldBoss;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.MathUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;

/**
 * 单人活动副本
 * @author Administrator
 *
 */
public class InstWorldBossManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static InstWorldBossManager inst() {
		return inst(InstWorldBossManager.class);
	}
	
	/**
	 * 打开世界boss界面，获得上次伤害排行前三和击杀者昵称
	 * @param actInstSn 活动副本sn
	 */
	public void _msg_CSWorldBossUponTop(HumanObject humanObj, int actInstSn) {
		WorldBossServiceProxy proxy = WorldBossServiceProxy.newInstance();
		proxy.getUponRecordNames(actInstSn);
		proxy.listenResult(this::_result_msg_CSWorldBossUponTop, "humanObj", humanObj);
	}
	private void _result_msg_CSWorldBossUponTop(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_isOpen humanObj is null");
			return;
		}
		String uponTop3Names = Utils.getParamValue(results, "uponTop3Names", "");
		String killerName = Utils.getParamValue(results, "killerName", "");
		List<String> names = Utils.strToStringList(uponTop3Names);
		SCWorldBossUponTop.Builder msg = SCWorldBossUponTop.newBuilder();
		msg.addAllHarmTop3Names(names);
		msg.setKillBossName(killerName);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 玩家完成加载登录到地图中时进行操作
	 * @param params
	 */
	@Listener(EventKey.StageHumanEnter)
	public void _listener_StageHumanEnter(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_StageHumanEnter humanObj is null");
			return;
		}
		if (humanObj.stageObj instanceof StageObjectInstWorldBoss) {
			StageObjectInstWorldBoss stage = (StageObjectInstWorldBoss)humanObj.stageObj;
			// 发送BOSS相关信息
			sendBossInfo(humanObj, stage.confInstActConfig.sn);
		}
	}
	/**
	 * 发送BOSS相关信息
	 */
	private void sendBossInfo(HumanObject humanObj, int actInstSn) {
		// 返回世界BOSS挑战信息
		sendSCWorldBossFightInfo(humanObj, actInstSn);
		// 返回世界BOSS信息
		_msg_CSWorldBossInfo(humanObj, actInstSn);
	}
	/**
	 * 返回世界BOSS挑战信息
	 */
	private void sendSCWorldBossFightInfo(HumanObject humanObj, int actInstSn) {
		SCWorldBossFightInfo.Builder msg = SCWorldBossFightInfo.newBuilder();
		msg.setActInstSn(actInstSn);
		msg.setTmNextFight(humanObj.extInfo.getWDBossNextFightTime());
		msg.setTmNextInspire(humanObj.extInfo.getWDBossNextInspireTime());
		msg.setNumInspire(humanObj.extInfo.getWDBossInspireNum());
		msg.setMaxInspire(ParamManager.worldBossInspireMaxNum);
		msg.setNumReborn(humanObj.extInfo.getWDBossRebornNum());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 进入世界BOSS地图
	 * @param humanObj
	 * @param actInstSn 活动副本sn
	 */
	public void _msg_CSWorldBossEnter(HumanObject humanObj, int actInstSn) {
		// 判断是否可以加入活动副本
		if (TeamManager.inst().canJoinActInst(humanObj, actInstSn)) {
			WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
			prx.isOpen();
			prx.listenResult(this::_result_isOpen, "humanObj", humanObj);
		} else {
			humanObj.sendSysMsg(520403);// 活动副本不在开启时间内
		}
	}
	private void _result_isOpen(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_isOpen humanObj is null");
			return;
		}
		// 获取返回值
		boolean isOpen = Utils.getParamValue(results, "isOpen", false);
		WBData wbData = Utils.getParamValue(results, "wbData", null);
		if (null == wbData) {
			Log.game.error("===_result_isOpen wbData is null");
			return;
		}
		
		// 是否开启中
		if (!isOpen) {
			humanObj.sendSysMsg(520403);// 活动副本不在开启时间内
			return;
		}
		
		ConfInstActConfig conf = ConfInstActConfig.get(wbData.actInstSn);
		if (conf != null) {
			// 判断是否首次参加，还是掉线后再上线的
			long tmNow = Port.getTime();
			int hourOpen = Utils.getHourOfDay();
			int minuteOpen = 0;
			for (int i = 0; i < conf.openHour.length; i++) {
				if (hourOpen == conf.openHour[i]) {
					minuteOpen = conf.openMinute[i];
					break;
				}
			}
			long tmOpen = Utils.getTimestampTodayAssign(hourOpen, minuteOpen, 0);
			if (humanObj.extInfo.getWDBossJoinTime() < tmOpen) {
				// 首次参加本次活动
				humanObj.extInfo.setWDBossJoinTime(tmNow);
				humanObj.extInfo.setWDBossInspireNum(0);
				humanObj.extInfo.setWDBossRebornNum(0);
				
				
				int num = humanObj.cultureTimes.getJoinWorldBoss() +1 ;
				Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num , "type",
						ActivitySevenTypeKey.Type_49);
				
				// 派发本次活动首次进入世界boss事件
				Event.fire(EventKey.ActInstWorldBossEnter, "humanObj", humanObj, "stageSn", wbData.bossInstSn);
				
			}
			// 用于掉线后再上线打世界BOSS，继续CD时间
			if (humanObj.extInfo.getWDBossNextFightTime() > tmNow) {
				// 开启复活CD
				long tmCD = humanObj.extInfo.getWDBossNextFightTime() - tmNow;
				humanObj.ttWorldBossReviveCD.start(tmCD);
			}
			if (humanObj.extInfo.getWDBossNextInspireTime() > tmNow) {
				// 开启鼓舞CD
				long tmCD = humanObj.extInfo.getWDBossNextInspireTime() - tmNow;
				humanObj.ttWorldBossInspireCD.start(tmCD);
			}
			
			// 获取地图sn
			ConfWorldBoss confWorldBoss = ConfWorldBoss.get(wbData.bossInstSn);
			if (null == confWorldBoss) {
				Log.stageCommon.info("===进入世界boss副本错误：humanId={}, ConfWorldBoss.sn={}", 
						humanObj.id, wbData.bossInstSn);
				// 发送文字提示消息 切换地图错误！
				humanObj.sendSysMsg(36);
				return;
			}
			
			// 创建副本
			create(humanObj, wbData.bossInstSn, wbData.bossMapSn);
		}
	}
	
	/**
	 * 创建副本
	 * @param humanObj
	 * @param stageSn
	 * @param mapSn
	 */
	private void create(HumanObject humanObj, int stageSn, int mapSn) {
		humanObj.setCreateRepTime();//进入异步前要先设置，避免重复操作
		
		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstBoss(stageSn, mapSn, ECrossFightType.FIGHT_WORLD_BOSS_VALUE);
		prx.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
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
		
		// 出生点及朝向
		Vector2D pos = StageManager.inst().getHumanPos(mapSn);
		Vector2D dir = MathUtils.getDir(pos, StageManager.inst().getHumanDir(mapSn));
		StageManager.inst().switchTo(humanObj, stageId, pos, dir);
	}
	
	/**
	 * 离开世界Boss地图，自动回到副本进入前的主地图
	 * @param humanObj
	 */
	public void _msg_CSWorldBossLeave(HumanObject humanObj) {
		if (humanObj.getTeamId() > 0) {// 离开单人副本则清除组队ID
			TeamManager.inst()._msg_CSTeamLeave(humanObj);
		}
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}
	
	/**
	 * 进入世界BOSS战斗
	 * @param humanObj
	 * @param actInstSn 活动副本sn
	 */
	public void _msg_CSWorldBossEnterFight(HumanObject humanObj, int actInstSn, boolean isReborn) {
		long stageId = 0;
		if (humanObj.stageObj instanceof StageObjectInstWorldBoss) {
			StageObjectInstWorldBoss stageObj = (StageObjectInstWorldBoss) (humanObj.stageObj);
			if (stageObj.fightType != ECrossFightType.FIGHT_WORLD_BOSS_VALUE) {
				Log.fight.error("===error in fightType={}", stageObj.fightType);
				return;
			}
			stageId = stageObj.stageId;
		} else {
			Log.fight.error("===玩家所在地图不对!");
			return;
		}
		
		int result = -1;// 0成功；非0失败（参见SysMsg.xlsx：）
		long timeLeft = humanObj.ttWorldBossReviveCD.getTimeLeft(Port.getTime());
		if (timeLeft > 0) {// 复活CD还在
			result = 580601;// 世界BOSS复活CD还未结束，不能进入战斗！
			// 返回错误提示
			SCWorldBossEnterFight.Builder msg = SCWorldBossEnterFight.newBuilder();
			msg.setActInstSn(actInstSn);
			msg.setResultCode(result);
			msg.setIsReborn(false);
			humanObj.sendMsg(msg);
		} else {
			result = 0;// 0成功
			WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
			prx.getBossInfo(actInstSn);
			prx.listenResult(this::_result_enterFight, "humanObj", humanObj, "stageId", stageId,  
					"actInstSn", actInstSn,	"result", result, "isReborn", isReborn);
		}
	}
	private void _result_enterFight(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_enterFight humanObj=null");
			return;
		}
		long stageId = Utils.getParamValue(context, "stageId", 0L);
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		int result = Utils.getParamValue(context, "result", 0);
		boolean isReborn = Utils.getParamValue(context, "isReborn", false);
		WBData wbData = Utils.getParamValue(results, "wbData", null);
		if (wbData == null) {
			Log.game.error("===_result_enterFight wbData=null");
			return;
		}
		// 组装boss信息
		int bossMapSn = wbData.bossMapSn;
		int bossInstSn = wbData.bossInstSn;
		//int bossHpCur = wbData.monsterHpCur.get(wbData.bossPos);
		// 获取鼓舞加成攻击万分比
		int addAtkPct = ParamManager.worldBossInspireAddAtk * humanObj.getHumanExtInfo().getWDBossInspireNum();
		// 获取涅槃重生加成攻击万分比
		if (isReborn) {
			addAtkPct += ParamManager.worldBossRebornAddAtk;
		}
		// 返回战斗数据
		SCWorldBossEnterFight.Builder msgFight = SCWorldBossEnterFight.newBuilder();
		msgFight.setActInstSn(actInstSn);
		msgFight.setResultCode(result);
		msgFight.setIsReborn(isReborn);
		humanObj.sendMsg(msgFight);
		
		Param param = new Param(HumanMirrorObject.WorldBossParam, new WorldBossParam(wbData, addAtkPct));
		humanObj.stageObj.combatObj = new CombatObjectWorldBoss(humanObj.stageObj.getPort(), humanObj.stageObj, 
				bossInstSn, bossMapSn, ECrossFightType.FIGHT_WORLD_BOSS_VALUE, param);
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_WORLD_BOSS, bossMapSn, stageId);
	}
	
	/**
	 * 离开世界Boss战斗
	 * @param humanObj
	 */
	public void _msg_CSWorldBossLeaveFight(HumanObject humanObj, int actInstSn) {
		// 发送BOSS相关信息
		//sendBossInfo(humanObj, actInstSn);
	}
	
	/**
	 * 请求世界BOSS副本SN
	 * @param humanObj
	 */
	public void _msg_CSWorldBossInstSn(HumanObject humanObj) {
		WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
		prx.isOpen();
		prx.listenResult(this::_result_CSWorldBossInstSn, "humanObj", humanObj);
	}
	private void _result_CSWorldBossInstSn(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_CSWorldBossInstSn humanObj is null");
			return;
		}
		// 获取返回值
		boolean isOpen = Utils.getParamValue(results, "isOpen", false);
		WBData wbData = Utils.getParamValue(results, "wbData", null);
		
		// 返回世界BOSS副本SN
		SCWorldBossInstSn.Builder msg = SCWorldBossInstSn.newBuilder();
		msg.setIsOpen(isOpen);
		msg.setActInstSn(wbData.actInstSn);
		msg.setBossInstSn(wbData.bossInstSn);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 请求世界BOSS信息
	 * @param humanObj
	 */
	public void _msg_CSWorldBossInfo(HumanObject humanObj, int actInstSn) {
		WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
		prx.getBossInfo(actInstSn);
		prx.listenResult(this::_result_getBossInfo, "humanObj", humanObj);
	}
	private void _result_getBossInfo(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_getBossInfo humanObj is null");
			return;
		}
		WBData wbData = Utils.getParamValue(results, "wbData", null);
		if (wbData == null) {
			Log.game.error("===_result_getBossInfo wbData=null");
			return;
		}
		// 发送世界BOSS信息
		sendSCWorldBossInfo(humanObj, wbData);
		
		if (Log.game.isDebugEnabled()) {
			Log.game.debug("世界boss信息 HpMax={},HpCur={}", wbData.monsterHpMax, wbData.monsterHpCur);
		}
	}
	
	/**
	 * 发送世界BOSS信息
	 * @param humanObj
	 */
	public void sendSCWorldBossInfo(HumanObject humanObj, WBData wbData) {
		SCWorldBossInfo.Builder msg = SCWorldBossInfo.newBuilder();
		msg.setActInstSn(wbData.actInstSn);
		msg.setBossInstSn(wbData.bossInstSn);
		msg.setLv(wbData.monsterLv.get(wbData.bossPos));
		msg.setHpMax(wbData.monsterHpMax.get(wbData.bossPos));
		msg.setHpCur(wbData.monsterHpCur.get(wbData.bossPos));
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 请求世界BOSS伤害排行前几名
	 * @param humanObj
	 * @param actInstSn 活动副本sn
	 */
	public void _msg_CSWorldBossRank(HumanObject humanObj, int actInstSn) {
		WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
		prx.getHarmRankList(actInstSn, humanObj.id);
		prx.listenResult(this::_result_getHarmRankList, "humanObj", humanObj, "actInstSn", actInstSn);
	}
	private void _result_getHarmRankList(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_getHarmRankList humanObj is null");
			return;
		}
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		List<WBHarmData> harmRankTopList = Utils.getParamValue(results, "harmRankTopList", null);
		if (harmRankTopList == null) {
			Log.game.error("===_result_getHarmRankList harmRankList=null");
			return;
		}
		
		int harmSelf = Utils.getParamValue(results, "harmSelf", 0);
		int rankSelf = Utils.getParamValue(results, "rankSelf", 0);
		
		SCWorldBossRank.Builder msg = SCWorldBossRank.newBuilder();
		msg.setActInstSn(actInstSn);
		for (WBHarmData wbHarmData : harmRankTopList) {
			DPVEHarm.Builder dHarm = DPVEHarm.newBuilder();
			dHarm.setName(wbHarmData.name);
			dHarm.setModelSn(wbHarmData.modelSn);
			dHarm.setHarm(wbHarmData.harm);
			msg.addDPVEHarm(dHarm.build());
		}
		msg.setHarmSelf(harmSelf);
		msg.setRankSelf(rankSelf);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 请求最终世界BOSS伤害排行前几名
	 * @param humanObj
	 * @param actInstSn 活动副本sn
	 */
	public void _msg_CSWorldBossRankFinal(HumanObject humanObj, int actInstSn) {
		WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
		prx.getHarmRankList(actInstSn, humanObj.id);
		prx.listenResult(this::_result_getHarmRankListFinal, "humanObj", humanObj, "actInstSn", actInstSn);
	}
	private void _result_getHarmRankListFinal(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_getHarmRankListFinal humanObj is null");
			return;
		}
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		List<WBHarmData> harmRankTopList = Utils.getParamValue(results, "harmRankTopList", null);
		if (harmRankTopList == null) {
			Log.game.error("===_result_getHarmRankListFinal harmRankList=null");
			return;
		}
		
		int harmSelf = Utils.getParamValue(results, "harmSelf", 0);
		int rankSelf = Utils.getParamValue(results, "rankSelf", 0);
		
		SCWorldBossRankFinal.Builder msg = SCWorldBossRankFinal.newBuilder();
		msg.setActInstSn(actInstSn);
		for (WBHarmData wbHarmData : harmRankTopList) {
			DPVEHarm.Builder dHarm = DPVEHarm.newBuilder();
			dHarm.setName(wbHarmData.name);
			dHarm.setModelSn(wbHarmData.modelSn);
			dHarm.setHarm(wbHarmData.harm);
			msg.addDPVEHarm(dHarm.build());
		}
		msg.setHarmSelf(harmSelf);
		msg.setRankSelf(rankSelf);
		humanObj.sendMsg(msg);
		
		// 发送世界boss伤害排行
		Event.fire(EventKey.ActInstWorldBossHarmRank, "humanObj", humanObj, "rank", rankSelf);
	}
	
	/**
	 * 结算一次世界BOSS伤害
	 * @param humanObj
	 * @param actInstSn 活动副本sn
	 */
	public void _msg_CSWorldBossHarm(HumanObject humanObj, int actInstSn, long harmTotal, List<Integer> harmList) {
		if(humanObj.ttWorldBossReviveCD.isStarted()){// 复活CD未到，提交结算伤害不处理
			Log.human.error("===世界BOSS活动中，{} 玩家复活CD未到，提交结算伤害不处理。", humanObj.name);
			return;
		}
		
		// 开启复活CD，并通知玩家
		long tmCD = ParamManager.worldBossFightCD * Time.SEC;
		humanObj.ttWorldBossReviveCD.start(tmCD);
		humanObj.extInfo.setWDBossNextFightTime(Port.getTime() + tmCD);
		
		// 结算伤害
		WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
		prx.reduceHp(humanObj.getHuman(), actInstSn, harmTotal, harmList);
		prx.listenResult(this::_result_reduceHp, "humanObj", humanObj, "actInstSn", actInstSn);
	}
	private void _result_reduceHp(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_reduceHp humanObj=null");
			return;
		}
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		// 获取返回信息
		long harmTotal = Utils.getParamValue(results, "harmTotal", 0L);
		WorldBoss worldBoss = Utils.getParamValue(results, "worldBoss", null);
		if (worldBoss == null) {
			Log.game.error("===_result_reduceHp worldBoss=null");
			return;
		}
		
		// 根据伤害给予铜币奖励
		int level = humanObj.getHuman().getLevel();
		ConfLevelExp conf = ConfLevelExp.get(level);
		if (null == conf) {
			Log.table.error("===ConfLevelExp no find sn={}", level);
			return;
		}
		long coinAward = 0;// 奖励铜币
		if (conf.coinInfo.length >= 3) {
			// 公式 = 基础值 + 伤害*奖励系数/10000，不能超过最大值
			long temp = (harmTotal * (long)conf.coinInfo[1]) / Utils.L10000;
			coinAward = conf.coinInfo[0] + temp;
			if (coinAward > conf.coinInfo[2]) {
				coinAward = conf.coinInfo[2];
			}
		}
		if (coinAward >= 0) {
			// 添加金钱奖励
			RewardHelper.reward(humanObj, EMoneyType.coin_VALUE, (int)coinAward, LogSysModType.WDBossHarmCoin);
		}
		
		// 返回结算一次世界BOSS伤害
		SCWorldBossHarm.Builder msg = SCWorldBossHarm.newBuilder();
		msg.setActInstSn(worldBoss.getActInstSn());
		msg.setHarmSelf((int)harmTotal);
		msg.setCoinAward((int)coinAward);
		int bossHpCur = (int)worldBoss.getHpCur();
		msg.setBossHpCur(bossHpCur);
		if (0 == bossHpCur) {// 0即boss死亡，需要加上击杀信息
			DWBKillInfo.Builder dWBKillInfo = DWBKillInfo.newBuilder();
			dWBKillInfo.setKillerId(worldBoss.getKillerId());
			dWBKillInfo.setKillerName(worldBoss.getKillerName());
			msg.setDWBKillInfo(dWBKillInfo);
			
			// boss死亡，重置玩家的世界BOSS记录信息
			resetHumanWDBossRecord(humanObj);
			
			// 如果击杀者id等于玩家id，则为最后一击玩家
			if (worldBoss.getKillerId() == humanObj.getHumanId()) {
				// 发送玩家击杀boss事件
				Event.fire(EventKey.ActInstWorldBossKiller, "humanObj", humanObj);
				// 击杀世界BOSS跑马灯
				String content = Utils.createStr("{}|{}|{}", ParamManager.sysMsgMark, 999016, humanObj.getHuman().getName());
				InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
			}
		}
		humanObj.sendMsg(msg);
		
		// 返回最新排名情况
		_msg_CSWorldBossRank(humanObj, actInstSn);
		
		// 返回世界BOSS挑战信息
		sendSCWorldBossFightInfo(humanObj, actInstSn);
		// 发送世界BOSS信息
		sendSCWorldBossInfo(humanObj, new WBData(worldBoss));
	}
	
	/**
	 * 广播世界BOSS活动结束，如果BOSS被击杀则附带被击杀信息
	 * @param humanObj
	 * @param wbData
	 */
	public void sendSCWorldBossEnd(HumanObject humanObj, WBData wbData) {
		SCWorldBossEnd.Builder msg = SCWorldBossEnd.newBuilder();
		msg.setActInstSn(wbData.actInstSn);
		msg.setBossHpCur(wbData.monsterHpCur.get(wbData.bossPos));
		if (0 == wbData.totalHpCur) {
			DWBKillInfo.Builder dWBKillInfo = DWBKillInfo.newBuilder();
			dWBKillInfo.setKillerId(wbData.killerId);
			dWBKillInfo.setKillerName(wbData.killerName);
			msg.setDWBKillInfo(dWBKillInfo);
		}
		humanObj.sendMsg(msg);
		Log.game.error("===sendSCWorldBossEnd id={},name={}", humanObj.getHumanId(), humanObj.name);
		// 重置玩家的世界BOSS记录信息（boss死亡或活动结束时重置）
		resetHumanWDBossRecord(humanObj);
	}
	/**
	 * 重置玩家的世界BOSS记录信息（boss死亡或活动结束时重置）
	 * @param humanObj
	 */
	public void resetHumanWDBossRecord(HumanObject humanObj) {
		// 重置玩家记录数据
		humanObj.extInfo.setWDBossNextFightTime(0);
		humanObj.extInfo.setWDBossNextInspireTime(0);
		humanObj.extInfo.setWDBossInspireNum(0);
		humanObj.extInfo.setWDBossRebornNum(0);
		// 停止计时器
		humanObj.ttWorldBossReviveCD.stop();
		humanObj.ttWorldBossInspireCD.stop();
	}
	
	/**
	 * 请求世界BOSS立即复活
	 * @param humanObj
	 * @param actInstSn
	 */
	public void _msg_CSWorldBossRevive(HumanObject humanObj, int actInstSn) {
		int result = -1;// 0成功；非0失败（参见SysMsg.xlsx：）
		long timeLeft = humanObj.ttWorldBossReviveCD.getTimeLeft(Port.getTime());
		if (timeLeft > 0) {// CD还在，则扣元宝
			int costGold = RewardHelper.getCostGold(ECostGoldType.wdBossReviveCost, 1);
			// 扣元宝
			if (RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold, LogSysModType.WDBossRevive)) {
				// 成功
				result = 0;
				humanObj.extInfo.setWDBossNextFightTime(Port.getTime());
				humanObj.ttWorldBossReviveCD.stop();
			} else {
				// 失败
				result = 15;// 元宝不足！
			}
		}
		if (result >= 0) {
			SCWorldBossRevive.Builder msg = SCWorldBossRevive.newBuilder();
			msg.setActInstSn(actInstSn);
			msg.setResultCode(result);
			humanObj.sendMsg(msg);
		}
	}
	
	/**
	 * 请求世界BOSS涅槃重生
	 * @param humanObj
	 * @param actInstSn
	 */
	public void _msg_CSWorldBossReborn(HumanObject humanObj, int actInstSn) {
		int result = -1;// 0成功；非0失败（参见SysMsg.xlsx：）
		int numReborn = humanObj.extInfo.getWDBossRebornNum();// 已涅槃重生次数
		long timeLeft = humanObj.ttWorldBossReviveCD.getTimeLeft(Port.getTime());
		if (timeLeft > 0) {// CD还在，则扣元宝
			int costGold = RewardHelper.getCostGold(ECostGoldType.wdBossRebornCost, numReborn+1);
			// 扣元宝
			if (RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold, LogSysModType.WDBossReborn)) {
				// 成功
				result = 0;
				humanObj.extInfo.setWDBossRebornNum(numReborn+1);
				humanObj.extInfo.setWDBossNextFightTime(Port.getTime());
				humanObj.ttWorldBossReviveCD.stop();
				
				int num = humanObj.cultureTimes.getWorldBossGoldResurrection() +1 ;
				Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num , "type",ActivitySevenTypeKey.Type_50);
				
				Event.fire(EventKey.ActInstWorldBossHumanReborn, "humanObj", humanObj);
			} else {
				// 失败
				result = 15;// 元宝不足！
			}
		}
		if (result >= 0) {
			SCWorldBossReborn.Builder msg = SCWorldBossReborn.newBuilder();
			msg.setActInstSn(actInstSn);
			msg.setResultCode(result);
			msg.setNumReborn(humanObj.extInfo.getWDBossRebornNum());
			humanObj.sendMsg(msg);
			
			if (result == 0) {// 涅槃重生成功则直接进入战斗
				_msg_CSWorldBossEnterFight(humanObj, actInstSn, true);
			}
		}
	}
	
	/**
	 * 请求世界BOSS清除鼓舞CD
	 * @param humanObj
	 * @param actInstSn
	 */
	public void _msg_CSWorldBossInspireCDClean(HumanObject humanObj, int actInstSn) {
		int result = -1;// 0成功；非0失败（参见SysMsg.xlsx：）
		int numInspire = humanObj.extInfo.getWDBossInspireNum();// 已鼓舞次数
		if (numInspire < ParamManager.worldBossInspireMaxNum) {
			if (numInspire < ParamManager.worldBossInspireFreeNum) {
				// 免费次数范围内
				result = 0;
			} else {
				long timeLeft = humanObj.ttWorldBossInspireCD.getTimeLeft(Port.getTime());
				if (timeLeft > 0) {// CD还在，则扣元宝
					int costGold = RewardHelper.getCostGold(ECostGoldType.wdBossInspireCDCost, 1);
					// 扣元宝
					if (RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold, LogSysModType.WDBossInspireCD)) {
						// 成功
						result = 0;
					} else {
						// 失败
						result = 15;// 元宝不足！
					}
				} else {// CD没了，不扣元宝
					result = 0;
				}
			}
		}
		if (result >= 0) {
			if (result == 0) {// 成功鼓舞
				long tmCD = ParamManager.worldBossInspireCD * Time.SEC;
				humanObj.ttWorldBossInspireCD.start(tmCD);
				humanObj.extInfo.setWDBossNextInspireTime(Port.getTime() + tmCD);
				humanObj.extInfo.setWDBossInspireNum(numInspire+1);
			}
			// 返回世界BOSS清除鼓舞CD结果
			SCWorldBossInspireCDClean.Builder msg = SCWorldBossInspireCDClean.newBuilder();
			msg.setActInstSn(actInstSn);
			msg.setResultCode(result);
			msg.setTmNextInspire(humanObj.extInfo.getWDBossNextInspireTime());
			msg.setNumInspire(humanObj.extInfo.getWDBossInspireNum());
			msg.setMaxInspire(ParamManager.worldBossInspireMaxNum);
			humanObj.sendMsg(msg);
		}
	}
	
	/**
	 * 请求世界BOSS其他玩家信息
	 * @param humanObj
	 * @param actInstSn
	 */
	public void _msg_CSWorldBossOtherHuman(HumanObject humanObj, int actInstSn) {
		WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
		prx.getOtherHuman(humanObj.id, actInstSn);
		prx.listenResult(this::_result_getOtherHuman, "humanObj", humanObj, "actInstSn", actInstSn);
	}
	private void _result_getOtherHuman(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_getBossInfo humanObj is null");
			return;
		}
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		Map<Long, Human> otherHumanMap = Utils.getParamValue(results, "otherHumanMap", null);
		// 返回世界BOSS其他玩家信息
		SCWorldBossOtherHuman.Builder msg = SCWorldBossOtherHuman.newBuilder();
		msg.setActInstSn(actInstSn);
		for (Human human : otherHumanMap.values()) {
			DHumanInfo.Builder dHumanInfo = DHumanInfo.newBuilder();
			dHumanInfo.setId(human.getId());
			dHumanInfo.setModelSn(human.getModelSn());
			dHumanInfo.setName(human.getName());
			msg.addDHumanInfoList(dHumanInfo);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 发放排名奖励
	 * @param humanId 玩家ID
	 * @param bossInstSn 即WorldBoss.sn
	 * @param rank 当前排名
	 */
	public void giveRankAward(long humanId, int bossInstSn, int rank) {
		int snBoss = 0;
		ConfWorldBoss conf = ConfWorldBoss.get(bossInstSn);
		if(null == conf) {
			Log.table.error("===配表错误ConfWorldBoss no find sn={}", bossInstSn);
			return;
		} else {
			snBoss = conf.sn;
		}
		
		int index = conf.rankingRange.length-1;// 默认最后一个
		for (int i = 0; i < conf.rankingRange.length-1; i++) {
			if (rank >= conf.rankingRange[i] && rank < conf.rankingRange[i+1]) {
				index = i;
				break;
			}
		}
		if (index < 0 || index >= conf.rankingReward.length) {
			Log.table.error("===配表错误ConfWorldBoss rankingReward[] no find index={}", index);
			return;
		}
		
		ConfRewards confRewards = ConfRewards.get(conf.rankingReward[index]);
		if (null == confRewards) {
			Log.table.error("===配表错误ConfRewards no find sn={}", conf.rankingReward[index]);
			return;
		} else {
			List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
			itemProduce.addAll(ProduceManager.inst().produceItem(confRewards.itemSn, confRewards.itemNum));

			// 特殊邮件内容：{MailTemplate.sn|bossName|rank}
			String detail = "{" + EMailType.MailWorldBossRankAward_VALUE + "|" + rank + "}";
			// 发送邮件到玩家
			MailManager.inst().sendSysMail(humanId, ParamManager.mailMark, detail, itemProduce);
		}
	}
	/**
	 * 发放击杀奖励
	 * @param humanId 玩家ID
	 * @param bossInstSn 即WorldBoss.sn
	 */
	public void giveKillerAward(long humanId, int bossInstSn) {
		int snBoss = 0;
		ConfWorldBoss conf = ConfWorldBoss.get(bossInstSn);
		if(null == conf) {
			Log.table.error("===配表错误ConfWorldBoss no find sn={}", bossInstSn);
			return;
		} else {
			snBoss = conf.sn;
		}
		
		ConfRewards confRewards = ConfRewards.get(conf.killerReward);
		if (null == confRewards) {
			Log.table.error("===配表错误ConfRewards no find sn={}", conf.killerReward);
			return;
		} else {
			List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
			itemProduce.addAll(ProduceManager.inst().produceItem(confRewards.itemSn, confRewards.itemNum));

			// 特殊邮件内容：{MailTemplate.sn|bossName}
			String detail = "{" + EMailType.MailWorldBossKillAward_VALUE + "}";
			// 发送邮件到玩家
			MailManager.inst().sendSysMail(humanId, ParamManager.mailMark, detail, itemProduce);
		}
	}

	
}
