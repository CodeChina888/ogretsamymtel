package core.gen;

import java.io.File;
import java.util.List;
import java.util.Set;

import core.gen.GenBase;
import core.gen.GofGenFile;
import core.support.PackageClass;

public class GenClean {
	private String targetDir; // 待删除文件夹
	private String packageName; // 待删除最外层包名

	public GenClean(String pack, String targetDir) {
		this.packageName = pack;
		this.targetDir = targetDir;
	}

	/**
	 * 清楚目录下的有GenCallbackFile，GenProxyFile, GenEntityFile注解的类
	 */
	public void clean() {
		// 获取待删除文件夹下的所有类
		List<Class<?>> sources = GenBase.getSources(packageName, true);

		// 遍历所有类，删除有注解的生成实体类
		for (Class<?> clazz : sources) {
			// 删除有GenCallbackFile，GenProxyFile注解的类
			if (clazz.isAnnotationPresent(GofGenFile.class)) {
				String filePath = targetDir + PackageClass.getInstance().packageToPath(clazz.getPackage().getName())
						+ "/" + clazz.getSimpleName() + ".java";

				delete(filePath);
			}
		}
	}

	/**
	 * 删除文件
	 * @param filePath
	 */
	public void delete(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			file.delete();
			System.out.println("删除文件：" + filePath);
		}
	}

}
