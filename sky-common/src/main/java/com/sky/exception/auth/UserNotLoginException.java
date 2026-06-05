package com.sky.exception.auth;

import com.sky.exception.BusinessException;

public class UserNotLoginException extends BusinessException {

    public UserNotLoginException() {
    }

    public UserNotLoginException(String msg) {
        super(msg);
    }

}
