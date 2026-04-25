package io.cooligc.scheduleit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledTask {
    long fixedDelay() default -1;
    String poolName() default "default";
}