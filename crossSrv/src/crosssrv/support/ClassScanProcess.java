package crosssrv.support;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import core.support.Utils;

public class ClassScanProcess {

	private static ClassScanProcess instance = new ClassScanProcess();

	public static ClassScanProcess getInstance() {
		return instance;
	}

	private static final String JAR_NAME = "game-cross.jar";

	private String jarPath = null;
	private String jarValue = null;
	private Map<String, Long> nameMapping = new HashMap<String, Long>();
	private List<String> changedFile = new LinkedList<String>();
	private Scanner scanner = null;
	private ScheduledExecutorService service = null;

	public void init() throws Exception {
		try {
			service = Executors.newScheduledThreadPool(1);

			jarPath = Utils.getClassPath(JAR_NAME);

			// 增加基础扫描数据
			scanner = new Scanner();
			scanner.scan();

			// 每301秒扫描一次
			Log.cross.info("ClassScanProcess开启类热更新扫描调度，每301秒扫描一次");
			service.scheduleAtFixedRate(new Task(), 10, 301, TimeUnit.SECONDS);

		} catch (Exception e1) {
			Log.cross.error("{}定位文件异常 {}", JAR_NAME, e1.toString());
		}
	}

	class Task implements Runnable {
		@Override
		public void run() {
			try {
				if (scanner.isChanged()) {
					Log.cross.info("{}文件变更,重新加载。", JAR_NAME);
					scanner.reloadClass();
					Log.cross.info("{}加载完毕。", JAR_NAME);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	class Scanner {

		public Scanner() {

		}

		public void reloadClass() throws Exception {
			for (String name : changedFile) {
				ClassReloadManager.getInstance().reloadClass(name, jarPath);
			}
			changedFile.clear();
		}

		public void scan() throws IOException {
			if (jarValue != null) {
				return;
			}
			jarValue = getValue(new File(jarPath));

			JarFile jarFile = new JarFile(jarPath);
			Enumeration<JarEntry> entrys = jarFile.entries();
			String tmpName;
			while (entrys.hasMoreElements()) {
				JarEntry jarEntry = entrys.nextElement();
				if (jarEntry.isDirectory())
					continue;

				tmpName = jarEntry.getName();
				tmpName = tmpName.replaceAll("/", ".");
				if (tmpName.endsWith(".class") && tmpName.indexOf("$") == -1) {// 忽略内部类
					tmpName = tmpName.substring(0, tmpName.lastIndexOf("."));
					nameMapping.put(tmpName, jarEntry.getSize());
				}
			}
			jarFile.close();
			Log.cross.info("{}扫描完毕 size={}", JAR_NAME, nameMapping.size());
		}

		private String getValue(File source) {
			if (!source.exists())
				return jarValue;

			String contentLength = String.valueOf((source.length()));
			String lastModified = String.valueOf((source.lastModified()));
			return new StringBuilder(contentLength).append(lastModified).toString();
		}

		public boolean isChanged() throws IOException {
			String key = getValue(new File(jarPath));
			Log.cross.info("{}开始一次扫描key={},jarValue={}", JAR_NAME, key, jarValue);
			if (key.equals(jarValue))
				return false;

			// 改变了则赋值
			jarValue = key;

			boolean isChanged = false;
			JarFile jarFile = new JarFile(jarPath);
			Enumeration<JarEntry> entrys = jarFile.entries();
			String tmpName;
			while (entrys.hasMoreElements()) {
				JarEntry jarEntry = entrys.nextElement();
				if (jarEntry.isDirectory())
					continue;

				tmpName = jarEntry.getName();
				tmpName = tmpName.replaceAll("/", ".");
				if (tmpName.endsWith(".class") && tmpName.indexOf("$") == -1) {// 忽略内部类
					tmpName = tmpName.substring(0, tmpName.lastIndexOf("."));
					if (nameMapping.containsKey(tmpName)) {
						if (nameMapping.get(tmpName).longValue() != jarEntry.getSize()) {
							isChanged = true;
							changedFile.add(tmpName);
							nameMapping.put(tmpName, jarEntry.getSize());
						}
					}

				}
			}
			jarFile.close();

			return isChanged;
		}

	}

	public void destroy() throws Exception {
		if (service != null) {
			service.shutdownNow();
		}
	}

}
