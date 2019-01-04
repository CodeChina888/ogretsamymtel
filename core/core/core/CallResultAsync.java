package core;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.Call;
import core.CallResultBase;
import core.statistics.StatisticsRPC;
import core.support.Config;
import core.support.Param;
import core.support.SysException;
import core.support.function.GofFunction2;
import core.support.function.GofFunction3;

/**
 * 异步接收返回值 配合port.listenResult()使用
 */
public class CallResultAsync extends CallResultBase {
	// private final Object resultObject; //接受返回值的对象
	private final GofFunction2<Param, Param> resultMethod; // 接受返回值的函数
	private final GofFunction3<Boolean, Param, Param> resultMethodTimeout;
	private final Param context; // 上下文环境
	//private final String callerInfo; //调用者信息

	/**
	 * 构造函数
	 * @param callId
	 * @param resultMethod
	 * @param context
	 */
	public CallResultAsync(long callId, long timeoutDelay, /*Object resultObject,*/
			GofFunction2<Param, Param> resultMethod, Param context) {
		super(callId, timeoutDelay);

		// this.resultObject = resultObject;
		this.resultMethod = resultMethod;
		this.resultMethodTimeout = null;
		this.context = context;
		
//		if(context.containsKey("_callerInfo")) {
//			this.callerInfo = context.get("_callerInfo");
//		} else {
//			this.callerInfo = "Unknown";
//		}
	}

	public CallResultAsync(long callId, long timeoutDelay, /*Object resultObject,*/
			GofFunction3<Boolean, Param, Param> resultMethod, Param context) {
		super(callId, timeoutDelay);

		// this.resultObject = resultObject;
		this.resultMethod = null;
		this.resultMethodTimeout = resultMethod;
		this.context = context;
		
//		if(context.containsKey("_callerInfo")) {
//			this.callerInfo = context.get("_callerInfo");
//		} else {
//			this.callerInfo = "Unknown";
//		}
	}
		
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("callId", getCallId())
					.append("resultMethod", resultMethod)
					.append("resultMethodTimeout", resultMethodTimeout)
					.append("context", context.toString())
					//.append("callInfo", callerInfo)
					.toString();
	}

	/**
	 * 处理返回值
	 * @param call
	 */
	@Override
	public void onResult(Call call) {
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;

		// 3个参数的时候 第一个参数是 超时标志 如果需要处理超时情况 那么使用此函数
		if (resultMethodTimeout != null) {
			resultMethodTimeout.apply(false, call.returns, context);

			if (start > 0)
				StatisticsRPC.rst(resultMethodTimeout.getClass().toString(), System.nanoTime() - start);

		} else { // 2个参数的时候 只有成功时才会被调用
			resultMethod.apply(call.returns, context);

			if (start > 0)
				StatisticsRPC.rst(resultMethod.getClass().toString(), System.nanoTime() - start);
		}
	}

	/**
	 * 返回值超时 进行后续处理
	 */
	@Override
	public void onTimeout() {
		// 超时的时候 通知函数请求超时
		try {
			// 当拥有3个参数的时候 第一个参数是 超时标志 如果需要处理超时情况 那么使用此函数
			// 如果只拥有2个参数 那么超时时不会被调用
			if (resultMethodTimeout != null) {
				resultMethodTimeout.apply(true, new Param(), context);
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

}
