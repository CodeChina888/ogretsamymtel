package game.turnbasedsrv.combatEvent;

import game.turnbasedsrv.combat.CombatObject;

public abstract class CombatEventBase {// implements IStageEvent {
	/**
	 * 获取类型
	 * 
	 * @return
	 */
	public abstract String getType();

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	public abstract String toString();

	/**
	 * 是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	public boolean isFinish(CombatObject stageObj) {
		return true;
	}

	/**
	 * 心跳时间,如果事件未结束，返回0
	 * 
	 * @param deltaTime
	 * @return
	 */
	public int pulse(CombatObject stageObj, int deltaTime) {
		if (this.isFinish(stageObj)) {
			stageObj.removeStageEvent(this);
			return deltaTime;
		}
		return 0;
	}
	/**
	 * 剧情时使用的默认结束
	 */
	public abstract void setDefaultFinish(CombatObject stageObj);

}
