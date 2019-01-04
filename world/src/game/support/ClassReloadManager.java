package game.support;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;

import core.support.ClassReloader;
import game.worldsrv.support.Log;

/**
 * 
 * @author root
 *
 */
public class ClassReloadManager {

	private static ClassReloadManager instance = new ClassReloadManager();

	public static ClassReloadManager getInstance() {
		return instance;
	}

	/**
	 * 私有的构造方法
	 */
	private ClassReloadManager() {

	}

	/**
	 * 默认目录
	 */
	private static final String DEFAULT_RELOD_FILE_PATH = "";

	public void reloadClass(String classPath, String filePath) {
		if (classPath == null || classPath.trim().equals(""))
			return;

		filePath = filePath == null ? DEFAULT_RELOD_FILE_PATH : filePath;
		try {
            Log.game.info("reloadClass:{}", classPath);

			Class<?> reloadClass = Class.forName(classPath);

			ClassReloader.reload(reloadClass, new File(filePath));
			
		} catch (ClassNotFoundException e) {
			Log.game.error("class类路径错误", e);
		} catch (IOException e) {
			Log.game.error("未找到需替换的 class 文件 或 jar包", e);
		} catch (UnmodifiableClassException e) {
			Log.game.error("class reload 失败", e);
		} catch (NullPointerException e) {
			Log.game.error(
					"可能是游戏启动没设置 jvm 参数： -javaagent:xxx\\classReloader.jar", e);
		} catch (UnsupportedOperationException e) {
			Log.game.error("目前暂不支持添加，删除方法，全局变量操作！");
		} catch (Exception e) {
			Log.game.error("未知错误请重试", e);
		}

	}
}
