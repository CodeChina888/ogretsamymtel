package core.support.observer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.protobuf.GeneratedMessage;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MsgReceiver {
	Class<? extends GeneratedMessage>[] value();
}