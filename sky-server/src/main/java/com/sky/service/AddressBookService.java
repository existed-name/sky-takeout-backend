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
    void saveWithUserId( Long userId, AddressBookDTO addressBookDTO  );

    /**
     * 查询默认地址
     */
    AddressBookVO getDefaultByUserId( Long userId );

    /**
     * 修改地址
     */
    void updateByUserId( Long userId, AddressBookDTO addressBookDTO );

    /**
     * 根据 id 删除地址
     *
     * @param userId 用于检验该条地址数据是否属于当前用户
     */
    void deleteById( Long id, Long userId );

    /**
     * 根据 id 查询地址信息
     */
    AddressBookVO getById( Long id );

    /**
     * 设置默认地址
     *
     * @param userId 用于检验该条地址数据是否属于当前用户
     */
    void setDefaultById( Long id, Long userId );
}
