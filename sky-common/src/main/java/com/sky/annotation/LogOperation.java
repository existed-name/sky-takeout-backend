package com.sky.annotation;

import com.sky.enumeration.OperationType;

/**
 * 操作日志注解
 * <br>
 * 注: 用动宾 LogOperation 表达动作
 *
 * @CreateTime 2026/5/31 8:55
 */
public @interface LogOperation {
    /** 操作描述，如"新增菜品-鱼香肉丝"、"启用账号-admin" */
    String description();

    /** 操作类型：INSERT / UPDATE / DELETE */
    OperationType operationType();

    /** 是否记录请求参数（默认记录，有些查询不想记可以关掉） */
    boolean recordParams() default true;
}
