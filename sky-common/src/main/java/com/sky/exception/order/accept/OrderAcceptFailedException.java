package com.sky.exception.order.accept;

import com.sky.exception.BusinessException;

/**
 * 接受订单失败, 比如订单状态不满足接单要求
 *
 * @CreateTime 2026/8/27 22:41
 */
public class OrderAcceptFailedException extends BusinessException {
    public OrderAcceptFailedException( String message ){
        super( message );
    }
}
