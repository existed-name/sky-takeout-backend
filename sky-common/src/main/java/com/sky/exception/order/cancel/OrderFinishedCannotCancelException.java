package com.sky.exception.order.cancel;

import com.sky.exception.BusinessException;

/**
 * 已完成的订单不能取消
 *
 * @CreateTime 2026/6/26 14:31
 * @deprecated 统一使用{@link OrderCancelFailedException}, 避免异常类膨胀, 除非后续需要细分业务异常
 */
public class OrderFinishedCannotCancelException extends BusinessException {
    public OrderFinishedCannotCancelException( String message ){
        super( message );
    }
}
