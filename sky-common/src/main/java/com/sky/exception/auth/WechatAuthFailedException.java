package com.sky.exception.auth;

/**
 * 微信小程序，用户登录失败
 *
 * @CreateTime 2026/6/4 20:06
 */
public class WeChatAuthFailedException extends LoginFailedException {
    public WeChatAuthFailedException( String message ){
        super( message );
    }
}
