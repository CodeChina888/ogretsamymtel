package game.platform.http;


/**
 * 自封装返回信息对象
 * @author GZC-WORK
 *
 */
public class Response{
	public boolean success;
	public String reason;
	public String param;
	
	public Response(){}
	
	public Response(boolean success, String reason, String param){
		this.success = success;
		this.reason = reason;
		this.param = param;
	}
}
