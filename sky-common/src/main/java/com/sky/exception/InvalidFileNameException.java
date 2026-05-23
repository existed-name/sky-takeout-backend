package com.sky.exception;

/**
 * 无效文件名
 *
 * @CreateTime 2026/5/23 9:31
 */
public class InvalidFileNameException extends BaseException {
    public InvalidFileNameException( String message ){
        super( message );
    }
}
