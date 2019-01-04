package game.support;

import game.worldsrv.param.ParamManager;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.RoleNameFix;
import game.worldsrv.support.SensitiveWordFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import core.gen.GenBase;
import core.support.ConfigJSON;
import core.support.PackageClass;

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
	public boolean reloadConf() {
		long timeStart = System.currentTimeMillis();
		// Set<Class<?>> classSet = GenBase.getSources("");
		Map<String, Class<?>> mapAllClass = PackageClass.getInstance().find();// 获取所有类的集合

		// 遍历所有类，取出类中有@DistrClass注解的方法
		// ClassPool pool = ClassPool.getDefault();
		int sizeSucc = 0;// 成功个数
		int sizeAll = 0;// 所有个数
		for (Class<?> clazz : mapAllClass.values()) {
			// 如果没有@ConfigJSON注解, 则不处理
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
		Log.table.info("===重新加载所有JSON表格数据成功：reLoadConf() sizeAll={},sizeSucc={},useTimeMillis={}",sizeAll, sizeSucc, timeUse);
		
		// 重新加载所有参数配置
		ParamManager.reloadAllParam();
		// 重新加载敏感词库
		SensitiveWordFilter filterSW = SensitiveWordFilter.getInstance();
		filterSW.initKeyWord();
		Log.human.info("===重新加载敏感词库：allSize={}", filterSW.getSizeOfSensitiveWord());
		// 重新加载玩家命名表
		RoleNameFix.reloadRoleName();
		// 重新加载战斗全局配置表
		game.turnbasedsrv.support.GlobalConfVal.reloadConfVal();
		// 重新加载白名单
		AssetsTxtFix.reloadAccountWhiteList();
		// 重新加载内部充值名单
		AssetsTxtFix.reloadAccountChargeList();
		// 重新加载特殊字符库
		AssetsTxtFix.reloadContentChar();
		//重新加载全局配置表
		game.worldsrv.support.GlobalConfVal.reloadConfVal();
		return sizeAll == sizeSucc;
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
