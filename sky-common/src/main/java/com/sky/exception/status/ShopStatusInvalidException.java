package com.sky.exception.status;

import com.sky.exception.BusinessException;

/**
 * 店铺营业状态无效
 *
 * @CreateTime 2026/10/3 9:15
 */
public class ShopStatusInvalidException extends BusinessException {
    public ShopStatusInvalidException( String message ){
        super( message );
    }
}
