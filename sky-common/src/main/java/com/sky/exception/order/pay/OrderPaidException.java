package com.sky.exception.order.pay;

import com.sky.exception.BusinessException;

/**
 * 该订单已支付, 请勿重复支付
 *
 * @CreateTime 2026/10/7 9:16
 */
public class OrderPaidException extends BusinessException {
    public OrderPaidException( String message ){
        super( message );
    }
}
