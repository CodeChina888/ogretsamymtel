package turnbasedsrv.stageCond;

import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.param.FightParamActionInfo;
import turnbasedsrv.param.FightParamBase;
import turnbasedsrv.stage.FightStageObject;

public abstract class StageCondBase {
	/** id **/
	public int id;
	public boolean isValid = true;
	
	public StageCondBase(int id) {
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
	public abstract void init(FightStageObject stageObj);

	/**
	 * 执行场景阶段前置行为
	 * 
	 * @param stageObj
	 */
	public void doStepActionBefore(FightStageObject stageObj) {

	}

	/**
	 * 执行场景阶段行为
	 * 
	 * @param stageObj
	 */
	public void doStepAction(FightStageObject stageObj) {

	}

	/**
	 * 设置下一个场景阶段
	 * 
	 * @param stageObj
	 * @return
	 */
	public void setNextStep(FightStageObject stageObj, StageStep nowStep) {

	}

	/**
	 * 检测场景阶段是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	public boolean checkStepFinish(FightStageObject stageObj) {
		return false;
	}

	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	public boolean checkStageFinish(FightStageObject stageObj) {
		return false;
	}

	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	public FightParamBase doStageOp(FightStageObject stageObj, StageOpType type, FightParamBase param) {
		return null;
	}
	
	/**
	 * 移除行为
	 * @param isNotify 是否发出触发事件
	 */
	protected void ActionFinish(FightStageObject stageObj, boolean isNotify) {
		this.isValid = false;
		stageObj.removeAction(this);
		if(isNotify) {
			FightParamActionInfo infoParam = new FightParamActionInfo(this.id);
			
			stageObj.doStageOp(StageOpType.ActionFinish, infoParam);
		}
	}
}
