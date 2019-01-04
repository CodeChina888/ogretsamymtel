package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.ETeamType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;

public class RuleFriendHumanDie extends CombatRuleBase {
	private boolean param;// 参数：是否死亡

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleFriendHumanDie(int id, String value) {
		super(id);
		param = true; //Utils.booleanValue(value);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.FriendHumanDie;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("param", param).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(CombatObject stageObj) {
		// 增加地图结束检测行为配置
		stageObj.addStageFinishCheck(this);
		stageObj.addStepAction(CombatStepType.CombatEnd, this);
	}
	
	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStageFinish(CombatObject stageObj) {
		if (stageObj.stageStep == CombatStepType.CombatWaitFighter || stageObj.stageStep == CombatStepType.CombatStart) {
			return false;
		}
		if (stageObj.stageStep == CombatStepType.RoundEnd && stageObj.round >= stageObj.roundMax) {
			return true;
		}
		
		boolean isDie = stageObj.isDieTeamHuman(ETeamType.Team1);
		if (isDie == param) {
			return true;
		} else {
			return false;
		}
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
		boolean isDie = stageObj.isDieTeamHuman(ETeamType.Team1);
		if (stageObj.stageStep == CombatStepType.CombatEnd && isDie == param) {
			stageObj.winTeam = ETeamType.Team2;
		}
	}

}
