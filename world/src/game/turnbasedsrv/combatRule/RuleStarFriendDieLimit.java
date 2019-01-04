package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.support.JsonKey;

public class RuleStarFriendDieLimit extends CombatRuleBase {
	/** 得星位置 **/
	String starIndex;
	/** 总人数 **/
	int totalCount = 0;
	/** 人数 **/
	int limitCount;

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleStarFriendDieLimit(int id,String value) {
		super(id);
		limitCount = Utils.intValue(value);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.StarFriendDieLimit;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("starIndex", starIndex)
				.append("totalCount", totalCount).append("limitCount", limitCount).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(CombatObject stageObj) {
		for (int i = 1; i <= 3; i++) {
			String index = JsonKey.Star + i;
			Boolean star = stageObj.exParam.get(index);
			if (star == null) {
				star = false;
				stageObj.exParam.put(index, star);
				starIndex = index;
				break;
			}
		}
		if (starIndex != null) {
			stageObj.addStepAction(CombatStepType.StepStart, this);
			stageObj.addStepAction(CombatStepType.CombatEnd, this);
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
		case StepStart:
			int count = 0;
			for (FightObject obj : stageObj.getFightObjs().values()) {
				if (obj.team == ETeamType.Team1) {
					count = count + 1;
				}
			}
			totalCount = count;
			return;
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
		int count = 0;
		for (FightObject obj : stageObj.getFightObjs().values()) {
			if (obj.team == ETeamType.Team1) {
				count = count + 1;
			}
		}
		int dieCount = totalCount - count;
		if (dieCount > limitCount) {
			return;
		}
		Boolean star = true;
		stageObj.exParam.put(starIndex, star);
	}

}
