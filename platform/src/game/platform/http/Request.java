package game.platform.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import game.platform.LogPF;

/**
 *  自封装HttpRequest处理对象
 * @author GZC-WORK
 *
 */
public class Request {
	public int id;				
	public RequestKey key;										//请求类型
	public HttpServerHandler handler;	 						//上下文环境
	public String uri;											//请求URI
	public Channel channel;										//保存HttpChannel
	public Map<String, String> params = new HashMap<>();		//请求参数
	
	/**
	 * 设置请求返回值
	 * @param result
	 */
	public void result(String result) {
		// 构建response
        ByteBuf buf = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buf);

        // 写入HTTP头
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // keepAlive需要设置'Content-Length' HTTP头
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        // 返回请求结果
        if(channel != null && channel.isActive() && channel.isWritable()) {
        	channel.write(response);
        	channel.flush();
//        	Log.sdk.info("[{}]HTTP返回值={}", channel.hashCode(), result);
        } else {
//        	Log.sdk.error("[{}]HTTP写入返回值失败={}", channel, result);
        }
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id)
										 .append("key", key)
										 .append("params", params)
										 .append("uri", uri)
										 .toString();
	}
}