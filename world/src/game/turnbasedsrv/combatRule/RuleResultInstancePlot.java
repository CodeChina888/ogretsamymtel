package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.ETeamType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.support.JsonKey;

public class RuleResultInstancePlot extends CombatRuleBase {
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleResultInstancePlot(int id,String value) {
		super(id);

	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.ResultInstancePlot;
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
		stageObj.addStepAction(CombatStepType.CombatEnd, this);
		stageObj.exParam.put(JsonKey.Star, 3);
	}

	/**
	 * 执行操作
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(CombatObject stageObj) {
		switch (stageObj.stageStep) {
		case CombatEnd:
			setFinishData(stageObj);
			return;
		default:
			break;

		}
	}

	/**
	 * 设置战斗结果
	 * 
	 * @param stageObj
	 */
	private void setFinishData(CombatObject stageObj) {
		stageObj.winTeam = ETeamType.Team1;
		stageObj.exParam.put(JsonKey.Star1, true);
		stageObj.exParam.put(JsonKey.Star2, true);
		stageObj.exParam.put(JsonKey.Star3, true);
		stageObj.exParam.put(JsonKey.Star, 3);
	}

}
