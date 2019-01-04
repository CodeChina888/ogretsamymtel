package game.platform.login.sdkFACEBOOK;

import game.platform.LogPF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSONObject;

import core.support.SysException;
import core.support.Utils;
import game.platform.login.sdkMI.HmacSHA1Encryption;

/**
 * FACEBOOK登录
 */
public class LoginFACEBOOK {

	private static final String URLCheck = "https://graph.facebook.com/debug_token";

	public static final String AppID = "1701200750119353";// AppID
	public static final String AppSecret = "f0d57969ade91395af7323a3159eab26";// AppSecret
	
	public static void main(String[] args) {
		//System.out.println(verifySession("1_644805704", "bc9b0e91d87b420b8e0b8c5f52975b53"));
	}

	/**
	 * 验证登录令牌
	 * @param uid
	 * @param input_token
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static int checkToken(String uid, String input_token) {
		// 调用URL进行登录验证
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("access_token", AppID + "|" + AppSecret);
		params.put("input_token", input_token);
		String result = checkToken(params);
		return parseResult(result);// 0成功，非0错误码
	}
	
	/**
	 * 验证登录令牌
	 */
	private static String checkToken(LinkedHashMap<String, String> params) {
		try {
			// 1 拼接地址
			StringBuilder urlSB = new StringBuilder(URLCheck);
			// 1.1 有需要拼接的参数
			if (!params.isEmpty()) {
				urlSB.append("?");
			}

			// 1.2 拼接参数
			for (Entry<String, String> entry : params.entrySet()) {
				Object value = entry.getValue();
				String v = (value == null) ? "" : URLEncoder.encode(entry.getValue().toString(), "UTF-8");

				urlSB.append(entry.getKey()).append("=").append(v).append("&");
			}

			// 1.3 最终地址
			String urlStrFinal = urlSB.toString();

			// 1.4 去除末尾的&
			if (urlStrFinal.endsWith("&")) {
				urlStrFinal = urlStrFinal.substring(0, urlStrFinal.length() - 1);
			}
			LogPF.platform.info("===平台登录验证：checkToken urlStrFinal={}", urlStrFinal);
			
			// 请求地址
			HttpGet get = new HttpGet(urlStrFinal);

			// 准备环境
			try (CloseableHttpClient http = HttpClients.createDefault();
					CloseableHttpResponse response = http.execute(get);) {

				// 返回内容
				HttpEntity entity = response.getEntity();

				// 主体数据
				InputStream in = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				// 读取
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
				return sb.toString();
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 解析返回结果
	 */
	public static int parseResult(String result) {
		LogPF.platform.info("===平台登录验证：parseResult result={}", result);
		
		int errcode = -1;// 0成功，非0错误码
		JSONObject jo = Utils.toJSONObject(result);
		Map<String, Object> data = jo.getJSONObject("data");
		boolean isValid = false;
		if (data.containsKey("is_valid")) {
			isValid = Utils.booleanValue(data.get("is_valid"));
		}
		if (isValid) {// 有效
			errcode = 0;
		} else {// 无效
			if (data.containsKey("error")) {
				Map<String, Object> error = Utils.toJSONObject(String.valueOf(data.get("error")));
				if (error.containsKey("code")) {
					errcode = Utils.intValue(error.get("code"));
				}
			}
		}
		return errcode;
	}
	
}
