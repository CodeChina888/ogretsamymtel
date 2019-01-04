package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import core.Call;
import core.CallPoint;
import core.CallPulseBuffer;
import core.CallResultAsync;
import core.CallResultBase;
import core.CallResultSync;
import core.CallReturn;
import core.Node;
import core.Port;
import core.PortPulseQueue;
import core.Service;
import core.connsrv.ConnPort;
import core.entity.EntityBase;
import core.interfaces.IThreadCase;
import core.statistics.StatisticsRPC;
import core.support.Config;
import core.support.Param;
import core.support.StepWatch;
import core.support.SysException;
import core.support.ThreadHandler;
import core.support.TickTimer;
import core.support.Utils;
import core.support.function.*;
import core.support.idAllot.IdAllotPoolBase;
import core.support.log.LogCore;

public abstract class Port implements IThreadCase {
	// 默认异步请求都是30秒过期
	public static final int DEFAULT_TIMEOUT = 60 * 1000;

	// 当前线程的Port实例
	private static final ThreadLocal<Port> portCurrent = new ThreadLocal<>();

	// 线程管理类
	private final ThreadHandler thread;

	// 所属Node
	private Node node;
	// Port名称
	private final String portId;

	// 当前线程开始时间(毫秒)
	private long timeCurrent = System.currentTimeMillis();// modify by shenjh//0
	// 发出的最后一个请求ID号
	//private long sendLastCallId = 0;
	private AtomicLong sendLastCallId = new AtomicLong(1l);

	// 下属服务
	private final Map<Object, Service> services = new ConcurrentHashMap<>();

	// 正在处理中的Call请求 利用LinkedList来模拟栈
	private final LinkedList<Call> callHandling = new LinkedList<>();
	// 接收到待处理的请求
	private final ConcurrentLinkedQueue<Call> calls = new ConcurrentLinkedQueue<>();
	// 接收到的请求返回值
	private final ConcurrentLinkedQueue<Call> callResults = new ConcurrentLinkedQueue<>();
	// 本次心跳需要处理的请求
	private final List<Call> pulseCalls = new ArrayList<>();
	// 本次心跳需要处理的请求返回值
	private final List<Call> pulseCallResults = new ArrayList<>();
	// 待处理执行队列
	private final ConcurrentLinkedQueue<PortPulseQueue> pulseQueues = new ConcurrentLinkedQueue<>();
	// 记录心跳中被修改过的实体对象
	private final Set<EntityBase> pulseEntityModify = new HashSet<>();
	// 心跳时间秒表
	private final StepWatch pulseStepWatch = new StepWatch(LogCore.statis.isDebugEnabled());

	// 请求返回值监听
	private final Map<Long, CallResultBase> callResultListener = new ConcurrentHashMap<>();//HashMap
	// 请求返回值监听超时计时器
	private final TickTimer callReusltTimeoutCleanTimer = new TickTimer(1000);
	// 记录异步返回需要的原始Call的信息
	private final Map<Long, CallReturn> callReturnAsync = new HashMap<>();
	// 异步返回已经分配出去的最大ID
	//private long callReturnAsyncId = 0;
	private AtomicLong callReturnAsyncId = new AtomicLong(1l);

	// 任务队列调度器
	public Scheduler scheduler;

	// 远程请求RPC缓冲区
	private final Map<String, CallPulseBuffer> callFrameBuffers = new HashMap<>();

	// 本类是否连接Port
	private final boolean isConnPort = this instanceof ConnPort;

	// 非玩家ID分配池
	private IdAllotPoolBase idPool = initIdAllotPool();

	/**
	 * 构造函数
	 * @param portId
	 */
	public Port(String portId) {
		this.portId = portId;
		this.thread = new ThreadHandler(this);
	}
	public Port(String portId, int interval) {
		this.portId = portId;
		this.thread = new ThreadHandler(this, interval);
	}

	/**
	 * 获取当前线程的Port实例
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Port> T getCurrent() {
		return (T) portCurrent.get();
	}

	/**
	 * 获取系统时间
	 * @return
	 */
	public static long getTime() {
		return getCurrent().getTimeCurrent();
	}

	/**
	 * 当前线程开始时间(毫秒)
	 * @return
	 */
	public long getTimeCurrent() {
		return timeCurrent;
	}

	/**
	 * 申请一个可用的非玩家流水ID
	 * @return
	 */
	public static long applyId() {
		return getCurrent().applySeqId();
	}

	/**
	 * 开始
	 * @param node
	 */
	public void startup(Node node) {
		// 设置与Node的关系
		this.node = node;
		this.node.addPort(this);

		// 初始化quartz相关环境
		try {
			this.scheduler = new StdSchedulerFactory().getScheduler();
			this.scheduler.start();
		} catch (Exception e) {
			throw new SysException(e);
		}

		// 启动独立线程
		this.thread.setName(toString());
		this.thread.startup();

		// 日志
		LogCore.core.info("启动Port={}", this);
	}
	
	/**
	 * 结束
	 */
	public void stop() {
		if (thread == null)
			return;

		// 解除与Node的关系
		this.node.delPort(this);
		this.node = null;

		// 停止独立线程
		this.thread.cleanup();
	}

	/**
	 * 暂停当前的port线程 目前只是给Node来调用，ClassLoader时使用
	 */
//	@Deprecated
//	final void pause() {
//		if (thread == null)
//			return;
//
//		thread.pauseT();
//	}

	/**
	 * 恢复当前线程 目前只是给Node来调用，ClassLoader时使用
	 */
//	@Deprecated
//	final void resume() {
//		if (thread == null)
//			return;
//
//		thread.resumeT();
//	}

	/**
	 * 心跳操作
	 */
	public final void pulse() {
		// 记录下心跳开始时的时间戳 供之后的操作来统一时间
		timeCurrent = System.currentTimeMillis();

		// 确认本心跳要执行的call及result
		pulseCallAffirm();
		
		// 记录一些日志调试信息
		int countCall = -1;
		int countResult = -1;
		int countQueue = -1;
		if(LogCore.statis.isDebugEnabled()) {
			countCall = pulseCalls.size();
			countResult = pulseCallResults.size();
			countQueue = pulseQueues.size();
			//if(countCall > 1)
			//	LogCore.statis.warn("callCount={}, resultCount={}, countQueue={}",
			//			countCall, countResult, countQueue);
		}
		
		// 计时开始
		StepWatch sw = pulseStepWatch;
		sw.step();
				
		/* 执行本心跳的任务 */
		// 处理Call请求
		pulseCalls();
		sw.logTime("tmCalls");

		// 处理Call请求返回
		pulseCallResults();
		sw.logTime("tmResults");

		// 清理超时的返回值监听
		pulseCallResultsTimeoutClean();
		sw.logTime("tmResultsClean");

		// 调用下属服务
		pulseSerivces();
		sw.logTime("tmSerivces");

		// 调用port子类的心跳操作
		try {
			pulseOverride();
		} catch (Exception e) {
			LogCore.core.error("执行子类pulseOverride方法错误", e);
		}
		sw.logTime("tmOverride");

		// 执行等待任务队列
		pulseQueue();
		sw.logTime("tmQueue");

		// 提交本次心跳被修改过的实体
		pulseEntityModifyUpdate();
		sw.logTime("tmEntityUpdate");

		// 刷新远程调用RPC缓冲区
		flushCallFrameBuffers();
		sw.logTime("tmflushBuffers");

		// 统计心跳间隔，记录超时心跳
		long timeFinish = System.currentTimeMillis();
		if (timeFinish - timeCurrent >= Config.STATIS_PULSE_INTERVAL) {
			LogCore.statis.warn("本次心跳操作总时间较长，达到了{}毫秒：portId={}, countCall={}, countResult={}, countQueue={}, timeLog={}",
					timeFinish - timeCurrent, this.portId, countCall, countResult, countQueue, sw.getLog(true));
		}
	}

	/**
	 * 确认本心跳要执行的call及result
	 */
	private void pulseCallAffirm() {
		// 本心跳要执行的call
		while (!calls.isEmpty()) {
			pulseCalls.add(calls.poll());
		}

		// 本心跳要执行的callResult
		while (!callResults.isEmpty()) {
			pulseCallResults.add(callResults.poll());
		}
	}

	/**
	 * 心跳中处理Call请求
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void pulseCalls() {
		while (!pulseCalls.isEmpty()) {
			// 因为下面的try中需要与出栈入栈配合 所以这句就不放在try中了
			Call call = pulseCalls.remove(0);

			try {
				// 压入栈 记录正在处理的Call请求
				// 一般情况下无用 为了应对特殊情况 比如下面的invoke操作中有waitForResult操作
				callHandling.addLast(call);

				// 执行Call请求
				Service serv = getService(call.to.servId);
				if (serv == null) {
					//LogCore.core.warn("执行Call队列时无法找到处理服务：call={}", call);
				} else {
					Object f = serv.getMethodFunction(call.methodKey);
					Object[] m = call.methodParam;

					// 开启统计
					long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;

					switch (call.methodParam.length) {
						case 0 :
							((GofFunction0) f).apply();
							break;
						case 1 :
							((GofFunction1) f).apply(m[0]);
							break;
						case 2 :
							((GofFunction2) f).apply(m[0], m[1]);
							break;
						case 3 :
							((GofFunction3) f).apply(m[0], m[1], m[2]);
							break;
						case 4 :
							((GofFunction4) f).apply(m[0], m[1], m[2], m[3]);
							break;
						case 5 :
							((GofFunction5) f).apply(m[0], m[1], m[2], m[3], m[4]);
							break;
						case 6 :
							((GofFunction6) f).apply(m[0], m[1], m[2], m[3], m[4], m[5]);
							break;
						case 7 :
							((GofFunction7) f).apply(m[0], m[1], m[2], m[3], m[4], m[5], m[6]);
							break;
						case 8 :
							((GofFunction8) f).apply(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7]);
							break;
						case 9 :
							((GofFunction9) f).apply(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
							break;
						case 10 :
							((GofFunction10) f).apply(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9]);
							break;
						case 11 :
							((GofFunction11) f)
									.apply(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10]);
							break;
						case 12 :
							((GofFunction12) f).apply(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9],
									m[10], m[11]);
							break;
						default :
							break;
					}

					if (start > 0)
						StatisticsRPC.rpc(Utils.createStr("{}${}", serv.getClass().toString(), call.methodKeyName),
								System.nanoTime() - start);
				}
			} catch (Exception e) {
				// 不做任何处理 仅仅记录异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.core.error("执行Call队列时发生错误：call={}", call, e);
			} finally {
				// 请求处理完毕 记录出栈
				callHandling.removeLast();
			}
		}
	}

	/**
	 * 心跳中处理Call请求返回
	 */
	private void pulseCallResults() {
		while (!pulseCallResults.isEmpty()) {
			try {
				Call call = pulseCallResults.remove(0);
				
				// 处理返回值
				CallResultBase listener = callResultListener.remove(call.id);
				if (listener != null) {
					listener.onResult(call);
				}// else {
					//LogCore.core.error("处理Call返回值时未发现接受对象：call={},port={},node={}", 
					//		call, this, getNode());
				//}
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.core.error("pulseCallResults() Exception:{}", ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * 清理超时的返回值监听
	 */
	private void pulseCallResultsTimeoutClean() {
		// 间隔一段时间清理一次
		if (!callReusltTimeoutCleanTimer.isPeriod(timeCurrent)) {
			return;
		}

		// 超时的返回值
		List<CallResultBase> timeoutResult = new ArrayList<>();
		for (CallResultBase r : callResultListener.values()) {
			if (!r.isTimeout())
				continue;

			timeoutResult.add(r);
		}

		long count = timeoutResult.size();
		// 删除超时的监听
		for (Iterator<CallResultBase> iter = timeoutResult.iterator(); iter.hasNext();) {
			CallResultBase r = iter.next();
			
			// 删除监听
			callResultListener.remove(r.getCallId());
			try {
				// 执行清理
				r.onTimeout();
			} catch (Exception e) {
				LogCore.core.error("callResult超时清理异常：r={}", r.toString(), e);
			}

			// 日志
			LogCore.core.error("发现超时的返回值监听：callResult={}, port={}, node={}", r, this, getNode());
		}
		
		// 日志
		if (count > 0)
			LogCore.core.error("发现超时的返回值监听：timeoutResult={}, port={}, node={}", count, this, getNode());
	}

	/**
	 * 心跳中执行等待任务队列
	 */
	private void pulseQueue() {
		while (!pulseQueues.isEmpty()) {
			try {
				PortPulseQueue msg = pulseQueues.poll();
				msg.execute(this);
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.core.error("pulseQueue() Exception:{}", ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * 调用下属服务的心跳操作 默认启动本操作 如果子Port不想自动调用可以覆盖本函数
	 */
	protected void pulseSerivces() {
		for (Service o : services.values()) {
			try {
				o.pulse();
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.core.error("pulseSerivces() Exception:{}", ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * 提交本次心跳被修改过的实体
	 */
	private void pulseEntityModifyUpdate() {
		// 遍历提交
		for (Iterator<EntityBase> iter = pulseEntityModify.iterator(); iter.hasNext();) {
			try {
				// 获取并从队列中删除 避免由于错误造成线程阻塞
				EntityBase e = iter.next();
				iter.remove();

				// 执行更新提交
				e.update();
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.core.error("pulseEntityModifyUpdate() Exception:{}", ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * 发起一个远程调用RPC请求
	 * @param toPoint
	 * @param methodKey
	 * @param methodParam
	 */
	public void call(CallPoint toPoint, int methodKey, Object[] methodParam) {
		call(false, toPoint, methodKey, "", methodParam);
	}
	
	/**
	 * 发起一个远程调用RPC请求
	 * @param immutable 是否不变的，同NODE间传递可设为true以提高性能
	 * @param toPoint
	 * @param methodKey
	 * @param methodParam
	 */
//	public void call(boolean immutable, CallPoint toPoint, int methodKey, Object[] methodParam) {
//		call(immutable, toPoint, methodKey, "", methodParam);
//	}
	
	/**
	 * 发起一个远程调用RPC请求
	 * @param immutable 是否不变的，同NODE间传递可设为true以提高性能
	 * @param toPoint
	 * @param methodKey
	 * @param methodKeyName
	 * @param methodParam
	 */
	public void call(boolean immutable, CallPoint toPoint, int methodKey, String methodKeyName, Object[] methodParam) {
		// 拼装请求
		Call call = new Call();
		call.id = applyCallId();

		call.methodKey = methodKey;
		call.methodKeyName = methodKeyName;
		call.methodParam = methodParam;

		call.fromNodeId = node.getId();
		call.fromPortId = portId;

		call.to = toPoint;
		call.immutable = immutable;

		// 消息类型
		if (isConnPort)
			call.type = Call.TYPE_SEAM; // 连接服务器 都是整合模式
		else
			call.type = Call.TYPE_RPC; // 默认处理方式

		if (StringUtils.isBlank(call.to.nodeId)) {// sjh
			LogCore.core.warn("Port.call：call.to={}, call={}", call.to.toString(), call);
		}
		// 发送
		sendCall(call);
	}
	
	/**
	 * 框架整合RPC请求调用（已弃用）
	 * @param fromId
	 * @param toPoint
	 * @param methodKey
	 * @param methodParam
	 */
	// public void callSeam(CallPoint toPoint, Object[] methodParam, Object...
	// params) {
	// Call call = new Call();
	// call.id = applyCallId();
	// call.type = Call.TYPE_SEAM;
	//
	// call.methodParam = methodParam;
	//
	// call.fromNodeId = node.getId();
	// call.fromPortId = portId;
	//
	// call.to = toPoint;
	//
	// call.param = new Param(params);
	//
	// if (StringUtils.isBlank(call.to.nodeId)) {// sjh
	// LogCore.core.warn("Port.callSeam：call.to={}, call={}",
	// call.to.toString(), call);
	// }
	// sendCall(call);
	// }

	/**
	 * 发起一个远程调用RPC请求
	 */
	private void sendCall(Call call) {
		// 目标点Node名称
		String toNodeId = call.to.nodeId;
		String localNodeId = node.getId();

		// 肯定错误的nodeId就忽略了
		if (StringUtils.isBlank(toNodeId)) {
			LogCore.core.warn("发送Call请求失败，错误的NodeId：call={}", call);
			return;
		}

		// 本Node直接发送
		if (localNodeId.equals(toNodeId)) {
			node.callHandle(call.immutable ? call : call.deepClone());

		} else {// 其余Node的请求先进行缓冲
			// 目标点的请求缓冲
			CallPulseBuffer buffer = callFrameBuffers.computeIfAbsent(toNodeId, k -> new CallPulseBuffer(toNodeId));

			// 将要发送内容放入发送缓冲中
			// 先尝试写入 如果失败(一般都是缓冲剩余空间不足)则先清空缓冲 后再尝试写入
			// 如果还是失败 那证明有可能是发送内容过大 不进行缓冲 直接发送
			if (!buffer.writeCall(call)) {
				// 日志 第一次尝试写入缓冲失败
				LogCore.core.warn("第一次尝试写入缓冲失败：bufferLen={}", buffer.getLength());

				// 刷新缓冲区
				buffer.flush(node);
				// 再次尝试写入缓冲
				if (!buffer.writeCall(call)) {
					// 日志 第二次尝试写入缓冲失败
					LogCore.core.warn("第二次尝试写入缓冲失败：bufferLen={}", buffer.getLength());

					node.sendCall(call);
				}
			}
		}
	}

	/**
	 * 刷新远程调用RPC缓冲区
	 */
	private void flushCallFrameBuffers() {
		for (CallPulseBuffer frameCache : callFrameBuffers.values()) {
			try {
				frameCache.flush(node);
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.core.error("flushCallFrameBuffers() Exception:{}", ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * 每次心跳可以进行的定制操作 供继承Port的各子类实现
	 */
	protected void pulseOverride() {
		// 默认是空实现 啥也不做
	}

	/**
	 * 添加待处理请求
	 * @param call
	 */
	public void addCall(Call call) {
		calls.add(call);
	}

	/**
	 * 添加待处理请求返回值
	 * @param call
	 */
	public void addCallResult(Call call) {
		callResults.add(call);
	}

	/**
	 * 添加延后队列任务
	 * @param msg
	 */
	public void addQueue(PortPulseQueue msg) {
		pulseQueues.add(msg);
	}

	/**
	 * 记录被修改的实体对象
	 */
	public void addEntityModify(EntityBase entity) {
		pulseEntityModify.add(entity);
	}

	/**
	 * 获取服务
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Service> T getService(Object id) {
		return (T) services.get(id);
	}

	/**
	 * 添加新服务
	 */
	public void addService(Service service) {
		services.put(service.getId(), service);
	}

	/**
	 * 删除服务
	 */
	public void delService(Object id) {
		Service serv = services.get(id);
		try {
			serv.deleteSchedulerJobsByGroup(id.toString());
		} catch (SchedulerException e) {
			LogCore.core.error("删除service时，清空该service的scheduler出错", e);
		}
		LogCore.core.info("删除服务NodeId={},PortId={},service={}", getNodeId(), getId(), serv.getId());
		services.remove(id);
	}

	/**
	 * 安全删除服务
	 */
	public void delServiceBySafe(Object id) {
		// 避免由于删除服务，造成心跳内后续操作报错，所以将实际删除工作延后至下一心跳
		this.addQueue(new PortPulseQueue(id) {
			public void execute(Port port) {
				Service serv = services.get(param.get());
				if (serv == null)
					return;

				// 将添加在service上的定时任务清除掉
				try {
					serv.deleteSchedulerJobsByGroup(param.get().toString());
				} catch (SchedulerException e) {
					LogCore.core.error("删除service时，清空该service的scheduler出错", e);
				}

				port.services.remove(param.get());
			}
		});
	}

	/**
	 * 监听请求返回值
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object... context) {
		listenResult(method, new Param(context));
	}

	public void listenResult(GofFunction3<Boolean, Param, Param> methodTimeout, Object... context) {
		listenResult(methodTimeout, new Param(context));
	}

	/**
	 * 监听请求返回值
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		// 加入一个异步监听
		//context.put("_callerInfo", Utils.getCallerStack());	// 加入调用堆栈信息
		CallResultBase crb = new CallResultAsync(sendLastCallId.get(), DEFAULT_TIMEOUT, method, context);
		callResultListener.put(crb.getCallId(), crb);
	}

	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		// 加入一个异步监听
		//context.put("_callerInfo", Utils.getCallerStack()); // 加入调用堆栈信息
		CallResultBase crb = new CallResultAsync(sendLastCallId.get(), DEFAULT_TIMEOUT, method, context);
		callResultListener.put(crb.getCallId(), crb);
	}

	/**
	 * 同步等待请求返回值 仅供系统启动等极少数场景使用 会阻塞进程执行 一般情况下不要调用
	 * @param timeout
	 * @return
	 */
	public Param waitForResult(long timeout) {
		// 先主动将请求发出去
		flushCallFrameBuffers();

		// 加入监听队列
		CallResultSync crs = new CallResultSync(sendLastCallId.get(), timeout);
		callResultListener.put(crs.getCallId(), crs);

		try {
			// 等待请求返回或超时
			while (!crs.isCompleted() && !crs.isTimeout()) {
				// 主动使请求返回值生效
				pulseCallAffirm();
				pulseCalls();//sjh0427屏蔽
				pulseCallResults();
				flushCallFrameBuffers();
				// 延迟10毫秒进行下一次检查
				Thread.sleep(10);
			}
		} catch (Exception e) {
			LogCore.core.error("waitForResult() Exception:{}", ExceptionUtils.getStackTrace(e));
		}

		// 删除监听
		callResultListener.remove(crs.getCallId());

		return crs.getResults();
	}

	/**
	 * 同步等待请求返回值 仅供系统启动等极少数场景使用 会阻塞进程执行 一般情况下不要调用
	 * @return
	 */
	public Param waitForResult() {
		return waitForResult(DEFAULT_TIMEOUT);
	}

	/**
	 * 创建一个异步返回
	 * @return
	 */
	public long createReturnAsync() {
		Call call = callHandling.getLast();

		// 记录异步返回
		long pid = applyRetrunAsyncId();
		callReturnAsync.put(pid, call.createCallReturn());

		return pid;
	}

	/**
	 * 申请一个异步返回ID
	 * @return
	 */
	private long applyRetrunAsyncId() {
		//return ++callReturnAsyncId;
		if(callReturnAsyncId.get() == Long.MAX_VALUE)
			callReturnAsyncId.set(0l);
		return callReturnAsyncId.incrementAndGet();
	}

	/**
	 * 发送请求返回值
	 * @param values
	 */
	public void returns(Object... values) {
		Call call = getCall();

		returns(false, call.createCallReturn(), new Param(values));
	}

	/**
	 * 发送请求返回值
	 * @param callReturn
	 * @param values
	 */
	public void returns(CallReturn callReturn, Object... values) {
		returns(false, callReturn, new Param(values));
	}

	/**
	 * 发送异步请求返回值
	 * @param rid
	 * @param values
	 */
	public void returnsAsync(long rid, Object... values) {
		CallReturn callReturn = callReturnAsync.remove(rid);

		returns(false, callReturn, new Param(values));
	}

	/**
	 * 发送请求返回值 同Node间通信将不在进行Call对象克隆，可极大提高性能。<br/>
	 * 但由于没进行克隆操作，接发双方都可对同一对象进行操作，可能会引起错误。<br/>
	 * *由于有危险性，并且大多数时候RPC成本不高，建议只有业务中频繁调用或参数克隆成本较高时才使用本函数。<br/>
	 * *当接发双方仅有一方会对通信参数进行处理时，哪怕参数中有可变类型，也可以调用本函数进行优化。<br/>
	 * *当接发双方Node不相同时，本参数无效，双方处理不同对象；<br/>
	 * 当接发双方Node相同时，双方处理相同对象，这种差异逻辑会对分布式应用带来隐患，要小心使用。 <br/>
	 * @param values
	 */
	public void returnsImmutable(Object... values) {
		Call call = getCall();

		returns(true, call.createCallReturn(), new Param(values));
	}

	/**
	 * 发送请求返回值 同Node间通信将不在进行Call对象克隆，可极大提高性能。<br/>
	 * 但由于没进行克隆操作，接发双方都可对同一对象进行操作，可能会引起错误。<br/>
	 * *由于有危险性，并且大多数时候RPC成本不高，建议只有业务中频繁调用或参数克隆成本较高时才使用本函数。<br/>
	 * *当接发双方仅有一方会对通信参数进行处理时，哪怕参数中有可变类型，也可以调用本函数进行优化。<br/>
	 * *当接发双方Node不相同时，本参数无效，双方处理不同对象；<br/>
	 * 当接发双方Node相同时，双方处理相同对象，这种差异逻辑会对分布式应用带来隐患，要小心使用。 <br/>
	 * @param callReturn
	 * @param values
	 */
	public void returnsImmutable(CallReturn callReturn, Object... values) {
		returns(true, callReturn, new Param(values));
	}

	/**
	 * 发送异步请求返回值 同Node间通信将不在进行Call对象克隆，可极大提高性能。<br/>
	 * 但由于没进行克隆操作，接发双方都可对同一对象进行操作，可能会引起错误。<br/>
	 * *由于有危险性，并且大多数时候RPC成本不高，建议只有业务中频繁调用或参数克隆成本较高时才使用本函数。<br/>
	 * *当接发双方仅有一方会对通信参数进行处理时，哪怕参数中有可变类型，也可以调用本函数进行优化。<br/>
	 * *当接发双方Node不相同时，本参数无效，双方处理不同对象；<br/>
	 * 当接发双方Node相同时，双方处理相同对象，这种差异逻辑会对分布式应用带来隐患，要小心使用。 <br/>
	 * @param rid
	 * @param values
	 */
	public void returnsImmutableAsync(long rid, Object... values) {
		CallReturn callReturn = callReturnAsync.remove(rid);

		returns(true, callReturn, new Param(values));
	}

	/**
	 * 发送请求返回值
	 */
	private void returns(boolean immutable, CallReturn callReturn, Param values) {
		Call call = new Call();
		call.id = callReturn.id;
		call.type = Call.TYPE_RPC_RETURN;
		call.fromNodeId = node.getId();
		call.fromPortId = portId;
		call.to = new CallPoint(callReturn.nodeId, callReturn.portId, null);
		call.returns = values;
		call.immutable = immutable;

		// 发送
		sendCall(call);
	}

	/**
	 * 申请一个新的请求ID
	 * @return
	 */
	private long applyCallId() {
		//return ++sendLastCallId;
		if(sendLastCallId.get() == Long.MAX_VALUE)
			sendLastCallId.set(0l);
		return sendLastCallId.incrementAndGet();
	}

	/**
	 * 获得当前Call
	 */
	public Call getCall() {
		return callHandling.getLast();
	}

	/**
	 * 获得当前Call请求的发送者结点NodeId
	 */
	public String getCallFromNodeId() {
		return callHandling.getLast().fromNodeId;
	}

	/**
	 * 获得当前Call请求的发送者结点PortId
	 */
	public String getCallFromPortId() {
		return callHandling.getLast().fromPortId;
	}

	/**
	 * 申请一个可用的流水ID
	 * @return
	 */
	public long applySeqId() {
		// 未初始化id池
		if (idPool == null) {
			throw new SysException("本Port未实现或初始化ID池失败，请覆盖实现initIdAllotPool函数：port={}", this);
		}

		return idPool.applyId();
	}

	/**
	 * 申请一个可用的流水玩家ID
	 * @return
	 */
	public long applySeqHumanId() {
		// 未初始化id池
		if (idPool == null) {
			throw new SysException("本Port未实现或初始化ID池失败，请覆盖实现initIdAllotPool函数：port={}", this);
		}
		
		return idPool.applyHumanId();
	}
	
	/**
	 * 获取10的5次方的游戏区编号
	 * @return
	 */
	public long getGameArea_pow5() {
		// 未初始化id池
		if (idPool == null) {
			throw new SysException("本Port未实现或初始化ID池失败，请覆盖实现initIdAllotPool函数：port={}", this);
		}

		return idPool.getGameArea_pow5();
	}

	/**
	 * 初始化ID分配池
	 * @return
	 */
	protected IdAllotPoolBase initIdAllotPool() {
		return null;
	}

	@Override
	public void caseStart() {
		portCurrent.set(this);
	}

	@Override
	public void caseStop() {
		portCurrent.set(null);
	}

	@Override
	public void caseRunOnce() {
		pulse();
	}

	public Node getNode() {
		return node;
	}

	public String getNodeId() {
		return node.getId();
	}

	public String getId() {
		return portId;
	}

	public int getCallSize() {
		return calls.size();
	}

	public int getPulseCallSize() {
		return pulseCalls.size();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("nodeId", getNodeId())
				.append("portId", getId()).toString();
	}
}
