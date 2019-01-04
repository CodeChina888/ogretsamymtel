package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.ETeamType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.stage.FightStageObject;

public class CondResultCompete extends StageCondBase {
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondResultCompete(int id, String value) {
		super(id);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.ResultCompete;
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
		if (stageObj.stageStep == StageStep.RoundEnd && stageObj.round >= stageObj.roundMax) {
			stageObj.winTeam = ETeamType.Team2;
			return;
		}
		// 计算战队人数
		int count1 = 0;
		int count2 = 0;
		for (FightObject obj : stageObj.getFightObjs().values()) {
			if (obj.team == ETeamType.Team1) {
				count1++;
			} else {
				count2++;
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
