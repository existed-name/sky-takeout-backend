package com.sky.exception.addressBook;

import com.sky.exception.BusinessException;

/**
 * 某条地址簿数据不属于当前用户
 *
 * @CreateTime 2026/6/16 11:13
 */
public class AddressBookUserNotMatchException extends BusinessException {
    public AddressBookUserNotMatchException( String message ){
        super( message );
    }
}
