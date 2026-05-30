package com.sky.exception;

/**
 * 账号被锁定异常
 */
public class AccountLockedException extends BusinessException {

    public AccountLockedException() {
    }

    public AccountLockedException(String msg) {
        super(msg);
    }

}
