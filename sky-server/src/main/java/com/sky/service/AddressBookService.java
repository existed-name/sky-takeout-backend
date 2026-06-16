package com.sky.service;

import com.sky.vo.AddressBookVO;

import java.util.List;

public interface AddressBookService {
    /**
     * 查看所有收货地址
     */
    List< AddressBookVO > listByUserId();
}
