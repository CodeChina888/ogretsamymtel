package game.platform.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestToSend {
	public String url;								//请求地址
	public Map<String, String> params = new HashMap<>();		//请求参数
	public boolean needReturn = false;
	public long pid;
	
	public HttpRequestToSend(){
		
	}
	
	public HttpRequestToSend(String url, Map<String, String> paramsm, long pid){
		this.url = url;
		this.params = params;
		this.needReturn = false;
		this.pid = pid;
	}
	
	public void needReturn(boolean needReturn){
		this.needReturn = needReturn;
	}
}

