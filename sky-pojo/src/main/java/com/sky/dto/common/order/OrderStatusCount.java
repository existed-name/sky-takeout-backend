package com.sky.dto.common.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 某种状态的订单数量统计对象, 仅在后端传递数据使用
 *
 * @CreateTime 2026/8/25 6:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusCount {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 订单状态, {@link com.sky.enumeration.OrderStatus} */
    private Integer status;

    /** 订单数量 */
    private Long count;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
