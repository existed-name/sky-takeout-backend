package com.sky.exception.shoppingCart;

import com.sky.exception.BusinessException;

/**
 * 购物车数据无效, 比如同时包含菜品、套餐( 但实际只能选 1 个 )
 *
 * @CreateTime 2026/10/5 11:03
 */
public class ShoppingCartInvalidException extends BusinessException {
    public ShoppingCartInvalidException( String message ){
        super( message );
    }
}
