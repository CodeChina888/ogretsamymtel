package game.worldsrv.pk;

import core.support.observer.MsgReceiver;
import game.msg.Define.ETeamType;
import game.msg.MsgPk.CSPKHumanEnd;
import game.msg.MsgPk.CSPKHumanFight;
import game.msg.MsgPk.CSPKHumanLeave;
import game.msg.MsgPk.CSPKMirrorEnd;
import game.msg.MsgPk.CSPKMirrorFight;
import game.msg.MsgPk.CSPKMirrorLeave;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class PKMsgHandler {
	
	/**
	 * 切磋镜像玩家
	 */
	@MsgReceiver(CSPKMirrorFight.class)
	public void _msg_CSPKMirrorFight(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPKMirrorFight msg = param.getMsg();
		long beFightId = msg.getBeFightId();
		PKManager.inst()._msg_CSPKMirrorFight(humanObj, beFightId);
	}
	
	/**
	 * 离开切磋镜像玩家副本
	 */
	@MsgReceiver(CSPKMirrorLeave.class)
	public void _msg_CSPKMirrorLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		PKManager.inst()._msg_CSPKMirrorLeave(humanObj);
	}
	
	/**
	 * 结算切磋镜像玩家副本
	 */
	@MsgReceiver(CSPKMirrorEnd.class)
	public void _msg_CSPKMirrorEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
		if(null == finishMsg) {
			return;
		}
		//long idDef = finishMsg.getParam64(0);// 战斗对手ID
		//long recordId = finishMsg.getParam64(1);// 战斗记录ID
		boolean isWin = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		PKManager.inst()._msg_CSPKMirrorEnd(humanObj, isWin);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 切磋真人玩家
	 */
	@MsgReceiver(CSPKHumanFight.class)
	public void _msg_CSPKHumanFight(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPKHumanFight msg = param.getMsg();
		long beFightId = msg.getBeFightId();
		PKManager.inst()._msg_CSPKHumanFight(humanObj, beFightId);
	}
	
	/**
	 * 离开切磋真人玩家副本
	 */
	@MsgReceiver(CSPKHumanLeave.class)
	public void _msg_CSPKHumanLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		PKManager.inst()._msg_CSPKHumanLeave(humanObj);
	}
	
	/**
	 * 结算切磋真人玩家副本
	 */
	@MsgReceiver(CSPKHumanEnd.class)
	public void _msg_CSPKHumanEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
		if(null == finishMsg) {
			return;
		}
		//long idDef = finishMsg.getParam64(0);// 战斗对手ID
		//long recordId = finishMsg.getParam64(1);// 战斗记录ID
		boolean isWin = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		PKManager.inst()._msg_CSPKHumanEnd(humanObj, isWin);
	}
}
