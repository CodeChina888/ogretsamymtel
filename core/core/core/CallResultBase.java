package core;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.Call;

public abstract class CallResultBase {
	private final long callId; // 对应的请求ID
	private final long timeout; // 请求过期时间

	/**
	 * 处理返回值
	 * @param call
	 */
	public abstract void onResult(Call call);

	/**
	 * 等待返回值超时 进行后续处理
	 */
	public abstract void onTimeout();

	/**
	 * 构造函数
	 */
	public CallResultBase(long callId, long timeoutDelay) {
		this.callId = callId;
		this.timeout = System.currentTimeMillis() + timeoutDelay;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("callId", getCallId())
					.toString();
	}

	/**
	 * 是否超时
	 * @return
	 */
	public boolean isTimeout() {
		return timeout < System.currentTimeMillis();
	}

	/**
	 * 获取监听的CallId
	 * @return
	 */
	public long getCallId() {
		return callId;
	}

}
