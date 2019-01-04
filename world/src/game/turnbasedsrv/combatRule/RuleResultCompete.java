package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.ETeamType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;

public class RuleResultCompete extends CombatRuleBase {
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleResultCompete(int id,String value) {
		super(id);

	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.ResultCompete;
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
		if (stageObj.stageStep == CombatStepType.CombatEnd && stageObj.round >= stageObj.roundMax) {
			stageObj.winTeam = ETeamType.Team2;
			return;
		}
		int count1 = 0;
		int count2 = 0;
		for (FightObject obj : stageObj.getFightObjs().values()) {
			if (obj.team == ETeamType.Team1) {
				count1 = count1 + 1;
			} else {
				count2 = count2 + 2;
			}
		}
		if (count1 == 0) {
			stageObj.winTeam = ETeamType.Team2;
		} else if (count2 == 0) {
			stageObj.winTeam = ETeamType.Team1;
		} else {
			stageObj.winTeam = ETeamType.Team2;
		}
	}

}
