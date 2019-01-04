package game.seam.msg;

import game.worldsrv.character.HumanObject;

import core.support.observer.MsgParamBase;

import com.google.protobuf.GeneratedMessage;

/**
 * 游戏中的客户端消息都通过这个类封装
 */
public class MsgParam extends MsgParamBase {
	private HumanObject humanObj; // 发送消息的玩家(必须有的)

	public MsgParam(GeneratedMessage msg, HumanObject humanObj) {
		super(msg);
		this.humanObj = humanObj;
	}

	public HumanObject getHumanObject() {
		return humanObj;
	}

}