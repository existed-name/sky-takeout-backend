package com.sky.exception;

/**
 * 上传文件的类型不支持
 *
 * @CreateTime 2026/5/23 11:16
 */
public class FileTypeNotSupportedException extends BusinessException {
    public FileTypeNotSupportedException( String message ){
        super( message );
    }
}
