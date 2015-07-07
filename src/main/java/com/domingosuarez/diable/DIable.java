package com.domingosuarez.diable;

import com.domingosuarez.diable.ast.DIableASTTransformation;
import com.domingosuarez.diable.ast.DIableASTTransformationV2;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by domix on 18/05/15.
 */

@Documented
@Retention(SOURCE)
@Target(TYPE)
@GroovyASTTransformationClass(classes = DIableASTTransformationV2.class)
@interface DIable {

}