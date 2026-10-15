package com.sky.exception.order.pay;

import com.sky.exception.BusinessException;

/**
 * 订单的支付方式无效,
 * <p>
 * TODO: 当前是在创建订单时定死了支付方式, 后续可以修改为完成支付时前端把用户选择的支付方式传入后端
 * </p>
 *
 * @CreateTime 2026/10/6 21:05
 */
public class PaymentMethodInvalidException extends BusinessException {
    public PaymentMethodInvalidException( String message ){
        super( message );
    }
}
