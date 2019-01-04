package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.support.JsonKey;

public class CondEnemyLifePct extends StageCondBase {
	private long totalHp = 0;// 总血量

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondEnemyLifePct(int id, String value) {
		super(id);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.EnemyLifePct;
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
	public void init(FightStageObject stageObj) {
		stageObj.addStepAction(StageStep.StageStepStart, this);
		stageObj.addStepAction(StageStep.RoundOrderEnd, this);
		stageObj.addStepAction(StageStep.StageEnd, this);
		// 重置副本进度
		stageObj.exParam.put(JsonKey.InstPercent, 0);
	}
	
	/**
	 * 移除行为
	 * @param isNotify 是否发出触发事件
	 */
	@Override
	public void ActionFinish(FightStageObject stageObj,boolean isNotify) {
		stageObj.addStepAction(StageStep.StageStepStart, this);
		stageObj.addStepAction(StageStep.RoundOrderEnd, this);
		stageObj.addStepAction(StageStep.StageEnd, this);
	}

	/**
	 * 执行场景阶段行为
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(FightStageObject stageObj) {
		switch (stageObj.stageStep) {
		case StageStepStart: {
			this.totalHp = stageObj.getTeamTotalMaxHp(ETeamType.Team2);
		}	return;
		case RoundOrderEnd:
			setFinishData(stageObj);
			return;
		case StageEnd:
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
	private void setFinishData(FightStageObject stageObj) {
		int percent = Utils.I100;
		if (this.totalHp > 0) {
			long nowHp = stageObj.getTeamTotalCurHp(ETeamType.Team2);
			percent = (int) (Utils.I100 - nowHp * Utils.I100 / this.totalHp);
		}
		stageObj.exParam.put(JsonKey.InstPercent, percent);
	}
	
}
