package core.gen.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成Proxy代理接口类需用设置此注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DistrClass {
	String servId() default ""; // 服务ID

	Class<?>[] importClass() default {}; // 生成的Proxy代理类中需要额外引用的类
}