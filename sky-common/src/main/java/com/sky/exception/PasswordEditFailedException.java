package com.sky.exception;

/**
 * 密码修改失败异常
 */
public class PasswordEditFailedException extends BusinessException {

    public PasswordEditFailedException(String msg){
        super(msg);
    }

}
