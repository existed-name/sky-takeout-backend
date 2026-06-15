package com.sky.exception;

/**
 * 某条购物车数据和某个用户不匹配(不是该用户的购物车数据)
 *
 * @CreateTime 2026/6/15 15:35
 */
public class ShoppingCartUserNotMatchException extends RuntimeException {
    public ShoppingCartUserNotMatchException( String message ){
        super( message );
    }
}
