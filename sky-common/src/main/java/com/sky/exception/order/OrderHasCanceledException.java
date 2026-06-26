package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 订单已经取消，不能重复取消
 *
 * @CreateTime 2026/6/26 14:17
 */
public class OrderHasCanceledException extends BusinessException {
    public OrderHasCanceledException( String message ){
        super( message );
    }
}
