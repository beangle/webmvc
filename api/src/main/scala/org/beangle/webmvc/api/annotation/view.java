package org.beangle.webmvc.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface view {
	/**
	 * @return The name of the result mapping. This is the value that is
	 *         returned from the action method and is used to associate a
	 *         location with a return value.
	 */
	String name() default "success";;

	/**
	 * @return The location of the result within the web application or anywhere
	 *         on disk.
	 */
	String location() default "";

	/**
	 * @return The type of the result. 
	 */
	String type() default "";

	/**
	 * @return The parameters passed to the result. This is a list of strings
	 *         that form a name/value pair chain since creating a Map for
	 *         annotations is not possible. An example would be:
	 *         <code>{"key", "value", "key2", "value2"}</code>.
	 */
	String[] params() default {};
}