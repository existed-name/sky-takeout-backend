package com.sky.exception.auth;

/**
 * 微信小程序，用户登录失败
 *
 * @CreateTime 2026/6/4 20:06
 */
public class WechatAuthFailedException extends LoginFailedException {
    public WechatAuthFailedException( String message ){
        super( message );
    }
}
