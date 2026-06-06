package com.sky.exception.auth;

/**
 * 拦截器解析 JwtToken 为空
 *
 * @CreateTime 2026/6/5 14:19
 */
public class EmptyJwtTokenException extends NotLoginException {
    public EmptyJwtTokenException( String message ){
        super( message );
    }
}
