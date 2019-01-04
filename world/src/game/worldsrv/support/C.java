package game.worldsrv.support;

import game.worldsrv.support.Utils;

import java.util.Properties;

import core.support.SysException;

/**
 * 系统参数 本类都是简单的参数配置，如果需要判断系统环境， 比如是手游还是页游，则使用C.java判断
 */
public class C {
	// 配置文件名称
	private static final String CONFIG_NAME = "config.properties";

	public static final String GAME_DATAEYE_APPID;// 数据分析appid
	public static final String GAME_DATAEYE_LOGDIR;// 数据分析日志目录
	
	public static final String GAME_I18N_KEY; // 游戏语言
	public static final int GAME_PLATFORM_ID; // 运营平台ID[1,91]：不可设置为0，1到91为可设置范围
	public static final int GAME_SERVER_ID; // 游戏区服ID[0,9999]：0为测试服，1到9999为正式服
	
	public static final boolean DEBUG_ENABLE; // 是否启动调试，开启则不处理心跳检测
	
		
	static {
		Properties prop = Utils.readProperties(CONFIG_NAME);

		if (prop.containsKey("game.dataeye.appid")) {
			GAME_DATAEYE_APPID = prop.getProperty("game.dataeye.appid");
		} else {// 默认为空，无需接入数据分析SDK
			GAME_DATAEYE_APPID = "";
		}
		if (prop.containsKey("game.dataeye.logdir")) {
			GAME_DATAEYE_LOGDIR = prop.getProperty("game.dataeye.logdir");
		} else {// 默认为空
			GAME_DATAEYE_LOGDIR = "";
		}
		
		GAME_I18N_KEY = prop.getProperty("game.i18n.key");
		
		// 运营平台ID[1,91]：不可设置为0，1到91为可设置范围
		if (prop.containsKey("game.platform.id")) {
			GAME_PLATFORM_ID = Integer.valueOf(prop.getProperty("game.platform.id"));
		} else {
			GAME_PLATFORM_ID = -1;
		}
		if (GAME_PLATFORM_ID < 1 || GAME_PLATFORM_ID > 91) {
			throw new SysException("==={}配置错误，请检查：运营平台ID={}", CONFIG_NAME, GAME_PLATFORM_ID);
		}
		// 游戏区服ID[0,9999]：0为测试服，1到9999为正式服
		if (prop.containsKey("game.server.id")) {
			GAME_SERVER_ID = Integer.valueOf(prop.getProperty("game.server.id"));
		} else {
			GAME_SERVER_ID = -1;
		}
		if (GAME_SERVER_ID < 0 || GAME_SERVER_ID > 9999) {
			throw new SysException("==={}配置错误，请检查：游戏区服ID={}", CONFIG_NAME, GAME_SERVER_ID);
		}
		
		DEBUG_ENABLE = Utils.booleanValue(prop.getProperty("debug.enable"));
		
	}
}