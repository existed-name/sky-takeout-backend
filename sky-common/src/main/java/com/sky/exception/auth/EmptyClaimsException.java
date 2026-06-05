package com.sky.exception.auth;

/**
 * 解析 JwtToken 携带的 Claims 为空
 *
 * @CreateTime 2026/6/5 14:23
 */
public class EmptyClaimsException extends LoginFailedException {
    public EmptyClaimsException( String message ){
        super( message );
    }
}
