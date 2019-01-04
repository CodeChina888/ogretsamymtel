package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.worldsrv.support.Log;
import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.param.FightParamActionInfo;
import turnbasedsrv.param.FightParamBase;
import turnbasedsrv.param.FightParamPlot;
import turnbasedsrv.param.FightParamPlotFinish;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.stageEvent.EventPlot;

public class CondPlot extends StageCondBase {
	/** 剧情ID **/
	int plot = 0;
	/** 触发eventID **/
	int event = 0;
	/** 入场回合 **/
	int round = 0;
	
	static final String KeyRound = "round";
	static final String KeyPlot = "plot";
	static final String KeyEvent = "event";

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondPlot(int id,String value) {
		super(id);
		String[] types=Utils.strToStrArray(value, ",");
		if(types==null){
			return;
		}
		for(String str:types){
			String[] params = Utils.strToStrArray(str, "\\|");
			if(params==null){
				Log.fight.error("CondMonsterEnter error:{}",value);
				return;
			}
			switch(params[0]){
			case KeyRound:
				{
					if(params.length<2){
						Log.fight.error("CondMonsterEnter error:{}",value);
						return;					
					}
					round = Utils.intValue(params[1]);
				}
				break;
			case KeyPlot:
				{
					if(params.length<2){
						Log.fight.error("CondMonsterEnter error:{}",value);
						return;					
					}
					plot = Utils.intValue(params[1]);
				}
				break;
			case KeyEvent:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				event = Utils.intValue(params[1]);
			}
			break;
			}
		}
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.Plot;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(FightStageObject stageObj) {
		if(plot<=0){
			return;
		}
		if(event>0){
			stageObj.addStageOpAction(StageOpType.ActionFinish, this);
		}
		if(round>0){
			stageObj.addStepAction(StageStep.RoundStart, this);
		}		
	}

	/**
	 * 执行操作
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(FightStageObject stageObj) {
		switch (stageObj.stageStep) {
		case RoundStart:
			{
				if(stageObj.round == round){
					FightParamPlot param = new FightParamPlot(plot);
					stageObj.addStageEvent(new EventPlot(plot));
					stageObj.addStageOpAction(StageOpType.ActionPlotFinish, this);
					stageObj.doStageOp(StageOpType.SendPlotMsg, param);
					return;
				}
			}
			return;
		default:
			break;

		}
	}
	
	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	@Override
	public FightParamBase doStageOp(FightStageObject stageObj,StageOpType type, FightParamBase param){
		switch(type){
		case ActionPlotFinish:
			if(param instanceof FightParamPlotFinish){
				FightParamPlotFinish plotParam = (FightParamPlotFinish)param;
				if(plotParam.plot == plot){
					stageObj.removeStageOpAction(StageOpType.ActionPlotFinish, this);
					this.ActionFinish(stageObj, true);
				}
			}
			break;
		case ActionFinish:
			if(param instanceof FightParamActionInfo){
				FightParamActionInfo actionParam = (FightParamActionInfo)param;
				if(actionParam.actionId == this.event){
					stageObj.removeStageOpAction(StageOpType.ActionFinish, this);
					FightParamPlot plotParam = new FightParamPlot(plot);
					stageObj.addStageEvent(new EventPlot(plot));
					stageObj.addStageOpAction(StageOpType.ActionPlotFinish, this);
					stageObj.doStageOp(StageOpType.SendPlotMsg, plotParam);
					return null;
				}
			}
			break;
		}
		return null;
	}
}
