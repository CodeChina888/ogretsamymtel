package core.connsrv.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

import core.Port;
import core.connsrv.Connection;
import core.connsrv.main.ConnStartup;
import core.connsrv.netty.RC4;
import core.support.Config;
import core.support.Distr;
import core.support.RandomUtils;
import core.support.log.LogCore;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	// 当前的全部连接
	public static final ConcurrentLinkedQueue<Connection> conns = new ConcurrentLinkedQueue<>();

	// 当前连接信息
	private Connection conn;
	private RC4 _rc4 = null;

	// 不准确，有交互时，空闲IDLE的时间就变了
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object _evt) throws Exception {
		if (_evt instanceof IdleStateEvent) {
			IdleStateEvent evt = (IdleStateEvent) _evt;
			// Channel channel = ctx.channel();
			if (evt.state() == IdleState.WRITER_IDLE) {// 写超时
				// if(LogCore.conn.isDebugEnabled()) {
				// LogCore.conn.debug("HeartbeatHandler.userEventTriggered : WRITER_IDLE address={}, isOpen={}, isActive={}",
				// ctx.channel().remoteAddress(),
				// ctx.channel().isOpen()?"true":"false",
				// ctx.channel().isActive()?"true":"false");
				// }
			} else if (evt.state() == IdleState.READER_IDLE) {// 读超时
				// if(LogCore.conn.isDebugEnabled()) {
				// LogCore.conn.debug("HeartbeatHandler.userEventTriggered : READER_IDLE address={}, isOpen={}, isActive={}",
				// ctx.channel().remoteAddress(),
				// ctx.channel().isOpen()?"true":"false",
				// ctx.channel().isActive()?"true":"false");
				// }
				// 失败计数器加1
				conn.timesNoClientPink++;
				// LogCore.conn.info("====心跳包：读超时，失败计数器++，curNum={},maxNum={},address={}",
				// conn.timesNoClientPink, Connection.MAX_TIMES_NO_CLIENT_PINK,
				// ctx.channel().remoteAddress());
				// 失败计数器次数大于等于3次的时候，关闭链接，等待client重连
				if (conn.timesNoClientPink >= Connection.MAX_TIMES_NO_CLIENT_PINK) {
					// 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
					LogCore.conn.info("====心跳包：读超时，失败计数>={}，已无心跳需关闭连接，address={}", Connection.MAX_TIMES_NO_CLIENT_PINK,
							ctx.channel().remoteAddress());
					//ctx.channel().close();
					conn.close();//sjh
				}
			} else if (evt.state() == IdleState.ALL_IDLE) {// 总超时
				// if(LogCore.conn.isDebugEnabled()) {
				// LogCore.conn.debug("HeartbeatHandler.userEventTriggered : ALL_IDLE address={}, isOpen={}, isActive={}",
				// ctx.channel().remoteAddress(),
				// ctx.channel().isOpen()?"true":"false",
				// ctx.channel().isActive()?"true":"false");
				// }
			}
		}
		super.userEventTriggered(ctx, _evt);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			// 记录数据
			byte[] buff = (byte[]) msg;
			if (Config.CONN_ENCRYPT && _rc4 != null) {
				_rc4.crypt(buff, 4, -1);
			}
			conn.putDate(buff);
		} catch (Exception et) {
			LogCore.conn.error("===连接服读取数据异常：", et);
		} finally {
			ReferenceCountUtil.release(msg);// sjh
		}
	}

	/*
	 * @Override public void channelReadComplete(ChannelHandlerContext ctx)
	 * throws Exception { //if(log.isDebugEnabled()) { //
	 * log.debug("====ServerHandler.channelReadComplete.flush"); //}
	 * //ctx.flush();//sjh }
	 */

	/**
	 * 建立新连接
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		Channel channel = ctx.channel();

		// 日志
		if (LogCore.conn.isDebugEnabled()) {
			InetSocketAddress addrLocal = (InetSocketAddress) channel.localAddress();
			LogCore.conn.debug("===新建一个连接：connId={}, port={}, addr={}", Connection.getId(channel), addrLocal.getPort(),
					channel.remoteAddress());
		}

		//默认游戏服连接配置
		String nodeId = Config.getGameWorldPartDefaultNodeId(); 
		String portId = Config.getGameWorldPartDefaultPortId();
		String servId = Distr.SERV_GATE;
		if(Config.isCrossSrv) {
			//跨服连接配置
			nodeId = Config.getCrossPartDefaultNodeId(); 
			portId = Config.getCrossPartDefaultPortId();
			servId = Distr.SERV_GATE;
		}
		
		int index = RandomUtils.nextInt(Distr.PORT_STARTUP_NUM_CONN);
		Port port = ConnStartup.CONN_NODE.getPort(Distr.PORT_CONNECT_PREFIX + Integer.toString(index));
		conn = new Connection(channel, port, nodeId, portId, servId);
		conn.startup();

		conns.add(conn);// 加入连接列表

		if (Config.CONN_ENCRYPT) {
			byte[] key = RC4.getRandomKey();
			_rc4 = new RC4(key);
			conn.sendBytesWithLength(key);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);

		// 日志
		if (LogCore.conn.isDebugEnabled()) {
			LogCore.conn.debug("===连接关闭：connId={}, status={}", (conn == null ? "null" : conn.getId()), 
					(conn == null ? "null" : conn.getStatusString()));
		}

		// 关闭玩家连接
		if (conn != null) {
			conn.closeDelay();//conn.close();// 关闭连接
			conns.remove(conn);// 清理连接列表
		}
	}

	/**
	 * 有异常发生
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);// sjh
		
		// 如果出异常直接关闭吧
		ctx.close();
		//ctx.channel().close();// sjh
		
		// 输出错误日志
		LogCore.conn.error("===连接发生异常：exception={},connId={},humanId={},accountId={}", cause.getMessage(), 
				Connection.getId(ctx.channel()), conn.getStatus().humanId, conn.getStatus().accountId);
		//cause.printStackTrace();
	}

	/**
	 * 水位发生异常
	 */
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		LogCore.conn.warn("发生channelWritabilityChanged事件。bufferHigh={}, conn={}", 
				ctx.channel().config().getWriteBufferHighWaterMark(), conn);
	}
}
