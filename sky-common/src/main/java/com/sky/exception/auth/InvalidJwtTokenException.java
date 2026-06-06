package com.sky.exception.auth;

/**
 * 无效 JwtToken
 *
 * @CreateTime 2026/6/5 21:51
 */
public class InvalidJwtTokenException extends NotLoginException {
    public InvalidJwtTokenException( String message ){
        super( message );
    }
}
