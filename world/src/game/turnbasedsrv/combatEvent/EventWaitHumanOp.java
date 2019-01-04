package game.turnbasedsrv.combatEvent;

import game.turnbasedsrv.combat.CombatObject;

/**
 * 等待玩家操作
 * @author Neak
 *
 */
public class EventWaitHumanOp extends CombatEventBase {

	private boolean isFinish = false;

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultFinish(CombatObject stageObj) {
		setFinish(stageObj);
	}
	
	public void setFinish(CombatObject stageObj) {
		isFinish = true;
		stageObj.removeStageEvent(this);
	}
	
	/**
	 * 是否结束
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean isFinish(CombatObject stageObj) {
		return isFinish;
	}

}
