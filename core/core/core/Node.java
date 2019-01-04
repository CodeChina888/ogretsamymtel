package core;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import core.Call;
import core.InputStream;
import core.OutputStream;
import core.Port;
import core.RemoteNode;
import core.RemoteNodeDaemon;
import core.interfaces.IThreadCase;
import core.support.BufferPool;
import core.support.CallSeamMethodKey;
import core.support.Config;
import core.support.Distr;
import core.support.SeamServiceBase;
import core.support.ThreadHandler;
import core.support.TickTimer;
import core.support.log.LogCore;

public final class Node implements IThreadCase {
	private final ThreadHandler thread; // 线程管理类

	private String partId = "";// 内部ID
	private final String id; // Node名称
	private final String addr; // Node地址
	private final ConcurrentMap<String, Port> ports = new ConcurrentHashMap<>(); // 下属Port
	private final ConcurrentMap<String, RemoteNode> remoteNodes = new ConcurrentHashMap<>(); // 连接的远程Node

	private final ZContext zmqContext; // ZMQ上下文
	private final ZMQ.Socket zmqPull; // ZMQ连接

	private long timeCurrent; // 当前时间戳
	private volatile boolean start; // 是否开启
	private final TickTimer remoteNodePulseTimer = new TickTimer(RemoteNode.INTERVAL_PING); // 远程Node调用定时器

	/**
	 * 构造函数
	 * @param id
	 * @param addr
	 */
	public Node(String id, String addr) {
		this.id = id;
		this.addr = addr;

		this.zmqContext = new ZContext();
		this.zmqContext.setIoThreads(1);

		this.zmqPull = zmqContext.createSocket(ZMQ.PULL);
		this.zmqPull.setRcvHWM(0); // 设置接收消息高水位为0，即接收队列无限制，避免阻塞或丢失消息，默认1000
		this.zmqPull.setLinger(3000);// 为socket关闭设置停留时间，默认-1无限的停留时间，单位毫秒
		String bindAddr= addr.split("//")[0] + "//0.0.0.0:"+addr.split(":")[2];
		this.zmqPull.bind(bindAddr);

		this.start = true;
		
		thread = new ThreadHandler(this);
	}
	
	public Node(String id, String addr, String partId) {
		this(id, addr);
		this.partId = partId;
	}

	/**
	 * 开始运行
	 */
	public void startup() {
		thread.setName(toString());
		thread.startup();

		// 启动远程连接守护线程
		RemoteNodeDaemon remoteNodeDaemon = new RemoteNodeDaemon(this);
		remoteNodeDaemon.setName("RemoteNodeDaemon " + id);
		remoteNodeDaemon.start();
	}
	
	/**
	 * 关闭
	 */
	public void stop() {
		if (!start)
			return;

		// 设置为关闭状态
		start = false;

		// 清理下属port
		for (Port p : ports.values()) {
			p.stop();
		}

		// 清理远程Node
		for (RemoteNode r : remoteNodes.values()) {
			r.close();
		}

		// 关闭ZMQ
		zmqPull.close();
		zmqContext.destroy();
	}

	/**
	 * 暂停当前node，暂停node下的所有的Port 此方法只有在ClassLoader的时候才可以调用！！
	 * @throws InterruptedException
	 */
//	@Deprecated
//	public void pause() {
//		if (!start)
//			return;
//
//		// 清理下属port
//		for (Port p : ports.values()) {
//			p.pause();
//		}
//	}

	/**
	 * 恢复当前node，暂恢复node下的所有的Port 此方法只有在ClassLoader的时候才可以调用！！
	 * @throws InterruptedException
	 */
//	@Deprecated
//	public void resume() {
//		if (!start)
//			return;
//
//		// 清理下属port
//		for (Port p : ports.values()) {
//			p.resume();
//		}
//	}

	/**
	 * 心跳操作
	 */
	public void pulse() {
		if (!start)
			return;

		// 当前时间
		timeCurrent = System.currentTimeMillis();

		// 接受其他Node发送过来的Call调用
		pulseCallPuller();
		// 调用远程Node的心跳操作
		pulseRemoteNodes();
	}

	/**
	 * 接受其他Node发送过来的Call请求
	 */
	private void pulseCallPuller() {
		while (true) {
			// 可重用buff
			byte[] buf = null;
			try {
				// 申请buff
				buf = BufferPool.allocate();
				
				// 接受到的字节流长度
				// zmq是基于块传输的 所以不用考虑流切割的问题
				int recvLen = zmqPull.recv(buf, 0, buf.length, ZMQ.DONTWAIT);
				// 如果长度<=0 代表没有接到数据 本心跳接收任务结束
				if (recvLen <= 0) {
					// 长度为0 代表当前没有未处理请求 休息一下
					// Thread.sleep(1);//sjh,是否有必要休息呢？
					break;
				}
				// 处理Call请求
				callHandle(buf, recvLen);
			} catch (Exception e) {
				// 吞掉并打印异常
				LogCore.core.error("", e);
			} finally {
				// 回收buff
				BufferPool.deallocate(buf);
			}
		}
	}

	/**
	 * 调用远程Node的心跳操作
	 */
	private void pulseRemoteNodes() {
		// 检查时间间隔
		if (!remoteNodePulseTimer.isPeriod(timeCurrent)) {
			return;
		}

		// 遍历远程Node
		for (RemoteNode r : remoteNodes.values()) {
			r.pulse();
		}
	}

	/**
	 * 发送请求
	 * @param nodeId
	 * @param buffer
	 * @param bufferLength
	 */
	public void sendCall(String nodeId, byte[] buffer, int bufferLength) {
		// 同一Node下 无需走传输协议 内部直接接收即可
		if (id.equals(nodeId)) {
			callHandle(buffer, bufferLength);
		} else { // 其余的需要通过远程Node来发送请求值目标Node
			RemoteNode node = remoteNodes.get(nodeId);
			if (node != null) {
				node.sendCall(buffer, bufferLength);
			} else {
				LogCore.remote.error("发送Call请求时，发现未知远程节点：id={},nodeId={}", id, nodeId);
			}
		}
	}

	/**
	 * 发送请求
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
			sendCall(call.to.nodeId, out.getBuffer(), out.getLength());
		} finally {
			// 关闭回收
			if (out != null)
				out.close();
		}
	}

	/**
	 * 通过名称来获取Port
	 * @param portId
	 * @return
	 */
	public Port getPort(String portId) {
		return ports.get(portId);
	}

	/**
	 * 添加Port
	 * @param port
	 */
	public void addPort(Port port) {
		ports.put(port.getId(), port);
	}

	/**
	 * 删除Port
	 * @param port
	 */
	public void delPort(Port port) {
		ports.remove(port.getId());
	}

	/**
	 * 添加远程Node
	 * @param id
	 * @param addr
	 */
	public RemoteNode addRemoteNode(String id, String addr) {
		// 创建远程Node并与本Node相连
		RemoteNode remote = RemoteNode.createActive(this, id, addr);
		addRemoteNode(remote);
		return remote;
	}

	/**
	 * 添加被动远程Node
	 * @param id
	 * @param addr
	 */
	private RemoteNode addRemoteNodeInactive(String id, String addr) {
		// 创建远程Node并与本Node相连
		RemoteNode remote = RemoteNode.createInactive(this, id, addr);
		addRemoteNode(remote);
		return remote;
	}

	/**
	 * 添加远程Node
	 */
	private void addRemoteNode(RemoteNode remoteNode) {
		if (remoteNode != null) {
			remoteNode.ping();// add by sjh,添加远程Node，马上进行ping连接测试
			// 创建远程Node并与本Node相连
			remoteNodes.put(remoteNode.getRemoteId(), remoteNode);
			// 日志
			LogCore.core.info("======建立{}远程连接：[{}-->{}], remoteNode={}", 
					(remoteNode.isMain() ? "主动" : "被动"), getId(), remoteNode.getRemoteId(), remoteNode);
		}
	}

	/**
	 * 删除远程Node
	 * @param name
	 */
	public void delRemoteNode(String name) {
		RemoteNode node = remoteNodes.remove(name);
		if (node != null) {
			node.close();
		}
	}

	/**
	 * 获取所有远程Node
	 */
	public Collection<RemoteNode> getRemoteNodeAll() {
		return remoteNodes.values();
	}

	/**
	 * 远程Node是否已连接
	 * @param remoteNodeName
	 * @return
	 */
	public boolean isRemoteNodeConnected(String remoteNodeName) {
		RemoteNode n = remoteNodes.get(remoteNodeName);
		if (n == null)
			return false;

		return n.isActive();
	}

	/**
	 * 处理Call请求
	 * @param buf
	 * @param len
	 */
	private void callHandle(byte[] buf, int len) {
		// 转化为输出流
		InputStream input = new InputStream(buf, 0, len);
		// 是否已读取到末尾
		while (!input.isAtEnd()) {
			// 先读取一个Call请求
			Call call = input.read();
			// 处理接收到的Call请求
			callHandle(call);
		}
	}

	/**
	 * 处理接收到的Call请求
	 */
	public void callHandle(Call call) {
		// 检查秘钥
		if (!StringUtils.equals(call.secretKey, Config.CORE_SECRET_KEY)) {
			LogCore.remote.error("错误的RPC秘钥错误，已忽略本消息，请检查配置和安全隐患：errorKey={}", call.secretKey);
			return;
		}

		// 根据请求类型来分别处理
		switch (call.type) {
			case Call.TYPE_RPC : {// PRC远程调用请求
				// 日志记录
				// if(LogCore.remote.isDebugEnabled()) {
				// LogCore.remote.debug("接收到RPC请求：call={}", call);
				// }

				// 请求分发
				Port port = ports.get(call.to.portId);
				if (port == null) {
					LogCore.remote.info("接收到RPC请求后，未能找到合适的接收者：call={}", call);
				} else {
					port.addCall(call);
				}
			}	break;
			case Call.TYPE_RPC_RETURN : {// PRC远程调用请求的返回值
				// 日志记录
				// if(LogCore.remote.isDebugEnabled()) {
				// LogCore.remote.debug("接收到RPC返回结果：call={}", call);
				// }

				// 请求分发
				Port port = ports.get(call.to.portId);
				if (port == null) {
					LogCore.remote.info("接收到RPC返回值后，未能找到合适的接收者：call={}", call);
				} else {
					port.addCallResult(call);
				}
			}	break;
			case Call.TYPE_SEAM : {// 系统整合调用
				// 日志记录
				// if(LogCore.remote.isDebugEnabled()) {
				// LogCore.remote.debug("接收到MSG消息：call={}", call);
				// }

				// 请求分发
				String portId =Config.getGameWorldPartDefaultPortId();
				if(Config.isCrossSrv){
					portId = Config.getCrossPartDefaultPortId();
				}
				Port port = ports.get(portId);
				if (port == null) {
					LogCore.remote.info("接收到MSG消息，未能找到合适的接收者：call={}", call);
				} else {
					// 整合函数类型
					CallSeamMethodKey methodKey = CallSeamMethodKey.values()[call.methodKey];
					SeamServiceBase serv = port.getService(Distr.SERV_SEAM);
					if (serv != null) {
						serv.handler(methodKey, call);
					}
				}
			}	break;
			case Call.TYPE_PING : {// 连接检测
				// 根据请求者的名称来获取远程Node
				RemoteNode remote = remoteNodes.get(call.fromNodeId);
				if (remote == null) {// 第一次收到，则添加远程Node，并创建ping请求连接测试
					// 第一次收到连接检测 反向增加一个对方的远程Node
					String name = (String) call.methodParam[0];
					String addr = (String) call.methodParam[1];
					boolean active = (boolean) call.methodParam[2];
					if (active) {// 只有对方是主动连接，才建立被动连接
						remote = addRemoteNodeInactive(name, addr);// 建立被动连接
					}
				}
				if (remote != null) {// 非第一次收到，则创建pong返回连接测试
					remote.pong();
				}
			}	break;
			case Call.TYPE_PONG : {// 连接检测
				// 根据请求者的名称来获取远程Node
				RemoteNode remote = remoteNodes.get(call.fromNodeId);
				if (remote == null) {
					// 未建立连接
					LogCore.remote.warn("[{}]收到远程Node连接检查反馈时，发现未与对方创建连接。remoteId={}", id, call.fromNodeId);
				} else {
					// 处理pong返回连接测试
					remote.pongHandle();
				}
			}	break;
		}
	}

	public String getPartId() {
		if("".equals(partId)) {
			return id;
		}
		return partId;
	}
	
	public String getId() {
		return id;
	}

	public String getAddr() {
		return addr;
	}

	public long getTimeCurrent() {
		return timeCurrent;
	}

	@Override
	public void caseRunOnce() {
		pulse();
	}

	@Override
	public void caseStart() {

	}

	@Override
	public void caseStop() {

	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
				.append("addr", getAddr()).append("partId", getPartId()).toString();
	}
}