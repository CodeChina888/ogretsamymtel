package crosssrv.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import core.gen.GenBase;
import core.support.ConfigJSON;
import core.support.PackageClass;
import turnbasedsrv.support.GlobalConfVal;

/**
 * 策划数据加载
 * 
 * @author root
 *
 */
public class DataReloadManager {

	private static DataReloadManager instance = new DataReloadManager();

	public static DataReloadManager inst() {
		return instance;
	}

	/**
	 * 初始化支持类(对于需要额外增加的类在这里处理)
	 */
	public void initReloadSupport() {
	}

	/**
	 * 重新加载所有JSON表格数据
	 */
	public void reloadConf() {
		long timeStart = System.currentTimeMillis();
		// Set<Class<?>> classSet = GenBase.getSources("");
		Map<String, Class<?>> mapAllClass = PackageClass.getInstance().find();// 获取所有类的集合

		// 遍历所有类，取出类中有@DistrClass注解的方法
		// ClassPool pool = ClassPool.getDefault();
		int sizeSucc = 0;// 成功个数
		int sizeAll = 0;// 所有个数
		for (Class<?> clazz : mapAllClass.values()) {
			// 如果没有@DistriClass注解, 则不处理
			if (!clazz.isAnnotationPresent(ConfigJSON.class)) {
				continue;
			}
			sizeAll++;
			// 重新加载JSON表格数据
			Method m = null;
			try {
				m = clazz.getMethod("reLoad");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			if (m != null) {
				try {
					m.invoke(clazz);
					sizeSucc++;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		long timeUse = System.currentTimeMillis() - timeStart;
		Log.cross.info("===重新加载所有JSON表格数据成功：reLoadConf() sizeAll={},sizeSucc={},useTimeMillis={}", sizeAll, sizeSucc,
				timeUse);

		// 重新加载全局配置表
		GlobalConfVal.reloadConfVal();
	}

	public void reLoadAllConf() {
		List<Class<?>> sources = GenBase.getSources("");
		// 遍历所有类，取出类中有@DistrClass注解的方法
		// ClassPool pool = ClassPool.getDefault();
		for (Class<?> clazz : sources) {

			// 如果没有@DistriClass注解, 则不处理
			if (!clazz.isAnnotationPresent(ConfigJSON.class)) {
				continue;
			}
			Method m = null;
			if (true) {
				try {
					m = clazz.getMethod("reLoad");
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				if (m != null) {
					try {
						m.invoke(clazz);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void reLoadConf(String className) {
		Method m = null;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());

			// 如果没有@DistriClass注解, 则不处理
			if (!clazz.isAnnotationPresent(ConfigJSON.class)) {
				return;
			}

			m = clazz.getMethod("reLoad");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (m != null) {
			try {
				m.invoke(clazz);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadConf(String className) {
		Method m = null;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());

			// 如果没有@DistriClass注解, 则不处理
			if (!clazz.isAnnotationPresent(ConfigJSON.class)) {
				return;
			}

			m = clazz.getMethod("findAll");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (m != null) {
			try {
				m.invoke(clazz);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
