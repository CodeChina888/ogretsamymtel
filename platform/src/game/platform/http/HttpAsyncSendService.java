package game.platform.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.log.LogCore;
import game.platform.DistrPF;

/**
 * httpSend
 * @author nathan
 *
 */
@DistrClass(
	importClass = {Map.class, List.class} 
)
public class HttpAsyncSendService extends Service {
//	//异步请求列表
//	private ConcurrentLinkedQueue<HttpRequestToSend> queueRequestGet = new ConcurrentLinkedQueue<>();

	//异步请求返回值列表
	private final ConcurrentLinkedQueue<Object[]> queueResultGet = new ConcurrentLinkedQueue<>();
		
	private CloseableHttpAsyncClient httpClient = null; 
	
	@Override
	public Object getId() {
		return DistrPF.SERV_HTTP_SEND;
	}
	/**
	 * 构造函数
	 * @param port
	 */
	public HttpAsyncSendService(Port port) {
		super(port);
		IOReactorConfig.Builder b = IOReactorConfig.custom();
		//设置超时时间
		b.setConnectTimeout(3000);
		b.setSoTimeout(3000); 	
		//设置io线程3个
		b.setIoThreadCount(3);
		
		httpClient =  HttpAsyncClients.custom().setDefaultIOReactorConfig(b.build()).build();
		httpClient.start();
	}
	
	@Override
	public void pulseOverride() {
		//返回get请求结果
		while(!queueResultGet.isEmpty()) {
			Object[] r = queueResultGet.poll();
			port.returnsAsync((Long)r[0], (String)r[1]);
		}
	}

	/**
	 * 异步进行Get请求操作
	 * @return
	 */	
	@DistrMethod
	public void httpGetAsync(String url, Map<String, String> params, boolean needResult){
		try {
			//默认参数
			if(params == null) params = new HashMap<>();
			
			//1 拼接地址
			StringBuilder urlSB = new StringBuilder(url);
			//1.1 有需要拼接的参数
			if(!params.isEmpty()) {
				urlSB.append("?");
			}
			
			//1.2 拼接参数
			for(Entry<String, String> entry : params.entrySet()) {
				Object value = entry.getValue();
				String v = (value == null) ? "" : URLEncoder.encode(entry.getValue().toString(), "UTF-8");
				
				urlSB.append(entry.getKey()).append("=").append(v).append("&");
			}
			
			//1.3 最终地址
			String urlStrFinal = urlSB.toString();
			
			//1.4 去除末尾的&
			if(urlStrFinal.endsWith("&")) {
				urlStrFinal = urlStrFinal.substring(0, urlStrFinal.length() - 1);
			}
			
			//请求地址
			HttpGet get = new HttpGet(urlStrFinal);
			

			httpClient.execute(get, needResult ? new Callback(queueResultGet, port.createReturnAsync()) : null);			
		} catch (Exception e) {
			LogCore.core.error("httpGetAsync Exception {}",ExceptionUtils.getStackTrace(e));
		}
	}
	
	/**
	 * 异步进行Post请求操作
	 * @return
	 */	
	@DistrMethod
	public void httpPostAsync(String url, Map<String, String> params, boolean needResult){
		try {
			//默认参数
			if(params == null) params = new HashMap<>();
			
			//1.2 拼接参数
			List<org.apache.http.NameValuePair> nvps = new java.util.ArrayList<org.apache.http.NameValuePair>();
			for (Entry<String, String> entry : params.entrySet()) {
				Object key = entry.getKey();
				Object val = entry.getValue();
				String valStr = (val == null) ? "" : val.toString();
				
				nvps.add(new org.apache.http.message.BasicNameValuePair(key.toString(), valStr));
			}
			
			//1.3 最终地址
			String urlStrFinal = url;
			
			//1.4 去除末尾的&
			if(urlStrFinal.endsWith("&")) {
				urlStrFinal = urlStrFinal.substring(0, urlStrFinal.length() - 1);
			}
			
			//请求地址
			HttpPost post = new HttpPost(urlStrFinal);
			post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(nvps, "UTF-8"));
			
			httpClient.execute(post, needResult ? new Callback(queueResultGet, port.createReturnAsync()) : null);			
		} catch (Exception e) {
			LogCore.core.error("httpPostAsync Exception {}",ExceptionUtils.getStackTrace(e));
		}
	}
		
	/**
	 * 监听HTTP返回值
	 */
	private static class Callback implements FutureCallback<HttpResponse> {
		private ConcurrentLinkedQueue<Object[]> results;
		private long pid;
		
		public Callback(ConcurrentLinkedQueue<Object[]> results, long pid) {
			this.results = results;
			this.pid = pid;
		}
		
		@Override
		public void completed(HttpResponse result) {
			try {
				//返回内容
			    HttpEntity entity = result.getEntity();
			    
			    //主体数据
			    InputStream in = entity.getContent();  
			    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			    //读取
			    StringBuilder sb = new StringBuilder();
			    String line = null;  
			    while ((line = reader.readLine()) != null) {  
			    	sb.append(line);
			    }
			    
			    results.add(new Object[] {pid, sb.toString()});
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void failed(Exception ex) {
			results.add(new Object[] {pid, ""});
		}

		@Override
		public void cancelled() {
			results.add(new Object[] {pid, ""});
		}
	}
}