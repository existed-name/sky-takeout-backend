package com.sky.exception.auth;

import com.sky.exception.BusinessException;

/**
 * 登录失败
 */
public class LoginFailedException extends BusinessException {
    public LoginFailedException(String msg){
        super(msg);
    }
}
