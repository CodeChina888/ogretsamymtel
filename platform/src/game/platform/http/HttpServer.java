package game.platform.http;

import core.support.SysException;
import game.platform.DistrPF;
import game.platform.LogPF;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServer extends Thread {
	@Override
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			//配置
			ServerBootstrap b = new ServerBootstrap();
			/**
			 * ChannelOption.SO_BACKLOG对应的是tcp/ip协议listen函数中的backlog参数
			 * 函数listen(int socketfd,int backlog)用来初始化服务端可连接队列
			 *　服务端处理客户端连接请求是顺序处理的,所以同一时间只能处理一个客户端连接，多个客户端来的时候
			 * 服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
			 */
			b.option(ChannelOption.SO_BACKLOG, 10240);
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						//编码解码器
						p.addLast("codec", new HttpServerCodec())
						//增加长度
						.addLast("aggregator", new HttpObjectAggregator(65535))
						//处理类
						.addLast("handler", new HttpServerHandler());
					}
				});
			
			//启动
			Channel ch = b.bind(DistrPF.HTTP_PORT1).sync().channel();
			ch.closeFuture().sync();
			LogPF.platform.info("PlantForm端口:"+DistrPF.HTTP_PORT1);
			
		} catch (Exception e) {
			throw new SysException(e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
