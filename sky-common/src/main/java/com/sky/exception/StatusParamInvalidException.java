package com.sky.exception;

/**
 * 状态参数不合法，用于修改状态时检验传入的 status 参数
 *
 * @CreateTime 2026/5/27 19:19
 */
public class StatusParamInvalidException extends BusinessException {
    public StatusParamInvalidException( String message ){
        super( message );
    }
}
