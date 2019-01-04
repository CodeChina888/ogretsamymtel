package core;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import core.Call;
import core.CallPoint;
import core.Node;
import core.OutputStream;
import core.RemoteNode;
import core.support.TickTimer;
import core.support.Time;
import core.support.Utils;
import core.support.log.LogCore;

public class RemoteNode {
	public static final long INTERVAL_PING = 6 * Time.SEC; // 连接检测时间间隔 6秒
	public static final long INTERVAL_LOST = 20 * Time.SEC; // 连接丢失时间间隔 20秒

	// 定时器
	private final TickTimer pingTimer = new TickTimer(INTERVAL_PING);
	private final TickTimer disconnTimer = new TickTimer(INTERVAL_PING);

	private final String remoteId; // 远程Node名称
	private final String remoteAddr; // 远程Node地址
	private final Node localNode; // 本地Node名称

	private final ZContext zmqContext; // ZMQ上下文
	private final ZMQ.Socket zmqPush; // ZMQ连接

	// 连接创建时间
	private long createTime;
	// 最后连接检查反馈时间
	private long pongTime;
	// 是否连接上
	private boolean connected;

	// 是否为主动连接（主动连接超时关闭后会重新建立连接，被动则不会）
	private boolean main;

	/**
	 * 构造函数
	 * @param localNode
	 * @param remoteName
	 * @param remoteAddr
	 */
	public RemoteNode(Node localNode, String remoteName, String remoteAddr) {
		this.localNode = localNode;
		this.remoteId = remoteName;
		this.remoteAddr = remoteAddr;

		this.createTime = System.currentTimeMillis();
		this.pongTime = createTime;

		this.zmqContext = new ZContext();
		// this.zmqContext.setIoThreads(1);// 设置I/O线程的个数，默认1

		this.zmqPush = zmqContext.createSocket(ZMQ.PUSH);
		this.zmqPush.setSndHWM(0); // 设置发送消息高水位为0，即发送队列无限制，避免阻塞或丢失消息，默认1000
		this.zmqPush.setLinger(3000);// 为socket关闭设置停留时间，默认-1无限的停留时间，单位毫秒
		this.zmqPush.setReconnectIVL(2000);// 设置重连间隔，默认100，单位毫秒
		this.zmqPush.setReconnectIVLMax(5000);// 设置重连间隔的最大值，默认0，单位毫秒
		this.zmqPush.setBacklog(1000);// 设置最大链接队列长度
		this.zmqPush.connect(remoteAddr);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("remoteId", remoteId).append("remoteAddr", remoteAddr)
				.append("connected", connected).append("main", main)
				.append("createTime", Utils.formatTime(createTime, "yyyy-MM-dd HH:mm:ss"))
				.append("rogerTime", Utils.formatTime(pongTime, "yyyy-MM-dd HH:mm:ss")).toString();
	}

	/**
	 * 创建主动连接
	 * @param localNode
	 * @param remoteName
	 * @param remoteAddr
	 * @return
	 */
	public static RemoteNode createActive(Node localNode, String remoteName, String remoteAddr) {
		RemoteNode r = new RemoteNode(localNode, remoteName, remoteAddr);
		r.main = true;
		return r;
	}

	/**
	 * 创建被动连接
	 * @param localNode
	 * @param remoteName
	 * @param remoteAddr
	 * @return
	 */
	public static RemoteNode createInactive(Node localNode, String remoteName, String remoteAddr) {
		RemoteNode r = new RemoteNode(localNode, remoteName, remoteAddr);
		r.main = false;
		return r;
	}

	/**
	 * 心跳操作
	 */
	public void pulse() {
		// 当前时间
		long timeCurr = localNode.getTimeCurrent();

		// 到达间隔时间后 进行连接检测
		pulsePing(timeCurr);

		// 活跃状态下 长时间没收到心跳检测 那么就认为连接已丢失
		pulseDisconn(timeCurr);
	}

	/**
	 * 活跃状态下 长时间没收到心跳检测 那么就认为连接已丢失
	 */
	private void pulseDisconn(long timeCurr) {
		if (!isActive())
			return;
		if (!disconnTimer.isPeriod(timeCurr))
			return;
		if ((timeCurr - pongTime) < INTERVAL_LOST)
			return;

		connected = false;
		LogCore.remote.error("fromNodeId={}失去远程Node的连接：remote={},remoteId={},remoteAddr={}", localNode.getId(), this,
				remoteId, remoteAddr);
	}
	
	/**
	 * 进行周期连接测试
	 */
	private void pulsePing(long timeCurr) {
		if (!pingTimer.isPeriod(timeCurr))
			return;
		
		ping();// 进行ping连接测试
	}
	
	/**
	 * 创建ping请求连接测试
	 */
	public void ping() {
		Call call = new Call();
		call.type = Call.TYPE_PING;
		call.fromNodeId = localNode.getId();
		call.to.nodeId = remoteId;
		call.methodParam = new Object[]{localNode.getId(), localNode.getAddr(), main};
		call.immutable = true;
		// 发送
		sendCall(call);
	}

	/**
	 * 创建pong返回连接测试
	 */
	public void pong() {
		Call call = new Call();
		call.type = Call.TYPE_PONG;
		call.fromNodeId = localNode.getId();
		call.to = new CallPoint(remoteId, null, null);
		// 发送
		sendCall(call);
	}

	/**
	 * 处理pong返回连接测试
	 */
	public void pongHandle() {
		// 非活跃的情况下收到连接测试
		if (!isActive()) {
			// 设置为已连接状态
			connected = true;
			LogCore.remote.info("======成功建立连接：[{}-->{}], remoteNode={}", 
					localNode.getId(), getRemoteId(), this);
		}
		
		// 设置最后心跳检查反馈时间
		pongTime = localNode.getTimeCurrent();
	}

	public String getRemoteId() {
		return remoteId;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public long getPongTime() {
		return pongTime;
	}

	public String getLocalId() {
		return localNode.getId();
	}

	/**
	 * 是否为活跃状态
	 * @return
	 */
	public boolean isActive() {
		return connected;
	}

	public boolean isMain() {
		return main;
	}

	/**
	 * 关闭
	 */
	public void close() {
		synchronized (zmqPush) {
			zmqContext.destroy();
		}
	}

	/**
	 * 发送调用请求
	 * @param call
	 */
	public void sendCall(Call call) {
		// 输出流
		OutputStream out = null;
		try {
			// 创建输出流并写入
			out = new OutputStream();
			out.write(call);

			// 发送消息
			sendCall(out.getBuffer(), out.getLength());
		} finally {
			// 关闭回收
			if (out != null) {
				out.close();
				out = null;
			}
		}
	}

	/**
	 * 发送调用请求 zmq内部不是线程安全的，必须做同步发送。
	 */
	public void sendCall(byte[] buf, int size) {
		synchronized (zmqPush) {
			zmqPush.send(buf, 0, size, 0);
		}
	}

}
