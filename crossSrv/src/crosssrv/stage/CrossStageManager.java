package crosssrv.stage;

import java.util.Collection;

import com.google.protobuf.Message.Builder;

import core.support.ManagerBase;
import crosssrv.character.CombatantObject;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class CrossStageManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static CrossStageManager inst() {
		return inst(CrossStageManager.class);
	}

	/**
	 * 发送消息至玩家
	 */
	public void sendMsgToHumans(Builder builder, Collection<CombatantObject> combatants) {
		for (CombatantObject combatant : combatants) {
			combatant.sendMsg(builder);
		}
	}

	/**
	 * 发送消息至地图中的全部玩家
	 */
	public void sendMsgToStage(Builder builder, CrossStageObject stageObj) {
		sendMsgToHumans(builder, stageObj.getCombatants());
	}

	/**
	 * 进入场景最后一步：客户端发送切入场景
	 * 
	 * @param combatantObj
	 */
	public void onCombatantStageEnter(CombatantObject combatantObj) {
		CrossStageObject stageObj = combatantObj.stageObj;

		// 收到这个消息分发出去之后才会设置humanObj.isClientStageReady = true 状态 代表客户端已经准备好了
		Event.fireEx(EventKey.CombatantStageEnterBefore, stageObj.mapSn, "combatantObj", combatantObj);

		// 发送进入地图事件
		Event.fireEx(EventKey.CombatantStageEnter, stageObj.mapSn, "combatantObj", combatantObj);

		// 客户端战斗，不处理下面的逻辑
		if (stageObj.isClient)
			return;

	}

}
