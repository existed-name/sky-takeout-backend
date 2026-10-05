package com.sky.exception.common;

import com.sky.enumeration.Gender;
import com.sky.exception.BusinessException;

/**
 * 性别无效, 用于检验前端传入的性别数字代码{@link Gender}
 *
 * @CreateTime 2026/10/5 19:41
 */
public class GenderInvalidException extends BusinessException {
    public GenderInvalidException( String message ){
        super( message );
    }
}
