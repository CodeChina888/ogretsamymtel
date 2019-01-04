package game.support;

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

import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.RoleNameFix;
import game.worldsrv.support.Utils;

/**
 * 数据扫描
 * 
 * @author root
 *
 */
public class DataScanProcess {

	private static DataScanProcess instance = new DataScanProcess();

	public static DataScanProcess getInstance() {
		return instance;
	}

	private static final String JAR_NAME = "game-data.jar";

	private String jarPath = null;
	private String jarValue = null;
	private Map<String, Long> confMapping = new HashMap<String, Long>();
	private Map<String, String> nameMapping = new HashMap<String, String>();
	private List<String> changedFile = new LinkedList<String>();
	private Scanner scanner = null;
	private ScheduledExecutorService service = null;

	public void init() throws Exception {
		try {
			service = Executors.newScheduledThreadPool(1);
			
			jarPath = Utils.getClassPath(JAR_NAME);
			
			// 增加基础扫描数据
			scanner = new Scanner();
			scanner.scanConf();
			scanner.scanTxt();
			
			// 每201秒扫描一次
			Log.game.error("DataScanProcess开启数据热更新扫描调度，每201秒扫描一次");
			service.scheduleAtFixedRate(new Task(), 10, 201, TimeUnit.SECONDS);

		} catch (Exception e1) {
			Log.game.error("{}定位文件异常 {}", JAR_NAME, e1.toString());
		}
	}

	class Task implements Runnable {
		@Override
		public void run() {
			try {
				if (scanner.isChanged()) {
					Log.game.error("{}文件变更,重新加载。", JAR_NAME);
					scanner.reloadConf();
					Log.game.error("重新加载完毕：{}", JAR_NAME);
				}
				
				// 文本文件的扫描
				scanner.scanAndReloadTxt();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	class Scanner {

		Scanner() {

		}

		void reloadConf() throws Exception {
			DataReloadManager.inst().reloadConf();// add by shenjh,加载所有JSON表格数据
//			for (String name : changedFile) {
//				ClassReloadManager.getInstance().reloadClass(name, jarPath);
//			}
			changedFile.clear();
		}

		//扫描jar配表数据////////////////////////////////////////////////////////////////////
		void scanConf() throws IOException {
			if (jarValue != null) {
				return;
			}
			jarValue = getJarValue(new File(jarPath));

			JarFile jarFile = new JarFile(jarPath);
			Enumeration<JarEntry> entrys = jarFile.entries();
			String tmpName;
			while (entrys.hasMoreElements()) {
				JarEntry jarEntry = entrys.nextElement();
				if (jarEntry.isDirectory())
					continue;

				tmpName = jarEntry.getName();
				tmpName = tmpName.replaceAll("/", ".");
				if (tmpName.endsWith(".json") && jarEntry.getSize() > 2) {// 2即空数据[]
					tmpName = tmpName.substring(0, tmpName.lastIndexOf("."));
					confMapping.put(tmpName, jarEntry.getTime());
				}
			}
			jarFile.close();
			Log.game.info("{}扫描完毕 size={}", JAR_NAME, confMapping.size());
		}
		
		private String getJarValue(File source) {
			if(!source.exists())
				return jarValue;
						
			String contentLength = String.valueOf((source.length()));
			String lastModified = String.valueOf((source.lastModified()));
			return new StringBuilder(contentLength).append(lastModified)
					.toString();
		}

		boolean isChanged() throws IOException {
			String key = getJarValue(new File(jarPath));
			Log.game.info("{}开始一次扫描key={},jarValue={}", JAR_NAME, key, jarValue);
			if(key.equals(jarValue))
				return false;
			
			//改变了则赋值
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
				if (tmpName.endsWith(".json") && jarEntry.getSize() > 2) {// 2即空数据[]
					tmpName = tmpName.substring(0, tmpName.lastIndexOf("."));
					if (confMapping.containsKey(tmpName)) {
						if (confMapping.get(tmpName).longValue() != jarEntry.getTime()) {
							isChanged = true;
							changedFile.add(tmpName);
							confMapping.put(tmpName, jarEntry.getTime());
						}
					}

				}
			}
			jarFile.close();

			return isChanged;
		}

		//扫描文本文件////////////////////////////////////////////////////////////////////
		void scanTxt() {
			if (!nameMapping.isEmpty()) {
				return;
			}
			// 重新加载白名单及内部充值名单
			scanSingleTxtFile(AssetsTxtFix.AccountWhiteList);
			scanSingleTxtFile(AssetsTxtFix.AccountChargeList);
			
		}
		private void scanSingleTxtFile(String fName) {
			String path = Thread.currentThread().getContextClassLoader()
					.getResource(fName).getPath();
			File file = new File(path);
			String multi_key = getValue(file);
			nameMapping.put(fName, multi_key);
		}
		private String getValue(File source) {
			String contentLength = String.valueOf((source.length()));
			String lastModified = String.valueOf((source.lastModified()));
			return new StringBuilder(contentLength).append(lastModified)
					.toString();
		}
		void scanAndReloadTxt() {
			// 重新加载白名单
			if(scanAndReloadSingleTxt(AssetsTxtFix.AccountWhiteList)){
				AssetsTxtFix.reloadAccountWhiteList();
				Log.game.info("重新加载完毕：{}", AssetsTxtFix.AccountWhiteList);
			}
			// 重新加载内部充值名单
			if(scanAndReloadSingleTxt(AssetsTxtFix.AccountChargeList)){
				AssetsTxtFix.reloadAccountChargeList();
				Log.game.info("重新加载完毕：{}", AssetsTxtFix.AccountChargeList);
			}
			// 重新加载特殊字符库
			if(scanAndReloadSingleTxt(AssetsTxtFix.ContentChar)){
				AssetsTxtFix.reloadContentChar();
				Log.game.info("重新加载完毕：{}", AssetsTxtFix.ContentChar);
			}
		}
		private boolean scanAndReloadSingleTxt(String fileName) {
			String path = Thread.currentThread().getContextClassLoader()
					.getResource(fileName).getPath();
			File file = new File(path);
			String multi_key = getValue(file);
			String value = nameMapping.get(fileName);
			if (!multi_key.equals(value)) {
				nameMapping.put(fileName, multi_key);
				return true;
			}
			return false;
		}
	}

	public void destroy() throws Exception {
		if (service != null) {
			service.shutdownNow();
		}
	}

}
