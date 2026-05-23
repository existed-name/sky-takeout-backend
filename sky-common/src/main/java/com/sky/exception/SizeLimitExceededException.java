package com.sky.exception;

/**
 * 文件大小超出限制
 * @CreateTime 2026/5/23 11:06
 */
public class SizeLimitExceededException extends BaseException {
    public SizeLimitExceededException( String message ){
        super( message );
    }
}
