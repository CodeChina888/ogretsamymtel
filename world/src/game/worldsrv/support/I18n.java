package game.worldsrv.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import core.support.SysException;
import game.worldsrv.support.C;
import game.worldsrv.support.Utils;

public class I18n {
	public static Map<String, Properties> propMap = new HashMap<String, Properties>();
	public static Map<String, String> messages = new HashMap<String, String>();

	public static String get(String key) {
		return get(key, new Object[0]);
	}

	public static String get(String key, Object... params) {
		try {
			// 缓存中有，就直接从缓存中取
			if (messages.get(key) != null) {
				return Utils.createStr(messages.get(key), params);
			}

			// 根据前缀来确定所属模块
			String module = key.substring(0, key.indexOf("."));
			Properties p = new Properties();

			if (propMap.get(module) != null) {
				// 如果缓存中有，直接从缓存中取出
				p = propMap.get(module);
			} else {
				File pf = new File(Utils.getClassPath() + "/META-INF/i18n/" + C.GAME_I18N_KEY + "/" + module
						+ ".properties");
				if (pf.exists()) {
					InputStream is = new FileInputStream(pf);
					p.load(is);
				}
				// 缓存中没有，加入缓存中
				propMap.put(module, p);
			}

			String msg = p.getProperty(key);
			// 缓存中没有，加入缓存中
			messages.put(key, msg);
			return Utils.createStr(msg, params);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
}