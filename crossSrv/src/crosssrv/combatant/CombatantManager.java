package crosssrv.combatant;

import core.Port;
import core.support.Config;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import core.support.observer.Listener;
import crosssrv.character.CombatantObject;
import crosssrv.stage.CrossStageGlobalServiceProxy;
import crosssrv.stage.CrossStageObject;
import crosssrv.support.Log;
import game.worldsrv.support.D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class CombatantManager extends ManagerBase {
	// 玩家初始主地图
	public static int stageInitSn = 10000; // 1
	public static int stageInitRepSn = 10000; // 1
	public static int stageInitMapSn = 10000; // 1

	public static final int ONLINE_CHANGE_INTERVAL_SEC = 10; // 在线时间心跳间隔
	public static final int LOGIN_STATE_NOMAL = 1; // 非第一次正常登录
	public static final int LOGIN_STATE_FIRST = 2; // 本日第一次登录

	/**
	 * o 获取实例
	 * 
	 * @return
	 */
	public static CombatantManager inst() {
		return inst(CombatantManager.class);
	}

	public void onScheduleEvent(CombatantObject combatantObj, int key, long timeNow) {
		Event.fire(key, "combatantObj", combatantObj, "timeLoginLast", timeNow);
	}

	/**
	 * 玩家数据加载完毕后 登录游戏
	 * 
	 * @param combatantObj
	 */
	public void login(CombatantObject combatantObj) {
		Log.cross.info("玩家数据加载完毕  开始登录 humanId={}", combatantObj.id);
		// 注册到地图
		CrossStageObject stageObj = combatantObj.stageObj;

		// 将玩家注册进地图 暂不显示
		combatantObj.stageRegister(stageObj);

		long timeNow = Port.getTime();
		// 发送登陆事件
		Event.fire(EventKey.CombatantLogin, "combatantObj", combatantObj, "timeLoginLast", timeNow);
		String nodeId = Config.getCrossPartDefaultNodeId();
		String portId = Config.getCrossPartDefaultPortId();
		CrossStageGlobalServiceProxy proxy = CrossStageGlobalServiceProxy.newInstance(nodeId, portId,
				D.CROSS_SERV_STAGE_GLOBAL);
		// 玩家地图人数+1
		proxy.stageHumanNumAdd(combatantObj.stageObj.stageId);
	}

	/**
	 * 玩家完成加载登录到地图中时进行操作
	 * 
	 * @param params
	 */
	@Listener(EventKey.CombatantStageEnter)
	public void _listener_StageCombatantEnter(Param params) {
		CombatantObject combatantObj = Utils.getParamValue(params, "combatantObj", null);
		if (combatantObj == null) {
			Log.logObjectNull(Log.fight,CombatantObject.class.getName());
			return;
		}
		combatantObj.isClientStageReady = true;
	}
}