package com.sky.exception.order;

/**
 * 订单无法取消, 请联系商家取消订单
 *
 * @CreateTime 2026/6/26 14:36
 */
public class OrderCannotCancelException extends RuntimeException {
    public OrderCannotCancelException( String message ){
        super( message );
    }
}
