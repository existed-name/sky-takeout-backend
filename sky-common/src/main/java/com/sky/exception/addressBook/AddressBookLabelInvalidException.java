package com.sky.exception.addressBook;

import com.sky.exception.BusinessException;

/**
 * 地址簿标签无效, {@link com.sky.enumeration.AddressBookLabel}
 *
 * @CreateTime 2026/10/5 20:35
 */
public class AddressBookLabelInvalidException extends BusinessException {
    public AddressBookLabelInvalidException( String message ){
        super( message );
    }
}
