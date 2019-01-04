package core.support.observer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import core.support.SysException;
import core.support.function.CommonFunction;

/**
 * 观察者模式基类 职责 1.初始化 2.发布事件 3.通知观察者
 */
public abstract class ObServer<K, P> {
	// <事件KEY, <类, 接收方法>>
	// private final Map<Object, Map<Class<?>, Set<CommonFunction>>> eventMethod
	// = new HashMap<>();

	/**
	 * 获取监听这个事件的注解
	 * @return
	 */
	protected abstract Class<? extends Annotation> getListenerAnnotation();

	/**
	 * 获取事件接收对象的实例
	 * @param targetClass
	 * @return
	 */
	public abstract <T> T getTargetBean(Class<T> targetClass);

	private final Map<Object, Set<CommonFunction>> eventMethod = new HashMap<>();

	/**
	 * 注册时间侦听函数，此函数一般由自动生成的代码调用
	 * @param key
	 * @param function
	 * @param paramSize
	 */
	public final void reg(String key, Object function, int paramSize) {
		Set<CommonFunction> funcs = eventMethod.get(key);
		if (null == funcs) {
			funcs = new LinkedHashSet<>();
			eventMethod.put(key, funcs);
		}
		funcs.add(new CommonFunction(function, paramSize));
	}

	/*
	 * 最终真正的执行
	 */
	private void _fireHandler(String fullKey, P param) {
		Set<CommonFunction> funcs = eventMethod.get(fullKey);
		if (null != funcs) {
			for (CommonFunction f : funcs) {
				f.apply(param);
			}
		}
	}

	/**
	 * 发布无参事件
	 * @param key
	 */
	protected final void fireHandler(K key, Object subKey) {
		try {
			fireHandler(key, subKey, null);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 发布事件 如果有子事件 那么会创建两个事件(主事件 + 子事件)
	 * @param key
	 * @param param
	 */
	protected final void fireHandler(K key, Object subKey, P param) {
		// 创建事件关键字
		// 如果有子事件 那么会创建两个事件(主事件 + 子事件)
		_fireHandler(createMethodKey(key, null), param);
		if (null != subKey) {
			_fireHandler(createMethodKey(key, subKey), param);
		}
	}

	/**
	 * 获取被监听的事件关键字
	 * @param method
	 * @param listenerAnnotation
	 * @return
	 */
	public static Set<String> getListenerKey(Method method, Class<? extends Annotation> listenerAnnotation) {
		try {
			Annotation annotation = method.getAnnotation(listenerAnnotation);
			Method mKey = listenerAnnotation.getMethod("value");
			Object oKey = mKey.invoke(annotation);
			Object[] vKey;
			if (oKey instanceof int[]) {
				int[] keys = (int[]) mKey.invoke(annotation);
				vKey = new Object[keys.length];
				for (int i = 0; i < keys.length; i++)
					vKey[i] = keys[i];
			} else {
				vKey = (Object[]) oKey;
			}
			// 获取注解设置的主事件
			// int[] vKey = (int[]) mKey.invoke(annotation);

			// 获取注解设置的子事件
			Object[] vSubStr = {};
			int[] vSubInt = {};
			long[] vSubLong = {};
			for (Method m : listenerAnnotation.getMethods()) {
				String mName = m.getName();

				if ("subStr".equals(mName))
					vSubStr = (Object[]) m.invoke(annotation);
				else if ("subInt".equals(mName))
					vSubInt = (int[]) m.invoke(annotation);
				else if ("subLong".equals(mName))
					vSubLong = (long[]) m.invoke(annotation);
			}

			// 多个子事件Key 只允许设置一个 这里确认和检查下
			int vSubCount = 0;
			if (vSubStr.length > 0)
				vSubCount++;
			if (vSubInt.length > 0)
				vSubCount++;
			if (vSubLong.length > 0)
				vSubCount++;

			// 设置监听了多个不同类型的子事件
			if (vSubCount > 1) {
				throw new SysException("Observer监听参数设置错误，不允许同事设置多种不同参数类型的子事件："
						+ "mthod={}, anno={}, subStr={}, subInt={}, subLong={}", method, annotation, vSubStr, vSubInt,
						vSubLong);
			}

			// 获取子类型设置
			String[] vSubKey = getSubKeysFromValue(vSubStr, vSubInt, vSubLong);

			// 监听关键字
			Set<String> results = new HashSet<>();

			// 需要监听的事件关键字
			for (Object k : vKey) {
				// 如果主事件与子事件都有多个 那么会出现乘积 暂时先允许
				for (String sk : vSubKey) {
					String smk = createMethodKey(k, sk);
					results.add(smk);
				}

				// 如果没有设置子事件 那么就生成主事件
				if (vSubKey.length == 0) {
					String smk = createMethodKey(k, null);
					results.add(smk);
				}
			}

			return results;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 根据不用的参数来获取最终配置 一段很恶心的判断
	 * @param vSubStr
	 * @param vSubInt
	 * @param vSubLong
	 * @return
	 */
	private static String[] getSubKeysFromValue(Object[] vSubStr, int[] vSubInt, long[] vSubLong) {
		// 获取子类型设置
		String[] vSubKey = {};

		if (vSubStr.length > 0) {// 字符参数
			vSubKey = new String[vSubStr.length];

			for (int i = 0; i < vSubStr.length; i++) {
				vSubKey[i] = vSubStr[i].toString();
			}
		} else if (vSubInt.length > 0) {// Int型参数
			vSubKey = new String[vSubInt.length];

			for (int i = 0; i < vSubInt.length; i++) {
				vSubKey[i] = Integer.toString(vSubInt[i]);
			}
		} else if (vSubLong.length > 0) {// Long型参数
			vSubKey = new String[vSubLong.length];

			for (int i = 0; i < vSubLong.length; i++) {
				vSubKey[i] = Long.toString(vSubLong[i]);
			}
		}

		return vSubKey;
	}

	/**
	 * 创建事件关键字 虽然fire时是生成了两个事件，但是这里不能 这个函数不能改成
	 * @param key
	 * @param subKey
	 * @return
	 */
	private static String createMethodKey(Object key, Object subKey) {
		String mk = key.toString();

		// 如果有 那么拼装子事件
		if (subKey != null && !subKey.toString().equals("")) {
			mk = mk + "$" + subKey.toString();
		}

		return mk;
	}
}