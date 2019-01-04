package game.platform.observer;

import java.lang.annotation.Annotation;
import core.support.ManagerBase;
import core.support.Param;
import core.support.SysException;
import core.support.Utils;
import core.support.observer.Listener;
import core.support.observer.ObServer;

public class EventPF extends ObServer<Integer, Param> {
	public static final EventPF instance = new EventPF();
	
	/**
	 * 获取事件接收对象的实例
	 * @param targetClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getTargetBean(Class<T> targetClass) {
		try {
			if(!Utils.isInstanceof(targetClass, ManagerBase.class)) {
				throw new SysException("只有ManagerBase的子类才能监听Event事件，当前错误Class={}", targetClass);
			}
			
			return (T)ManagerBase.inst(targetClass);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 发布无参事件
	 * @param key
	 */
	public static void fire(int key) {
		instance.fireHandler(key, null);
	}
	
	/**
	 * 发布无参事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey) {
		instance.fireHandler(key, subKey);
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fire(int key, Param param) {
		instance.fireHandler(key, null, param);
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey, Param param) {
		instance.fireHandler(key, subKey, param);
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fire(int key, Object...params) {
		instance.fireHandler(key, null, new Param(params));
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey, Object...params) {
		instance.fireHandler(key, subKey, new Param(params));
	}

	@Override
	protected Class<? extends Annotation> getListenerAnnotation() {
		return Listener.class;
	}
}