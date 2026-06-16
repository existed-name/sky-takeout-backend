package com.sky.service;

import com.sky.dto.AddressBookDTO;
import com.sky.vo.AddressBookVO;

import java.util.List;

public interface AddressBookService {
    /**
     * 查看所有收货地址
     */
    List< AddressBookVO > listByUserId( Long userId );

    /**
     * 新增收货地址
     */
    void saveWithUserId( AddressBookDTO addressBookDTO, Long userId );
}
