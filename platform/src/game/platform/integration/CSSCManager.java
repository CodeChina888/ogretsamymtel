package game.platform.integration;

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
import game.platform.observer.EventKeyPF;

import com.alibaba.fastjson.JSONObject;

/**
 * CSSC蓝版平台命令请求
 * @author GZC-WORK
 *
 */
public class CSSCManager extends ManagerBase {
	
	//禁言封号
	public static final String CSSC_BANS = "palm.platfom.productServer.shutUp";	
	//角色查询
	public static final String CSSC_QUERY = "palm.platfom.productServer.getProductUserInfo";
	
	public static CSSCManager inst() {
		return inst(CSSCManager.class);
	}
	
	/**	封号禁言接口
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.CSSC)
	public void onCSSC(Param param) {
		try {
		
			//获取request对象
			Request req =  Utils.getParamValue(param, "req", null);
			
			//打印日志
			String json = Utils.toJSONString(req.params);
			LogPF.platform.info("<onMonitor>: {}", json);
			
			//获取远程节点
			Port port = Port.getCurrent();
			CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), 
					Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
			
			//根据类型，进行分发
			String service = req.params.get("service");
			
			//调用远程方法处理
			if(service.contains(CSSC_BANS)){
				port.call(false, toPoint, CallMethodType.CSSC_BANS.value(), CallMethodType.CSSC_BANS.name(), new Object[]{ json });
				port.listenResult(this::_result_BANS, "req", req);
				
			} else if(service.contains(CSSC_QUERY)) {
				port.call(false, toPoint, CallMethodType.GM_QUERY_ROLE.value(), CallMethodType.GM_QUERY_ROLE.name(), new Object[]{ json });
				port.listenResult(this::_result_QUERY_ROLE, "req", req);
				
			}
			
		} catch (Exception e) {
			Request req =  Utils.getParamValue(param, "req", null);
			JSONObject jo = new JSONObject();
			jo.put("errorCode", "00001");
			jo.put("errorDesc", "发生异常");
			req.result(jo.toJSONString());
			
			LogPF.platform.error("执行【封号禁言】时发生错误={}", param, e);
		}
	}
	
	/**
	 * 禁言封号返回
	 * @param results
	 * @param context
	 */
	public void _result_BANS(Param results, Param context) {
		Request req =  Utils.getParamValue(context,"req", null);
		
		JSONObject jo = new JSONObject();
		boolean success = Utils.getParamValue(results,"success",false);
		if(success){
			jo.put("errorCode", "00000");
			jo.put("errorDesc", "成功");
		}else{
			jo.put("errorCode", "00001");
			jo.put("errorDesc", "发生异常");
		}
		req.result(jo.toJSONString());
		
	}
	
	/**
	 * 角色查询返回
	 * @param results
	 * @param context
	 */
	public void _result_QUERY_ROLE(Param results, Param context) {
		Request req = context.get("req");
		
		JSONObject jo = new JSONObject();
		boolean success = results.getBoolean("success");
		List<Map<String, Object>> param = results.get("param");
		jo.put("errorDesc", results.getString("reason"));
		if(success){
			jo.put("errorCode", "00000");
		}else{
			jo.put("errorCode", "00001");
		}
		
		JSONObject data = new JSONObject();
		data.put("productUsers", param);
		jo.put("data", data);
		
		req.result(jo.toJSONString());
	}
	
}
