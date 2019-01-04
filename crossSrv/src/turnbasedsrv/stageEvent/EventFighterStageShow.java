package turnbasedsrv.stageEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.param.FightParamActionInfo;
import turnbasedsrv.stage.FightStageObject;

public class EventFighterStageShow extends StageEventBase {
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
		return StageEventDefine.FighterStageShow;
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
	public boolean isFinish(FightStageObject stageObj) {
		return isFinish;
	}
	
	public void setFinish(FightStageObject stageObj){
		isFinish = true;
		stageObj.removeStageEvent(this);
		FightParamActionInfo param = new FightParamActionInfo(this.actionId);
		stageObj.doStageOp(StageOpType.ActionFighterShowFinish, param);
	}
	
	/**
	 * 剧情时使用的默认结束
	 */
	public void setDefaultFinish(FightStageObject stageObj) {
		setFinish(stageObj);
	}
}
