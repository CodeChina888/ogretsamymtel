package game.worldsrv.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	public static Logger game = LoggerFactory.getLogger("GAME");
	public static Logger stageCommon = LoggerFactory.getLogger("STAGE_COMMON");
	public static Logger stageMove = LoggerFactory.getLogger("STAGE_MOVE");
	
	public static Logger temp = LoggerFactory.getLogger("TEMP");
	public static Logger human = LoggerFactory.getLogger("HUMAN");
	public static Logger ai = LoggerFactory.getLogger("AI");
	public static Logger fight = LoggerFactory.getLogger("FIGHT");
	public static Logger skill = LoggerFactory.getLogger("SKILL");
	public static Logger item = LoggerFactory.getLogger("ITEM");
	public static Logger chat = LoggerFactory.getLogger("CHAT");
	public static Logger table = LoggerFactory.getLogger("TABLE");// 表格引发的错误
	public static Logger notice = LoggerFactory.getLogger("NOTICE");
	public static Logger random = LoggerFactory.getLogger("RANDOM");
	public static Logger charge = LoggerFactory.getLogger("CHARGE");
	public static Logger battle = LoggerFactory.getLogger("BATTLE"); //战斗日志
	public static Logger globalConf = LoggerFactory.getLogger("globalConf");
	public static Logger lootMap = LoggerFactory.getLogger("LOOTMAP"); //抢夺本日志
	public static Logger tower = LoggerFactory.getLogger("TOWER"); //爬塔日志
	public static Logger partner = LoggerFactory.getLogger("PARTNER"); //伙伴日志
	public static Logger activity = LoggerFactory.getLogger("ACTIVITY"); //活动日志
	public static Logger guild = LoggerFactory.getLogger("GUILD"); //工会日志;
	
	/**
	 * Log.game.error(定位+"humanObj==null")
	 */
	public static void logGameObjectNull() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" Object is null").toString();
		Log.game.error(log);
	}
	/**
	 * Log.game.error(定位+"objectName==null")
	 * @param objectName
	 */
	public static void logGameObjectNull(String objectName) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(objectName).append(" is null").toString();
		Log.game.error(log);
	}
	/**
	 * Log.game.error(定位+clazz.getName()+"==null")
	 * @param clazz
	 */
	public static void logGameObjectNull(Class<?> clazz) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(clazz.getName()).append(" is null").toString();
		Log.game.error(log);
	}
	/**
	 * logger.error(定位+"objectName==null")
	 * @param logger
	 * @param objectName
	 */
	public static void logObjectNull(Logger logger,String objectName) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(objectName).append(" is null").toString();
		logger.error(log);
	}
	/**
	 * logger.error(定位+clazz.getName()+"==null")
	 * @param logger
	 * @param clazz
	 */
	public static void logObjectNull(Logger logger,Class<?> clazz) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(clazz.getName()).append(" is null").toString();
		logger.error(log);
	}
	/**
	 * Log.game.error(定位+errorInfo,args)
	 * @param errorInfo
	 * @param args
	 */
	public static void logGameError(String errorInfo,Object...args) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ").toString();
		Log.game.error(log+errorInfo,args);
	}
	/**
	 * logger.error(定位+errorInfo,args)
	 * @param logger
	 * @param args
	 */
	public static void logError(Logger logger,String errorInfo,Object...args) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ").toString();
		logger.error(log+errorInfo,args);
	}
	/**
	 * Log.table.error(定位+clazz.getName()+"配表错误："+errorInfo,args)
	 * @param clazz
	 * @param errorInfo
	 * @param args
	 */
	public static void logTableError(Class<?> clazz, String errorInfo,Object...args) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(clazz.getName()).append("配表错误：").toString();
		Log.table.error(log+errorInfo,args);
	}
	/**
	 * Log.game.info(定位+info,args)
	 * @param info
	 * @param args
	 */
	public static void logGameInfo(String info,Object...args) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ").toString();
		Log.game.info(log+info,args);
	}
	/**
	 * logger.info(定位+info,args)
	 * @param logger
	 * @param info
	 * @param args
	 */
	public static void logInfo(Logger logger,String info,Object...args) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ").toString();
		logger.info(log+info,args);
	}
	/**
	 * test的方法，在正式服中不会打印<br/>
	 * Log.game.info(定位+info,args)
	 * @param info
	 * @param args
	 */
	public static void logGameTest(String info,Object...args) {
		if(!C.DEBUG_ENABLE) {
			return;
		}
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		StackTraceElement e1 = stackTrace[3];
		String log = new StringBuilder().append("---").append(e1.getFileName()).append(":")
				.append(e1.getLineNumber()).append(" ").append(e.getFileName()).append(":")
				.append(e.getLineNumber()).append(" ").toString();
		Log.game.info(log+info,args);
	}
	/**
	 * test的方法，在正式服中不会打印<br/>
	 * logger.info(定位+info,args)
	 * @param info
	 * @param args
	 */
	public static void logTest(Logger logger,String info,Object...args) {
		if(!C.DEBUG_ENABLE) {
			return;
		}
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("---").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ").toString();
		logger.info(log+info,args);
	}
}
