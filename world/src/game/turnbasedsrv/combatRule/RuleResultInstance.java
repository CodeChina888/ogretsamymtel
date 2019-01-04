package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Param;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.support.JsonKey;

public class RuleResultInstance extends CombatRuleBase {
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleResultInstance(int id,String value) {
		super(id);

	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.ResultInstance;
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
		// 副本可能存在多场景，重置场景时，用于记录星级的exParam数据要被重置
		stageObj.exParam = new Param();
		stageObj.exParam.put(JsonKey.Star, 0);
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
		}
		// 得星计算
		int starCount = 1;
		for (int i = 1; i <= 3; i++) {
			String index = JsonKey.Star + i;
			Boolean star = stageObj.exParam.get(index);
			if (star != null && star) {
				starCount++;
			}
		}
		stageObj.exParam.put(JsonKey.Star, starCount);
	}

}
