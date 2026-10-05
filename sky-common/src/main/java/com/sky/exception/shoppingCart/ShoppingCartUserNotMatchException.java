package com.sky.exception.shoppingCart;

import com.sky.exception.BusinessException;

/**
 * 某条购物车数据和某个用户不匹配(不是该用户的购物车数据)
 *
 * @CreateTime 2026/6/15 15:35
 */
public class ShoppingCartUserNotMatchException extends BusinessException {
    public ShoppingCartUserNotMatchException( String message ){
        super( message );
    }
}