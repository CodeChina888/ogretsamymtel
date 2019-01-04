package core;

import core.Port;
import core.support.Param;

public abstract class PortPulseQueue {
	/**
	 * 任务执行
	 * @param port
	 */
	public abstract void execute(Port port);

	// 上下文环境
	public final Param param;

	/**
	 * 构造函数
	 */
	public PortPulseQueue() {
		this.param = new Param();
	}

	/**
	 * 构造函数 可传递上下文参数
	 */
	public PortPulseQueue(Object... params) {
		this.param = new Param(params);
	}
}