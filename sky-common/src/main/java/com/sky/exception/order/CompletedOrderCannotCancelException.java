package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 已完成的订单不能取消
 *
 * @CreateTime 2026/6/26 14:31
 */
public class CompletedOrderCannotCancelException extends BusinessException {
    public CompletedOrderCannotCancelException( String message ){
        super( message );
    }
}
