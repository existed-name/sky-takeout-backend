package com.sky.exception;

/**
 * 文件上传失败
 */
public class FileUploadFailedException extends RuntimeException {
    public FileUploadFailedException( String message ){
        super( message );
    }
}
