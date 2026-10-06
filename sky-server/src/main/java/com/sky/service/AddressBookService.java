package com.sky.service;

import com.sky.dto.user.addressBook.AddressBookDTO;
import com.sky.vo.user.addressBook.AddressBookDefaultVO;
import com.sky.vo.user.addressBook.AddressBookDetailVO;
import com.sky.vo.user.addressBook.AddressBookListItemVO;

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
     * 用户端某个用户查询默认地址
     *
     * @param userId 用户 id
     * @return 默认地址展示对象
     */
    AddressBookDefaultVO getDefaultByUserId( Long userId );

    /**
     * 用户端某个用户修改地址信息
     *
     * @param id 地址簿 id
     * @param userId 用户 id
     * @param addressBookDTO 修改后的地址簿信息
     */
    void updateByIdAndUserId( Long id, Long userId, AddressBookDTO addressBookDTO );

    /**
     * 用户端某个用户删除某个地址簿
     *
     * @param id 地址簿 id, 用于判断数据存在性
     * @param userId 用户 id, 用于判断数据是否属于当前用户
     */
    void deleteByIdAndUserId( Long id, Long userId );

    /**
     * 查询某个地址簿
     *
     * @param id 地址簿 id, 用于判断数据存在性
     * @param userId 用户 id, 用于判断数据是否属于当前用户
     * @return 地址簿详情
     */
    AddressBookDetailVO getByIdAndUserId( Long id, Long userId );

    /**
     * 用户端某个用户设置默认地址
     *
     * @param id 地址簿 id, 用于判断数据存在性
     * @param userId 用户 id, 用于判断数据是否属于当前用户
     */
    void setDefaultByIdAndUserId( Long id, Long userId );
}
