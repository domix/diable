package com.domingosuarez.diable.provider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by domix on 18/05/15.
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD})
public @interface FooProvider {
}
