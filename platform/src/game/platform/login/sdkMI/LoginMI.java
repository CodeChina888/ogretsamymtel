package game.platform.login.sdkMI;

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
import core.support.SysException;
import core.support.Utils;
import game.platform.login.sdkMI.HmacSHA1Encryption;

/**
 * 小米登录
 */
public class LoginMI {

	private static final String URLCheck = "http://mis.migc.xiaomi.com/api/biz/service/verifySession.do";

	public static final String AppID = "2882303761517418827";// AppID
	public static final String AppSecret = "EPX8bUl42hYoSzQPT+zWYw==";// AppSecret
	
	public static void main(String[] args) {
		//System.out.println(verifySession("1_644805704", "bc9b0e91d87b420b8e0b8c5f52975b53"));
	}

	/**
	 * 验证登录令牌
	 * @param uid
	 * @param session
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static int checkToken(String uid, String session) {
		String encryptText = "appId="+AppID;
		encryptText += "&session="+session;
		encryptText += "&uid="+uid;
		String signature = "";	// 签名
		try {
			signature = HmacSHA1Encryption.HmacSHA1Encrypt(encryptText, AppSecret);
		} catch (Exception e) {
			e.printStackTrace();
			LogPF.platform.error("===error in checkToken HmacSHA1Encrypt");
		}
		// 调用URL进行登录验证
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("appId", AppID);			// 游戏ID
		params.put("session", session);		// 用户sessionID
		params.put("uid", uid);				// 用户ID
		params.put("signature", signature);	// 签名
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
		
		int errcode = 1001;// 1001系统错误
		Map<String, Object> resMap = Utils.toJSONObject(result);
		if (resMap != null) {
			Object code = resMap.get("errcode");
			if (code instanceof Integer) {
				errcode = (Integer) code;
			} else if (code instanceof String) {
				errcode = Integer.valueOf((String) code);
			}
		}
		// 0成功，非0错误码
		if (errcode == 200) {// 200成功
			return 0;
		} else {
			return errcode;
		}
	}
	
}
