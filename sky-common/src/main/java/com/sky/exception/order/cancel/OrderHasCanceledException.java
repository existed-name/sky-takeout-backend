package com.sky.exception.order.cancel;

import com.sky.exception.BusinessException;

/**
 * 订单已经取消，不能重复取消
 *
 * @CreateTime 2026/6/26 14:17
 * @deprecated 统一使用{@link OrderCancelFailedException}, 避免异常类膨胀, 除非后续需要细分业务异常
 */
public class OrderHasCanceledException extends BusinessException {
    public OrderHasCanceledException( String message ){
        super( message );
    }
}
