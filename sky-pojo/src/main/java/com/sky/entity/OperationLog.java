package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志数据记录对象
 *
 * @CreateTime 2026/5/31 8:56
 */
@Data
@AllArgsConstructor
@Builder
public class OperationLog {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 主键 */
    private Long id;

    /** 操作员工ID，对应 employee.id */
    private Long operatorId;

    /** 操作类型枚举：INSERT/UPDATE/DELETE/LOGIN/SELECT */
    private String operationType;

    /** 操作描述，如"新增菜品-鱼香肉丝"、"启用账号-admin" */
    private String description;

    /** 被调用的Controller方法全限定名，如 com.sky.controller.admin.DishController.save */
    private String methodName;

    /** 请求参数(JSON格式)，用于追查当时传入的数据 */
    private String requestParams;

    /** 操作结果：1成功 0失败 */
    private Integer status;

    /** 操作失败时的异常信息，成功时为NULL */
    private String errorMsg;

    /** 方法执行耗时(毫秒) */
    private Integer costTime;

    /** 记录创建时间，即操作时间 */
    private LocalDateTime createTime;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
