package core.gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import core.gen.GenBase;
import core.support.PackageClass;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 自动生成基类
 */
public abstract class GenBase {
	// 配置相关
	protected String pageEncoding = "UTF-8";
	protected Configuration configuration; // FreeMarker配置

	// 文件夹相关
	protected String packageName; // 配置源文件夹
	protected String targetDir; // 输出目标文件夹

	public static String pluginDesFileName; // 插件指定生成的文件名
	
	public static String userDir = System.getProperty("user.dir"); // 用户当前执行的所在路径

	// 文件生成相关
	protected List<Map<String, Object>> rootMaps = new ArrayList<>(); // 要放到模板的信息
	public boolean canGen; // 是否能正常生成

	/**
	 * 创建和调整配置,这个在生命周期中只做一次
	 */
	public GenBase(String packageName, String targetDir) {
		this.packageName = packageName;
		this.targetDir = targetDir;
		configuration = new Configuration();

		try {
			String tempDir = userDir + "/templates";
			configuration.setDirectoryForTemplateLoading(new File(tempDir));
			configuration.setEncoding(Locale.getDefault(), pageEncoding);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 生成文件统一方法
	 * @param fileName
	 * @param rootMap
	 * @throws Exception
	 */
	protected void writeFile(String targetFileDir, String fileName, String tempName, Object rootMap) throws Exception {
		/* 1，获取模板 */
		Template temp = configuration.getTemplate(tempName, pageEncoding);
		temp.setEncoding(pageEncoding);

		// 判断目标文件夹不存在，则新建文件夹
		File dir = new File(targetFileDir);
		if (!dir.exists())
			dir.mkdirs();

		/* 2，将模板和数据合并，并生成文件 */
		String fileFullName = targetFileDir + fileName;
		System.out.println("-------开始生成" + fileFullName + "文件......------");

		File target = new File(fileFullName);
		Writer out = new OutputStreamWriter(new FileOutputStream(target), pageEncoding);
		temp.process(rootMap, out);
		out.flush();
		out.close();

		System.out.println("-------" + fileName + "文件生成完毕!-------\n");
	}

	/**
	 * 自动生成文件的具体方法
	 * @throws Exception
	 */
	public void genFiles() throws Exception {
		// 判断不能生成
		if (!this.canGen) {
			System.out.println("代码生成失败, 请检查错误后重试!");
		} else {
			genGlobalFile(this.rootMaps);

			// 遍历生成文件
			for (Map<String, Object> rootMap : this.rootMaps) {
				genFileHandler(rootMap);
			}
		}
	}

	/**
	 * 生成类的核心方法, 各自子类实现
	 * @param rootMap
	 * @throws Exception
	 */
	protected abstract void genFileHandler(Map<String, Object> rootMap) throws Exception;

	/**
	 * 对该类注解生成全局唯一的文件，子类可覆盖
	 * @throws Exception
	 */
	private void genGlobalFile(List<Map<String, Object>> mapList) throws Exception {
		// 判断是否为空
		int count = 0;
		for (Map<String, Object> map : mapList) {
			count += map.size();
		}
		if (count == 0)
			return;

		if (this.rootMaps.size() == 0)
			return;
		Map<String, Object> data = new HashMap<>();
		data.put("rootPackageName", this.packageName);
		data.put("methodsList", this.rootMaps);
		genGlobalFileHandler(data);
	}

	protected void genGlobalFileHandler(Map<String, Object> rootMaps) throws Exception {

	}

	public static Set<Class<?>> getPluginGenDesFile(String packageName) {
		// System.out.println("getPluginGenDesFile = "+packageName);
		String pathStr = System.getProperty("java.class.path");
		String[] PATHS = pathStr.split(";");

		// 处理插件发来的路径 (src/game/worldsrv/test/TestManager.java)
		// 替换.java为.class
		GenBase.pluginDesFileName = GenBase.pluginDesFileName.replace(".java", ".class");
		// 去掉SRC目录
		GenBase.pluginDesFileName = GenBase.pluginDesFileName.replace("src", "");

		// 去掉第一个seam
		if (GenBase.pluginDesFileName.indexOf("seam") != -1) {
			String temp = GenBase.pluginDesFileName.replaceFirst("seam", "");
			// 如果有两个seam 则替换第一个出现的
			if (temp.indexOf("seam") != -1)
				GenBase.pluginDesFileName = GenBase.pluginDesFileName.replaceFirst("seam", "");
		}

		// 获得包名 + 文件名
		packageName = GenBase.pluginDesFileName.substring(1, GenBase.pluginDesFileName.indexOf(".")).replace("/", ".");

		// 返回值
		Set<Class<?>> result = new HashSet<Class<?>>();

		for (String path : PATHS) {
			// 只处理Bin目录下的文件 处理过的目录跳过
			if (!path.contains("/bin"))
				continue;

			String filePath = GenBase.pluginDesFileName;

			try {
				// class文件
				File file = new File(path, filePath);

				if (file.exists()) {
					// 得到类class信息
					Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName);
					result.add(clazz);
				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	/**
	 * 获得要生成的类
	 * @param packageName
	 * @return
	 */
	public static List<Class<?>> getSources(String packageName, boolean excludeJar) {
		Set<Class<?>> sources = new HashSet<>();
		if (StringUtils.isNotEmpty(GenBase.pluginDesFileName)) {
			sources = GenBase.getPluginGenDesFile(packageName);
		} else {
			// 获取待删除文件夹下的所有类
			//Map<String, Class<?>> mapAllClass = PackageClass.getInstance().find(packageName);// 找文件夹里的类文件
			//if (mapAllClass.isEmpty()) {// add by shenjh,文件夹里找不到，到jar包里找
			//	mapAllClass = PackageClass.getInstance().getClassInPackage(packageName);// 找jar包里的类文件
			//}
			Map<String, Class<?>> mapAllClass = PackageClass.getInstance().getPackageClasses(packageName, excludeJar);
			//LogCore.core.info("===GenBase.getSources size={},packageName={}", mapAllClass.size(), packageName);
			if (!mapAllClass.isEmpty()) {
				sources.addAll(mapAllClass.values());
			}
		}

		List<Class<?>> result = new ArrayList<>();
		result.addAll(sources);
		Collections.sort(result, (a, b) -> a.getName().compareTo(b.getName()));
		return result;
	}
	public static List<Class<?>> getSources(String packageName) {
		return getSources(packageName, false);
	}
	
}
