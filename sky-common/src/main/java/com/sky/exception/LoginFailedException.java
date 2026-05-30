package com.sky.exception;

/**
 * 登录失败
 */
public class LoginFailedException extends BusinessException {
    public LoginFailedException(String msg){
        super(msg);
    }
}
