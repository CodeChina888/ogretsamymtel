package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.support.JsonKey;

public class RuleStarFriendHumanDie extends CombatRuleBase {
	private String starIndex;// 得星位置
	private boolean param;// 参数：是否死亡
	
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleStarFriendHumanDie(int id, String value) {
		super(id);
		param = Utils.booleanValue(value);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.StarFriendHumanDie;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("starIndex", starIndex)
				.append("param", param).toString();
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
	 * 执行场景阶段行为
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
		boolean isDie = stageObj.isDieTeamHuman(ETeamType.Team1);
		if (isDie == param) {
			stageObj.exParam.put(starIndex, true);
		} else {
			stageObj.exParam.put(starIndex, false);
		}
	}

}
