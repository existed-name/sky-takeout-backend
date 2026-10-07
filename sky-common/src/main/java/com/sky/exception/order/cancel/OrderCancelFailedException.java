package com.sky.exception.order.cancel;

import com.sky.exception.BusinessException;

/**
 * 订单取消失败, 比如订单状态不支持取消订单
 * <p>
 *     TODO: 后续根据需求区分多种取消失败的场景
 * </p>
 *
 * @CreateTime 2026/8/30 21:18
 */
public class OrderCancelFailedException extends BusinessException {
    public OrderCancelFailedException( String message ){
        super( message );
    }
}
