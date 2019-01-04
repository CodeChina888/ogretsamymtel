package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.ETeamType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.support.JsonKey;

public class CondResultInstancePlot extends StageCondBase {
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondResultInstancePlot(int id,String value) {
		super(id);

	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.ResultInstancePlot;
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
		stageObj.exParam.put(JsonKey.Star, 3);
	}

	/**
	 * 执行操作
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
		stageObj.winTeam = ETeamType.Team1;
		stageObj.exParam.put(JsonKey.Star1, true);
		stageObj.exParam.put(JsonKey.Star2, true);
		stageObj.exParam.put(JsonKey.Star3, true);
		stageObj.exParam.put(JsonKey.Star, 3);
	}

}
