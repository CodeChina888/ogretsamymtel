package game.platform.login.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import core.support.SysException;
import core.support.Utils;
import game.platform.login.sdk.SecurityUtil;

/**
 * SDK登录
 */
public class Login {

	private static final String URLCheck = "http://api.dev.laohu.com/user/sdkCheckToken";

	public static final String AppID = "1001";// AppID
	public static final String AppSecret = "d44aad69bb50d9bb321fa1298c1cdeed";// AppSecret
	
	public static void main(String[] args) {
		//System.out.println(checkToken("1_644805704", "bc9b0e91d87b420b8e0b8c5f52975b53"));
	}

	/**
	 * 验证登录令牌
	 * @param userIdentity
	 * @param token
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static int checkToken(String userIdentity, String token) {
		StringBuilder origin = new StringBuilder();
		origin.append(userIdentity);
		origin.append(token);
		origin.append(AppID);
		origin.append(AppSecret);
		String sign = SecurityUtil.md5(origin.toString());

		Map<String, String> params = new HashMap<String, String>();
		params.put("userIdentity", userIdentity);
		params.put("token", token);
		params.put("appId", AppID);
		params.put("sign", sign);

		String result = checkToken(params);
		return parseResult(result);// 0成功，非0错误码
	}

	/**
	 * 验证登录令牌
	 */
	private static String checkToken(Map<String, String> params) {
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
		int errcode = -1;// 0成功，非0错误码
		Map<String, Object> resMap = Utils.toJSONObject(result);
		if (resMap != null) {
			Object code = resMap.get("code");
			if (code instanceof Integer) {
				errcode = (Integer) code;
			} else if (code instanceof String) {
				errcode = Integer.valueOf((String) code);
			}
		}
		return errcode;
	}
}
