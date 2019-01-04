package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.support.JsonKey;

public class RuleStarRoundLimit extends CombatRuleBase {
	/** 得星位置 **/
	String starIndex;
	/** 回合数 **/
	int round;

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleStarRoundLimit(int id,String value) {
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
		return CombatRuleDefine.StarRoundLimit;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("starIndex", starIndex)
				.append("round", round).toString();
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
		if (stageObj.round > round) {
			return;
		}
		Boolean star = true;
		stageObj.exParam.put(starIndex, star);
	}

}
