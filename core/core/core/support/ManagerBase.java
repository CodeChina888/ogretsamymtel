package core.support;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import core.support.ManagerBase;
import core.support.PackageClass;
import core.support.SysException;
import core.support.Utils;
import core.support.log.LogCore;

public abstract class ManagerBase {
	// public static Map<String, ManagerBase> instances = new HashMap<>();
	//
	// /**
	// * 获取唯一实例
	// * @param clazz
	// * @return
	// */
	// @SuppressWarnings("unchecked")
	// public static <T extends ManagerBase> T inst(Class<?> clazz) {
	// Object inst = instances.get(clazz.getName());
	// if(inst == null) {
	// try {
	// inst = clazz.newInstance();
	// }catch (InstantiationException | IllegalAccessException e) {
	// throw new SysException("创建实例失败，Clazz{}", clazz);
	// }
	//
	// instances.put(clazz.getName(), (ManagerBase) inst);
	// }
	//
	// return (T)inst;
	// }

	public static final Map<String, ManagerBase> instances = new ConcurrentHashMap<>();

	static {
		init();
	}

	public static void init() {
		instances.clear();
		LogCore.core.info("ManagerBase.init()");// sjh

		Map<String, Class<?>> mapAllClass = PackageClass.getInstance().find();
		LogCore.core.info("ManagerBase.init(), AllClass.size=" + mapAllClass.size());
		
//		// 从ClassLoader中读取所有的Manager
//		int num2 = 0;
//		for (Class<?> clazz : mapAllClass.values()) {
//			// 只需要加载ManagerBase类注解数据
//			if (!Utils.isInstanceof(clazz, ManagerBase.class) || Modifier.isAbstract(clazz.getModifiers())) {
//				continue;
//			}
//			if (GofClassLoader.newInstance(Utils.getClassPath(), clazz.getName()) != null) {
//				num2++;
//			} else {
//				LogCore.core.error("ManagerBase.init() : fail loadClassName = [{}:{}]", Utils.getClassPath(),
//						clazz.getName());
//			}
//		}
//		LogCore.core.info("ManagerBase.init() : success loadClassNum={}", num2);
//
//		// 等待1秒
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			// TODO 自动生成的 catch 块
//			e1.printStackTrace();
//		}
		
		int num = 0;
		// 创建实例
		for (Class<?> clazz : mapAllClass.values()) {
			// 只需要加载ManagerBase类注解数据
			if (!Utils.isInstanceof(clazz, ManagerBase.class) || Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}

			Object inst = null;
			// 创建实例
			try {
				inst = clazz.newInstance();
			}catch (InstantiationException | IllegalAccessException e) {
				throw new SysException("ManagerBase.init(), fail in newInstance,clazz={}", clazz);
			} 
			// LogCore.core.info("ManagerBase.init(), success newInstance,clazz={}", clazz.getName());
			num++;
			instances.put(clazz.getName(), (ManagerBase) inst);
		}
		LogCore.core.info("ManagerBase.init(), success newInstance,num={}", num);
	}
	
	/**
	 * 获取唯一实例
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ManagerBase> T inst(Class<?> clazz) {
		Object inst = instances.get(clazz.getName());
		if (inst == null) {
			throw new SysException("获取Manager实例时出错：未能找到对应实例，class={}", clazz.getName());
		}
		return (T) inst;
	}
	
	/**
	 * 获取唯一实例
	 * @param className
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ManagerBase> T inst(String className) {
		Object inst = instances.get(className);
		if (inst == null) {
			throw new SysException("获取Manager实例时出错：未能找到对应实例，class={}", className);
		}
		return (T) inst;
	}
}