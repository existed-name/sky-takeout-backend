package com.sky.exception.common;

import com.sky.exception.BusinessException;

/**
 * 数据库数据无效，查出来的数据不符合业务要求、不应该有这种数据
 *
 * @CreateTime 2026/6/15 14:49
 * @deprecated 这种情况属于系统错误, 暂时抛出 JDK 原生异常, 后续需要再包装业务异常
 */
public class DataBaseDataInvalidException extends BusinessException {
    public DataBaseDataInvalidException( String message ){
        super( message );
    }
}
