package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.stage.FightStageObject;

public class CondFriendDieLimit extends StageCondBase {
	private int param;// 参数：人数
	private int total = 0;// 总人数

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondFriendDieLimit(int id, String value) {
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
		return StageCondDefine.FriendDieLimit;
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
	public void init(FightStageObject stageObj) {
		// 增加地图结束检测行为配置
		stageObj.addStageFinishCheck(this);
		stageObj.addStepAction(StageStep.StageStepStart, this);
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
			// 开场时记录我方总数
			total = stageObj.getTeamCount(ETeamType.Team1);
		}	return;
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
	public boolean checkStageFinish(FightStageObject stageObj) {
		if (stageObj.stageStep == StageStep.RoundEnd) {
			int alive = stageObj.getTeamCount(ETeamType.Team1);
			int die = total - alive;
			if (die > param) {
				return true;
			}
		}
		return false;
	}

}
