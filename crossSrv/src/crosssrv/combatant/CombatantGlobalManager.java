package crosssrv.combatant;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Chunk;
import core.connsrv.ConnectionProxy;
import core.support.ManagerBase;
import game.msg.MsgIds;

public class CombatantGlobalManager extends ManagerBase {
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static CombatantGlobalManager inst() {
		return inst(CombatantGlobalManager.class);
	}

	/**
	 * 发送消息
	 * 
	 * @param connPoint
	 * @param msg
	 */
	public void sendMsg(CallPoint connPoint, Message msg) {
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
	}
}