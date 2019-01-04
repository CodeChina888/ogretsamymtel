package core.gen.observer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import core.gen.GenBase;
import core.gen.GofGenFile;
import core.support.PackageClass;
import core.support.observer.Listener;
import core.support.observer.MsgReceiver;
import core.support.observer.ObServer;

/**
 * Proxy自动生成类
 */
public class GenObServerInit extends GenBase {
	// public final String CLASS_SUFFIX = "Proxy"; // 文件后缀
	// public final String TEMP_NAME = "Proxy.ftl"; // 模板名
	public final Class<GofGenFile> ANNOTATION_CLASS = GofGenFile.class;

	private String _genClassName = "MsgReceiverInit";
	private String _ftlFileName = "MsgReceiverInit.ftl";
	private Class<? extends Annotation> _annoClass = MsgReceiver.class;

	public GenObServerInit(String packageName, String targetDir, String genClassName, String ftlFileName,
			Class<? extends Annotation> annoClass) throws Exception {
		super(packageName, targetDir);
		_genClassName = genClassName;
		_ftlFileName = ftlFileName;
		_annoClass = annoClass;
		this.init();
	}

	/**
	 * 初始化class信息，是否能生成等字段
	 * @throws Exception
	 */
	private void init() throws Exception {
		this.canGen = true; // 默认是true

		// 遍历所有类信息, 获取模板信息, 判断是否出错, 出错则初始化异常不能生成
		Map<Class<?>, List<Method>> classes = getClassInfoToGen();
		for (Entry<Class<?>, List<Method>> entry : classes.entrySet()) {
			Class<?> clazz = entry.getKey();
			List<Method> ms = entry.getValue();
			try {
				this.rootMaps.add(getRootMap(clazz, ms));
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

		String targetFileDir = this.targetDir + PackageClass.getInstance().packageToPath(this.packageName) + "/";
		this.writeFile(targetFileDir, _genClassName + ".java", _ftlFileName, rootMaps);
	}

	/**
	 * 获取要生成的class及其method
	 * @return
	 * @throws Exception
	 */
	private Map<Class<?>, List<Method>> getClassInfoToGen() throws Exception {
		Map<Class<?>, List<Method>> result = new LinkedHashMap<>();

		// 获取源文件夹下的所有类
		List<Class<?>> sources = GenBase.getSources(packageName, true);

		// 遍历所有类，取出类中有@相应注解的方法
		// ClassPool pool = ClassPool.getDefault();
		for (Class<?> clazz : sources) {
			// 如果没有@DistriClass注解, 则不处理
			// System.out.println(clazz.getName());
			// if(clazz.getName().equals("Msg"))
			// continue;
			// CtClass cc = pool.get(clazz.getName());
			// Method[] ms = cc.getMethods();
			Method[] ms = clazz.getMethods();
			List<Method> methods = new ArrayList<>();

			// 遍历所有方法, 如果有@DistrMethod注解，则加入list
			for (Method m : ms) {
				if (m.isAnnotationPresent(_annoClass)) {
					methods.add(m);
				}
			}

			// 如果有@相应注解的方法, 则加入待创建数据
			if (!methods.isEmpty()) {
				// 排序
				Collections.sort(methods, new Comparator<Method>() {
					@Override
					public int compare(Method m1, Method m2) {
						return m1.getName().compareTo(m2.getName());
					}
				});

				result.put(clazz, methods);
			}
		}

		return result;
	}

	/**
	 * 根据Class及其methods获取填充模板内容
	 * @param clazz
	 * @param methods
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getRootMap(Class<?> clazz, List<Method> methods) throws Exception {
		// 获取实体类名,表名,包名
		String packageName = clazz.getPackage().getName();
		String className = clazz.getSimpleName();

		// 填充Map
		Map<String, Object> rootMap = new HashMap<>();
		List<Map<String, Object>> methodInfos = new ArrayList<>();
		rootMap.put("packageName", packageName);
		rootMap.put("className", className);
		rootMap.put("methods", methodInfos);

		// 遍历methods，获取方法名，方法参数等信息
		for (Method m : methods) {
			// 模板所需数据
			String name = m.getName();
			// String callerStr = packageName + "." + className + ":" + name;
			String callerStr = className + ":" + name;// sjh
			String paramsCall = "";
			String params = "";
			String functionTypes = "";
			boolean hasException = m.getExceptionTypes().length > 0;

			Map<String, Object> method = new LinkedHashMap<>();
			Map<String, Object> paramInfo = new LinkedHashMap<>();

			// 获取注解相关信息
			// 对MsgReceiver特殊处理
			if (_annoClass == MsgReceiver.class) {
				Set<String> keys = ObServer.getListenerKey(m, MsgReceiver.class);
				method.put("keys", keys);
			}
			if (_annoClass == Listener.class) {
				Set<String> keys = ObServer.getListenerKey(m, Listener.class);
				method.put("keys", keys);
			}

			// 使用javaassis 获取方法形参的类型，参数名
			// CodeAttribute codeAttr = m.getMethodInfo().getCodeAttribute();
			// LocalVariableAttribute attr = (LocalVariableAttribute)
			// codeAttr.getAttribute(LocalVariableAttribute.tag);
			// int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1;

			Class<?>[] paramTypes = m.getParameterTypes(); // 获取所有参数类型
			for (int i = 0; i < paramTypes.length; i++) {
				/*
				 * 这边为了只保留class.getSimpleName()得到的类名，而不是类名$内部类名，用正则替换了$以前的字符为空
				 * 如com
				 * .google.protobuf.Message.Builder，Builder是Message的内部类，分别是如下结果
				 * ctclass.getSimpleName() -> Message$Builder
				 * ctclass.getSimpleName().replaceAll("^.+\\$", "") -> Builder
				 * 非内部类不影响结果
				 */
				String ptype = paramTypes[i].getName();// .replaceAll("^.+\\$", ""); // 参数类型
				String pname = "p" + String.valueOf(i);// attr.variableName(i + pos); // 参数名
				paramInfo.put(pname, ptype);
			}

			// 取出具体模板所需方法形参类型，形参名
			int j = 0;
			String callerStrTmp = "";
			for (Entry<String, Object> info : paramInfo.entrySet()) {
				String pname = info.getKey();
				String ptype = (String) info.getValue();

				if (j > 0) {
					params += ", ";
					callerStrTmp += ", ";
					paramsCall += ", ";
					functionTypes += ", ";
				}

				callerStrTmp += ptype;
				paramsCall += pname;
				params += ptype.replaceAll("\\[\\]", "...") + " " + pname;
				functionTypes += primitiveTowrapper(ptype);

				j++;
			}

			callerStr += "(" + callerStrTmp + ")";

			if (StringUtils.isNotBlank(functionTypes)) {
				functionTypes = "<" + functionTypes + ">";
			}

			method.put("name", name);
			method.put("params", params);
			method.put("hasException", hasException);
			method.put("callerStr", callerStr);
			method.put("paramsCall", paramsCall);
			method.put("functionTypes", functionTypes);
			method.put("paramsSize", j);

			// 生成方法名对应的Enum常量
			String enumCall = callerStr.replace("()", "").replace("[]", "s").replaceAll("[.:(,]", "_")
					.replaceAll("[ )]", "");// sjh//.toUpperCase();
			method.put("enumCall", enumCall);
			method.put("enumCallHashCode", String.valueOf(enumCall.hashCode()));

			methodInfos.add(method);
		}

		return rootMap;
	}

	/**
	 * 将基础类型转为包装类型
	 * @param primitive
	 * @return
	 */
	private String primitiveTowrapper(String primitive) {
		String wrapper = primitive;

		switch (primitive) {
			case "int" :
				wrapper = "Integer";
				break;
			case "long" :
				wrapper = "Long";
				break;
			case "double" :
				wrapper = "Double";
				break;
			case "boolean" :
				wrapper = "Boolean";
				break;
			case "byte" :
				wrapper = "Byte";
				break;
			case "short" :
				wrapper = "Short";
				break;

			default :
				break;
		}

		return wrapper;
	}
}
