package game.worldsrv.team.instance;

import java.util.List;

import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.msg.Define.DMemberInfo;
import game.msg.Define.EActInstType;
import game.msg.MsgTeam.SCMonsterMadeIndex;
import game.worldsrv.character.HumanObject;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamData;
import game.worldsrv.team.TeamManager;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.team.TeamServiceProxy;
import game.worldsrv.team.instance.StageObjectInstTeam;
import game.worldsrv.team.instance.TeamInstanceManager;

public class TeamInstanceManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static TeamInstanceManager inst() {
		return inst(TeamInstanceManager.class);
	}

	/**
	 * 进入PVP副本
	 */
	public void enterPVP(TeamData team1, TeamData team2) {
		// 判断副本配置是否合法
		int stageSn = 0;
		ConfInstActConfig conf = ConfInstActConfig.get(team1.actInstSn);
		if (conf == null) {
			Log.stageCommon.error("===进入PVP副本错误：team1Id={}, team2Id={}, ConfInstActConfig.sn={}", team1.teamId,
					team2.teamId, team1.actInstSn);
			return;
		} else {
			stageSn = conf.instSn;
		}
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.stageCommon.error("===进入PVP副本错误：team1Id={}, team2Id={}, ConfInstStage.sn={}", team1.teamId,
					team2.teamId, stageSn);
			return;
		}

		createInstPVP(confInstStage.sn, confInstStage.mapSN, team1, team2);// 创建PVP副本
		
//		if (conf.type == EActInstType.Moba_VALUE) {
//			createInstPVPMoba(confInstStage.sn, confInstStage.mapSN, team1, team2);// 创建PVP副本：Moba
//		} else {
//			createInstPVP(confInstStage.sn, confInstStage.mapSN, team1, team2);// 创建PVP副本
//		}
	}

	/**
	 * 创建PVP副本：Moba
	 * @param stageSn
	 * @param mapSn
	 */
	private void createInstPVPMoba(int stageSn, int mapSn, TeamData team1, TeamData team2) {
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstMoba(stageSn, mapSn, false);
		prx.listenResult(this::_result_createInstPVP, "actInstSn", team1.actInstSn, "mapSn", mapSn, "team1", team1,
				"team2", team2);
	}

	/**
	 * 创建PVP副本
	 * @param stageSn
	 * @param mapSn
	 */
	private void createInstPVP(int stageSn, int mapSn, TeamData team1, TeamData team2) {
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstPVP(stageSn, mapSn, false);
		prx.listenResult(this::_result_createInstPVP, "actInstSn", team1.actInstSn, "mapSn", mapSn, "team1", team1,
				"team2", team2);
	}

	public void _result_createInstPVP(Param results, Param context) {
		// 创建完毕，用户切换地图
		TeamData team2 = Utils.getParamValue(context, "team2", null);
		TeamData team1 = Utils.getParamValue(context, "team1", null);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		long stageId = Utils.getParamValue(results, "stageId", -1L);

		if (team2 == null || team1 == null || stageId < 0) {
			Log.game.error("===_result_createPVP team2={}, team1={}, stageId={}", team2, team1, stageId);
			return;
		}

		// ***传入队伍1
		int group = team1.getGroupId();
		List<Long> listHumanId = team1.getAllId();
		// 查找队员：通知队员进入副本
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(listHumanId);
		prx.listenResult(this::_result_teamEnterPVP, "actInstSn", actInstSn, "mapSn", mapSn, "stageId", stageId, "group",
				group, "listDMemInfoTeam1", team1.createDMemberInfo(), "listDMemInfoTeam2", team2.createDMemberInfo());

		// ***传入队伍2
		group = team2.getGroupId();
		listHumanId = team2.getAllId();
		// 查找队员：通知队员进入副本
		prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(listHumanId);
		prx.listenResult(this::_result_teamEnterPVP, "actInstSn", actInstSn, "mapSn", mapSn, "stageId", stageId, "group",
				group, "listDMemInfoTeam1", team1.createDMemberInfo(), "listDMemInfoTeam2", team2.createDMemberInfo());
	}

	private void _result_teamEnterPVP(Param results, Param context) {
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		long stageId = Utils.getParamValue(context, "stageId", -1L);
		int group = Utils.getParamValue(context, "group", 0);
		List<DMemberInfo> listDMemInfoTeam1 = Utils.getParamValue(context, "listDMemInfoTeam1", null);
		List<DMemberInfo> listDMemInfoTeam2 = Utils.getParamValue(context, "listDMemInfoTeam2", null);

		List<HumanGlobalInfo> listHumanGlobalInfo = results.get();
		int index = 0;// 队员序号索引：出生在第几个位置
		for (HumanGlobalInfo hg : listHumanGlobalInfo) {
			if (hg != null) {// 通知队员进入副本
				index++;
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId, hg.id);
				prxHumanObj.teamEnterPVP(actInstSn, mapSn, stageId, group, index, listDMemInfoTeam1, listDMemInfoTeam2);
			}
		}
	}

	/**
	 * 进入PVE副本
	 * @param team
	 * @param stageSn 关卡sn
	 */
	public void enterPVE(TeamData team, int stageSn) {
		// 判断副本配置是否合法
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.stageCommon.error("===进入PVE副本错误：teamId={}, ConfInstStage.sn={}", team.teamId, stageSn);
			return;
		}

		createInstPVE(confInstStage.sn, confInstStage.mapSN, team);// 创建PVE副本
	}

	/**
	 * 创建PVE副本
	 * @param stageSn
	 * @param mapSn
	 */
	private void createInstPVE(int stageSn, int mapSn, TeamData team) {
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstPVE(stageSn, mapSn, false);
		prx.listenResult(this::_result_InstPVE, "actInstSn", team.actInstSn, "mapSn", mapSn, "team", team);
	}

	public void _result_InstPVE(Param results, Param context) {
		// 创建完毕，用户切换地图
		if (!results.containsKey("stageId"))
			return;

		long stageId = Utils.getParamValue(results, "stageId", -1L);
		TeamData team = Utils.getParamValue(context, "team", null);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		if (stageId < 0 || team == null) {
			Log.game.error("===_result_create stageId={}, team={}", stageId, team);
			return;
		}

		List<Long> listHumanId = team.getAllId();
		// 查找队员：通知队员进入副本
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(listHumanId);
		prx.listenResult(this::_result_teamEnterRep, "actInstSn", actInstSn, "mapSn", mapSn, "stageId", stageId);
	}

	private void _result_teamEnterRep(Param results, Param context) {
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		long stageId = Utils.getParamValue(context, "stageId", -1L);
		if (stageId < 0) {
			Log.game.error("===_result_teamEnterRep stageId={} ", stageId);
			return;
		}
		List<HumanGlobalInfo> listHumanGlobalInfo = results.get();
		int index = 0;// 队员序号索引：出生在第几个位置
		for (HumanGlobalInfo hg : listHumanGlobalInfo) {
			if (hg != null) {// 通知队员进入副本
				index++;
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId, hg.id);
				prxHumanObj.teamEnterInst(actInstSn, mapSn, stageId, index);
			}
		}
	}

	/**
	 * PVE副本通知刷第几波怪
	 * @param humanObj
	 * @param indexCur
	 * @param indexMax
	 */
	public void _send_SCMonsterMadeIndex(HumanObject humanObj, int indexCur, int indexMax) {
		SCMonsterMadeIndex.Builder msg = SCMonsterMadeIndex.newBuilder();
		msg.setIndexCur(indexCur);
		msg.setIndexMax(indexMax);
		humanObj.sendMsg(msg);
	}

	/**
	 * 离开副本 自动回到副本进入前的主地图
	 * @param humanObj
	 */
	public void _msg_CSInstanceLeave(HumanObject humanObj) {
		if (humanObj.stageObj instanceof StageObjectInstTeam) {// 组队副本：PVE,PVP,MOBA
			StageObjectInstTeam stageObj = (StageObjectInstTeam) humanObj.stageObj;
			// 检查是否需离队再退出副本
			TeamData team = humanObj.getTeam();
			if (team != null) {
				boolean leaveTeam = true;// 中途强制退出副本，马上离队
				if (stageObj.isSendInstanceEnd) {// 正常结束退出副本，需要再判断是否离队
					// 判断是否可以加入活动副本
					if (TeamManager.inst().canJoinActInst(humanObj, stageObj.getActInstSn())) {
						leaveTeam = false;
					}
				}
				if (leaveTeam) {// 离开队伍，有可能：时间段不对或已无参与次数
					TeamServiceProxy prx = TeamServiceProxy.newInstance();
					prx.teamLeave(humanObj.id, team.teamId, team.actInstSn);
				}
			}
			// 离开副本
//			humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
			StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
		}
	}

}
