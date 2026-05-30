package com.sky.exception;

public class DeletionNotAllowedException extends BusinessException {

    public DeletionNotAllowedException(String msg) {
        super(msg);
    }

}
