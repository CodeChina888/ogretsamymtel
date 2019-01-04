package turnbasedsrv.stageEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import turnbasedsrv.stage.FightStageObject;

public class EventCastSkill extends StageEventBase {
	/** 间隔值 **/
	long leftTime;

	public EventCastSkill(long leftTime) {
		this.leftTime = leftTime;
	}

	/**
	 * 获取属性值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageEventDefine.CastSkill;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("leftTime", leftTime).toString();
	}

	/**
	 * 是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean isFinish(FightStageObject stageObj) {
		if (this.leftTime <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 心跳时间
	 * 
	 * @param deltaTime
	 * @return
	 */
	@Override
	public int pulse(FightStageObject stageObj, int deltaTime) {
		if (leftTime >= deltaTime) {
			this.leftTime = this.leftTime - deltaTime;
			return 0;
		} else {
			int left = (int) (deltaTime - this.leftTime);
			this.leftTime = 0;
			stageObj.removeStageEvent(this);
			return left;
		}
	}

	/**
	 * 剧情时使用的默认结束
	 */
	public void setDefaultFinish(FightStageObject stageObj) {
		this.leftTime = 0;
	}
}
