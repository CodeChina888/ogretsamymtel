package game.worldsrv.quest;

import core.support.observer.MsgReceiver;
import game.msg.MsgQuest.CSCommitQuestDaily;
import game.msg.MsgQuest.CSGetLivenessReward;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 任务
 */
public class QuestMsgHandler {

	/**
	 * 领取活跃度奖励
	 * @param param
	 */
	@MsgReceiver(CSGetLivenessReward.class)
	public void _msg_CSGetLivenessReward(MsgParam param) {
		CSGetLivenessReward msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		QuestDailyManager.inst()._msg_CSGetLivenessReward(humanObj, msg.getSn());
	}

	
	/**
	 * 提交每日任务，即完成任务领取奖励
	 * @param param
	 */
	@MsgReceiver(CSCommitQuestDaily.class)
	public void _msg_CSCommitQuestDaily(MsgParam param) {
		CSCommitQuestDaily msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		QuestDailyManager.inst()._msg_CSCommitQuestDaily(humanObj, msg.getSn());
	}
	
}
