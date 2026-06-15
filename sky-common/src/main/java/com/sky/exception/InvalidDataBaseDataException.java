package com.sky.exception;

/**
 * 数据库数据无效，查出来的数据不符合业务要求、不应该有这种数据
 *
 * @CreateTime 2026/6/15 14:49
 */
public class InvalidDataBaseDataException extends BusinessException {
    public InvalidDataBaseDataException( String message ){
        super( message );
    }
}
