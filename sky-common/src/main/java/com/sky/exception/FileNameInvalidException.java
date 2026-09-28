package com.sky.exception;

/**
 * 无效文件名: 文件名为空、缺少文件后缀
 *
 * @CreateTime 2026/5/23 9:31
 */
public class FileNameInvalidException extends BusinessException {
    public FileNameInvalidException( String message ){
        super( message );
    }
}
