package com.sky.projection.order;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记录和订单支付超时相关的字段, 用于定时任务查询未支付订单的信息
 *
 * @CreateTime 2026/9/13 8:26
 */
@Data
public class PaymentTimeoutProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 主键id */
    private Long id;

    /** 订单状态：1 待付款  2 待接单  3 已接单  4 派送中  5 已完成  6 已取消  7 退款 */
    private Integer status;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 支付状态：0 未支付  1 已支付  2 退款 */
    private Integer payStatus;



    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
