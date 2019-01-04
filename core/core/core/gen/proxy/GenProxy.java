package core.gen.proxy;

import java.lang.reflect.Modifier;
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
import core.gen.GenUtils;
import core.gen.GofGenFile;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.PackageClass;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;

/**
 * Proxy自动生成类
 */
public class GenProxy extends GenBase {
	public final String CLASS_SUFFIX = "Proxy"; // 文件后缀
	public final String TEMP_NAME = "Proxy.ftl"; // 模板名
	public final Class<GofGenFile> ANNOTATION_CLASS = GofGenFile.class;

	class SlotIndex {
		public SlotIndex(int slot, int index) {
	    	this.slot = slot;
	    	this.index = index;
		}
		public int slot;
		public int index;
	};

	public GenProxy(String packageName, String targetDir) throws Exception {
		super(packageName, targetDir);
		this.init();
	}

	/**
	 * 初始化class信息，是否能生成等字段
	 * @throws Exception
	 */
	private void init() throws Exception {
		this.canGen = true; // 默认是true
		
		// 遍历所有类信息, 获取模板信息, 判断是否出错, 出错则初始化异常不能生成
		Map<Class<?>, List<CtMethod>> classes = getClassInfoToGen();
		System.out.println("===GenProxy.init size=" + classes.size());
		for (Entry<Class<?>, List<CtMethod>> entry : classes.entrySet()) {
			Class<?> clazz = entry.getKey();
			List<CtMethod> ms = entry.getValue();
			try {
				//System.out.println("===GenProxy.clazz=" + clazz.toString());
				this.rootMaps.add(getRootMap(clazz, ms));
			} catch (Exception e) {
				// 如果获取模板内容异常，表示不能生成
				this.canGen = false; // 不能生成
				System.err.println("文件存在错误，不继续进行Proxy生成了，错误如下：");
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
		// 准备参数生成文件
		String fileName = (String) rootMap.get("proxyName");
		String packName = (String) rootMap.get("packageName");
		String targetFileDir = this.targetDir + PackageClass.getInstance().packageToPath(packName) + "/";

		this.writeFile(targetFileDir, fileName + ".java", TEMP_NAME, rootMap);
	}

	// @Override
	// protected void genAllFileHandler(Map<String, Object> rootMaps) throws
	// Exception {
	// String targetFileDir = this.targetDir +
	// PackageClass.getInstance().packageToPath(this.packageName) + "/";
	// this.writeFile(targetFileDir, "EnumCall.java", "EnumCall.ftl", rootMaps);
	// }

	/**
	 * 获取要生成的class及其method
	 * @return
	 * @throws Exception
	 */
	private Map<Class<?>, List<CtMethod>> getClassInfoToGen() throws Exception {
		Map<Class<?>, List<CtMethod>> result = new LinkedHashMap<Class<?>, List<CtMethod>>();

		// 获取源文件夹下的所有类
		List<Class<?>> sources = GenBase.getSources(packageName, true);
		//System.out.println("===GenProxy.getClassInfoToGen packageName=" + packageName + " size=" + sources.size());
		// 遍历所有类，取出类中有@DistrClass注解的方法
		ClassPool pool = ClassPool.getDefault();
		for (Class<?> clazz : sources) {
			// 如果没有@DistriClass注解, 则不处理
			if (!clazz.isAnnotationPresent(DistrClass.class)) {
				continue;
			}
			//System.out.println("===GenProxy:" + clazz.getName());
			CtClass cc = pool.get(clazz.getName());
			CtMethod[] ms = cc.getMethods();
			List<CtMethod> methods = new ArrayList<CtMethod>();

			// 遍历所有方法, 如果有@DistrMethod注解，则加入list
			for (CtMethod m : ms) {
				if (m.getAnnotation(DistrMethod.class) != null) {
					methods.add(m);
				}
			}

			// 如果有@DistrMethod注解的方法, 则加入待创建数据
			if (!methods.isEmpty()) {
				// 排序
				Collections.sort(methods, new Comparator<CtMethod>() {
					@Override
					public int compare(CtMethod m1, CtMethod m2) {
						return m1.getName().compareTo(m2.getName());
					}
				});

				result.put(clazz, methods);
			}
		}

		return result;
	}

	private List<SlotIndex> buildSlotIndex(LocalVariableAttribute attr) {
	    List<SlotIndex> list = new ArrayList<>();
		for (int i=0; i<attr.tableLength(); ++i) {
			int slot = attr.index(i);
			list.add(new SlotIndex(slot, i));
		}
		Collections.sort(list, (m1, m2) -> (m1.slot - m2.slot));
		//for (SlotIndex s : list) {
		//	System.out.println("slot:"+s.slot+ " index:"+s.index);
		//}
		return list;
	}
	/**
	 * 根据Class及其methods获取填充模板内容
	 * @param clazz
	 * @param methods
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getRootMap(Class<?> clazz, List<CtMethod> methods) throws Exception {
		// 获取实体类名,表名,包名
		String packageName = clazz.getPackage().getName();
		String className = clazz.getSimpleName();
		String proxyName = className + CLASS_SUFFIX;

		// 获取@DistributedProxyClass注解相关信息, port名,node名,id,需要引入的包等
		Class<?>[] importPacks = (Class<?>[]) GenUtils.getPropFromDProxy(clazz, "importClass");
		List<String> importPackages = new ArrayList<String>();
		// 遍历获取包路径全名
		for (Class<?> ip : importPacks) {
			// 判断是内部类则替换$
			String packName = ip.getName();
			if (ip.isMemberClass())
				packName = ip.getName().replace("$", ".");

			importPackages.add(packName);
		}

		String servId = (String) GenUtils.getPropFromDProxy(clazz, "servId");

		// 填充Map
		Map<String, Object> rootMap = new HashMap<>();
		List<Map<String, Object>> methodInfos = new ArrayList<>();
		rootMap.put("rootPacageName", this.packageName);
		rootMap.put("packageName", packageName);
		rootMap.put("className", className);
		rootMap.put("proxyName", proxyName);
		rootMap.put("annotationPack", ANNOTATION_CLASS.getName());
		rootMap.put("annotation", "@" + ANNOTATION_CLASS.getSimpleName());
		rootMap.put("methods", methodInfos);
		rootMap.put("servId", servId);
		rootMap.put("methodFunctionAnnotation", "@SuppressWarnings(\"unchecked\")");

		// 如果servId设值了，则有相关语句
		if (!"".equals(servId)) {
			rootMap.put("hasDefault", true);
		} else {
			rootMap.put("hasDefault", false);
		}

		// 如果有引入的包，那么则设置
		if (importPacks.length != 0) {
			rootMap.put("importPackages", importPackages);
		}

		// 遍历methods，获取方法名，方法参数等信息
		for (CtMethod m : methods) {
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

			// 使用javaassis 获取方法形参的类型，参数名
			CodeAttribute codeAttr = m.getMethodInfo().getCodeAttribute();
			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttr.getAttribute(LocalVariableAttribute.tag);
			int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1;
			CtClass[] paramTypes = m.getParameterTypes(); // 获取所有参数类型

			List<SlotIndex> slotIndex = buildSlotIndex(attr);
			for (int i = 0; i < paramTypes.length; i++) {
				/*
				 * 这边为了只保留class.getSimpleName()得到的类名，而不是类名$内部类名，用正则替换了$以前的字符为空
				 * 如com
				 * .google.protobuf.Message.Builder，Builder是Message的内部类，分别是如下结果
				 * ctclass.getSimpleName() -> Message$Builder
				 * ctclass.getSimpleName().replaceAll("^.+\\$", "") -> Builder
				 * 非内部类不影响结果
				 */
				String ptype = paramTypes[i].getSimpleName().replaceAll("^.+\\$", ""); // 参数类型
				int index = slotIndex.get(i).index;
				String pname = attr.variableName(index+pos);
				paramInfo.put(pname, ptype);
				//System.out.println("===method+:"+name+"["+ptype+" "+pname+"]"+" len:"+paramTypes.length+" i:"+i+" pos:"+pos + "----- index:"+index);
			}

			// 参数是不可变的
			DistrMethod mthAnn = (DistrMethod) m.getAnnotation(DistrMethod.class);
			boolean argsImmutable = mthAnn.argsImmutable();

			// 取出具体模板所需方法形参类型，形参名
			int j = 0;
			String callerStrTmp = "";
			boolean hasAnnotation = false;
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
				if (ptype.equals("List") || ptype.equals("Map") || ptype.equals("Set"))
					hasAnnotation = true;
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
			method.put("argsImmutable", argsImmutable);

			if (hasAnnotation) {
				method.put("annotation", "@SuppressWarnings(\"rawtypes\")");
				rootMap.put("methodFunctionAnnotation", "@SuppressWarnings({ \"unchecked\", \"rawtypes\" })");
			} else
				method.put("annotation", "");

			// 生成方法名对应的Enum常量
			String enumCall = callerStr.replace("()", "").replace("[]", "s").replaceAll("[.:(,]", "_")
					.replaceAll("[ )]", "");// sjh//.toUpperCase()
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

			default :
				break;
		}

		return wrapper;
	}

}
