package com.sky.exception.order.submit;

import com.sky.exception.BusinessException;

/**
 * 用户下单时, 指定的餐具数量状态无效
 *
 * @CreateTime 2026/10/6 21:22
 */
public class TablewareStatusInvalidException extends BusinessException {
    public TablewareStatusInvalidException( String message ){
        super( message );
    }
}
