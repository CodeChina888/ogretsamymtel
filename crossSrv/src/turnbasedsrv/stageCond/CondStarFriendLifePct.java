package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.support.JsonKey;

public class CondStarFriendLifePct extends StageCondBase {
	private String starIndex;// 得星位置
	private int param;// 参数：剩余生命万分比
	private long total = 0;// 总血量

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondStarFriendLifePct(int id, String value) {
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
		return StageCondDefine.StarFriendLifePct;
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
			this.total = stageObj.getTeamTotalMaxHp(ETeamType.Team1);
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
		long pct = Utils.L10000;
		if (this.total > 0) {
			long nowHp = stageObj.getTeamTotalCurHp(ETeamType.Team1);
			pct = nowHp * pct / this.total;
		}
		if (pct >= param) {
			stageObj.exParam.put(starIndex, true);
		} else {
			stageObj.exParam.put(starIndex, false);
		}
	}

}
