package com.sky.exception.setmeal;

import com.sky.exception.BusinessException;

/**
 * 套餐启用失败异常
 */
public class SetmealEnableFailedException extends BusinessException {

    public SetmealEnableFailedException(){}

    public SetmealEnableFailedException(String msg){
        super(msg);
    }
}
