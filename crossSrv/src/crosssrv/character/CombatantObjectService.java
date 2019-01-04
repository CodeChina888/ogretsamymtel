package crosssrv.character;

import core.CallPoint;
import core.Port;
import core.Service;
import core.connsrv.ConnectionProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.MsgHandler;
import crosssrv.combatant.CombatantManager;
import crosssrv.seam.CrossPort;
import crosssrv.seam.msg.CombatantExtendMsgHandler;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

@DistrClass(importClass = {})
public class CombatantObjectService extends Service {
	private CombatantExtendMsgHandler msgHandler = MsgHandler.getInstance(CombatantExtendMsgHandler.class);
	private final CombatantObject combatantObj;

	/**
	 * 构造函数
	 * 
	 * @param humanObj
	 */
	public CombatantObjectService(CombatantObject combatantObj, Port port) {
		super(port);
		this.combatantObj = combatantObj;
	}

	@DistrMethod
	public void ChangeConnPoint(CallPoint point) {
		combatantObj.connPoint = point;
		combatantObj.clearCloseStatus();
	}

	@Override
	public Object getId() {
		return combatantObj.id;
	}

	/**
	 * 获取所属Port
	 * 
	 * @return
	 */
	public CrossPort getPort() {
		return combatantObj.getPort();
	}

	public CombatantObject getCombatantObj() {
		return combatantObj;
	}

	/**
	 * 离开地图
	 * 
	 * @param unitId
	 */
	@DistrMethod
	public void leave(long stageTargetId) {
		if (combatantObj.stageObj.stageId == stageTargetId)
			return;

		combatantObj.stageLeave();
		// 发送离开地图事件
		Event.fireEx(EventKey.CombatantStageLeave, combatantObj.stageObj.mapSn, "stageSn",
				combatantObj.stageObj.getStageSn(), "combatantObj", combatantObj);
	}

	/**
	 * 接受并转发通信消息
	 * 
	 * @param unitId
	 * @param chunk
	 */
	@DistrMethod
	public void msgHandler(long connId, byte[] chunk) {
		// 忽略错误连接ID的请求
		long humanConnId = (long) combatantObj.connPoint.servId;
		if (humanConnId != connId) {
			// 将发送错误连接的请求连接关了
			CallPoint connPoint = new CallPoint();
			connPoint.nodeId = port.getCallFromNodeId();
			connPoint.portId = port.getCallFromPortId();
			connPoint.servId = connId;

			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			prx.close();
			return;
		}

		msgHandler.handle(chunk, "combatantObj", combatantObj);
	}

	/**
	 * 连接关闭
	 * 
	 * @param unitId
	 */
	@DistrMethod
	public void connClosed(long connId) {
		// 忽略错误连接ID的请求
		long combatantConnId = (long) combatantObj.connPoint.servId;
		// Log.temp.info("connClosed {} {} {}", humanConnId, connId,
		// Port.getTime());
		if (combatantConnId != connId) {
			return;
		}

		combatantObj.connDelayCloseClear();
	}

	@DistrMethod
	public void kickClosed() {
		// 直接T人
		combatantObj.connCloseClear();
	}

	/**
	 * 连接存活验证
	 * 
	 * @param unitId
	 */
	@DistrMethod
	public void connCheck(long connId) {
		port.returns(true);
	}

	/**
	 * 调度事件的处理 ， 来自于 combatantGlobalSerivce
	 * 
	 * @param key
	 */
	@DistrMethod
	public void onSchedule(int key, long timeLast) {
		CombatantManager.inst().onScheduleEvent(combatantObj, key, timeLast);
	}

	/**
	 * 向玩家发送简要消息事件
	 */
	@DistrMethod
	public void fireEventToHuman(int key) {
		Event.fire(key, "combatantObj", combatantObj);
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {

	}
}
