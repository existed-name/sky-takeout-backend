package com.sky.service;

import com.sky.dto.AddressBookDTO;
import com.sky.vo.AddressBookVO;
import com.sky.vo.user.addressBook.AddressBookListItemVO;

import javax.validation.constraints.Positive;
import java.util.List;

public interface AddressBookService {
    /**
     * 用户端查看该用户的所有收货地址
     *
     * @param userId 用户 id
     * @return 地址簿数据项列表, <b>用户不存在时返回空列表</b>
     */
    List< AddressBookListItemVO > listByUserId( Long userId );

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
     * 用户端某个用户修改地址信息
     *
     * @param id 地址簿 id
     * @param userId 用户 id
     * @param addressBookDTO 修改后的地址簿信息
     */
    void updateByIdAndUserId( Long id, Long userId, AddressBookDTO addressBookDTO );

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
