package game.platform.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.CallPoint;
import core.Port;
import core.support.Config;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import core.support.observer.Listener;
import game.platform.DistrPF;
import game.platform.LogPF;
import game.platform.enumType.CallMethodType;
import game.platform.http.HttpServerHandler;
import game.platform.http.Request;
import game.platform.http.Response;
import game.platform.observer.EventKeyPF;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
;/**
 * GM命令请求
 * @author GZC-WORK
 *
 */
public class GmCmdManager extends ManagerBase {
	public static GmCmdManager inst() {
		return inst(GmCmdManager.class);
	}
	
	/**
	 * GM命令分发入口
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.GM_CMD)
	public void onGMCmd(Param param) {
		try {
			System.out.println("GM命令分发入口");
			_gm(param);
		} catch (Exception e) {
			Request req = Utils.getParamValue(param,"req", null);
			req.result(HttpServerHandler.RESULT_ERROR_EXE);
			
			LogPF.platform.error("执行【GM】请求时发生错误={}", param, e);
		}
	}
	private void _gm(Param param) {
		Request req = Utils.getParamValue(param,"req",null);
		//发送请求
		String json = Utils.toJSONString(req.params);
		LogPF.platform.info("<onGMCmd>: {}", json);
		
		Port port = Port.getCurrent();
		CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), 
				Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
		port.call(false, toPoint, CallMethodType.GM_CMD.value(), CallMethodType.GM_CMD.name(), new Object[]{ json });
		port.listenResult(this::_result_gm, "req", req);
	}
	public void _result_gm(Param results, Param context) {
		Request req = Utils.getParamValue(context, "req", null);
		boolean success = Utils.getParamValue(results,"success", false);
		String reason = Utils.getParamValue(results,"reason","");
		String para = Utils.getParamValue(results,"param","");
		LogPF.platform.info("success={}, reason={}, params={}", success, reason, para);
		
		Response res = new Response(success, reason, para);
		req.result(Utils.toJSONString(res));
	}
	
	
	
	/**	查询角色
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.QUERY_ROLE)
	public void onQUERY_ROLE(Param param) {
		try {
			Request req = Utils.getParamValue(param,"req",null);
			
			//发送请求
			String json = Utils.toJSONString(req.params);
			LogPF.platform.info("<onMonitor>: {}", json);
			
			//调用远程方法处理
			Port port = Port.getCurrent();
			CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), 
					Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
			port.call(false, toPoint, CallMethodType.GM_QUERY_ROLE.value(), CallMethodType.GM_QUERY_ROLE.name(), new Object[]{ json });
			port.listenResult(this::_result_onQUERY_ROLE, "req", req);
		} catch (Exception e) {
			Request req = Utils.getParamValue(param,"req",null);
			JSONObject jo = new JSONObject();
			jo.put("errorCode", "00001");
			jo.put("errorDesc", "发生异常");
			req.result(jo.toJSONString());
			
			LogPF.platform.error("执行【查询角色】时发生错误={}", param, e);
		}
	}
	public void _result_onQUERY_ROLE(Param results, Param context) {
		Request req = Utils.getParamValue(context,"req", null);
		
		JSONObject jo = new JSONObject();
		boolean success = Utils.getParamValue(results,"success", false);
		
		List<Map<String, Object>> param = Utils.getParamValue(results,"param", null);
		String userId =  Utils.getParamValue(results,"userId", null);
		Integer serverId =  Utils.getParamValue(results,"serverId", null);
		jo.put("errorDesc", Utils.getParamValue(results,"reason",""));
		if(success){
			jo.put("errorCode", "00000");
			jo.put("result", 0);
		}else{
			jo.put("errorCode", "00001");
		}
		
		jo.put("role", param);
		jo.put("userId", userId);
		jo.put("serverId", serverId);
		req.result(jo.toJSONString());
	}
	
	/**	查询在线人数
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.COUNT_ALL)
	public void onCOUNT_BY(Param param) {
		try {
			Request req = Utils.getParamValue(param,"req",null);
			
			//发送请求
			String json = Utils.toJSONString(req.params);
			LogPF.platform.info("<onMonitor>: {}", json);
			
			Port port = Port.getCurrent();
			CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), 
					Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
			port.call(false, toPoint, CallMethodType.GM_COUNT_ALL.value(), CallMethodType.GM_COUNT_ALL.name(),new Object[]{ json });
			port.listenResult(this::_result_onCOUNT_BY, "req", req);
		} catch (Exception e) {
			Request req = Utils.getParamValue(param,"req",null);
			JSONObject jo = new JSONObject();
			jo.put("errorCode", "00001");
			jo.put("errorDesc", "发生异常");
			req.result(jo.toJSONString());
			
			LogPF.platform.error("执行【查询数量】时发生错误={}", param, e);
		}
	}
	public void _result_onCOUNT_BY(Param results, Param context) {
		Request req = Utils.getParamValue(context,"req",null);
		int count = Utils.getParamValue(results,"param",0);
		
		
		JSONObject jo = new JSONObject();
		jo.put("count", count);
		jo.put("errorDesc", Utils.getParamValue(results,"reason",""));
		
		boolean success = Utils.getParamValue(results,"success", false);
		if(success){
			jo.put("errorCode", "00000");
		}else{
			jo.put("errorCode", "00001");
		}
		req.result(jo.toJSONString());
	}
	
	/** 查询等级排行榜
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.QUERY_TOPLEVEL)
	public void onQUERY_TOPLEVEL(Param param) {
		try {
			Request req = Utils.getParamValue(param,"req",null);
			//发送请求
			String json = Utils.toJSONString(req.params);
			LogPF.platform.info("<onMonitor>: {}", json);
			
			//调用远程方法处理
			Port port = Port.getCurrent();
			CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), 
					Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
			port.call(false, toPoint, CallMethodType.GM_QUERY_TOPLEVEL.value(), CallMethodType.GM_QUERY_TOPLEVEL.name(), new Object[]{ json });
			port.listenResult(this::_result_onQUERY_TOPLEVEL, "req", req);
		
		}catch (Exception e) {
			Request req = Utils.getParamValue(param,"req",null);
			JSONObject jo = new JSONObject();
			jo.put("errorCode", "00001");
			jo.put("errorDesc", "发生异常");
			req.result(jo.toJSONString());
			
			LogPF.platform.error("执行【查询等级排行】时发生错误={}", param, e);
		}
	}		
			
	public void _result_onQUERY_TOPLEVEL(Param results, Param context) {
		Request req = Utils.getParamValue(context,"req", null);
		
		JSONObject jo = new JSONObject();
		boolean success = Utils.getParamValue(results,"success", false);
		ArrayList param = Utils.getParamValue(results,"param", null);
		jo.put("errorDesc", Utils.getParamValue(results,"reason",""));
		jo.put("serverId", Utils.getParamValue(results,"serverId",""));
		jo.put("result", Utils.getParamValue(results,"result",""));
		if(success){
			jo.put("errorCode", "00000");
		}else{
			jo.put("errorCode", "00001");
		}
		jo.put("top", param);
		req.result(jo.toJSONString());
	}
}
