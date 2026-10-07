package com.sky.exception.shoppingCart;

import com.sky.exception.BusinessException;

/**
 * 用户购物车数据为空, 无法下单
 *
 * @CreateTime 2026/6/19 21:40
 */
public class ShoppingCartEmptyException extends BusinessException {
    public ShoppingCartEmptyException( String message ){
        super( message );
    }
}
