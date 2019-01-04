package core.gen.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Entity {
	/**
	 * 生成的实体名称
	 */
	String entityName();

	/**
	 * 生成的数据库表名称
	 */
	String tableName() default "";

	/**
	 * 设置需要继承Entity父类
	 */
	Class<?> superEntity() default Object.class;

	/**
	 * 是否为Entity父类
	 */
	boolean isSuper() default false;

	/**
	 * 生成的实体继承的父类 - 当与superEntity同时设置时 创建库时子类会继承superEntity的字段
	 * 但生成的实体的父类会继承superClass 所以这种情况下, superClass类一般会继承superEntity生成的实体类
	 */
	Class<?> superClass() default Object.class;
}
