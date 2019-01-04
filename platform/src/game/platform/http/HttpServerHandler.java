package game.platform.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import core.support.SysException;
import core.support.Utils;
import game.platform.HttpPort;
import game.platform.LogPF;
import game.platform.enumType.RequestParseType;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	
	//各个GM接口路径
	public static final String LOGIN_CHECK = "/loginCheck";		//登陆检查
	public static final String PAY_NOTIFY = "/payNotify";			//充值
	public static final String IOS_PAY_NOTIFY = "/iOSPayNotify";	//ios充值
	public static final String CSSC = "/CSSC";					//蓝版平台【GM平台转发】
	
	public static final String QUERY_ROLE = "/queryRole";			//玩家查询【GM平台接口】
	public static final String QUERY_TOPLEVEL ="/getTopLevel" ; //获取等级排行的玩家列表
	public static final String GM_CMD = "/GM";					//玩家查询【GM平台接口】
	public static final String COUNT_ALL = "countAll";			//查询在线人数【GM平台接口】
	//成功统一回复
	public static final String RESULT_OK = "{\"code\": 1, \"reason\": \"完成请求\"}";
	//错误请求地址提示
	public static final String RESULT_ERROR_PATH = "{\"errorCode\": 11111, \"errorDesc\": \"地址错误\"}";
	public static final String RESULT_ERROR_EXE = "{\"errorCode\": 00001, \"errorDesc\": \"处理请求发生错误\"}";
	
	//请求编号
	private static final AtomicInteger httpNo = new AtomicInteger();
	//数据工厂类
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	// 解析POST的
	private HttpPostRequestDecoder decoder;	
	// 请求数据
	private RequestData data;		
	
    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

	@Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws InterruptedException {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			//IP过滤
			String filterResult = HttpIpFilter.chekIp(ctx, request);
			if(filterResult != null){
				Request req = new Request();
				req.channel = ctx.channel();
				req.result(RESULT_ERROR_PATH + ",请求者ip:" + filterResult);
				return;
			}
			
			// 解析request请求参数
			data = new RequestData();
			data.uri = request.getUri();
	        QueryStringDecoder query = new QueryStringDecoder(data.uri);
	        
	        //打印收到的参数
	        if(LogPF.platform.isInfoEnabled()) {
	        	LogPF.platform.info("[{}]收到HTTP请求:{}", ctx.channel().hashCode(), query.parameters());
	        }
	        //请求地址
	        data.action = query.path();
			
	        //处理POST请求
	    	if(request.getMethod().equals(HttpMethod.POST)) {
	    		//解析
	    		decoder = new HttpPostRequestDecoder(factory, request);
	    	}
	    	//处理GET请求
	    	else {       
	    		//请求参数
	    		data.params = new HashMap<>();
	    		
	            //解析参数
	            Map<String, List<String>> ps = query.parameters();
	            for (Entry<String, List<String>> p: ps.entrySet()) {
	                String key = p.getKey();
	                //如果有多个值 那么只有最后一个会生效
	                List<String> vals = p.getValue();
	                for (String val : vals) {
	                	data.params.put(key, val);
	                }
	            }
				LogPF.platform.info("GET请求：uri={}, param={}", request.getUri(), data.params);
	            doRequest(ctx, data);
	    	}
		}
		
		// POST请求有数据，则继续处理
    	if(decoder != null) {
	    	if(msg instanceof HttpContent) {
	    		HttpContent chunk = (HttpContent) msg;
	    		RequestKey key = RequestKey.getKeyByAction(data.action);
	    		RequestParseType type = RequestParseType.getByValue(key.getParseType());
	    		//解析类型， 
	    		switch(type){
	    		case RequestParam:		//1是requestParam，明码参数，带名字的
	    			parseParam(ctx, chunk);
	    			break;
	    		case RequestBody:		//2是request Body 从流中读取json
	    			parseBody(ctx, chunk);
	    			break;
	    		}
				LogPF.platform.info("POST请求：uri={}, param={}", data.action, data.params);
				
				//处理逻辑
	    		doRequest(ctx, data);
    		}
    	}
    }
	
	/**
	 * 解析Http信息中的body为Json
	 * @param ctx
	 * @param chunk
	 */
	private void parseBody(ChannelHandlerContext ctx, HttpContent chunk) {
		ByteBuf buffer = chunk.content();
		byte[] dst = new byte[buffer.readableBytes()]; 
		//读出上传信息
		buffer.readBytes(dst); 
		String param = new String(dst);
		JSONObject jo = Utils.toJSONObject(param);
		data.params = new HashMap<String, String>();
		for(Entry<String, Object> en : jo.entrySet()){
			data.params.put(en.getKey(), en.getValue()==null? "":en.getValue().toString());
		}
	}

	/**
	 * 解析http中的request param的键值对参数
	 * @param ctx
	 * @param chunk
	 * @throws InterruptedException
	 */
	private void parseParam(ChannelHandlerContext ctx, HttpContent chunk) throws InterruptedException {
		decoder.offer(chunk);
		//请求参数
		data.params = new HashMap<>();
        
		//挨个处理参数
		try {
    		for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
    			if (data != null) {
    				try {
    					//处理参数
    					if (data.getHttpDataType() == HttpDataType.Attribute) {
    						Attribute attribute = (Attribute) data;
    						this.data.params.put(attribute.getName(), attribute.getValue());
    					}
    				} catch(Exception e) {
    					throw new SysException(e);
    				} finally {
    					data.release();
    				}
    			}
    		}

		} catch (EndOfDataDecoderException e1) {
            //忽略这种异常
        }
	}

	/**
	 * 处理POST或者GET请求
	 * @param ctx
	 * @param data
	 * @throws InterruptedException
	 */
	public void doRequest(ChannelHandlerContext ctx, RequestData data) throws InterruptedException {
		//收到的请求信息
        Request req = new Request();
        req.id = httpNo.incrementAndGet();
        req.params = data.params;
    	req.handler = this;
    	
    	req.key = RequestKey.getKeyByAction(data.action);
    	
    	req.uri = data.uri;
    	req.channel = ctx.channel();

        if(req.key == null) {
        	// 错误的请求地址
        	req.result(RESULT_ERROR_PATH);
        } else {
			// 是充值，增加渠道标识
			if(data.action.contains(RequestKey.PAY_NOTIFY.getAction())) {
				//渠道KEY
				req.params.put("pfKey", data.payKey);
			}
			
			//发送请求对象
			HttpPort.addRequest(req);
        }
	}

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogPF.platform.error("", cause);
        ctx.close();
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
