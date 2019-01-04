package game.worldsrv.achievement;

import core.support.observer.MsgReceiver;
import game.msg.MsgQuest.CSCommitAchievement;
import game.msg.MsgQuest.CSOpenAchievement;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 	任务
 */
public class AchievementMsgHandler {
	
	/**
	 * 提交成就
	 * @param param
	 */
	@MsgReceiver(CSCommitAchievement.class)
	public void onCommitAchievement(MsgParam param){		
		HumanObject humanObj = param.getHumanObject();
		CSCommitAchievement msg = param.getMsg();
		int sn = msg.getSn();
		AchievementManager.inst()._msg_CSCommitAchievement(humanObj, sn);
	}
	
	/**
	 * 查询所有的成就
	 * @param param
	 */
	@MsgReceiver(CSOpenAchievement.class)
	public void onCSOpenAchievement(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		AchievementManager.inst().sendMsg(humanObj, humanObj.achievements);
	}
	
}
