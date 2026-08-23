package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 订单状态无效, 有效状态为{@link com.sky.constant.OrderConstant.Status#VALID_STATUS}
 *
 * @CreateTime 2026/6/25 6:42
 */
public class OrderStatusInvalidException extends BusinessException {
    public OrderStatusInvalidException( String message ){
        super( message );
    }
}
