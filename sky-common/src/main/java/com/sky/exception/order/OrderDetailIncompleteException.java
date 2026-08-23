package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * orderDetail 订单明细数据不完整，比如缺少 dishId、setmealId
 *
 * @CreateTime 2026/6/27 7:01
 */
public class OrderDetailIncompleteException extends BusinessException {
    public OrderDetailIncompleteException( String message ){
        super( message );
    }
}
