package turnbasedsrv.trigger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import turnbasedsrv.enumType.TriggerPoint;
import turnbasedsrv.param.TriggerParam;

public class Trigger {
	/** 触发点 **/
	public TriggerPoint triggerPoint;
	/** 触发参数 **/
	public TriggerParam triggerParam;

	/**
	 * 构造函数
	 * 
	 * @param triggerPoint
	 *            触发点
	 * @param paramCondition
	 *            条件参数
	 * @param paramExcute
	 *            执行参数
	 */
	public Trigger(TriggerPoint triggerPoint, TriggerParam triggerParam) {
		this.triggerPoint = triggerPoint;
		this.triggerParam = triggerParam;
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
}
