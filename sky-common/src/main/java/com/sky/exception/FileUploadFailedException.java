package com.sky.exception;

/**
 * 文件上传失败
 */
public class FileUploadFailedException extends BaseException {
    public FileUploadFailedException( String message ){
        super( message );
    }
}
