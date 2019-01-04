package game.turnbasedsrv.trigger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.function.GofFunction1;
import game.turnbasedsrv.enumType.TriggerPoint;

public class TriggerListen {
	/** 触发点 **/
	public TriggerPoint triggerPoint;
	public int idFight;// 所属对象的临时id
	/** 触发执行函数 **/
	GofFunction1<Trigger> excuteFunc;

	/**
	 * 构造函数
	 * 
	 * @param triggerPoint
	 *            触发点
	 *            条件参数
	 * @param excuteFunc
	 *            触发执行函数
	 */
	public TriggerListen(TriggerPoint triggerPoint, int idFight, GofFunction1<Trigger> excuteFunc) {
		this.triggerPoint = triggerPoint;
		this.idFight = idFight;
		this.excuteFunc = excuteFunc;
	}

	/**
	 * 执行触发
	 * 
	 */
	public void excuteTrigger(Trigger trigger) {
		excuteFunc.apply(trigger);
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
