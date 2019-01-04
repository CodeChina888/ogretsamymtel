package game.worldsrv.instResource;

import java.util.ArrayList;
import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.ETeamType;
import game.msg.MsgInstance.CSInstResAuto;
import game.msg.MsgInstance.CSInstResEnd;
import game.msg.MsgInstance.CSInstResEnter;
import game.msg.MsgInstance.CSInstResLeave;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class InstResMsgHandler {
	/**
	 * 请求，进入资源本
	 */
	@MsgReceiver(CSInstResEnter.class)
	public void _msg_CSInstResEnter(MsgParam param) {
		CSInstResEnter msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int instResSn = msg.getInstResSn();
		InstResManager.inst()._msg_CSInstResEnter(humanObj, instResSn);
	}
	
	/**
	 * 请求，中途离开资源本
	 */
	@MsgReceiver(CSInstResLeave.class)
	public void _msg_CSInstResLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		InstResManager.inst()._msg_CSInstResLeave(humanObj);
	}
	
	/**
	 * 请求，资源本结算
	 */
	@MsgReceiver(CSInstResEnd.class)
	public void _msg_CSInstResEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;		
		if(null == finishMsg) {
			return;
		}
		boolean isWin = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		int damageRatio = finishMsg.getParam32(0);
		InstResManager.inst()._msg_CSInstResEnd(humanObj, isWin, damageRatio);
	}
	
	/**
	 * 请求，资源本扫荡
	 */
	@MsgReceiver(CSInstResAuto.class)
	public void _msg_CSInstResAuto(MsgParam param) {
		CSInstResAuto msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int instResSn = msg.getInstResSn();
		InstResManager.inst()._msg_CSInstResAuto(humanObj, instResSn);
	}
}
