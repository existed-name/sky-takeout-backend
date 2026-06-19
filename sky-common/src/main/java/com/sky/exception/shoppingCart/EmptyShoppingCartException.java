package com.sky.exception.shoppingCart;

import com.sky.exception.BusinessException;

/**
 * 用户购物车数据为空
 *
 * @CreateTime 2026/6/19 21:40
 */
public class EmptyShoppingCartException extends BusinessException {
    public EmptyShoppingCartException( String message ){
        super( message );
    }
}
