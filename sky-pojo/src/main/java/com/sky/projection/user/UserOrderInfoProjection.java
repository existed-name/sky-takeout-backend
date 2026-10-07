package com.sky.projection.user;

import lombok.Data;

/**
 * 用户订单信息投影对象, 用来查询组装订单实体对象{@link com.sky.entity.Orders}
 * 需要的用户信息, 比如用户名
 *
 * @CreateTime 2026/10/6 17:50
 */
@Data
public class UserOrderInfoProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 用户名, user 表为{@code name}, orders 表为{@code user_name} */
    private String name;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
