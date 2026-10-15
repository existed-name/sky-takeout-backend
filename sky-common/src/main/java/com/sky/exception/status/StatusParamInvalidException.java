package com.sky.exception.status;

import com.sky.exception.BusinessException;

/**
 * 状态参数不合法，用于修改状态时检验传入的 status 参数
 *
 * @CreateTime 2026/5/27 19:19
 * @deprecated 请使用具体的状态异常, 避免语义模糊、过于宽泛, 比如{@link com.sky.exception.status}中的商品状态、店铺状态、启用状态异常
 */
public class StatusParamInvalidException extends BusinessException {
    public StatusParamInvalidException( String message ){
        super( message );
    }
}
