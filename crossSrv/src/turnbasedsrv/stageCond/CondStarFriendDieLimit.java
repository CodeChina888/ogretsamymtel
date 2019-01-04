package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.support.JsonKey;

public class CondStarFriendDieLimit extends StageCondBase {
	private String starIndex;// 得星位置
	private int param;// 参数：人数
	private int total = 0;// 总人数
	
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondStarFriendDieLimit(int id, String value) {
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
		return StageCondDefine.StarFriendDieLimit;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("starIndex", starIndex)
				.append("param", param).append("total", total).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(FightStageObject stageObj) {
		for (int i = 2; i <= 3; i++) {
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
			stageObj.addStepAction(StageStep.StageStepStart, this);
			stageObj.addStepAction(StageStep.StageEnd, this);
		}
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
		int alive = stageObj.getTeamCount(ETeamType.Team1);
		int die = total - alive;
		if (die <= param) {
			stageObj.exParam.put(starIndex, true);
		} else {
			stageObj.exParam.put(starIndex, false);
		}
	}

}
