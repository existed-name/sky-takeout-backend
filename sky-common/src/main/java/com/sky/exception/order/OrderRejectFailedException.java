package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 管理端拒绝订单失败
 *
 * @CreateTime 2026/8/28 16:27
 */
public class OrderRejectFailedException extends BusinessException {
    public OrderRejectFailedException( String message ){
        super( message );
    }
}
