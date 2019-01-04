package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.support.JsonKey;

public class RuleEnemyLifePct extends CombatRuleBase {
	private long totalHp = 0;// 总血量

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleEnemyLifePct(int id, String value) {
		super(id);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.EnemyLifePct;
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
		stageObj.addStepAction(CombatStepType.StepStart, this);
		stageObj.addStepAction(CombatStepType.RoundOrderEnd, this);
		stageObj.addStepAction(CombatStepType.CombatEnd, this);
		// 重置副本进度
		stageObj.exParam.put(JsonKey.InstPercent, 0);
	}
	
	/**
	 * 移除行为
	 * @param isNotify 是否发出触发事件
	 */
	@Override
	public void ActionFinish(CombatObject stageObj,boolean isNotify) {
		stageObj.addStepAction(CombatStepType.StepStart, this);
		stageObj.addStepAction(CombatStepType.RoundOrderEnd, this);
		stageObj.addStepAction(CombatStepType.CombatEnd, this);
	}

	/**
	 * 执行场景阶段行为
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(CombatObject stageObj) {
		switch (stageObj.stageStep) {
		case StepStart: {
			this.totalHp = stageObj.getTeamTotalMaxHp(ETeamType.Team2);
		}	return;
		case RoundOrderEnd:
			setFinishData(stageObj);
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
		int percent = Utils.I100;
		if (this.totalHp > 0) {
			long nowHp = stageObj.getTeamTotalCurHp(ETeamType.Team2);
			percent = (int) (Utils.D100 - nowHp * Utils.D100 / this.totalHp);
		}
		stageObj.exParam.put(JsonKey.InstPercent, percent);
	}
	
}
