package game.worldsrv.instance;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.stage.types.StageObjectInstance;
import game.msg.MsgInstance.CSInstEnd;
import game.msg.MsgInstance.CSInstFightNumReset;
import game.msg.MsgInstance.CSInstInfoAll;
import game.msg.MsgInstance.CSInstOpenBox;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.msg.Define.ETeamType;
import game.msg.MsgInstance.CSInstAuto;
import game.msg.MsgInstance.CSInstEnter;
import game.msg.MsgInstance.CSInstLeave;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 单人副本
 */
public class InstanceMsgHandler {
	
	/**
	 * 获取所有副本章节信息
	 * @param param
	 */
	@MsgReceiver(CSInstInfoAll.class)
	public void _msg_CSInstInfoAll(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		// 下发所有副本章节信息
		InstanceManager.inst()._send_SCInstInfoAll(humanObj);
	}
	
	/**
	 * 进入副本
	 * @param param
	 */
	@MsgReceiver(CSInstEnter.class)
	public void _msg_CSInstEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstEnter msg = param.getMsg();
		int stageSn = msg.getInstSn(); // 副本关卡RepStage的sn
		InstanceManager.inst()._msg_CSInstEnter(humanObj, stageSn);
	}
	
	/**
	 * 中途离开副本
	 * @param param
	 */
	@MsgReceiver(CSInstLeave.class)
	public void _msg_CSInstLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		InstanceManager.inst()._msg_CSInstLeave(humanObj);
	}

	/**
	 * 正常结束副本
	 * @param param
	 */
	@MsgReceiver(CSInstEnd.class)
	public void _msg_CSInstEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;		
		if(null == finishMsg) {
			return;
		}
		List<Integer> stars = finishMsg.getStarList(); //通关获得星星数
		boolean win = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		InstanceManager.inst()._msg_CSInstEnd(humanObj, !win, stars);
	}
	
	/**
	 * 副本扫荡
	 * @param param
	 */
	@MsgReceiver(CSInstAuto.class)
	public void _msg_CSInstAuto(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstAuto msg = param.getMsg();
		InstanceManager.inst()._msg_CSInstAuto(humanObj, msg.getInstSn(), msg.getNum());
	}
	
	/**
	 * 重置副本次数
	 * @param param
	 */
	@MsgReceiver(CSInstFightNumReset.class)
	public void _msg_CSInstFightNumReset(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstFightNumReset msg = param.getMsg();
		InstanceManager.inst()._msg_CSInstFightNumReset(humanObj, msg.getInstSn());
	}
	
	/**
	 * 领取章节通关宝箱
	 * @param param
	 */
	@MsgReceiver(CSInstOpenBox.class)
	public void _msg_CSInstOpenBox(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstOpenBox msg = param.getMsg();
		InstanceManager.inst()._msg_CSInstOpenBox(humanObj, msg.getChapSn(), msg.getIndex());
	}
	
}
