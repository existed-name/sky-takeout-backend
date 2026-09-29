package com.sky.exception.file;

import com.sky.exception.BusinessException;

/**
 * 文件上传失败
 */
public class FileUploadFailedException extends BusinessException {
    public FileUploadFailedException( String message ){
        super( message );
    }
}
