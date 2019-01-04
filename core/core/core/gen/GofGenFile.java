package core.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 所有自动生成的类均被标注此注解，以便于自动删除
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GofGenFile {

}
