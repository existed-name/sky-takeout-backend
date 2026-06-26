package com.sky.exception.order;

/**
 * 已完成的订单不能取消
 *
 * @CreateTime 2026/6/26 14:31
 */
public class CompletedOrderCannotCancelException extends RuntimeException {
    public CompletedOrderCannotCancelException( String message ){
        super( message );
    }
}
