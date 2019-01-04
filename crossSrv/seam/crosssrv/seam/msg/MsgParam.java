package crosssrv.seam.msg;

import com.google.protobuf.GeneratedMessage;

import core.support.observer.MsgParamBase;
import crosssrv.character.CombatantObject;

/**
 * 游戏中的客户端消息都通过这个类封装
 */
public class MsgParam extends MsgParamBase {
	private CombatantObject combatantObj; // 发送消息的玩家

	public MsgParam(GeneratedMessage msg) {
		super(msg);
	}

	public CombatantObject getCombatantObject() {
		return combatantObj;
	}

	public void setCombatantObject(CombatantObject combatantObj) {
		this.combatantObj = combatantObj;
	}
}