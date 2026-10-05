package com.sky.service.impl;

import com.sky.constant.AddressBookConstant;
import com.sky.constant.MessageConstant;
import com.sky.convertor.AddressBookConverter;
import com.sky.dto.AddressBookDTO;
import com.sky.enumeration.Gender;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.vo.AddressBookVO;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final AddressBookMapper addressBookMapper;
    private final AddressBookConverter addressBookConverter;


    // ==================== 构造器 ====================
    public AddressBookServiceImpl( AddressBookMapper addressBookMapper, AddressBookConverter addressBookConverter ){
        this.addressBookMapper = addressBookMapper;
        this.addressBookConverter = addressBookConverter;
    }



    // ==================== 公有方法 ====================
    @Transactional( readOnly = true )
    @Override
    public List< AddressBookVO > listByUserId( Long userId ){
        List< AddressBookVO > addressBookVOList = addressBookMapper.selectVOListByUserId( userId );
        return addressBookVOList;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithUserId( Long userId, AddressBookDTO addressBookDTO ){
        /*
            实现思路
            业务检验: 大部分都在 dto 字段检验过了, 这里自己补了性别数字代码检验
            dto 转实体对象
            设置实体对象缺失的字段:
              - userId
              - 没有审计字段
              - 是否为默认地址, 另外我没有考虑把首次创建的地址设为默认地址, 交给用户自己设置
         */
        Gender.throwIfInvalid( Integer.valueOf( addressBookDTO.getSex() ) );

        AddressBook entity = addressBookConverter.toEntity( addressBookDTO );
        entity.setUserId( userId );
        if ( entity.getIsDefault() == null ){
            entity.setIsDefault( AddressBookConstant.NOT_DEFAULT );
        }
        addressBookMapper.insert( entity );
    }

    @Transactional( readOnly = true )
    @Override
    public AddressBookVO getDefaultByUserId( Long userId ){
        AddressBookVO addressBookVO = addressBookMapper.selectVOByUserIdAndIsDefault(
                userId, AddressBookConstant.IS_DEFAULT
        );
        // 看了教程过后才发现没有考虑用户没设置默认地址的情况
        if ( addressBookVO == null ){
            throw new NotFoundException( MessageConstant.DEFAULT_ADDRESS_BOOK_NOT_FOUND );
        }
        return addressBookVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateByIdAndUserId( Long userId, AddressBookDTO addressBookDTO ){
        AddressBook entity = addressBookConverter.toEntity( addressBookDTO );
        entity.setUserId( userId );
        // 猛然想起: userId 是 BaseContext，dto 不带 userId 但是有 id, 修改之前应该先看这个 id 是不是当前用户的
        // 这里用 id、userId 定位，找得到说明是这个用户的数据
        int row = addressBookMapper.updateByIdAndUserId( entity );
        validateUpdatedRowById( entity.getId(), row );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteByIdAndUserId( Long id, Long userId ){
        // 直接用 id + userId 定位数据，根据修改行数判断是数据不存在、数据不匹配(可见底部 setDefaultById 方法)
        int row = addressBookMapper.deleteByIdAndUserId( id, userId );
        validateUpdatedRowById( id, row );
    }

    @Transactional( readOnly = true )
    @Override
    public AddressBookVO getByIdAndUserId( Long id, Long userId ){
        AddressBookVO addressBookVO = addressBookMapper.selectVOByIdAndUserId( id, userId );
        if ( addressBookVO == null ){
            throwIfNotFoundOrNotMatch( id );
        }
        return addressBookVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void setDefaultByIdAndUserId( Long id, Long userId ){
        /*
            * 由于只能有 1 个默认地址，所以需要先把之前的默认地址设为非默认，再设置现在的地址为默认
            * 撤销之前的默认地址: 直接修改( user_id + is_default = 1 定位 )
            * 使用 id、userId 2 个参数精准定位 + Mapper 修改行数判断异常，确保数据匹配当前用户
            * 由于重置默认状态有 WHERE is_default = 1 可能用户不存在默认地址，就会干扰判断
            * 所以先设置默认地址, 此时只有 WHERE id = #{id}, user_id = #{userId}
            * 再把其他地址设为非默认
         */
        int updatedRow = addressBookMapper.updateIsDefaultByIdAndUserId( id, userId, AddressBookConstant.IS_DEFAULT );
        validateUpdatedRowById( id, updatedRow );
        addressBookMapper.resetDefaultByIdAndUserId( id, userId );
    }



    // ==================== 私有辅助方法 ====================

    /**
     * 根据 Mapper 修改行数判断异常: id 对应的数据不存在、或者跟当前用户不匹配
     *
     * @param id 数据库表 id
     * @param updatedRow 修改行数
     */
    private void validateUpdatedRowById( Long id, int updatedRow ){
        if ( updatedRow == 0 ){
            throwIfNotFoundOrNotMatch( id );
        }
    }

    /**
     * 判断异常: id 对应的数据不存在、或者跟当前用户不匹配
     *
     * @param id 数据库表 id
     */
    private void throwIfNotFoundOrNotMatch( Long id ){
        // 理论上 id, userId 定位到同一条数据，然而修改行数为 0
        // 说明 id 跟 userId 对不上 => id 错误(无效)或者 userId 错误(该数据不属于当前用户)
        if ( addressBookMapper.countById( id ) == 0 ){
            throw new NotFoundException( MessageConstant.ADDRESS_BOOK_NOT_FOUND );
        }
        throw new AddressBookUserNotMatchException( MessageConstant.ADDRESS_BOOK_USER_NOT_MATCH );
    }

}
