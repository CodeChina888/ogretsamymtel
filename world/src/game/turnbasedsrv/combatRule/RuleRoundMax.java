package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.turnbasedsrv.combat.CombatObject;
import game.worldsrv.support.Utils;

public class RuleRoundMax extends CombatRuleBase {
	/** 回合数 **/
	int round;

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleRoundMax(int id,String value) {
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
		stageObj.roundMax = round;
	}

}
