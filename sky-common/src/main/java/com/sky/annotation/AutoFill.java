package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 自动填写字段 AOP 注解
 *
 * - 审计字段
 * </pre>
 */
@Retention( RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD } )
public @interface AutoFill {
    OperationType value();
}
