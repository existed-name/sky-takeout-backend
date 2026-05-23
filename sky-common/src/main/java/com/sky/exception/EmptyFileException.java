package com.sky.exception;

/**
 * 上传文件为空
 */
public class EmptyFileException extends BaseException {
    public EmptyFileException( String message ){
        super( message );
    }
}
