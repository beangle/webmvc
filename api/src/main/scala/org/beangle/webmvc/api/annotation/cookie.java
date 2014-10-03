package org.beangle.webmvc.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface cookie {

	String value();

	boolean required() default true;

	String defaultValue() default DefaultNone.value;
}