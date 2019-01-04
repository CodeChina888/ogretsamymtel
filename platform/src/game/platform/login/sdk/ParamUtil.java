package game.platform.login.sdk;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.net.URLEncoder;
import java.net.URLDecoder;

public class ParamUtil {

	public static String encode(String s, String charset) {
		try {
			return URLEncoder.encode(s, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decode(String s, String charset) {
		try {
			return URLDecoder.decode(s, charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSourceParams(HashMap<String, String> params) {
		StringBuilder sb = new StringBuilder();
		TreeMap<String, String> paramsMap = new TreeMap<String, String>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			paramsMap.put(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		return sb.toString();
	}

	public static String joinValueOnly(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		TreeMap<String, String> paramsMap = new TreeMap<String, String>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			paramsMap.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			sb.append(entry.getValue());
		}
		return sb.toString();
	}

	public static String getSourceParams(HashMap<String, String> params, String md5key) {
		return new StringBuilder().append(getSourceParams(params)).append("md5key=").append(md5key).toString();
	}
}
