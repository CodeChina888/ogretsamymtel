package game.worldsrv.support.observer;

import game.worldsrv.support.Utils;

import java.lang.annotation.Annotation;

import core.statistics.StatisticsOB;
import core.support.Config;
import core.support.ManagerBase;
import core.support.Param;
import core.support.SysException;
import core.support.observer.Listener;
import core.support.observer.ObServer;
import game.worldsrv.support.observer.Event;

public class Event extends ObServer<Integer, Param> {
	public static final Event instance = new Event();

	/**
	 * 获取事件接收对象的实例
	 * @param targetClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getTargetBean(Class<T> targetClass) {
		try {
			if (!Utils.isInstanceof(targetClass, ManagerBase.class)) {
				throw new SysException("只有ManagerBase的子类才能监听Event事件，当前错误Class={}", targetClass);
			}

			return (T) ManagerBase.inst(targetClass);//, this);
		} catch (Exception e) {
			throw new SysException(e, targetClass.toString());
		}
	}

	/**
	 * 初始化 遍历全部给定包 找到观察者的处理函数 并缓存起来待用
	 * @param packageNames
	 */
	// public static void init() {
	// init("");
	// }

	/**
	 * 初始化 遍历全部给定包 找到观察者的处理函数 并缓存起来待用
	 * @param packageNames
	 */
	// public static void init(String...packageNames) {
	// instance.initHandler(packageNames);
	// }

	/**
	 * 发布无参事件
	 * @param key
	 */
	public static void fire(int key) {
		// 添加监控
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		instance.fireHandler(key, null);
		if(start > 0)
			StatisticsOB.event(key+"", 1, System.nanoTime() - start);
	}

	/**
	 * 发布无参事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey) {
		// 添加监控
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		instance.fireHandler(key, subKey);
		if(start > 0)
			StatisticsOB.event(key+"", 1, System.nanoTime() - start);
	}

	/**
	 * 发布事件
	 * @param key
	 */
	public static void fire(int key, Param param) {
		// 添加监控
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		instance.fireHandler(key, null, param);
		if(start > 0)
			StatisticsOB.event(key+"", 1, System.nanoTime() - start);
	}

	/**
	 * 发布事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey, Param param) {
		// 添加监控
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		instance.fireHandler(key, subKey, param);
		if(start > 0)
			StatisticsOB.event(key+"", 1, System.nanoTime() - start);
	}

	/**
	 * 发布事件
	 * @param key
	 */
	public static void fire(int key, Object... params) {
		// 添加监控
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		instance.fireHandler(key, null, new Param(params));
		if(start > 0)
			StatisticsOB.event(key+"", 1, System.nanoTime() - start);
	}

	/**
	 * 发布事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey, Object... params) {
		// 添加监控
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		instance.fireHandler(key, subKey, new Param(params));
		if(start > 0)
			StatisticsOB.event(key+"", 1, System.nanoTime() - start);
	}

	@Override
	protected Class<? extends Annotation> getListenerAnnotation() {
		return Listener.class;
	}
}