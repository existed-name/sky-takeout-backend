package com.sky.exception.common;

import com.sky.exception.BusinessException;

/**
 * 时间范围无效, 比如起始时间 > 截止时间
 *
 * @CreateTime 2026/9/17 11:25
 */
public class TimeRangeInvalidException extends BusinessException {
    public TimeRangeInvalidException( String message ){
        super( message );
    }
}
