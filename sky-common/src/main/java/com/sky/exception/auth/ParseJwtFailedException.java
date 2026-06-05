package com.sky.exception.auth;

/**
 * 解析 JwtToken 失败
 *
 * @CreateTime 2026/6/5 21:53
 */
public class ParseJwtFailedException extends UserNotLoginException {
    public ParseJwtFailedException( String message ){
        super( message );
    }
}
