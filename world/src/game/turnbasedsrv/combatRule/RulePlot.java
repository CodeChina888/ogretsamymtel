package game.turnbasedsrv.combatRule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.combatEvent.EventPlot;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.param.FightParamActionInfo;
import game.turnbasedsrv.param.FightParamBase;
import game.turnbasedsrv.param.FightParamPlot;
import game.turnbasedsrv.param.FightParamPlotFinish;
import game.worldsrv.support.Log;

public class RulePlot extends CombatRuleBase {
	/** 剧情ID **/
	List<Integer> plotList = new ArrayList<>();
	/** 触发eventID **/
	int event = 0;
	/** 入场回合 **/
	int round = 0;
	/** 剧情选择 **/
	int select  = 0;
	/** 等待的plot **/
	int sendPlot = 0;
	
	static final String KeyRound = "round";
	static final String KeyPlot = "plot";
	static final String KeyEvent = "event";
	static final String KeySelect = "select";

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RulePlot(int id,String value) {
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
			case KeySelect:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				select = Utils.intValue(params[1]);
			}
			break;
			case KeyPlot:
				{
					if(params.length<2){
						Log.fight.error("CondMonsterEnter error:{}",value);
						return;					
					}
					for(int i=1;i<params.length;i++){
						int plot = Utils.intValue(params[i]);
						plotList.add(plot);
					}
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
		return CombatRuleDefine.Plot;
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
	public void init(CombatObject stageObj) {
		if(plotList.isEmpty()){
			return;
		}
		if(event>0){
			stageObj.addStageOpAction(CombatOpType.ActionFinish, this);
		}
		if(round>0){
			stageObj.addStepAction(CombatStepType.RoundStart, this);
		}		
	}

	/**
	 * 执行操作
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(CombatObject stageObj) {
		switch (stageObj.stageStep) {
		case RoundStart:
			{
				if(stageObj.round == round){
					int index = Utils.getParamValue(stageObj.exParam, "plot"+select, 0);
					sendPlot = plotList.get(index);
					FightParamPlot param = new FightParamPlot(sendPlot);
					stageObj.addStageEvent(new EventPlot(sendPlot));
					stageObj.addStageOpAction(CombatOpType.ActionPlotFinish, this);
					stageObj.doStageOp(CombatOpType.SendPlotMsg, param);
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
	public FightParamBase doStageOp(CombatObject stageObj,CombatOpType type, FightParamBase param){
		switch(type){
		case ActionPlotFinish:
			if(param instanceof FightParamPlotFinish){
				FightParamPlotFinish plotParam = (FightParamPlotFinish)param;
				if(plotParam.plot == sendPlot){
					stageObj.removeStageOpAction(CombatOpType.ActionPlotFinish, this);
					this.ActionFinish(stageObj, true);
				}
			}
			break;
		case ActionFinish:
			if(param instanceof FightParamActionInfo){
				FightParamActionInfo actionParam = (FightParamActionInfo)param;
				if(actionParam.actionId == this.event){
					stageObj.removeStageOpAction(CombatOpType.ActionFinish, this);
					int index = Utils.getParamValue(stageObj.exParam, "plot"+select, 0);
					sendPlot = plotList.get(index);
					FightParamPlot plotParam = new FightParamPlot(sendPlot);
					stageObj.addStageEvent(new EventPlot(sendPlot));
					stageObj.addStageOpAction(CombatOpType.ActionPlotFinish, this);
					stageObj.doStageOp(CombatOpType.SendPlotMsg, plotParam);
					return null;
				}
			}
			break;
		default:
			break;
		}
		return null;
	}
}
