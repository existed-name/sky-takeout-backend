package com.sky.exception.account;

import com.sky.exception.BusinessException;

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
