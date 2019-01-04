package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.ETeamType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.worldsrv.support.Utils;

public class RuleRoundLimit extends CombatRuleBase {
	/** 回合数 **/
	int round;

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleRoundLimit(int id,String value) {
		super(id);
		round = Utils.intValue(value);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.RoundLimit;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("round", round).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(CombatObject stageObj) {
		stageObj.addStageFinishCheck(this);
		stageObj.addStepAction(CombatStepType.CombatEnd, this);
	}

	/**
	 * 判断战斗是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStageFinish(CombatObject stageObj) {
		if (stageObj.stageStep == CombatStepType.RoundEnd && stageObj.round > this.round) {
			return true;
		}
		return false;
	}
	
	/**
	 * 执行场景阶段行为
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
	 * 触发结束
	 * @param stageObj
	 */
	private void setFinishData(CombatObject stageObj) {
		if (stageObj.stageStep == CombatStepType.CombatEnd && stageObj.round > this.round) {
			stageObj.winTeam = ETeamType.Team2;
		}
	}

}
