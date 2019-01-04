package game.worldsrv.human;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Chunk;
import core.connsrv.ConnectionProxy;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.worldsrv.character.UnitManager;
import game.worldsrv.support.PropCalcCommon;
import game.msg.MsgIds;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Unit;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

public class HumanGlobalManager extends ManagerBase {
	/**
	 * 获取实例
	 * @return
	 */
	public static HumanGlobalManager inst() {
		return inst(HumanGlobalManager.class);
	}

	/**
	 * 发送消息
	 * @param connPoint
	 * @param msg
	 */
	public void sendMsg(CallPoint connPoint, Message msg) {
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
	}
	
	/**
	 * 监听副本星星总数变化
	 */
	@Listener(EventKey.InstAnyPass)
	public void _listener_InstAnyPass(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		// 获取副本星星总数
		int instStar = InstanceManager.inst().getAllStarCount(humanObj);
		// 同步全局信息
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateInstStar(humanObj.getHumanId(), instStar);
	}
		
}