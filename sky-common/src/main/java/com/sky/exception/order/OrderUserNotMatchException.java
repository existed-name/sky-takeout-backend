package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 该订单不属于当前用户
 *
 * @CreateTime 2026/6/23 20:47
 */
public class OrderUserNotMatchException extends BusinessException {
    public OrderUserNotMatchException( String message ){
        super( message );
    }
}
