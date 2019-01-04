package game.platform.login.sdkSina;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import core.support.Utils;
import core.support.log.LogCore;
import game.platform.DistrPF;
import game.platform.LogPF;
import game.platform.http.HttpAsyncSendServiceProxy;
import game.platform.http.Request;
import game.platform.integration.PayPlaceConf;
import game.platform.integration.PayPlaceIntegration;
import game.platform.integration.PayPlaceKey;
import game.platform.login.sdk.SecurityUtil;
import game.platform.support.ReasonResult;;

//App Key：1246832129
//App Secret：e0b5343ce7b97a7e2f8710b4da5db916
//redirectURL：http://m.game.weibo.cn/oauth2/default
//signature key：9a968445e2197f716ffb2484184cd4ad


@PayPlaceConf(PayPlaceKey.sina)
public class PlaceSina extends PayPlaceIntegration{
	//新浪用户登录校验接口
	//旧接口
	
	//private static final String CHECK_TOKEN_URL = "http://m.game.weibo.cn/ddsdk/distsvc/1/user/serververify";
	//新接口
	
	private static final String CHECK_TOKEN_URL = "http://m.game.weibo.cn/ddsdk/distsvc/1/user/serververify_v2";
	
	
	@SuppressWarnings("unused")
	private static final String IPID = "0002";
	
	public static final String APPID = "1001";//申请app时进行替换
	public static final String APPKEY = "1246832129";
	public static final String SECURITY_KEY = "e0b5343ce7b97a7e2f8710b4da5db916";//申请app时进行替换
	public static final String SIGNATURE_KEY = "9a968445e2197f716ffb2484184cd4ad";//登录签名用
	
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;
	public static final int CHECKTOKEN_ERROR = 2;
	public static final int OTHER_ERROR = -99;
	
	public static final String STATUS_RECEIVE = "0001";	//收到计费服务器的请求
	public static final String STATUS_RECEIVE_DESC = "发货中";	//
	public static final String STATUS_SUCCESS = "0002";	//添加物品成功
	public static final String STATUS_SUCCESS_DESC = "发送成功";	//成功描述
	
	public static final String STATUS_FAIL = "1005";	//发货失败
	public static final String STATUS_FAIL_DESC = "充值失败";	//失败描述

	
	public static void main(String[] args) {
		String uid = "200001026346";
		String token = "2.0038auHCdzZ43B18bbf77185XO9TgC";
		checkToken(uid, token);
	}

	/**
	 * 校验接口
	 * @param uid
	 * @param token
	 * @return 默认正确（由于很慢，不进行等待）
	 */
	public static int checkToken(String uid, String token) {

		Map<String, String> params = new HashMap<String, String>();
		//新接口参数
		params.put("suid", uid);		// suid用户的编号  
		params.put("appkey", APPKEY);	// appkey当前游戏的key
		params.put("token", token);		// token用户身份标识
		//生成参数校验签名
		String signature = makeLoginSign(params);
		params.put("signature", signature); // signature签名串

		//*//开~关 新浪校验回执(通过本行第一个斜杠/控制)
		checkTokenUnreceipt(params);
		return SUCCESS;
	}
	/**
	 * 新浪校验
	 * @param params
	 */
	public static void checkTokenUnreceipt(Map<String, String> params) {
		// 连接一个随机的验证服务
		String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_HTTP_ASYNC);
		// 远程校验
		HttpAsyncSendServiceProxy asyncSendProxy = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
		asyncSendProxy.httpGetAsync(CHECK_TOKEN_URL, params, false);
//		asyncSendProxy.listenResult();
	}
	
	/**
	 * @param result
	 * @return
	 */
	public static int parseCheckTokenResult(String result, String uid, String token) {

		try {
			Map<String, Object> resMap = JSON.parseObject(result);
			if(resMap != null){
				//System.out.println("Receive the remote Indentity:["+result+"].");
				if(resMap.containsKey("request") || resMap.containsKey("error_code") || resMap.containsKey("error")){
					
					return FAIL;
				}
				if(resMap.containsKey("suid") && resMap.containsKey("token")){
					String resUid = resMap.get("suid").toString();
					String resToken = resMap.get("token").toString();
					if(resUid.equals(uid) && resToken.equals(token)){
						return SUCCESS;
					}
				}
			}
		}catch (Exception e) {
			System.out.println("parseCheckTokenResult Exception:"+e.getMessage());
			}
		return OTHER_ERROR;
	}

	private static String makeLoginSign(Map<String, String> params){
		String signStr = "";

		List<String> keyList = new ArrayList<String>();
		keyList.addAll(params.keySet());
		Collections.sort(keyList); // 将所有待签名参数按参数名排序

		// 把数组所有元素，按照“参数|参数值”的模式用“|”字符拼接成字符串，组成字符串A
		for (String key : keyList) {
			signStr += key + "|" + params.get(key) + "|";
		}
		
		// 上面尾部刚好多个"|"，留给后面B = A+"|"+appsecret
		// 将字符串A与appsecret，用英文竖杠进行连接, 得到字符串B
		signStr += SIGNATURE_KEY;
		// LogCore.conn.info("makeLoginSign signStr:[" + signStr + "]");
		
		String signature = SecurityUtil.sha1(signStr);
		// LogCore.conn.info("makeLoginSign signature:[" + signature + "]");
		return signature;
	}
	
	/**
	 * 验证登陆信息
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String checkToken(Map<String, String> params) {
		try {
			StringBuilder urlSB;
			//1 拼接地址
			urlSB = new StringBuilder(CHECK_TOKEN_URL); 
			
			
			//1.1 有需要拼接的参数
			if(!params.isEmpty()) {
				urlSB.append("?");
			}

			//1.2 拼接参数
			for(Entry<String, String> entry : params.entrySet()) {
				Object value = entry.getValue();
				String v = (value == null) ? "" : URLEncoder.encode(entry.getValue(), "UTF-8");
				urlSB.append(entry.getKey()).append("=").append(v).append("&");
			}
			
			//1.3 最终地址
			String urlStrFinal = urlSB.toString();
			
			//1.4 去除末尾的&
			if(urlStrFinal.endsWith("&")) {
				urlStrFinal = urlStrFinal.substring(0, urlStrFinal.length() - 1);
			}

			LogCore.conn.info("checkToken HttpGet  {}", urlStrFinal);
			//请求地址
			HttpGet get = new HttpGet(urlStrFinal);

			//准备环境
			CloseableHttpClient http = HttpClients.createDefault();
			

			
			CloseableHttpResponse response = http.execute(get);
			//返回内容
		    HttpEntity entity = response.getEntity();
		    //主体数据
		    InputStream in = entity.getContent();  
		    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		    //读取
		    StringBuilder sb = new StringBuilder();
		    String line = null;  
		    while ((line = reader.readLine()) != null) {  
		    	sb.append(line);
		    }
		    reader.close();
		    reader = null;
		    return sb.toString();
			
		} catch (Exception e) {
			LogCore.conn.error("checkToken Exception : {}", ExceptionUtils.getStackTrace(e));
			return "";
		}
	}
	//对接文档 
	//https://git.oschina.net/sinagamesdk/sina_eversdk/blob/master/%E6%96%B0%E6%B5%AA%E6%B8%B8%E6%88%8F%E8%9E%8D%E5%90%88SDK%E6%8E%A5%E5%85%A5%E6%96%87%E6%A1%A3%E2%80%94%E6%9C%8D%E5%8A%A1%E7%AB%AF.md
	//旧接口参数字段	类型	说明	必选
	//cid	int	渠道标识	true
	//deviceid	string	用户设备id，设备登录渠道必须	false
	//uid	string	登录用户id	true
	//appkey	string	应用的appkey	true
	//token	string	用户登录授权	true
	//signature	string	用于参数校验的签名，生成办法参考2.2	true
		
	//	新接口参数字段 	类型 	说明 	必选
	//	suid 	string 	融合sdk用户唯一标识，只包含数字，对应sdk返回的suid 	true
	//	appkey 	string 	应用的appkey 	true
	//	token 	string 	用户登录授权 	true
	//	signature 	string 	用于参数校验的签名，生成办法参考1.2 	true
	@Override
	public ReasonResult checkToken(JSONObject json) {
		//验证信息
//		String uid = json.getString("uid");
//		String token = json.getString("token");
//		String cid = json.getString("cid");
//		String deviceid = json.getString("deviceid");
//		int result = checkToken(uid, token, cid, deviceid);
//		//返回结果
//		ReasonResult rt = new ReasonResult();
//		rt.success = (result == 0);
//		rt.code = result;
//		rt.reason = "";
		
		ReasonResult rt = new ReasonResult();
		return rt;
	}

	@Override
	public ReasonResult payCheck(Request req) {
		LogPF.platform.info("payCheck");
		ReasonResult result = new ReasonResult();
		result.success = true;
		
//		JSONObject jo = new JSONObject();
//		JSONObject status = new JSONObject();
//		status.put("deliverCode", STATUS_RECEIVE);
//		status.put("deliverDesc", "发货中");
//		jo.put("common", status);
//		//返回给计费服务器
//		req.result(Utils.toJSONString(jo));
		
		return result;
	}

	@Override
	public String paySuccess(String reason) {
		return null;
	}

	@Override
	public String payFail(String reason) {
		return null;
	}

	@Override
	public void paySuccess(Request req, String reason) {
		
		JSONObject result = new JSONObject();
		result.put("deliverCode", STATUS_SUCCESS);
		result.put("deliverDesc", "0"/*STATUS_SUCCESS_DESC*/);    //  PF_PAY_Manager.PAY_DELIVER_DESC_SUCCESS
		result.put("orderId", req.params.get("orderId"));
		result.put("result", true);
		req.result(Utils.toJSONString(result));
		LogPF.platform.info("POST返回：result={}", result);

	}

	@Override
	public void payFail(Request req, String reason) {
		
		JSONObject result = new JSONObject();
		result.put("deliverCode", STATUS_RECEIVE);
		result.put("deliverDesc", reason);
		result.put("orderId", req.params.get("orderId"));	
		result.put("result", false);
		req.result(Utils.toJSONString(result));
		LogPF.platform.info("POST返回：result={}", result);
	}
}
