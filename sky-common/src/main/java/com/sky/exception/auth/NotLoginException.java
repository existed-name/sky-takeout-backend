package com.sky.exception.auth;

import com.sky.exception.BusinessException;

public class NotLoginException extends BusinessException {

    public NotLoginException() {
    }

    public NotLoginException( String msg) {
        super(msg);
    }

}
