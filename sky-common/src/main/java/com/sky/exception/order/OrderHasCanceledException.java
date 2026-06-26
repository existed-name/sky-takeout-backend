package com.sky.exception.order;

/**
 * 订单已经取消，不能重复取消
 *
 * @CreateTime 2026/6/26 14:17
 */
public class OrderHasCanceledException extends RuntimeException {
    public OrderHasCanceledException( String message ){
        super( message );
    }
}
