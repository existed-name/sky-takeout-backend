package com.sky.exception;

/**
 * 密码错误异常
 */
public class PasswordErrorException extends BusinessException {

    public PasswordErrorException() {
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }

}
