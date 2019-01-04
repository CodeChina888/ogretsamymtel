package game.platform.integration;

import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import core.support.observer.Listener;
import game.platform.LogPF;
import game.platform.http.HttpServerHandler;
import game.platform.http.Request;
import game.platform.http.Response;
import game.platform.observer.EventKeyPF;

/**
 * 登陆逻辑请求
 * @author GZC-WORK
 *
 */
public class LoginManager extends ManagerBase{
	public static LoginManager inst() {
		return inst(LoginManager.class);
	}
	
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.LOGIN_CHECK)
	public void onLogin(Param param) {
		try {
			_login(param);
		} catch (Exception e) {
			Request req =  Utils.getParamValue(param, "req", null);
			req.result(HttpServerHandler.RESULT_ERROR_EXE);
			
			LogPF.platform.error("执行支付请求时发生错误={}", param, e);
		}
	}
	private void _login(Param param) {
		Request req =  Utils.getParamValue(param,"req", null);
		Response res = new Response(true, null, null);
		req.result(Utils.toJSONString(res));
		
	}
}
