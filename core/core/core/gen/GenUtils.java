package core.gen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import core.gen.entity.Column;
import core.gen.entity.Entity;
import core.gen.proxy.DistrClass;

public class GenUtils {
	/**
	 * 从class的@EntityConfig注解中获取name对应的值
	 * @param clazz
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static Object getPropFromEntityConfig(Class<?> clazz, String name) throws Exception {
		Annotation annotation = clazz.getAnnotation(Entity.class);
		Method method = Entity.class.getMethod(name);

		Object result = method.invoke(annotation);
		return result;
	}

	/**
	 * 从class的DistributedProxyClass注解中获取name对应的值
	 * @param clazz
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static Object getPropFromDProxy(Class<?> clazz, String name) throws Exception {
		Annotation annotation = clazz.getAnnotation(DistrClass.class);
		Method method = DistrClass.class.getMethod(name);

		Object result = method.invoke(annotation);
		return result;
	}

	/**
	 * 获取class的object字段的name字段值
	 * @param clazz
	 * @param object
	 * @param name
	 * @return
	 * @throws Exception public static Object getFieldValue(Class<?> clazz,
	 *             Object object, String name) throws Exception {
	 *             //遍历所有属性，获取EntityConfigBase属性 Field[] fields =
	 *             object.getClass().getFields(); Field field = null; for(Field
	 *             f : fields) { if(f.get(object) instanceof EntityConfigData) {
	 *             field = f; break; } } //获取EntityConfigBase中name字段值 if(field
	 *             == null) return null; Object obj = field.get(object); Object
	 *             result = obj.getClass().getField(name).get(obj); return
	 *             result; }
	 */

	/**
	 * 根据enumm获得其Column注解的name值
	 * @param enumm
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static Object getFieldInfo(Object enumm, String name) throws Exception {
		// 获取@Column注解 和 和想要的方法
		String enumName = ((Enum<?>) enumm).name(); // 先获取枚举自身名称如sn, level,
													// type, sex...
		Annotation annotation = enumm.getClass().getField(enumName).getAnnotation(Column.class); // 再根据enum获取对应name字段的注解
		Method method = Column.class.getMethod(name);

		Object result = method.invoke(annotation);
		return result;
	}
}
