package com.sky.exception;

/**
 * 资源未找到异常
 * 用于查询时目标资源不存在的场景
 */
public class NotFoundException extends BaseException {

    public NotFoundException(String msg) {
        super(msg);
    }
}
