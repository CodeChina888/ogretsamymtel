package game.platform.integration;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

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
import game.platform.support.ReasonResult;
import game.platform.login.sdk.SecurityUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 支付请求
 * @author 
 *
 */
public class PayManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static PayManager getInstance() {
		return inst(PayManager.class);
	}
	
	/**
	 * 处理后台iOS支付通知
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.IOS_PAY_NOTIFY)
	public void onIOSPay(Param param) {
		LogPF.platform.info("onIOSPayNotify");
		try {
			_payIOS(param);
		} catch (Exception e) {
			Request req = param.get("req");
			req.result(HttpServerHandler.RESULT_ERROR_EXE);
			
			LogPF.platform.error("执行iOS支付请求时发生错误={}", param, e);
		}
	}
	/**
	 * 处理后台iOS支付通知
	 * @param param
	 */
	private void _payIOS(Param param) {
		Request req = param.get("req");
		
		String platform = Config.GAME_PLATFORM_NAME;
		PayPlaceIntegration inte = PayPlaceIntegration.getInstance("sina");//写死新浪，因为外网走的是后台，不直接连服务器，由GM后台统一接入参数
		ReasonResult rt = inte.payCheck(req);
		//验证状态
		if(!rt.success) {
			req.result(inte.payFail(rt.reason));
			return;
		}
		
		//充值
		String json = Utils.toJSONString(req.params);
		Port port = Port.getCurrent();

		CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT),Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
		port.call(toPoint, 5, new Object[]{ json });	//远程调用  PF5_payNoticeIOS
		port.listenResult(this::_result_pay_ios, "req", req, "inte", inte);
	}
	public void _result_pay_ios(Param results, Param context) {
		Request req = context.get("req");
//		String json = Utils.toJSONString(req.params);
//		JSONObject param = Utils.toJSONObject(json);
		PayPlaceIntegration inte = context.get("inte");
		boolean success = results.getBoolean("success");
		if(success){
			inte.paySuccess(req, results.getString("reason"));
		}else{
			inte.payFail(req, results.getString("reason"));
		}
	}

	/**
	 * 处理支付通知
	 * @param param
	 */
	@Listener(value=EventKeyPF.HTTP_RECEIVE, subStr=HttpServerHandler.PAY_NOTIFY)
	public void onPay(Param param) {
		LogPF.platform.info("onPay");
		try {
			_pay(param);
		} catch (Exception e) {
			Request req = Utils.getParamValue(param, "req", null);
			req.result(HttpServerHandler.RESULT_ERROR_EXE);
			
			LogPF.platform.error("执行支付请求时发生错误={}", param, e);
		}
	}
	
	/**
	 * 处理支付通知
	 * @param param
	 */
	private void _pay(Param param) {
		// 新浪
		Request req = Utils.getParamValue(param, "req", null);
		
		String platform = Config.GAME_PLATFORM_NAME;
		PayPlaceIntegration inte = PayPlaceIntegration.getInstance("sina");//写死新浪，因为外网走的是后台，不直接连服务器，由GM后台统一接入参数
		ReasonResult rt = inte.payCheck(req);
		
		//验证状态
		if(!rt.success) {
			req.result(inte.payFail(rt.reason));
			return;
		}
		//充值
		String json = Utils.toJSONString(req.params);
		Port port = Port.getCurrent();
		CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT),Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
		port.call(toPoint, 1, new Object[]{ json });	//远程调用  PF1_payNotice
		port.listenResult(this::_result_pay, "req", req, "inte", inte);
		
/**		// 公司后台
		Request req = Utils.getParamValue(param, "req", null);
		
		//渠道KEY
//		String pfKey = req.params.remove("channelId");

		//获取平台处理代码
//		PayPlaceIntegration inte = PayPlaceIntegration.getInstance("zhangqu");
//		ReasonResult rt = inte.payCheck(req);
//		
//		//验证状态
//		if(!rt.success) {
//			req.result(inte.payFail(rt.reason));
//			return;
//		}
//		req.getBoolean("result");
		//充值
		String json = Utils.toJSONString(req.params);
		Port port = Port.getCurrent();
		CallPoint toPoint = new CallPoint(Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT), Distr.PORT_DEFAULT, DistrPF.SERV_WORLD_PF);
		port.call(false, toPoint, CallMethodType.PAY.value(), CallMethodType.PAY.name(), new Object[]{ json });
		port.listenResult(this::_result_pay, "req", req);//, "inte", inte);
*/
	}
	public void _result_pay(Param results, Param context) {
		// 新浪
		Request req = context.get("req");
		PayPlaceIntegration inte = context.get("inte");
		boolean success = results.getBoolean("result");
		if(success){
			inte.paySuccess(req, results.getString("reason"));
			
		}else{
			inte.payFail(req, results.getString("reason"));
		}
		
/**		// 公司后台
  		Request req = context.get("req");
//		String json = Utils.toJSONString(req.params);
//		JSONObject param = Utils.toJSONObject(json);
//		PayPlaceIntegration inte = context.get("inte");
//		boolean success = results.getBoolean("success");
		boolean success = results.getBoolean("result");
		String msg = results.getString("reason");

		if(success){
//			inte.paySuccess(req, results.getString("reason"));
			JSONObject result = new JSONObject();
			result.put("result", "true");
			result.put("msg", "充值成功");
			req.result(Utils.toJSONString(result));
			LogPF.platform.info("POST返回：result{}", result);
//			int prefix = Integer.parseInt(Config.GAME_SERVER_PREFIX);
//			if(prefix < 20000){//现在仅限ios
//				
//				JSONObject jo = new JSONObject();
//				
//				JSONObject commonJson = new JSONObject();
//				commonJson.put("interfaceId", "0002");
//				commonJson.put("pCode", "");
//				jo.put("common", commonJson);
//				
//				JSONObject userInfo = new JSONObject();
//				userInfo.put("userName", "");
//				userInfo.put("userId", param.getString("userId"));
//				userInfo.put("type", "BYUSERID");
//				jo.put("options", userInfo);
//				
//				Map<String, String> params = new HashMap<String, String>();
//				params.put("jsonStr", jo.toJSONString());
//				String result = getPostMethod("http://auth.gamebean.net/ucenter1.0/platform/main.do", params);
//				LogCore.temp.info("result={}",result);
//				
//				syncToTalkingData("66987dca06644a07abd7c2b3dc62d585", req, result);
//			}
		}else{
//			inte.payFail(req, results.getString("reason"));
			JSONObject result = new JSONObject();
			result.put("result", "false");
			result.put("msg", msg);
			req.result(Utils.toJSONString(result));
			LogPF.platform.info("POST返回：result{}", result);
			
			
		}
*/
	}
	
	/**
	 * 充值成功后同步数据到TalkingData
	 * @param appKey
	 * @param req
	 */
	public void syncToTalkingData(String appKey, Request req, String clientJson){
		String json = Utils.toJSONString(req.params);
		JSONObject jo = Utils.toJSONObject(json);
		JSONObject client = Utils.toJSONObject(clientJson);
		JSONObject options = (JSONObject)client.get("options");
		//String IDFA = "9294B69E-B4BD-438A-AB43-7998347219A1";
		if(options == null){
			//没有IDFA直接返回(即:仅限ios)
			return;
		}
		String IDFA = (String)options.get("IDFA");
		//构建一个json对象
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("msgid", SecurityUtil.getUUID());
		jsonObj.put("actiontime", Port.getTime());
		jsonObj.put("platform", "2");
		jsonObj.put("osversion", "9.2");
		jsonObj.put("ip", "0.0.0.0");
		jsonObj.put("orderid", jo.getString("orderId"));
		jsonObj.put("account", jo.getString("userId"));
		jsonObj.put("payamount", jo.getString("actualPrice"));
		jsonObj.put("paycurrency", getCurrencyType(jo.getIntValue("currencyType")));
		jsonObj.put("paytype", "苹果iap");
		jsonObj.put("mac", options.get("MAC"));
		jsonObj.put("idfa", IDFA);
		jsonObj.put("idfv", "");
		jsonObj.put("gpid", "");
		jsonObj.put("androidid", "");
		jsonObj.put("manufacturer", "");
		jsonObj.put("devicemodel", "");
		jsonObj.put("operators", "");
		jsonObj.put("network", "");
		jsonObj.put("imsi", "");
		jsonObj.put("imei", "");
		jsonObj.put("devicename", "");
		jsonArray.add(jsonObj);
		// 请求代码
		try{
			URL url = new URL(Utils.createStr("http://a.appcpa.net/ss/v1/pay/{}", appKey));
			HttpURLConnection _HttpURLConnection = (HttpURLConnection)url.openConnection();
			_HttpURLConnection.setRequestMethod("POST");
			_HttpURLConnection.setDoOutput(true);
			_HttpURLConnection.setRequestProperty("content-encoding", "gzip");
			_HttpURLConnection.setRequestProperty("Content-Type", "application/octet-stream");
			String message = jsonArray.toJSONString();
			byte[] body = compress(message.getBytes("UTF-8"));
			_HttpURLConnection.setRequestProperty("Content-Length", String.valueOf(body.length));
			DataOutputStream ds = new DataOutputStream(_HttpURLConnection.getOutputStream());
			ds.write(body);
			ds.flush();
			ds.close();
			InputStream inputStream = _HttpURLConnection.getInputStream();
			byte[] returns = new byte[inputStream.available()];
			inputStream.read(returns);
			LogPF.platform.info("syncToTalkingData:"+new String(returns, "UTF-8"));
			_HttpURLConnection.disconnect();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	
	// GZIP 压缩
	public static byte[] compress(byte[] message) throws IOException {
		ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(message.length);
		DataOutputStream outputStream = null;
		 try {
			 outputStream = new DataOutputStream(new
			 GZIPOutputStream(byteArrayStream));
			 outputStream.write(message);
			 outputStream.flush();
		 } finally {
			 if (outputStream != null) {
				 outputStream.close();
			 }
		 }
		 return byteArrayStream.toByteArray();
	}
	
	/**
	 * 获取充值的货币名称
	 * // currencyType 1人民币 2美元 3日元 4港币 5英镑 6新加坡币 7越南盾 8台币 9韩元
	 * // talkingData  人民币 CNY，美元 USD；日元 JPY；港元 HKD，英镑 GBP，台币：TWD，欧元 EUR；
	 * @param currencyType
	 * @return
	 */
	public String getCurrencyType(int currencyType){
		switch(currencyType){
			case 1:
				return "CNY";
			case 2:
				return "USD";
			case 3:
				return "JPY";
			case 4:
				return "HKD";
			case 5:
				return "GBP";
			case 8:
				return "TWD";
			default:
				return "CNY";
		}
	}
	
	
	/**
	 * 使用POST方式提交数据
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getPostMethod(String url, Map<String, String> params) {
		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (Entry<String, String> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
			}
//			sb.substring(0, sb.length() - 1);
			//删掉最后一个&
			sb.deleteCharAt(sb.length() - 1);
		}
		
		// 用于返回内容的记录
		StringBuffer buffer = new StringBuffer();
		
		// 尝试发送请求
		try {
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestProperty(
					"oUa",
					"oUa=0|Nexus 5|4.4.4|1080*1776|9d05ab13-7c6c-30b6-b921-e1dc0ecbb503|46002|0|google");
			con.setRequestProperty("version", "1.3.5|1.3.5|4.5.6");
			con.setRequestProperty("device", "123|ert|345");
			con.setRequestProperty("oService", "1000097931069301000");
			con.setRequestProperty("ochannel", "3106930131069301");
			con.setRequestProperty("localeId", "08");
			OutputStreamWriter osw = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");

			osw.write(sb.toString());
			osw.flush();
			osw.close();
			
			//读取数据流
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		
		return buffer.toString();
	}
	
}
