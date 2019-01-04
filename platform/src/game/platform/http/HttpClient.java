package game.platform.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import com.alibaba.fastjson.JSONObject;

public class HttpClient {
	HttpURLConnection _HttpURLConnection = null;
	URL url = null;
	@SuppressWarnings("unused")
	private String DEFAULT_PROTOCOL = "http";
	@SuppressWarnings("unused")
	private String SLASH = "/";
	@SuppressWarnings("unused")
	private String COLON = ":";
	public String DEFAULT_NET_ERROR = "NetError";
	public String POST = "POST";

	public String doPost(byte[] param) {
		String result = "";
		try {
			_HttpURLConnection = (HttpURLConnection) url.openConnection();
			_HttpURLConnection.setRequestMethod(POST);
			_HttpURLConnection.setDoOutput(true);
			_HttpURLConnection.setRequestProperty("Content-Type", "application/msgpack");
			_HttpURLConnection.setRequestProperty("Content-Length", String.valueOf(param.length));
			DataOutputStream ds = new DataOutputStream(_HttpURLConnection.getOutputStream());
			ds.write(param);
			ds.flush();
			ds.close();
			result = _gzipStream2Str(_HttpURLConnection.getInputStream());
			_HttpURLConnection.disconnect();
		} catch (Exception e) {
			_HttpURLConnection.disconnect();
			e.printStackTrace();
		}
		return result;
	}
	
	public String doPost(String param) {
		String result = "";
		BufferedReader in = null;
		byte[] paramByte = param.getBytes();
		try {
			_HttpURLConnection = (HttpURLConnection) url.openConnection();
			_HttpURLConnection.setRequestMethod(POST);
			_HttpURLConnection.setDoOutput(true);
			_HttpURLConnection.setRequestProperty("Content-Type", "application/msgpack");
			_HttpURLConnection.setRequestProperty("Content-Length", String.valueOf(paramByte.length));
			DataOutputStream ds = new DataOutputStream(_HttpURLConnection.getOutputStream());
			ds.write(paramByte);
			ds.flush();
			ds.close();
			in = new BufferedReader(new InputStreamReader(_HttpURLConnection.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			in.close();
			in = null;
			_HttpURLConnection.disconnect();
		} catch (Exception e) {
			_HttpURLConnection.disconnect();
			e.printStackTrace();
		} finally{
			if(in!=null){
				try{
				in.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				in = null;
			}
		}
		return result;
	}

	private String _gzipStream2Str(InputStream inputStream) throws IOException {
		GZIPInputStream gzipinputStream = new GZIPInputStream(inputStream);
		byte[] buf = new byte[1024];
		int num = -1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((num = gzipinputStream.read(buf, 0, buf.length)) != -1) {
			baos.write(buf, 0, num);
		}
		return new String(baos.toByteArray(), "utf-8");
	}

	public HttpClient(String urlString) {
		try {
			url = new URL(urlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//测试聊天上传
//	public static void main(String[] args) {
//		JSONArray ja = new JSONArray();
//		JSONObject json = new JSONObject();
//		json.put("msgID",  "12313123");
//		json.put("status",  "success");
//			json.put("OS",  "ios");
//		json.put("accountID",  "1231123123");
//		json.put("orderID",  "orderId");
//		json.put("currencyAmount",  123);
//		json.put("currencyType",  "CNY");
//		json.put("virtualCurrencyAmount",  123);
//		json.put("chargeTime", 13123123);
//		
//		ja.add(json);
//		System.out.println(ja);
//		uploadChargeRecord("http://api.talkinggame.com/api/charge/212944421E5AD4C07F8A1FFFB8714DA4", ja.toJSONString());
//	}
//	public static void uploadChargeRecord(String url, String jsonData){
//		byte[] dataByte = Utils.gzip(jsonData);
//		HttpClient clinet = new HttpClient(url);
//		System.out.println("result data : " + clinet.doPost(dataByte));
//	}
	
	//测试充值
//	public static void main(String[] args) {
//		JSONObject jo = new JSONObject();
//		jo.put("tableKey", "general");
//		JSONObject param = new JSONObject();
//		param.put("star", 6);
//		jo.put("param", param);
//		
//		HttpClient hc = new HttpClient("http://127.0.0.1:8018/countBy");
//		System.out.println(hc.doPost(jo.toJSONString()));
//		
//	}
	public static void main(String[] args) {
		JSONObject jo = new JSONObject();
//		jo.put("roleId", "100010000192440002");
//		jo.put("orderId", "145");
//		jo.put("propId", "lingzhu_1");
//		jo.put("actualPrice", "600");
//		jo.put("chargePrice", "600");
		jo.put("userId", "100000000000100016");
		jo.put("result", "true");
		jo.put("productId", "mzcs_qzgametw_05");

		
		
		jo.put("currencyType", "1");
		jo.put("sign", "4f084e92796d8200a829634c0adc2d33");
		jo.put("userId", "010282008C9E70AB3957964CF4F3E28258366C7");
		jo.put("serverId", "2001");
		jo.put("chargeUnitId", "96103");
		jo.put("extendParams", "");
		jo.put("deviceGroupId", "0000");
		jo.put("serviceId", "1000094131021000000");
		jo.put("localeId", "01");
		jo.put("channelId", "3102100031021000");
		jo.put("payChannelId", "210210000014013051014300");
		
		HttpClient hc = new HttpClient("http://127.0.0.1:8018/payNotify");
		System.out.println(hc.doPost(jo.toJSONString()));
		
	}
	//测试封号禁言
//	public static void main(String[] args) {
//		JSONObject jo = new JSONObject();
//		jo.put("service", "palm.platfom.productServer.shutUp");
//		jo.put("serverId", "1001");
//		jo.put("type", "BANROLE");
//		
//		JSONArray ja = new JSONArray();
//		JSONObject data = new JSONObject();
//		data.put("userId", "n1");
//		data.put("roleId", "100010000192440002");
//		data.put("beginTime", "2015-07-06 20:09:10");
//		data.put("endTime", "2016-10-07 10:10:50");
//		
//		ja.add(data);
//		
//		jo.put("data", ja);
//		
//		HttpClient hc = new HttpClient("http://127.0.0.1:8018/banRole");
//		System.out.println(hc.doPost(jo.toJSONString()));
//	}
	
	//查询
//	public static void main(String[] args) {
//		JSONObject jo = new JSONObject();
//		jo.put("service", "palm.platfom.productServer.getProductUserInfo");
//		jo.put("serverId", "1001");
//		jo.put("searchId", "n11");
//		jo.put("type", "BYUSERID");
//		jo.put("type", "BYROLEID");
//		jo.put("type", "BYROLENAME");
//		
//		
//		HttpClient hc = new HttpClient("http://127.0.0.1:8018/queryRole");
//		System.out.println(hc.doPost(jo.toJSONString()));
//	}
}