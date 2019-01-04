package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.stage.FightStageObject;

public class CondRoundLimit extends StageCondBase {
	private int param;// 回合数

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondRoundLimit(int id, String value) {
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
		return StageCondDefine.RoundLimit;
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
	public void init(FightStageObject stageObj) {
		// 增加地图结束检测行为配置
		stageObj.addStageFinishCheck(this);
	}

	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStageFinish(FightStageObject stageObj) {
		if (stageObj.stageStep == StageStep.RoundEnd && stageObj.round >= this.param) {
			return true;
		}
		return false;
	}

}
