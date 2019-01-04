package game.turnbasedsrv.combatRule;

import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.param.FightParamActionInfo;
import game.turnbasedsrv.param.FightParamBase;

public abstract class CombatRuleBase {// implements IStageCond {
	/** id **/
	public int id;
	public boolean isValid = true;
	
	public CombatRuleBase(int id){
		this.id = id;
	}
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
	 * 初始化
	 * 
	 * @param stageObj
	 */
	public abstract void init(CombatObject stageObj);

	/**
	 * 执行场景阶段前置行为
	 * 
	 * @param stageObj
	 */
	public void doStepActionBefore(CombatObject stageObj) {

	}

	/**
	 * 执行场景阶段行为
	 * 
	 * @param stageObj
	 */
	public void doStepAction(CombatObject stageObj) {

	}

	/**
	 * 设置下一个场景阶段
	 * 
	 * @param stageObj
	 * @return
	 */
	public void setNextStep(CombatObject stageObj, CombatStepType nowStep) {

	}

	/**
	 * 检测场景阶段是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	public boolean checkStepFinish(CombatObject stageObj) {
		return false;
	}

	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	public boolean checkStageFinish(CombatObject stageObj) {
		return false;
	}
	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	public FightParamBase doStageOp(CombatObject stageObj,CombatOpType type, FightParamBase param){
		return null;
	}
	/**
	 * 移除行为
	 * @param isNotify 是否发出触发事件
	 */
	protected void ActionFinish(CombatObject stageObj,boolean isNotify){
		this.isValid = false;
		stageObj.removeAction(this);
		if(isNotify){
			FightParamActionInfo infoParam = new FightParamActionInfo(this.id);
			
			stageObj.doStageOp(CombatOpType.ActionFinish, infoParam);
		}
	}
}
