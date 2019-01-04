package core.support.observer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.GeneratedMessage;

import core.support.SysException;
import core.support.observer.MsgParamBase;
import core.support.observer.MsgReceiver;
import core.support.observer.ObServer;

public final class MsgSender extends ObServer<Class<? extends GeneratedMessage>, MsgParamBase> {
	// 实例化单例
	public static final MsgSender instance = new MsgSender();

	// 缓存目标实例
	private static final Map<Class<?>, Object> targetMsgHandlers = new ConcurrentHashMap<>();

	/**
	 * 获取事件接收对象的实例
	 * @param targetClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getTargetBean(Class<T> targetClass) {
		try {
			Object inst = targetMsgHandlers.get(targetClass);
			if (inst == null) {
				Constructor<?> constr = targetClass.getDeclaredConstructor();
				constr.setAccessible(true);
				inst = constr.newInstance();
				targetMsgHandlers.put(targetClass, inst);
			}
			return (T) inst;
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
	 * 发布事件
	 * @param param
	 */
	public static void fire(MsgParamBase param) {
		GeneratedMessage msg = param.getMsg();
		if(null == msg) 
			return;

		instance.fireHandler(msg.getClass(), null, param);
	}

	@Override
	protected Class<? extends Annotation> getListenerAnnotation() {
		return MsgReceiver.class;
	}
}