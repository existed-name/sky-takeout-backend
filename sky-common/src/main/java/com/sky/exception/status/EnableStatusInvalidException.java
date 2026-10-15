package com.sky.exception.status;

import com.sky.exception.BusinessException;

/**
 * 启用状态无效
 *
 * @CreateTime 2026/10/15 11:08
 */
public class EnableStatusInvalidException extends BusinessException {
    public EnableStatusInvalidException( String message ){
        super( message );
    }
}
