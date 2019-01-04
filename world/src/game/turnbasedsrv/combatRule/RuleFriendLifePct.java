package game.turnbasedsrv.combatRule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.combat.CombatObject;

public class RuleFriendLifePct extends CombatRuleBase {
	private int param;// 参数：剩余生命万分比
	private long total = 0;// 总血量

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleFriendLifePct(int id, String value) {
		super(id);
		param = Utils.intValue(value);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.FriendLifePct;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("param", param).toString();
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
		stageObj.addStepAction(CombatStepType.StepStart, this);
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
			// 开场时记录我方总数
			this.total = stageObj.getTeamTotalMaxHp(ETeamType.Team1);
		}	return;
		case CombatEnd:
			setFinishData(stageObj);
			return;
		default:
			break;
		}
	}

	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStageFinish(CombatObject stageObj) {
		if (stageObj.stageStep == CombatStepType.RoundEnd) {
			long pct = Utils.L10000;
			if (this.total > 0) {
				long nowHp = stageObj.getTeamTotalCurHp(ETeamType.Team1);
				pct = nowHp * pct / this.total;
			}
			if (pct < param) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 触发结束
	 * @param stageObj
	 */
	private void setFinishData(CombatObject stageObj) {
		long pct = Utils.L10000;
		if (this.total > 0) {
			long nowHp = stageObj.getTeamTotalCurHp(ETeamType.Team1);
			pct = nowHp * pct / this.total;
		}
		// 比值小于则队伍胜利
		if (pct < param) {
			stageObj.winTeam = ETeamType.Team2;
		}
	}

}
