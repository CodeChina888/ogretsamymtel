package core.gen.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {
	// 表中字段属性名称常量
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String LENGTH = "length";
	public static final String INDEX = "index";
	public static final String NULLABLE = "nullable";
	public static final String DEFAULTS = "defaults";
	public static final String COMMENT = "comment";
		
	// 不同类型字段的长度默认值
	public static final int TYPE_DEFAULT_TINYINT = 4;
	public static final int TYPE_DEFUALT_INT = 11;
	public static final int TYPE_DEFUALT_LONG = 20;
	public static final int TYPE_DEFUALT_STRING = 255;
	public static final int TYPE_DEFUALT_DOUBLE = 0;
	public static final int TYPE_DEFUALT_FLOAT = 0;
	public static final int TEXT_MIN_SIZE = 10240;
	
	// 表中字段约束信息，（类型，长度，索引，是否为空，默认值）
	Class<?> type(); // 字段类型

	String comment(); // 字段注释

	int length() default 0; // 字段长度

	boolean index() default false; // 字段是否为索引

	boolean nullable() default false; // 字段是否为空

	String defaults() default ""; // 字段默认值
}
