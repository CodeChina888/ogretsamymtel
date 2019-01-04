package core.support;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.apache.commons.lang3.StringUtils;

import core.support.PackageClass;
import core.support.Sys;
import core.support.log.LogCore;

/**
 * 通过给定的包名，来得到包下的全部CLASS类。
 */
public class PackageClass {
	private String[] PATHS; // 类根目录地址
	// 忽略的类 有些类Class.forName()时会报错...
	private List<String> ignoreClass = new ArrayList<>();
	// 暂存找到的所有类文件：<类名，类>
	private Map<String, Class<?>> mapAllClass = new HashMap<>();

	private static PackageClass inst = null;// 单例

	/**
	 * 构造函数
	 */
	private PackageClass() {
		initPaths();// 初始化类根目录地址
	}

	/**
	 * 获取单例
	 * @return
	 */
	public static PackageClass getInstance() {
		if (null == inst) {
			inst = new PackageClass();
		}
		return inst;
	}

	/**
	 * 初始化类根目录地址
	 * @return
	 */
	private void initPaths() {
		try {
			String pathStr = System.getProperty("java.class.path");
			LogCore.core.info("PackageClass initPaths() = " + pathStr);// sjh
			if (Sys.isWin()) {
				PATHS = pathStr.split(";");
			} else {
				PATHS = pathStr.split(":");
			}
			// 找出所有类文件暂存起来
			if (mapAllClass.isEmpty()) {
				//long tm = System.currentTimeMillis();
				mapAllClass = getPackageClasses("game");
				mapAllClass.putAll(getPackageClasses("crosssrv"));
				mapAllClass.putAll(getPackageClasses("turnbasedsrv"));
				//LogCore.core.info("===mapAllClass.size={},tm={}ms", mapAllClass.size(), System.currentTimeMillis()-tm);
				
				//long tm1 = System.currentTimeMillis();
				//mapAllClass = getClassInPackage("game");
				//LogCore.core.info("===mapAllClass.size={},tm={}ms", mapAllClass.size(), System.currentTimeMillis()-tm1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 读取包内所有的类获取class对象，并根据指定的条件过滤
	 * @param pname
	 * @return
	 */
	public Map<String, Class<?>> getPackageClasses(String pname, boolean excludeJar) {
		Map<String, Class<?>> classes = new HashMap<>();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String packageDirName = pname.replace('.', '/');
		//LogCore.core.info("===getPackageClasses packageDirName={}", packageDirName);
		try {
			// 查找所有给定名称的资源（资源名称是以 '/'分隔的标识资源的路径名称）
			// 打包成jar时，必须勾选添加目录条目(Add directory entries)，否则getResources结果为空
			Enumeration<URL> dirs = cl.getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				// 获取路径的协议头：如file,jar,http等标识
				String protocol = url.getProtocol();
				//LogCore.core.info("===getPackageClasses pt={}, url={}", protocol, url.getFile());
				if ("file".equals(protocol)) {
					findByFile(cl, pname, URLDecoder.decode(url.getFile(), "utf-8"), classes);
					//LogCore.core.info("===getPackageClasses size={}, pt={}, url={}", classes.size(), protocol, url.getFile());
				} else if ("jar".equals(protocol)) {
					if (!excludeJar) {// 没有排除查找JAR
						findInJar(cl, pname, packageDirName, url, classes);
						//LogCore.core.info("===getPackageClasses size={}, pt={}, url={}", classes.size(), protocol, url.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return classes;
	}
	public Map<String, Class<?>> getPackageClasses(String pname) {
		return getPackageClasses(pname, false);
	}

	/**
	 * 从文件获取java类
	 * @param cl
	 * @param packageName
	 * @param filePath
	 * @param classes
	 */
	private void findByFile(ClassLoader cl, String packageName, String filePath, Map<String, Class<?>> classes) {
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory())
			return;

		File[] dirFiles = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".class");
			}
		});

		if (dirFiles == null || dirFiles.length == 0)
			return;

		for (File file : dirFiles) {
			if (file.isDirectory()) {
				findByFile(cl, packageName + "." + file.getName(), file.getAbsolutePath(), classes);
			} else {
				try {
					// 去掉后缀.class
					String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
					Class<?> clazz = cl.loadClass(className);
					classes.put(className, clazz);
				} catch (ExceptionInInitializerError e) {
					LogCore.core.error("init class error",e);
					// 这个没关系 是无法初始化类
				} catch (NoClassDefFoundError e) {
					LogCore.core.error("init class error",e);
					// 这个没关系 是无法初始化类
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 读取jar中的java类
	 * @param cl
	 * @param pname
	 * @param packageDirName
	 * @param url
	 * @param classes
	 */
	private void findInJar(ClassLoader cl, String pname, String packageDirName, URL url, Map<String, Class<?>> classes) {
		try {
			JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
			Enumeration<JarEntry> entries = jar.entries();

			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory())
					continue;

				String name = entry.getName();
				if (name.charAt(0) == '/') {
					name = name.substring(0);
				}
				if (name.startsWith(packageDirName) && name.contains("/") 
						&& name.endsWith(".class") && !name.startsWith("game/msg")) {
					// 只要.class类文件，排除.json的表格数据，排除msg协议
					name = name.substring(0, name.length() - 6).replace('/', '.');// 去掉后缀.class
					try {
						Class<?> clazz = cl.loadClass(name);
						classes.put(name, clazz);
					} catch (Throwable e) {
						LogCore.core.error("===findInJar无法直接加载的类={}", name);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Class<?>> getClassInPackage(String pkgName) {
		// LogCore.core.info("===检测加载类文件：PackageClass getClassInPackage() pkgName={}",
		// pkgName);//sjh
		// 返回值
		Map<String, Class<?>> result = new HashMap<>();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		List<String> listClassName = new ArrayList<String>();
		String rPath = pkgName.replace('.', '/') + "/";
		try {
			for (String path : PATHS) {
				// 包对应的文件目录
				File classPath = new File(path);
				if (!classPath.exists())
					continue;
				
				//LogCore.core.info("===getClassInPackage rPath={},classPath={}", rPath, classPath.getPath());
				if (classPath.isDirectory()) {// 目录
					File dir = new File(classPath, rPath);
					if (dir == null || !dir.exists())
						continue;

					File[] files = dir.listFiles();
					if (files == null)
						continue;
					
					for (File file : files) {
						if (file != null && file.isFile()) {
							String clsName = file.getName();
							clsName = pkgName + "." + clsName.substring(0, clsName.length() - 6);// 去掉后缀.class
							listClassName.add(clsName);
						}
					}
				} else {// jar包
					// LogCore.core.info("===检测加载类文件：PackageClass getClassInPackage() classPath={}",
					// classPath);//sjh
					FileInputStream fis = new FileInputStream(classPath);
					JarInputStream jis = new JarInputStream(fis, false);
					JarEntry je = null;
					while ((je = jis.getNextJarEntry()) != null) {
						String jeName = je.getName();
						if (jeName.startsWith(rPath) && !jeName.endsWith("/") 
								&& jeName.endsWith(".class") && !jeName.startsWith("game/msg")) {
							// 只要.class类文件，排除.json的表格数据
							jeName = jeName.replace('/', '.').substring(0, jeName.length() - 6);// 去掉后缀.class
							listClassName.add(jeName);// jeName="game.worldsrv.test.Test"
							//System.out.println(" jeName="+jeName);
						}
						jis.closeEntry();
					}
					jis.close();
				}
			}

			for (String className : listClassName) {
				// 忽略类
				if (ignoreClass.contains(className))
					continue;

				// 得到类class信息
				Class<?> clazz = cl.loadClass(className);

				// 记录返回值
				result.put(className, clazz);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * 获取包目录下的全部CLASS
	 * @return
	 */
	public Map<String, Class<?>> find() {
		return mapAllClass;
	}

	/**
	 * 获取包目录下的全部CLASS
	 * @param packageNames
	 * @return
	 */
	public Map<String, Class<?>> find(Collection<String> packageNames) {
		Map<String, Class<?>> result = new HashMap<>();
		for (String p : packageNames) {
			result.putAll(find(p));
		}
		return result;
	}

	/**
	 * 获取包目录下的全部CLASS
	 * @param packageName
	 * @return
	 */
	public Map<String, Class<?>> find(String packageName) {
		try {
			// LogCore.core.info("===检测加载类文件：PackageClass find() packageName={}",
			// packageName);//sjh
			// 返回值
			Map<String, Class<?>> result = new HashMap<>();
			ClassLoader cl = Thread.currentThread().getContextClassLoader();

			// 从指定的目录中查找目录和以.class结尾的文件
			List<File> files = findFiles(packageName);
			// 遍历此目录所有的文件
			for (File file : files) {
				String fileName = file.getName();
				// LogCore.core.info("===检测加载类文件：PackageClass find() fileName={}",
				// fileName);//sjh
				// 如果是目录 递归遍历
				if (file.isDirectory()) {
					String pack;
					if (StringUtils.isEmpty(packageName))
						pack = fileName;
					else
						pack = packageName + "." + fileName;
					Map<String, Class<?>> r = find(pack);
					if (r != null && !r.isEmpty()) {
						result.putAll(r);
					}
				}

				// 如果是文件 直接进行记录
				if (file.isFile()) {
					// 得到类名 比如将User.class变为User
					String classSimpleName = fileName.substring(0, fileName.lastIndexOf('.'));
					// 得到类全名 比如core.Node
					String className = packageName + "." + classSimpleName;

					// 忽略类
					if (ignoreClass.contains(className))
						continue;

					// 得到类class信息
					Class<?> clazz = cl.loadClass(className);

					// 记录返回值
					result.put(className, clazz);
				}
			}

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将从配置文件中读取的形如core.support转成core/support路径格式
	 * @param packageName
	 * @return
	 */
	public String packageToPath(String packageName) {
		return packageName.replaceAll("\\.", "/");
	}

	/**
	 * 忽略的类 有些类Class.forName()时会报错或有其他问题
	 * @param ignoreClass
	 */
	public void ignoreClass(List<Class<?>> ignoreClass) {
		this.ignoreClass.clear();
		for (Class<?> c : ignoreClass) {
			this.ignoreClass.add(c.getName());
		}
	}

	/**
	 * 获取符合包限制的文件
	 * @return
	 */
	private List<File> findFiles(String packageName) {
		List<File> result = new ArrayList<>();
		for (String path : PATHS) {
			// 包对应的文件目录
			File dir = new File(path, packageToPath(packageName));

			// 从指定的目录中查找目录和以.class结尾的文件
			File[] files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return true;
					return pathname.getName().matches(".*\\.class$");
				}
			});
			if (files == null)
				files = new File[0];

			// 记录本地址下符合的类文件
			for (File f : files) {
				result.add(f);
			}
		}

		return result;
	}
	
}
