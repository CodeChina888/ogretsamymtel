package game.worldsrv.activitySeven;

import core.support.observer.MsgReceiver;
import game.msg.MsgActivity.CSActivityIntegral;
import game.msg.MsgActivitySeven.CSCommitNoviceActivity;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class ActivitySevenMsgHandler {
	
	/**
	 * 所有的七日活动目标的成就
	 * @param param
	 */
	@MsgReceiver(CSCommitNoviceActivity.class)
	public void onCSCommitNoviceActivity(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSCommitNoviceActivity msg = param.getMsg();
		ActivitySevenManager.inst().commitServiceActivity(humanObj, msg.getSn());
	}
	
	@MsgReceiver(CSActivityIntegral.class)
	public void onCSActivityIntegral(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSActivityIntegral msg = param.getMsg();
		int sn = msg.getSn();
		ActivitySevenManager.inst().onCSActivityIntegral(humanObj,sn);
	}
	
}
