package core.connsrv.netty;

import java.util.concurrent.TimeUnit;

import core.connsrv.netty.Decoder;
import core.connsrv.netty.Encoder;
import core.connsrv.netty.ServerHandler;
import core.support.Config;
import core.support.SysException;
import core.support.log.LogCore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class Server extends Thread {

	@Override
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
		try {
			// .option()是提供给NioServerSocketChannel用来接收进来的连接，也就是boss线程。
			// .childOption()是提供给由父管道ServerChannel接收到的连接，也就是worker线程
			// 启动netty监听
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 10240)
					.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT) // 设置接收缓冲器
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // 设置内存分配器，使用内存池
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // 设置内存分配器，使用内存池
					.childOption(ChannelOption.TCP_NODELAY, true) // 非阻塞，NAGLE算法通过将缓冲区内的小封包自动相连，组成较大的封包，阻止大量小封包的发送阻塞网络，从而提高网络应用效率
					.childOption(ChannelOption.SO_KEEPALIVE, true) // 长连接
					.childOption(ChannelOption.SO_REUSEADDR, true) // 重用地址，如果端口忙，但TCP状态位于TIME_WAIT，可以重用端口
					.childOption(ChannelOption.SO_RCVBUF, 256 * 1024) // 套接字接收缓冲区大小，CentOS6.3下默认43690bytes
					.childOption(ChannelOption.SO_SNDBUF, 256 * 1024) // 套接字发送缓冲区大小，CentOS6.3下默认9800bytes
					.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(128 * 1024, 256 * 1024) ) //上下水位
					//.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 256 * 1024) // 控制输出流量，设置写buffer高水位，默认64K
					//.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 128 * 1024) // 控制输出流量，设置写buffer低水位，默认32K
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							// 设置10秒的心跳检测
							p.addLast("pingpong", new IdleStateHandler(Config.CONN_PING_SECOND, 0, 0, TimeUnit.SECONDS));
							p.addLast(new Decoder(), new Encoder(), new ServerHandler());
						}
					});

			// 启动
			int port = Config.CONN_PORT;
			if(Config.isCrossSrv) {
				port = Config.getCrossMainNodeConnPort();
			}
			Channel ch = b.bind(port).sync().channel();
			LogCore.conn.info("Server Listen:tcp://:{}", port);
			ch.closeFuture().sync();
		} catch (Exception e) {
			throw new SysException(e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
