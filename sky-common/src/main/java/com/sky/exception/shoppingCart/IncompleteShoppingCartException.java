package com.sky.exception.shoppingCart;

import com.sky.exception.BusinessException;

/**
 * 购物车数据不完整，比如 DTO 缺少 dishId、setmealId
 *
 * @CreateTime 2026/6/20 14:20
 */
public class IncompleteShoppingCartException extends BusinessException {
    public IncompleteShoppingCartException( String message ){
        super( message );
    }
}
