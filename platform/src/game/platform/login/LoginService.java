package game.platform.login;

import game.platform.DistrPF;
import game.platform.LogPF;
import game.platform.http.HttpAsyncSendServiceProxy;
import game.platform.login.sdk.SecurityUtil;
import game.platform.login.sdkPLATFORM.LoginPLATFORM;
import game.platform.login.sdkSina.PlaceSina;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Param;
import core.support.Utils;

@DistrClass
public class LoginService extends Service {

	public LoginService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return DistrPF.SERV_LOGIN;
	}

	private static final boolean isCheck = false;//是否要校验
	
	private static final String Address = "http://api.lcatgame.com/v1/user/verify";
	private static final String AddressTest = "http://10.163.254.246:8000/1";
	private static final String SECRETKEY ="noD7v10SvfJZnS7FINFbaWgijv7fjfoa";//密钥
	
//	private static final String QuickSDK_ProductCode = "78059106745724500670019297567124"; 	
	private static final String QuickSDK_ProductCode = "45568003654761600642888780209293"; 	
	private static final String QuickSDK_Normal_ProductCode = "49873215777955606608628847315929"; 	
	/**
	 * 登录验证统一入口
	 * @param loginType 登录类型ELoginType
	 * @param account 账号，即用户ID
	 * @param password 密码，即访问口令（提供给服务器验证用的）
	 */
	@DistrMethod
	public void check(int loginType, String account, String password,
			String token, String timeStamp) {
		this.check(loginType, account, password, token, timeStamp, null);
	}
	
	@DistrMethod
	public void check(int loginType, String account, String password,
			String token, String timeStamp, String channel) {
		boolean result = false;
		String accountId = "";
		String uid = "";
		if(token == null) {
			token = "";
		}
		long startTime = Port.getTime();
		LoginType type = LoginType.valueOf(loginType);
		if (type==LoginType.QuickSDK || type==LoginType.QuickNormal) {
			uid = account;
			long pid = port.createReturnAsync();// 创建一个异步返回
			/*组装参数*/
			Map<String,String> param = new HashMap<>();
			param.put("token", token);
			param.put("product_code", type==LoginType.QuickSDK?QuickSDK_ProductCode:QuickSDK_Normal_ProductCode);
			param.put("uid", uid);
			String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_CHAT);
			HttpAsyncSendServiceProxy prx = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
			prx.httpPostAsync("http://checkuser.sdk.quicksdk.net/v2/checkUserInfo",param,true);
			prx.listenResult(this::result_QuickSDK_check,"pid",pid,"accountId",account,"uid", uid,"startTime",startTime);
			return;
		} else {
			switch (type) {
			case PC: {// pc登录验证
				result = true;
				accountId = account;
			}	break;
			case SINA: {// SINA平台登录
				uid = account;
				token = password;
				accountId = account;
				// 新浪验证，始终正确（新浪那边校验时间过长，无法）
				PlaceSina.checkToken(uid, token);
				result = true;
				break;
			}
			case LANMAO:{
				//要校验
				String SecretKey,AppID,AppKey;
				if ("10171".equals(channel)) {
					SecretKey = "r4PE45HX3CwS8anx0dWPO3ehg3Pu0O6C";
					AppID = "1017";
					AppKey = "ry2By0hNwxfqQqzQ";
				}
				else if ("10181".equals(channel)) {
					SecretKey = "gcAsH3mKKqOpr3uJtsn8ajsGm5xxmL1F";
					AppID = "1018";
					AppKey = "nLkd5Drx2pg23gTN";
				}
				else if ("10191".equals(channel)) {
					SecretKey = "hrEkf1REwOhK94t3qhl2THa5FsochYH3";
					AppID = "1019";
					AppKey = "hHbSb45e9kMP2Hmj";
				}
				else if ("10201".equals(channel)) {
					SecretKey = "zwdaz9rOsSbLVgcLRj5hXLw2OydzJ98o";
					AppID = "1020";
					AppKey = "b6tY3Yer7tLTjIQM";
				}
				else if ("10211".equals(channel)) {
					SecretKey = "Ay0NUbyj0Hmw1oKOZQA8bOoyGfCtA5XC";
					AppID = "1021";
					AppKey = "0IXH2HMcxj673E0K";
				}
				else if ("10091".equals(channel)) {
					SecretKey = "kIQkaFRYgXno6YAb25XScKvasx5CwRuu";
					AppID = "1009";
					AppKey = "oDlqrUY8UTzmBnoU";
				}
				else {
					SecretKey = SECRETKEY;
				}
				SecretKey = SecretKey==null?"":SecretKey;
				String source = account+timeStamp+token+SecretKey;
				String sha1 =  SecurityUtil.getSha1(source).toLowerCase();
				if(!sha1.equals(password)) {
					LogPF.sdk.warn("account={},timeStamp={},token={},SECRETKEY={},source={},signature={}",account,timeStamp,token,SecretKey,source,password);
					result = false;
					break;
				}
				
				
				if(isCheck) {
					uid = account;
					long pid = port.createReturnAsync();// 创建一个异步返回
					/*组装参数*/
					Map<String,String> param = new HashMap<>();
					param.put("openID", account);
					param.put("token", token);
					param.put("rnd", "1510663238");
					String signature = getSignature(param, SecretKey);
					param.put("signature", signature);
					String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_CHAT);
					HttpAsyncSendServiceProxy prx = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
					prx.httpPostAsync(Address,param,true);
					prx.listenResult(this::result_lammao_check,"pid",pid,"accountId",account,"uid", uid,"startTime",startTime);
					return;
				}else {
					result = true;
					accountId = account;
				}
				break;
			}
			case YYBSQ:{
				uid = account;
				String appid = "1106385818";
				String appkey = "j7uKUId45ksQ51rH";
				String openid = account;
				String openkey = token;
				String sig = Utils.md5(appkey+timeStamp);
				Map<String,String> param = new HashMap<>();
				param.put("appid", appid);
				param.put("openid", openid);
				param.put("openkey", openkey);
				param.put("sig", sig);
				param.put("timestamp", timeStamp);
				long pid = port.createReturnAsync();// 创建一个异步返回

				String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_CHAT);
				HttpAsyncSendServiceProxy prx = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
				prx.httpPostAsync("http://ysdk.qq.com/auth/qq_check_token",param,true);
//				prx.httpGetAsync("http://ysdktest.qq.com/auth/qq_check_token",param,true);
				prx.listenResult(this::result_YYBSDK_check,"pid",pid,"accountId",account,"uid", uid,"startTime",startTime);
				return;
			}
			case YYBWX:{
				uid = account;
				String appid = "wxca1fb3db906e96cd";
				String appkey = "7d7b25745c7bcb7b19d8b3c31faae97f";
				String openid = account;
				String openkey = token;
				String sig = Utils.md5(appkey+timeStamp);
				Map<String,String> param = new HashMap<>();
				param.put("appid", appid);
				param.put("openid", openid);
				param.put("openkey", openkey);
				param.put("sig", sig);
				param.put("timestamp", timeStamp);
				long pid = port.createReturnAsync();// 创建一个异步返回
				
				String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_CHAT);
				HttpAsyncSendServiceProxy prx = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
				prx.httpPostAsync("http://ysdk.qq.com/auth/wx_check_token",param,true);
//				prx.httpGetAsync("http://ysdktest.qq.com/auth/wx_check_token",param,true);
				prx.listenResult(this::result_YYBSDK_check,"pid",pid,"accountId",account,"uid", uid,"startTime",startTime);
				return;
			}
			default : {// 修改为后台统一验证
				uid = account;
				token = password;
				String id = LoginPLATFORM.checkToken(uid, token);
				if (null != id) {
					result = true;
					accountId = String.valueOf(id);
				}
				break;
			}
			}
		}
		// 返回
		port.returns("result", result, "accountId", accountId, "uid", uid, "token", token);
	}
	/**
	 * 七政平台登录验证统一入口
	 * @param account 账号，即用户ID
	 * @param password 密码，即访问口令（提供给服务器验证用的）
	 */
	@DistrMethod
	public void check(String account, int channel, int serverId, String password) {
		boolean result = false;
		String accountId = "";
		// 渠道小于0默认为PC，由客户端发送
		if (channel < 0) {
			result = true;
			accountId = account;
			// 返回
			port.returns("result", result, "accountId", accountId, "uid", account, "token", password);
			return;
		}
 		String id = LoginPLATFORM.checkQzToken(channel, serverId, password);
		if (null != id) {
			result = true;
			accountId = String.valueOf(id);
		}
		// 返回
		port.returns("result", result, "accountId", accountId, "uid", account, "token", password);
	}
	
	/**
	 * 懒猫数字签名
	 */
	private String getSignature(Map<String,String> param, String secretKey) {
		Set<String> allKeys = param.keySet();
		LinkedList<String> list = new LinkedList<>();
		list.addAll(allKeys);
		Collections.sort(list);
		String sinature = "";
		for(String key : list ) {
			sinature +=  param.get(key);
		}
		
		return SecurityUtil.getSha1(sinature+secretKey).toLowerCase();
	} 
	
	/**
	 * QuickSDK SDK回调
	 */
	private void  result_QuickSDK_check(Param results, Param context) {
		long pid = Utils.getParamValue(context, "pid", -1L);
		long startTime = context.get("startTime");
		String accountId = context.get("accountId");
		String uid = context.get("uid");
		
		String result_str = results.get();
		LogPF.sdk.info("QuickSDK costTime={} ms ,result={}",Port.getTime()-startTime,result_str);
		boolean result = false;
		if(result_str.equals("1")) {
			result = true;
		}else {
			LogPF.sdk.info("QuickSDK  error msg={}",result_str);
		}
		
		port.returnsAsync(pid,"result", result, "accountId", accountId, "uid", uid);
	}
	
	/**
	 * 应用宝登陆回调
	 * @param results
	 * @param context
	 */
	private void  result_YYBSDK_check(Param results, Param context) {
		long pid = Utils.getParamValue(context, "pid", -1L);
		long startTime = context.get("startTime");
		String accountId = context.get("accountId");
		String uid = context.get("uid");
		
		String result_str = results.get();
		LogPF.sdk.info("YYBSDK costTime={} ms ,result={}",Port.getTime()-startTime,result_str);
		boolean result = false;
		JSONObject jsonObject = Utils.toJSONObject(result_str);
		String ret = jsonObject.getString("ret");
		if("0".equals(ret)) {
			result = true;
		}else {
			String msg = jsonObject.getString("msg");
			LogPF.sdk.info("YYBSDK  error msg={}",msg);
		}
		
		port.returnsAsync(pid,"result", result, "accountId", accountId, "uid", uid);
	}
	
	/**
	 * 懒猫SDK回调
	 * @param results
	 * @param context
	 */
	private void  result_lammao_check(Param results, Param context) {
		long pid = Utils.getParamValue(context, "pid", -1L);
		long startTime = context.get("startTime");
		String result_str = results.getString();
		String accountId = context.get("accountId");
		JSONObject json = Utils.toJSONObject(result_str);
		String rc = json.getString("rc");
		String uid = context.get("uid");
		
		boolean result = false;
		if(rc.equals("0")) {
			result = true;
		}else {
			String msg = json.getString("msg");
			LogPF.sdk.info("懒猫sdk error accountId={},msg={}",accountId,msg);
		}
		
		
		LogPF.sdk.info("懒猫sdk costTime={} ms",Port.getTime()-startTime);
		port.returnsAsync(pid,"result", result, "accountId", accountId, "uid", uid);
	}
}
