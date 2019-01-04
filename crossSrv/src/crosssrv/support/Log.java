package crosssrv.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	public static Logger cross = LoggerFactory.getLogger("CROSS");
	public static Logger fight = LoggerFactory.getLogger("FIGHT");
	public static Logger table = LoggerFactory.getLogger("TABLE");

	
	/**
	 * logger.error(定位+"objectName==null")
	 * @param logger
	 * @param objectName
	 */
	public static void logObjectNull(Logger logger,String objectName){
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
	public static void logObjectNull(Logger logger,Class<?> clazz){
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(clazz.getName()).append(" is null").toString();
		logger.error(log);
	}
	
	/**
	 * logger.error(定位+errorInfo,args)
	 * @param logger
	 * @param info
	 * @param args
	 */
	public static void logError(Logger logger,String errorInfo,Object...args){
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
	public static void logTableError(Class<?> clazz, String errorInfo,Object...args){
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ")
		.append(clazz.getName()).append("配表错误：").toString();
		Log.table.error(log+errorInfo,args);
	}
	
	/**
	 * logger.info(定位+info,args)
	 * @param logger
	 * @param info
	 * @param args
	 */
	public static void logInfo(Logger logger,String info,Object...args){
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[2];
		String log = new StringBuilder().append("===").append(e.getFileName()).append(":")
		.append(e.getLineNumber()).append(" ")
		.append(e.getMethodName()).append(" ").toString();
		logger.info(log+info,args);
	}
	
}
