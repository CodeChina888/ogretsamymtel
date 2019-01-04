package game.turnbasedsrv.combatEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.param.FightParamActionInfo;

public class EventFighterStageShow extends CombatEventBase {
	/** 是否完成 **/
	boolean isFinish=false;
	/** actionId **/
	int actionId=0;

	public EventFighterStageShow() {
	}
	public EventFighterStageShow(int actionId) {
		this.actionId = actionId;
	}

	/**
	 * 获取属性值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatEventDefine.FighterStageShow;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("isFinish", isFinish).toString();
	}

	/**
	 * 是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean isFinish(CombatObject stageObj) {
		return isFinish;
	}
	
	public void setFinish(CombatObject stageObj){
		isFinish = true;
		stageObj.removeStageEvent(this);
		FightParamActionInfo param = new FightParamActionInfo(this.actionId);
		stageObj.doStageOp(CombatOpType.ActionFighterShowFinish, param);
	}
	/**
	 * 剧情时使用的默认结束
	 */
	public void setDefaultFinish(CombatObject stageObj){
		setFinish(stageObj);
	}
}
