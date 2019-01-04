package game.worldsrv.raffle;

import core.support.observer.MsgReceiver;
import game.msg.MsgRaffle.CSLeaveLuckTurntable;
import game.msg.MsgRaffle.CSLuckTurntable;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 
 * @author Neak
 * 处理所有抽奖的协议
 */
public class RaffleMsgHandler {
	
	/**
	 * 抽幸运转盘
	 * @param param
	 */
	@MsgReceiver(CSLuckTurntable.class)
	public void _msg_CSLuckTurntable(MsgParam param) {
		@SuppressWarnings("unused")
		CSLuckTurntable msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		RaffleManager.inst()._msg_CSLuckTurntable(humanObj);
	}

	/**
	 * 离开幸运转盘
	 * @param param
	 */
	@MsgReceiver(CSLeaveLuckTurntable.class)
	public void _msg_CSLeaveLuckTurntable(MsgParam param) {
		@SuppressWarnings("unused")
		CSLeaveLuckTurntable msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		RaffleManager.inst()._msg_CSLeaveLuckTurntable(humanObj);
	}
	
}
