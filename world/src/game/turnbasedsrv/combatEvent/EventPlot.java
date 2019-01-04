package game.turnbasedsrv.combatEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.param.FightParamPlotFinish;

public class EventPlot extends CombatEventBase {
	/** 场景ID **/
	int plot;
	/** 是否完成 **/
	boolean isFinish=false;

	public EventPlot(int plot) {
		this.plot = plot;
	}

	/**
	 * 获取属性值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatEventDefine.Plot;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("plot", plot).toString();
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
	
	public int getPlot(){
		return plot;
	}
	public void setFinish(CombatObject stageObj,int select){
		stageObj.exParam.put("plot"+plot, select);
		isFinish = true;
		stageObj.removeStageEvent(this);
		FightParamPlotFinish param = new FightParamPlotFinish(plot,select);
		stageObj.doStageOp(CombatOpType.ActionPlotFinish, param);
	}

	/**
	 * 剧情时使用的默认结束
	 */
	public void setDefaultFinish(CombatObject stageObj){
		setFinish(stageObj,0);
	}

}
