package game.platform.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import java.net.InetSocketAddress;
import game.platform.DistrPF;
import game.platform.LogPF;

public class HttpIpFilter {

	/**
	 * 检查来源IP是否是在白名单里
	 * @param ctx
	 * @param request
	 * @return
	 */
	public static String chekIp(ChannelHandlerContext ctx, HttpRequest request) {
		String clientIP = request.headers().get("X-Forwarded-For");
		//获取远程IP得至
		if (clientIP == null) {
			InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
			clientIP = insocket.getAddress().getHostAddress();
		}
		//判断是否在白名单里
		for(String ip : DistrPF.httpAcceptIpList){
			if(ip.equals(clientIP)){
				LogPF.platform.info("请求者的Ip={}",clientIP);
				clientIP = null;
				break;
			}
		}
		if(clientIP != null){
			LogPF.platform.error("非法的请求：ip={}, uri={}",clientIP, request.getUri());
		}
		
		return clientIP;
	}
}
