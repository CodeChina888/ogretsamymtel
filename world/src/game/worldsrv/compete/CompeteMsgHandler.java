package game.worldsrv.compete;

import core.support.observer.MsgReceiver;
import game.msg.Define.ETeamType;
import game.msg.MsgCompete.CSCompeteBuyNum;
import game.msg.MsgCompete.CSCompeteEnd;
import game.msg.MsgCompete.CSCompeteFight;
import game.msg.MsgCompete.CSCompeteFightRecord;
import game.msg.MsgCompete.CSCompeteLeave;
import game.msg.MsgCompete.CSCompeteOpen;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 竞技场
 */
public class CompeteMsgHandler {

	/**
	 * 打开竞技场
	 * @param param
	 */
	@MsgReceiver(CSCompeteOpen.class)
	public void _msg_CSCompeteOpen(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompeteManager.inst()._msg_CSCompeteOpen(humanObj);
	}
	
	/**
	 * 进入挑战
	 * @param param
	 */
	@MsgReceiver(CSCompeteFight.class)
	public void _msg_CSCompeteFight(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCompeteFight msg = param.getMsg();
		// 判断不能挑战
		long fightId = msg.getBeChallengerRoleId();
		int fightRank = msg. getBeChallengerRank();
		// 进入挑战
		CompeteManager.inst()._msg_CSCompeteFight(humanObj, fightRank, fightId);
	}
	
	/**
	 * 请求离开竞技场
	 * @param param
	 */
	@MsgReceiver(CSCompeteLeave.class)
	public void _msg_CSCompeteLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompeteManager.inst()._msg_CSCompeteLeave(humanObj);
	}
	
	/**
	 * 竞技场战斗结束
	 * @param param
	 */
	@MsgReceiver(CSCompeteEnd.class)
	public void _msg_CSCompeteEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
		if(null == finishMsg) {
			return;
		}
		long idDef = finishMsg.getParam64(0);// 战斗对手ID
		long recordId = 0;//finishMsg.getParam64(1);// 战斗记录ID
		boolean isWin = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		CompeteManager.inst()._msg_CSCompeteEnd(humanObj, idDef, isWin, recordId);
	}
	
	/**
	 * 请求战报
	 * @param param
	 */
	@MsgReceiver(CSCompeteFightRecord.class)
	public void _msg_CSCompeteFightRecord(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompeteManager.inst()._msg_CSCompeteFightRecord(humanObj);
	}
	
	/**
	 * 购买挑战次数
	 * @param param
	 */
	@MsgReceiver(CSCompeteBuyNum.class)
	public void _msg_CSCompeteBuyNum(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompeteManager.inst().CSCompeteBuyNum(humanObj);
	}
	
}
