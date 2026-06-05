package com.sky.exception.auth;

/**
 * 登录状态过期
 *
 * @CreateTime 2026/6/5 21:53
 */
public class AuthExpiredException extends UserNotLoginException {
    public AuthExpiredException( String message ){
        super( message );
    }
}
