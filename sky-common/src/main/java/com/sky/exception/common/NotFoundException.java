package com.sky.exception.common;

import com.sky.exception.BusinessException;

/**
 * 资源未找到异常
 * 用于查询时目标资源不存在的场景
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String msg) {
        super(msg);
    }
}
