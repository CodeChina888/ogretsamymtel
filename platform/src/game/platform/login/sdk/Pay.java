/**
 * 
 */
package game.platform.login.sdk;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import core.support.Utils;
import game.platform.login.sdk.Login;
import game.platform.login.sdk.ParamUtil;
import game.platform.login.sdk.SecurityUtil;

public class Pay {
	public String generate(String userIdentity, String appId, String generalOrder, String t, String appOrder,
			String amount, String payStatus, String serverId, String ext, String sign) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("userIdentity", userIdentity);
		params.put("appId", appId); // 通用sdk的appid
		params.put("generalOrder", generalOrder);
		params.put("t", String.valueOf(t));
		params.put("appOrder", appOrder);
		params.put("amount", amount);
		params.put("payStatus", payStatus);
		if (null != serverId && Integer.parseInt(serverId) > 0) {
			params.put("serverId", serverId);
		}
		if (!StringUtils.isEmpty(ext)) {
			params.put("ext", ext);
		}
		System.out.println("parameters -- " + params);
		String original = ParamUtil.joinValueOnly(params);

		String signature = SecurityUtil.md5(original + Login.AppSecret);
		System.out.println("param sign -- " + sign);
		System.out.println("game signature -- " + signature);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (signature.equalsIgnoreCase(sign)) {
			if ("1".equals(payStatus)) {
				System.out.println("pay -- success");
				// 支付成功
				// TODO: 游戏相应的业务逻辑
				resultMap.put("code", "0");// 成功则返回0
			} else {
				System.out.println("pay -- fail");
				resultMap.put("code", "1");// 失败
			}

		} else {
			System.out.println("pay -- sign does not match");
			resultMap.put("code", "2");// 签名不匹配
		}

		return Utils.toJSONString(resultMap); // 转json
	}
}
