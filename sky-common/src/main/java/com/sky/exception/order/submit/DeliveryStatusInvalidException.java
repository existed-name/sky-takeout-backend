package com.sky.exception.order.submit;

import com.sky.exception.BusinessException;

/**
 * 用户下单时，指定的配送状态无效, {@link com.sky.enumeration.DeliveryStatus}
 *
 * @CreateTime 2026/10/7 8:45
 */
public class DeliveryStatusInvalidException extends BusinessException {
    public DeliveryStatusInvalidException( String message ){
        super( message );
    }
}
