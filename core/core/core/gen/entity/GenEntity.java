package core.gen.entity;

import static core.gen.entity.Column.DEFAULTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import core.entity.EntityBase;
import core.gen.GenBase;
import core.gen.GenUtils;
import core.gen.GofGenFile;
import core.gen.entity.Entity;
import core.gen.entity.Transient;
import core.support.PackageClass;

/**
 * Entity自动生成类
 */
public class GenEntity extends GenBase {
	public final String TEMP_NAME = "Entity.ftl";
	public final String TEMP_NAME_SUPER = "SuperEntity.ftl";
	public final Class<GofGenFile> ANNOTATION_CLASS = GofGenFile.class;

	/**
	 * 创建和调整配置,这个在生命周期中只做一次
	 */
	public GenEntity(String packageName, String targetDir) {
		super(packageName, targetDir);
		this.init();
	}

	/**
	 * 初始化class信息，是否能生成等字段
	 */
	private void init() {
		this.canGen = true; // 默认是true

		// 遍历所有类信息, 获取模板信息, 判断是否出错, 出错则初始化异常不能生成
		List<Class<?>> classes = getClassInfoToGen();
		for (Class<?> clazz : classes) {
			try {
				this.rootMaps.add(getRootMap(clazz));
			} catch (Exception e) {
				// 如果获取模板内容异常，表示不能生成
				this.canGen = false; // 不能生成
				System.err.println("文件存在错误，不继续进行Entity生成了，错误如下：");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 生成java实体类的核心方法
	 * @param rootMap
	 * @throws Exception
	 */
	@Override
	protected void genFileHandler(Map<String, Object> rootMap) throws Exception {
		// 准备参数生成文件
		String fileName = (String) rootMap.get("entityName");
		String packName = (String) rootMap.get("packageName");
		String tempName = (String) rootMap.get("templateName");
		String targetFileDir = this.targetDir + PackageClass.getInstance().packageToPath(packName) + "/";

		this.writeFile(targetFileDir, fileName + ".java", tempName, rootMap);
	}

	/**
	 * 获取要生成的class信息
	 * @return
	 */
	public List<Class<?>> getClassInfoToGen() {
		List<Class<?>> result = new ArrayList<>();

		// 获取源文件夹下的所有类
		List<Class<?>> sources = GenBase.getSources(packageName, true);

		// 遍历所有类，取出有注解的生成实体类
		for (Class<?> clazz : sources) {
			// 过滤没有@Entity注解的类
			if (clazz.isAnnotationPresent(Entity.class)) {
				result.add(clazz);
			}
		}

		return result;
	}

	/**
	 * 根据Class获取填充模板内容
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getRootMap(Class<?> clazz) throws Exception {
		/* 设置模板的数据内容 */
		Map<String, Object> rootMap = new HashMap<String, Object>();
		List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();

		// 设置父类包名和类名
		// 设置默认父类名, 所在包名
		Class<?> superClassInfo = EntityBase.class;
		String superClassPackage = superClassInfo.getPackage().getName();
		String superClassName = superClassInfo.getSimpleName();
		Class<?> superClass = (Class<?>) GenUtils.getPropFromEntityConfig(clazz, "superClass");
		Class<?> superEntity = (Class<?>) GenUtils.getPropFromEntityConfig(clazz, "superEntity");
		boolean isSuper = (boolean) GenUtils.getPropFromEntityConfig(clazz, "isSuper");
		boolean hasSuper = false;

		// superClass不是Object，则父类为superClass
		if (!superClass.equals(Object.class)) {
			superClassPackage = superClass.getPackage().getName();
			superClassName = superClass.getSimpleName();
		} else if (!superEntity.equals(Object.class)) {
			// 若未设superClass，且superEntity不是Object，则父类为superEntity
			superClassPackage = superEntity.getPackage().getName();
			superClassName = (String) GenUtils.getPropFromEntityConfig(superEntity, "entityName");
		}

		// 有上级entity
		if (!superEntity.equals(Object.class)) {
			hasSuper = true;
		}

		// 模板名
		String templateName = TEMP_NAME;
		// 若isSuper为true，则设置模板为Super
		if (isSuper) {
			templateName = TEMP_NAME_SUPER;
		}

		// 获取实体类名,表名,包名
		String packageName = clazz.getPackage().getName();
		String entityName = (String) GenUtils.getPropFromEntityConfig(clazz, "entityName");
		String tableName = (String) GenUtils.getPropFromEntityConfig(clazz, "tableName");

		// 填充Map
		rootMap.put("packageName", packageName);
		rootMap.put("superClassPackage", superClassPackage);
		rootMap.put("superClassName", superClassName);
		rootMap.put("annotationPack", ANNOTATION_CLASS.getName());
		rootMap.put("annotation", "@" + ANNOTATION_CLASS.getSimpleName());
		rootMap.put("entityName", entityName);
		rootMap.put("tableName", tableName);
		rootMap.put("fields", fields);
		rootMap.put("templateName", templateName);
		rootMap.put("hasSuper", hasSuper);

		// 默认加入id的get和set信息，id这玩意儿在枚举类里面木有，所以在这儿处理
		Map<String, Object> id = new HashMap<String, Object>();
		id.put("isTransient", false);
		id.put("name", "id");
		id.put("type", "long");
		id.put("comment", "id");

		fields.add(id);

		// 获得所有枚举字段成员（id, account, name, profession...），
		// 遍历每个枚举成员，获取属性，放入Map中
		Object[] enums = clazz.getEnumConstants();
		for (Object e : enums) {
			Map<String, Object> field = new HashMap<String, Object>();
			String name = e.toString();
			// 获取字段Column注解中的的type, comment等信息
			Class<?> type = (Class<?>) GenUtils.getFieldInfo(e, "type");
			String comment = (String) GenUtils.getFieldInfo(e, "comment");
			String defaults = (String) GenUtils.getFieldInfo(e, DEFAULTS);

			// 判断是否有Transient注解，在模板中进行相应的处理
			// 根据e获取所在class的对应name字段的注解是否有Transient
			if (e.getClass().getField(((Enum<?>) e).name()).isAnnotationPresent(Transient.class)) {
				field.put("isTransient", true);
			} else {
				field.put("isTransient", false);
			}
			// 判断有默认值则加上
			if (!StringUtils.isEmpty(defaults)) {
				field.put("defaults", defaults);
			}
			field.put("name", name);
			field.put("type", type.getSimpleName());
			field.put("comment", comment);

			fields.add(field);
		}

		return rootMap;
	}
}
