package com.sky.exception;

/**
 * 文件上传失败
 */
public class FileUploadFailedException extends BusinessException {
    public FileUploadFailedException( String message ){
        super( message );
    }
}
