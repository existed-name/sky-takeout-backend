package com.sky.exception.baiduMap;

import com.sky.exception.BusinessException;

/**
 * 进行地理编码的地址不能为空
 *
 * @CreateTime 2026/9/6 6:40
 */
public class AddressBlankException extends BusinessException {
    public AddressBlankException( String message ){
        super( message );
    }
}
