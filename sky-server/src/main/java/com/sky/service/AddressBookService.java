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
     * 用户端新增收货地址
     *
     * @param userId 用户 id
     * @param addressBookDTO 地址簿信息
     */
    void saveWithUserId( Long userId, AddressBookDTO addressBookDTO  );

    /**
     * 查询默认地址
     */
    AddressBookVO getDefaultByUserId( Long userId );

    /**
     * 修改地址
     */
    void updateByIdAndUserId( Long userId, AddressBookDTO addressBookDTO );

    /**
     * 根据 id 删除地址
     *
     * @param userId 用于检验该条地址数据是否属于当前用户
     */
    void deleteByIdAndUserId( Long id, Long userId );

    /**
     * 根据 id 查询地址信息
     */
    AddressBookVO getByIdAndUserId( Long id, Long userId );

    /**
     * 设置默认地址
     *
     * @param userId 用于检验该条地址数据是否属于当前用户
     */
    void setDefaultByIdAndUserId( Long id, Long userId );
}
