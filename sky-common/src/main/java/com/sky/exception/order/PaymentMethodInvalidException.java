package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 订单的支付方式无效
 *
 * @CreateTime 2026/10/6 21:05
 */
public class PaymentMethodInvalidException extends BusinessException {
    public PaymentMethodInvalidException( String message ){
        super( message );
    }
}
