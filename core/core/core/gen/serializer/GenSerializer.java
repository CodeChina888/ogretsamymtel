package core.gen.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.gen.GenBase;
import core.gen.GofGenFile;
import core.interfaces.ISerilizable;
import core.support.PackageClass;

/**
 * Serializer自动生成类
 */
public class GenSerializer extends GenBase {

	public final Class<GofGenFile> ANNOTATION_CLASS = GofGenFile.class;

	private String _genClassName = "CommonSerializer";
	private String _ftlFileName = "Serializer.ftl";
	private Class<?> _annoClass = ISerilizable.class;
	private int _constructorParamCount = 0;

	public GenSerializer(String packageName, String targetDir, String genClassName, String ftlFileName, Class<?> clazz,
			int constructorParamCount) throws Exception {
		super(packageName, targetDir);
		_genClassName = genClassName;
		_ftlFileName = ftlFileName;
		_annoClass = clazz;
		_constructorParamCount = constructorParamCount;
		this.init();
	}

	/**
	 * 初始化class信息，是否能生成等字段
	 * @throws Exception
	 */
	private void init() throws Exception {
		this.canGen = true; // 默认是true

		// 遍历所有类信息, 获取模板信息, 判断是否出错, 出错则初始化异常不能生成
		List<Class<?>> classes = getClassInfoToGen();
		for (Class<?> clazz : classes) {
			try {
				this.rootMaps.add(getRootMap(clazz));
			} catch (Exception e) {
				// 如果获取模板内容异常，表示不能生成
				this.canGen = false; // 不能生成
				System.err.println("文件存在错误，不继续进行ObServer生成了，错误如下：");
				e.printStackTrace();
			}
		}

	}

	/**
	 * 生成Proxy类的核心方法
	 * @param rootMap
	 * @throws Exception
	 */

	@Override
	protected void genFileHandler(Map<String, Object> rootMap) throws Exception {
		/*
		 * // 准备参数生成文件 String fileName = (String) rootMap.get("proxyName");
		 * String packName = (String) rootMap.get("packageName"); String
		 * targetFileDir = this.targetDir +
		 * PackageClass.getInstance().packageToPath(packName) + "/";
		 * this.writeFile(targetFileDir, fileName + ".java", TEMP_NAME,
		 * rootMap);
		 */
	}

	@Override
	protected void genGlobalFileHandler(Map<String, Object> rootMaps) throws Exception {
		rootMaps.put("rootClassName", _genClassName);
		rootMaps.put("interfaceName", _annoClass.getName());

		String targetFileDir = this.targetDir + PackageClass.getInstance().packageToPath(this.packageName) + "/";
		this.writeFile(targetFileDir, _genClassName + ".java", _ftlFileName, rootMaps);
	}

	/**
	 * 获取要生成的class及其method
	 * @return
	 * @throws Exception
	 */
	private List<Class<?>> getClassInfoToGen() throws Exception {
		List<Class<?>> result = new LinkedList<>();
		// 获取源文件夹下的所有类
		List<Class<?>> sources = GenBase.getSources(packageName, true);

		// 遍历所有类，取出类中有@相应注解的方法
		// ClassPool pool = ClassPool.getDefault();
		for (Class<?> clazz : sources) {
			int mod = clazz.getModifiers();
			if (Modifier.isInterface(mod) || Modifier.isAbstract(mod))
				continue;
			if (!Modifier.isPublic(mod))
				continue;

			if (!checkConstructorParamCount(clazz, this._constructorParamCount))
				continue;

			if (_annoClass.isAssignableFrom(clazz)) {
				result.add(clazz);
			}
		}

		return result;
	}

	private boolean checkConstructorParamCount(Class<?> clazz, int count) {
		Constructor<?>[] cs = clazz.getDeclaredConstructors();
		for (Constructor<?> c : cs) {
			if (c.getParameterCount() == count)
				return true;
		}
		return false;
	}

	/**
	 * 根据Class及其methods获取填充模板内容
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getRootMap(Class<?> clazz) throws Exception {
		// 获取实体类名,表名,包名
		// String packageName = clazz.getPackage().getName();

		String className = clazz.getName();
		className = className.replace('$', '.');
		int id = clazz.getName().hashCode();
		// 填充Map
		Map<String, Object> rootMap = new HashMap<>();
		// rootMap.put("packageName", packageName);
		rootMap.put("className", className);
		rootMap.put("id", String.valueOf(id));
		return rootMap;
	}

}
