package game.platform.login.sdkPLATFORM;

import game.platform.DistrPF;
import game.platform.LogPF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import core.support.SysException;
import core.support.Utils;
import core.support.log.LogCore;

/**
 * 平台登录
 */
public class LoginPLATFORM {

	//private static final String URLCheck = "http://10.163.254.246:8082/rest/sdk/token";
	
	public static void main(String[] args) {
		//System.out.println(verifySession("1_644805704", "bc9b0e91d87b420b8e0b8c5f52975b53"));
	}

	/**
	 * 验证登录令牌，返回账号ID
	 * @param uid
	 * @param input_token
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String checkToken(String uid, String input_token) {
		// 调用URL进行登录验证
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("token", input_token);
		String tokenResult = checkToken(params);
		return parseResult(tokenResult);
	}
	
	/**
	 * 七政平台sdk校验
	 * @param adid 渠道id
	 * @param serverId 服务器id
	 * @param token token
	 * @return
	 */
	public static String checkQzToken(int adid,int serverId,String token){
		// 调用URL进行登录验证
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("adid", String.valueOf(adid));
		params.put("serverId",String.valueOf(serverId));
		params.put("token", token);
		String tokenResult = checkToken(params);
		return parseResult(tokenResult);
	}
	
	/**
	 * 验证登录令牌
	 */
	private static String checkToken(LinkedHashMap<String, String> params) {
		try {
			// 1 拼接地址
			StringBuilder urlSB = new StringBuilder(DistrPF.HTTP_SDK_TOKEN);
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
			System.out.println("========= HttpGet ========="+ urlStrFinal);// 

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
	 * 解析返回账号ID
	 */
	public static String parseResult(String tokenResult) {
		LogPF.platform.info("===平台登录验证：parseResult tokenResult={}", tokenResult);
		String accountId = null;
		boolean result = Utils.getJSONValueBoolean(tokenResult, "result");
		if (result) {
			accountId = Utils.getJSONValueStr(tokenResult, "accountId");
		}
		return accountId;
	}
	
}
