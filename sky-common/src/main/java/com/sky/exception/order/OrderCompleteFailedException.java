package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 完成订单失败
 *
 * @CreateTime 2026/9/1 6:42
 */
public class OrderCompleteFailedException extends BusinessException {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    public OrderCompleteFailedException( String message ){
        super( message );
    }


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
