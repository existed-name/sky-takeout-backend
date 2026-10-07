package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 餐具数量状态无效
 *
 * @CreateTime 2026/10/6 21:22
 */
public class TablewareStatusInvalidException extends BusinessException {
    public TablewareStatusInvalidException( String message ){
        super( message );
    }
}
