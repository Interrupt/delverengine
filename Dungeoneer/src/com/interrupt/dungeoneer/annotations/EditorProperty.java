package com.interrupt.dungeoneer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.badlogic.gdx.utils.Array;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EditorProperty {
	String group() default "";
	String[] valid() default {};
	String type() default "";
	String params() default "";
	boolean include_base() default true;
}
