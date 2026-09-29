package com.sky.exception.file;

import com.sky.exception.BusinessException;

/**
 * 上传文件为空( null、字节流长度为 0 )
 */
public class FileEmptyException extends BusinessException {
    public FileEmptyException( String message ){
        super( message );
    }
}
