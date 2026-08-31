package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 派送订单失败
 *
 * @CreateTime 2026/8/31 9:10
 */
public class OrderDeliverFailedException extends BusinessException {
    public OrderDeliverFailedException( String message ){
        super( message );
    }
}
