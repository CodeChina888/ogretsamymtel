package game.worldsrv.achieveTitle;

import core.support.observer.MsgReceiver;
import game.msg.MsgTitle.CSSelectAchieveTitle;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 成就称号
 */
public class AchieveTitleMsgHandler {
	
	/**
	 * 领取成就称号
	 * @param param
	 */
	@MsgReceiver(CSSelectAchieveTitle.class)
	public void onCommitAchievement(MsgParam param){		
		HumanObject humanObj = param.getHumanObject();
		CSSelectAchieveTitle msg = param.getMsg();
		int titleSn = msg.getTitleSn();
		AchieveTitleManager.inst()._msg_CSSelectAchieveTitle(humanObj, titleSn);
	}
	
}
