package com.sky.exception.order.cancel;

import com.sky.exception.BusinessException;

/**
 * 订单无法取消, 请联系商家取消订单
 *
 * @CreateTime 2026/6/26 14:36
 * @deprecated 统一使用{@link OrderCancelFailedException}, 避免异常类膨胀, 除非后续需要细分业务异常
 */
public class OrderCannotCancelException extends BusinessException {
    public OrderCannotCancelException( String message ){
        super( message );
    }
}
