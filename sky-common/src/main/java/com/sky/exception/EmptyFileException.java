package com.sky.exception;

/**
 * 上传文件为空
 */
public class EmptyFileException extends BusinessException {
    public EmptyFileException( String message ){
        super( message );
    }
}
