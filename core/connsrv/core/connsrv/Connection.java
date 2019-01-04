package core.connsrv;

import game.msg.MsgAccount;
import game.msg.MsgCommon;
import game.msg.MsgIds;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.Chunk;
import core.Port;
import core.PortPulseQueue;
import core.Service;
import core.connsrv.ConnPort;
import core.connsrv.Connection;
import core.connsrv.ConnectionBuf;
import core.connsrv.ConnectionProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleTask;
import core.statistics.StatisticsMSG;
import core.support.CallSeamMethodKey;
import core.support.Config;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.Param;
import core.support.SysException;
import core.support.TickTimer;
import core.support.Time;
import core.support.Utils;
import core.support.log.LogCore;

@DistrClass(importClass = {Chunk.class, ConnectionStatus.class, List.class})
public class Connection extends Service {
	// 默认Gate阶段游戏服务器的CallPoint地址
	private static CallPoint pointGate = null;//new CallPoint(Distr.NODE_DEFAULT, Distr.PORT_DEFAULT, Distr.SERV_GATE);

//	//待发送数据刷新阀值
//	private static final int SEND_FLUSH_LENGTH = Config.CONN_PORT*1024/2;
//	//待发送数据
//	private final List<Object[]> sendList = new ArrayList<>();
//	//待发送数据大小
//	private int sendLen = 0;
//	//发送头大小
//	private int SND_HEAD_SIZE = 8;
	//连接Port
	private ConnPort connPort;
		
	protected static int connNum = 0; // 连接数
	protected long connId = 0;
	protected ConnectionStatus connStatus = new ConnectionStatus();
	protected boolean m_closed = false;
	protected boolean m_delayCloseRequested = false;
	protected boolean m_waitResponse = false;
	protected TickTimer m_waitTimer = new TickTimer();
	protected TickTimer m_closeTimer = new TickTimer();

	protected final Channel channel;
	protected final LinkedBlockingQueue<byte[]> datas = new LinkedBlockingQueue<>();

	/* 连接验证，检测和世界服WorldSrv的连接是否正常 */
	public static long MAX_TIMES_NO_CONN_CHECK = 3L; // 连接检查超时次数
	public int timesNoConnCheck = 0; // 累计连续未收到返回的次数
	public TickTimer tickTimerConnCheck = new TickTimer(3 * Time.SEC); // 连接检查计时器，设置间隔3秒检测一次

	/* 连接验证，检测和客户端Client的连接是否正常 */
	public static long MAX_TIMES_NO_CLIENT_PINK = 3L;// 定义服务端没有收到客户端的ping请求的最大次数
	public int timesNoClientPink = 0;// 掉线计数器：未收到client端发送的ping请求

	public static long MAX_TIMES_ERR_CLIENT_PINK = 3L;// 定义服务端收到客户端的ping请求间隔太短的最大次数
	public int timesErrClientPink = 0;// 错误计数器：收到client端发送的ping请求间隔太短
	public long timeClientPinkLast = 0;// 记录上一次client端发送的ping请求的时间戳

	// 消息缓存队列
	ConnectionBuf conBuf = new ConnectionBuf();

//	// ping的响应
//	public static int PING_MSGID;
//	// ping的返回状态
//	public static int ECHO_PING_MSGID;
//	public static Chunk ECHO_PING;
//	// 是否忽略ping
//	public static boolean IGNORE_PING;
//	private long lastPingTime = 0;
//	private static long pingTimeOut = 10 * Time.MIN;
	
	static {
		// Window下认为是开发环境 避免Debug造成的超时断开连接 这里加大检查次数
		if (Config.DEBUG_ENABLE) {// && Sys.isWin()
			MAX_TIMES_NO_CONN_CHECK = Long.MAX_VALUE;// 服务端调试时，不检查断线
			MAX_TIMES_NO_CLIENT_PINK = Long.MAX_VALUE;// 客户端调试时，不检查断线
		}
	}
		
	public Connection(Channel channel, Port port) {
		super(port);
		connPort = (ConnPort)port;
		connId = getId(channel);
		this.channel = channel;
		if(pointGate == null) {
			pointGate = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), 
					Config.getGameWorldPartDefaultPortId(), Distr.SERV_GATE);
		}
		connStatus.clientIP = getClientIP(channel);
	}
	
	public Connection(Channel channel, Port port, String nodeId, String portId, String servId) {
		super(port);
		connPort = (ConnPort)port;
		connId = getId(channel);
		this.channel = channel;
		if(pointGate == null) {
			pointGate = new CallPoint(nodeId, portId, servId);
		}
		connStatus.clientIP = getClientIP(channel);
	}
	public static String getClientIP(Channel channel) {
		InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
		return  insocket.getAddress().getHostAddress();
	}

	/**
	 * FIXME 获取连接ID（等Netty5恢复channel.id()函数前，先用这个暂代）
	 * @param channel
	 * @return
	 */
	public static int getId(Channel channel) {
		return channel.hashCode();
	}

	public void putDate(byte[] data) {
		try {
			int msgId = Utils.bytesToInt(data, 4);
			if (null == MsgIds.getClassById(msgId)) {
				LogCore.conn.error("====过滤无效消息：no find msgId={}", msgId);
				return;
			}
			if (msgId != MsgIds.CSMsgPing) {// 非心跳协议的处理
				datas.put(data);
				// 信息统计
				StatisticsMSG.recevice(data);
			} else {// CSMsgPing心跳包之请求连接验证
				// if(LogCore.conn.isDebugEnabled()) {
				// LogCore.conn.debug("====客户端心跳请求：msg={}:{}, remoteAddress={}",
				// msgId, MsgIds.getNameById(msgId), channel.remoteAddress());
				// }
				// LogCore.conn.debug("====心跳包：重置错误心跳次数为0，timesNoClientPink={},address={}",
				// timesNoClientPink,
				// channel.remoteAddress());
				timesNoClientPink = 0;// 重置错误心跳次数为0
				long timeCur = port.getTimeCurrent();
				if (timeClientPinkLast == 0) {
					timeClientPinkLast = timeCur;
				} else {// 检查和上次心跳的时间差，太短的话则认为是加速挂，连续三次则踢下线
					long timeGap = timeCur - timeClientPinkLast;
					if (timeGap <= (Config.CONN_PING_SECOND / 2 * Time.SEC)) {
						timesErrClientPink++;
						if (timesErrClientPink >= Connection.MAX_TIMES_ERR_CLIENT_PINK) {
							// 踢加速挂（测试）
							MsgCommon.SCHumanKick.Builder msgKick = MsgCommon.SCHumanKick.newBuilder();
							msgKick.setReason(99);// 您已被踢下线，请勿使用加速工具！
							sendMsg(MsgIds.SCHumanKick, new Chunk(msgKick));
							LogCore.conn.info("===踢加速挂：错误计数>={}，address={}", Connection.MAX_TIMES_ERR_CLIENT_PINK, this.channel.remoteAddress());
							this.close();// 关闭连接
						}
					} else {
						timesErrClientPink = 0;
					}
				}
				
				sendMsg(MsgIds.SCMsgPong, new Chunk(MsgAccount.SCMsgPong.newBuilder()));// SCMsgPong心跳包之返回连接验证
			}
		} catch (InterruptedException e) {
			throw new SysException(e);
		}
	}

	@DistrMethod(argsImmutable=true)
	public void updateStatus(String node, String port, long stage) {
		connStatus.stageNodeId = node;
		connStatus.stagePortId = port;
	}

	@DistrMethod(argsImmutable=true)
	public void updateStatus(ConnectionStatus status) {
		this.handleContinue(status);
	}

	@DistrMethod(argsImmutable=true)
	public void setStatus(int status) {
		connStatus.status = status;
		this.handleContinue(connStatus);
	}

	public ConnectionStatus getStatus() {
		return connStatus;
	}
	
	/**
	 * 获取连接状态字符串信息
	 * @return
	 */
	public String getStatusString() {
		return connStatus.toString();
	}
	
	public void sendBytesWithLength(byte[] buffer) {
		if (!channel.isActive())
			return;
		if (!channel.isWritable())
			return;

		ByteBuf buf = channel.alloc().buffer(4 + buffer.length);
		buf.writeInt(buffer.length + 4);
		buf.writeBytes(buffer);
		
		//信息统计
		StatisticsMSG.send(buf);
				
		channel.writeAndFlush(buf);
	}

	/**
	 * 发送返回单个消息
	 * @param msgId
	 * @param msgbuf
	 */
	@DistrMethod(argsImmutable=true)
	public void sendMsg(int msgId, Chunk msgbuf) {
		conBuf.addMsg(msgId, msgbuf);

		if (!channel.isActive())
			return;
		if (!channel.isWritable())
			return;

		// 构造头文件数据
		ByteBuf buf = channel.alloc().buffer(8);
		buf.writeInt(msgbuf.length + 8);
		buf.writeInt(msgId);
		// Chunk类型的msgbuf肯定是protobuf直接生成的 所以buffer属性中不会有多余数据 才能这么用
		// 其余地方Chunk类不建议直接使用内部的buffer
		buf.writeBytes(msgbuf.buffer);
		
		// 写入数据
		ChannelFuture f = channel.write(buf);
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				conBuf.removeMsg();
				// LogCore.conn.info("conBuf msgId {} conBuf.idBufList.size() {} conBuf.size() {} msgbuf.length {}",
				// msgId, conBuf.idBufList.size(), conBuf.size(),
				// msgbuf.length);
			}
		});
		
		// 信息统计
		StatisticsMSG.send(buf);
				
		channel.flush();

		// 记录日志
		if (LogCore.conn.isDebugEnabled()) {
			if (msgId != MsgIds.SCStageMove && msgId != MsgIds.SCStageMoveStop && msgId != MsgIds.SCMsgFill // 填充消息
			) {// 屏蔽移动返回消息
				if (msgId != MsgIds.SCMsgPong) {// 屏蔽心跳返回消息
					LogCore.conn.debug("===服务端返回消息：msg={}:{}:[{}], accountId={}, connId={}, remoteAddress={}", msgId,
							MsgIds.getNameById(msgId), msgbuf.toString(), connStatus.accountId, connId,
							channel.remoteAddress());
				} /*
				 * else { LogCore.conn.debug(
				 * "===服务端心跳返回：msg={}:{}:[{}], account={}, connId={}, remoteAddress={}"
				 * , msgId, MsgIds.getNameById(msgId), msgbuf.toString(),
				 * connStatus.account, connId, channel.remoteAddress()); }
				 */
			}
		}
	}
	
	/**
	 * 发送返回多个消息，如广播九宫格中的同步消息，例如：同步移动等
	 * @param idList
	 * @param chunkList
	 */
	@DistrMethod(argsImmutable=true)
	public void sendMsg(List<Integer> idList, List<Chunk> chunkList) {
		if (!channel.isActive())
			return;
		if (!channel.isWritable())
			return;
		
		for (int i = 0; i < idList.size(); i++) {
			int msgId = idList.get(i);
			Chunk msgbuf = chunkList.get(i);
			// 构造头文件数据
			ByteBuf buf = channel.alloc().buffer(8);
			buf.writeInt(msgbuf.length + 8);
			buf.writeInt(msgId);
			// Chunk类型的msgbuf肯定是protobuf直接生成的 所以buffer属性中不会有多余数据 才能这么用
			// 其余地方Chunk类不建议直接使用内部的buffer
			buf.writeBytes(msgbuf.buffer);

			// 写入数据
			channel.write(buf);
			
			//信息统计
			StatisticsMSG.send(buf);
		}
		channel.flush();
		
		// 记录日志
		if (LogCore.conn.isDebugEnabled()) {
			String msg = "";
			for (int i = 0; i < idList.size(); i++) {
				int msgId = idList.get(i);
				if (msgId != MsgIds.SCStageMove && msgId != MsgIds.SCStageMoveStop) {// 屏蔽移动消息
					msg += msgId + ":" + MsgIds.getNameById(msgId) + " ";
				}
			}
			if (!msg.isEmpty()) {
				LogCore.conn.debug("====服务端返回广播消息：msg={}, accountId={}, connId={}", msg, connStatus.accountId, connId);
			}
		}
	}

	/**
	 * 初始化消息缓存 只有在掉线重连的时候才用的上
	 * @param connPoint
	 */
	@DistrMethod(argsImmutable=true)
	public void initMsgBuf(CallPoint connPoint) {
		ConnectionProxy connPrx = ConnectionProxy.newInstance(connPoint);
		connPrx.getMsgBuf();
		connPrx.listenResult(this::_result_InitMsgBuf);
	}
	private void _result_InitMsgBuf(Param results, Param context) {
		ConnectionBuf conBuf = results.get();
		this.conBuf = conBuf;
	}

	@DistrMethod(argsImmutable=true)
	public void getMsgBuf() {
		port.returns(conBuf);
	}

	/**
	 * 发送消息缓存的数据 只有在消息重新连接的时候才用的上
	 */
	@DistrMethod(argsImmutable=true)
	public void sendMsgBuf() {
		// 获得对应connect的conBuf信息
		conBuf.sendMsg(channel);
	}

	@Override
	public Object getId() {
		return connId;
	}
	
	public void startup() {
		super.startup();
		port.addQueue(new PortPulseQueue(this) {
			@Override
			public void execute(Port port) {
				ConnPort portConn = (ConnPort) port;
				portConn.openConnection(param.<Connection> get());
			}
		});
	}

	// public boolean isClose() {
	// //return ((connStatus.status == ConnectionStatus.STATUS_LOSTED) ? true :
	// false);
	// return ((m_closed || m_delayCloseRequested) ? true : false);
	// }
	/**
	 * 关闭连接
	 */
	@DistrMethod(argsImmutable=true)
	public void close() {
		// 延迟100毫秒再真正断开 有些消息可能还没传递完毕
		scheduleOnce(new ScheduleTask() {
			@Override
			public void execute() {
				// if (LogCore.conn.isDebugEnabled()) {
				// LogCore.conn.debug("Connection.close().execute()");
				// }
				handleClose();// 进行关闭处理
				if (channel.isOpen()) {
					channel.close();
				}
			}
		}, 100);
	}

	/**
	 * 延时关闭连接
	 */
	public void closeDelay() {
		if (!m_closed && !m_delayCloseRequested) {
			m_delayCloseRequested = true;
			m_closeTimer.start(1000);// 300
		}
	}

	@Override
	public void pulseOverride() {
		// 如果延时关闭已经到时间 那么就进行关闭
		if (m_delayCloseRequested && m_closeTimer.isOnce(port.getTimeCurrent())) {
			// 清理延时关闭状态 避免再次触发
			m_delayCloseRequested = false;
			// 进行关闭处理
			handleClose();
		}

		// 如果计时器超时 那么就进行关闭连接操作
		if (m_waitTimer.isStarted() && m_waitTimer.isOnce(port.getTimeCurrent())) {
			LogCore.conn.warn("登陆阶段超时，主动关闭连接：connId={}", connId);// 记录日志
			// 进行关闭处理
			handleClose();//this.close();// 关闭连接
		}
				
		// 连接验证
		connCheck();
	}

	/**
	 * 连接验证
	 */
	private void connCheck() {
		// 验证间隔
		if (!tickTimerConnCheck.isPeriod(port.getTimeCurrent())) {
			return;
		}

		// 避免由于Debug断点等情况 造成瞬间发送多个检查请求
		tickTimerConnCheck.reStart();

		// 清理掉超时的连接
		if (timesNoConnCheck >= MAX_TIMES_NO_CONN_CHECK) {// 超过检查次数则清理
			// 日志
			LogCore.conn
					.warn("===connCheck()和世界服的连接验证超时，清理错误的连接：connCheckIncreaseTimes={}, address={}, isOpen={}, isActive={}, connId={}, status={}, accountId={}, humanId={}",
							timesNoConnCheck, channel.remoteAddress(), channel.isOpen() ? "true" : "false",
							channel.isActive() ? "true" : "false", connId, connStatus.status, connStatus.accountId,
							connStatus.humanId);
			// 进行关闭处理
			handleClose();//this.close();// 关闭连接
			return;
		}

		// 根据状态进行验证
		switch (connStatus.status) {
			case ConnectionStatus.STATUS_LOGIN :
			case ConnectionStatus.STATUS_GATE : {
				port.call(true, pointGate, CallSeamMethodKey.ACCOUNT_CHECK.ordinal(),
						CallSeamMethodKey.ACCOUNT_CHECK.name(), new Object[]{connId});
				port.listenResult(this::_result_pulseConnCheck, new Param());
			}
				break;
			case ConnectionStatus.STATUS_PLAYING : {
				CallPoint toPoint = new CallPoint(connStatus.stageNodeId, connStatus.stagePortId, connStatus.humanId);
				port.call(true, toPoint, CallSeamMethodKey.WORLD_CHECK.ordinal(), CallSeamMethodKey.WORLD_CHECK.name(),
						new Object[]{connId});
				port.listenResult(this::_result_pulseConnCheck, new Param());
			}
				break;
		}
		
		// 累加连接检查次数
		timesNoConnCheck++;
	}
	private void _result_pulseConnCheck(boolean timeout, Param results, Param context) {
		if (timeout)
			return;
		
		boolean has = results.get();
		if (has) {
			// 收到过就清空累计次数
			timesNoConnCheck = 0;
		}
	}

	/**
	 * 连接关闭，同时通知其他服务器进行信息清理
	 */
	public void handleClose() {
		m_closed = true;

		// 日志
		if (LogCore.conn.isDebugEnabled()) {
			LogCore.conn.debug("===Connection.handleClose 进行连接关闭的清理工作：connId={}, status={}", connId, connStatus.status);
		}

		switch (connStatus.status) {
			case ConnectionStatus.STATUS_LOGIN :
			case ConnectionStatus.STATUS_GATE : {
				if (LogCore.conn.isDebugEnabled()) {
					LogCore.conn.debug("===Connection.handleClose 关闭选择角色的玩家连接：connId={}, status={}", connId,
							connStatus.status);
				}
				port.call(true, pointGate, CallSeamMethodKey.ACCOUNT_LOST.ordinal(),
						CallSeamMethodKey.ACCOUNT_LOST.name(), new Object[]{connId});
			}
				break;
			case ConnectionStatus.STATUS_PLAYING : {
				if (LogCore.conn.isDebugEnabled()) {
					LogCore.conn.debug("===Connection.handleClose 关闭游戏中的玩家连接：connId={}, status={}", connId,
							connStatus.status);
				}
				CallPoint toPoint = new CallPoint(connStatus.stageNodeId, connStatus.stagePortId, connStatus.humanId);
				port.call(true, toPoint, CallSeamMethodKey.WORLD_LOST.ordinal(), CallSeamMethodKey.WORLD_LOST.name(),
						new Object[]{connId});
			}
				break;
			case ConnectionStatus.STATUS_LOSTED : {
				if (LogCore.conn.isDebugEnabled()) {
					LogCore.conn.debug("===Connection.handleClose 关闭已断开连接的玩家：connId={}", connId);
				}
			}
				break;
			default : {
				LogCore.conn.warn("===连接关闭时发现错误的连接状态：{}", connStatus.status);
			}
		}

		port.addQueue(new PortPulseQueue(this) {
			@Override
			public void execute(Port port) {
				ConnPort portConn = (ConnPort) port;
				portConn.closeConnection(param.<Connection> get());
			}
		});
	}

	/**
	 * 接收新数据并转发数据至其他服务器处理
	 * @param msgbuf
	 */
	private void handleIncoming(byte[] msgbuf) {
		if (null == msgbuf) {
			//LogCore.conn.error("====过滤无效消息：msgbuf is null");// 偶尔原地转圈或移动时出现，有空再查吧
			return;
		}
		int msgId = Utils.bytesToInt(msgbuf, 4);
		if (null == MsgIds.getClassById(msgId)) {
			LogCore.conn.error("====过滤无效消息：no find msgId={}", msgId);
			return;
		}
		if(msgId == MsgIds.CSPing) {// 客户端PING服务器，用于验证是否连接正常
			sendMsg(MsgIds.SCPing, new Chunk(MsgCommon.SCPing.newBuilder()));
			return;
		}

		// 关闭或关闭中的连接 不在接收新的客户端请求
		if (m_closed || m_delayCloseRequested) {
			if (LogCore.conn.isDebugEnabled()) {
				if (msgId != MsgIds.CSStageMove && msgId != MsgIds.CSStageMoveStop 
						&& msgId != MsgIds.CSStageDirection 
				) {// 排除一部分频繁的消息，例如：移动，转向等
					LogCore.conn.debug("===连接关闭中，忽略收到的客户端消息：msg={}:{}", msgId, MsgIds.getNameById(msgId));
				}
			}
			return;
		}

		switch (connStatus.status) {
			case ConnectionStatus.STATUS_LOGIN :
			case ConnectionStatus.STATUS_GATE : {
				port.call(true, pointGate, CallSeamMethodKey.ACCOUNT_MSG.ordinal(),
						CallSeamMethodKey.ACCOUNT_MSG.name(), new Object[]{connId, connStatus, msgbuf});

				//m_waitResponse = true;
				//m_waitTimer.start(150 * Time.SEC);// 150秒后关闭创角玩家的连接
				
				// 记录日志
				if (LogCore.conn.isDebugEnabled()) {
					String msg = "";
					try {
						GeneratedMessage gMsg = MsgIds.parseFrom(msgId,
								CodedInputStream.newInstance(Arrays.copyOfRange(msgbuf, 8, msgbuf.length)));
						if (null != gMsg)
							msg = gMsg.toString();
					} catch (IOException e) {
						e.printStackTrace();
					}
					LogCore.conn.debug("===客户端请求消息至账号服：msg={}:{}:[{}], accountId={}, connId={}", msgId,
							MsgIds.getNameById(msgId), msg, connStatus.accountId, connId);
				}
			}
				break;
			case ConnectionStatus.STATUS_PLAYING : {
				CallPoint toPoint = new CallPoint(connStatus.stageNodeId, connStatus.stagePortId, connStatus.humanId);
				port.call(true, toPoint, CallSeamMethodKey.WORLD_MSG.ordinal(), CallSeamMethodKey.WORLD_MSG.name(),
						new Object[]{connId, msgbuf});

				// 记录日志
				if (LogCore.conn.isDebugEnabled()) {
					if (msgId != MsgIds.CSStageMove && msgId != MsgIds.CSStageMoveStop
							&& msgId != MsgIds.CSStageDirection 
					) {// 排除一部分频繁的消息，例如：移动，转向等
						String msg = "";
						try {
							GeneratedMessage gMsg = MsgIds.parseFrom(msgId,
									CodedInputStream.newInstance(Arrays.copyOfRange(msgbuf, 8, msgbuf.length)));
							if (null != gMsg)
								msg = gMsg.toString();
						} catch (IOException e) {
							e.printStackTrace();
						}
						LogCore.conn.debug("===客户端请求消息：msg={}:{}:[{}], accountId={}, connId={}", msgId,
								MsgIds.getNameById(msgId), msg, connStatus.accountId, connId);
					}
				}
			}
				break;
			case ConnectionStatus.STATUS_LOSTED : {
				if (LogCore.conn.isDebugEnabled()) {
					LogCore.conn.debug("===接到客户端信息，但是玩家已是断线状态，忽略此消息。");
				}
			}
				break;
			default : {
				LogCore.conn.warn("===转发消息时发现错误的连接状态：{}", connStatus.status);
			}
		}
	}

	/**
	 * 心跳里调用的接收新数据
	 */
	public void handleInput() {
		while (!datas.isEmpty()) {
			try {
				handleIncoming(datas.poll());// 接收新数据并转发数据至相应的服处理
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				LogCore.conn.error("===error in Connection.handleInput() : ", e);
			}
		}
	}

	protected void handleContinue(ConnectionStatus status) {
		if (status.status == ConnectionStatus.STATUS_LOSTED) {
			if (LogCore.conn.isDebugEnabled()) {
				LogCore.conn.debug("Connection.handleContinue()：connId={}, status={}", connId, status.status);
			}
			this.close();// 关闭连接
		} else {
			connStatus = status;
		}
		m_waitResponse = false;
		m_waitTimer.stop();
	}
	
//	public static void registerPing(int csId, int scId, Chunk msg, boolean isIgnore){
//		PING_MSGID = csId;
//		ECHO_PING_MSGID = scId;
//		ECHO_PING = msg;
//		IGNORE_PING = isIgnore;
//	}
	
	/**
	 * 获取IP地址
	 */
	@DistrMethod
	public void getIpAddress() {
		if(channel != null && channel.remoteAddress() != null
				&& channel.remoteAddress() instanceof InetSocketAddress) {
			InetSocketAddress socketAddress = (InetSocketAddress)channel.remoteAddress();
			InetAddress ia = socketAddress.getAddress();
			port.returns(ia.getHostAddress());
		}
		port.returns("");
	}
}
