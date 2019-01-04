package game.worldsrv.name;

import core.support.observer.MsgReceiver;
import game.worldsrv.name.NameManager;

import game.msg.MsgName.CSChangeName;
import game.msg.MsgName.CSChangeNameRandom;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class NameMsgHandler {

	/**
	 * 改名字
	 * @param param
	 */
	@MsgReceiver(CSChangeName.class)
	public void _msg_CSChangeName(MsgParam param) {
		CSChangeName msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		// NameManager.inst().changeNameOnce(humanObj, msg.getName());//只允许一次改名
		NameManager.inst()._msg_CSChangeName(humanObj, msg.getName());// 跟任务挂钩
	}

	/**
	 * 获取一个随机名字
	 * @param param
	 */
	@MsgReceiver(CSChangeNameRandom.class)
	public void _msg_CSChangeNameRandom(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		NameManager.inst()._msg_CSChangeNameRandom(humanObj);
	}
}
