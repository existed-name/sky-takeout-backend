package com.sky.service.impl;

import com.sky.constant.AddressBookConstant;
import com.sky.constant.MessageConstant;
import com.sky.convertor.AddressBookConvertor;
import com.sky.dto.AddressBookDTO;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.vo.AddressBookVO;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final AddressBookMapper addressBookMapper;
    private final AddressBookConvertor addressBookConvertor;


    // ==================== 构造器 ====================
    public AddressBookServiceImpl( AddressBookMapper addressBookMapper, AddressBookConvertor addressBookConvertor ){
        this.addressBookMapper = addressBookMapper;
        this.addressBookConvertor = addressBookConvertor;
    }



    // ==================== 公有方法 ====================
    @Override
    public List< AddressBookVO > listByUserId( Long userId ){
        List< AddressBookVO > addressBookVOList = addressBookMapper.selectVOByUserId( userId );
        return addressBookVOList;
    }

    @Override
    public void saveWithUserId( Long userId, AddressBookDTO addressBookDTO ){
        AddressBook entity = addressBookConvertor.toEntity( addressBookDTO );
        // DTO、VO 没有 userId 入库前需要手动给 entity 设置 userId
        entity.setUserId( userId );
        addressBookMapper.insert( entity );
    }

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

    @Override
    public void updateByUserId( Long userId, AddressBookDTO addressBookDTO ){
        // 猛然想起: userId 是 BaseContext，dto 不带 userId 但是有 id, 修改之前应该先看这个 id 是不是当前用户的
        Long id = addressBookDTO.getId();
        // DTO 对象已经参数检验过 id 非空
        // 这里用 id、userId 定位，找得到说明是这个用户的数据
        if ( addressBookMapper.countByIdAndUserId( id, userId ) != 1L ){
            throw new AddressBookUserNotMatchException( MessageConstant.ADDRESS_BOOK_USER_NOT_MATCH );
        }

        AddressBook entity = addressBookConvertor.toEntity( addressBookDTO );
        // entity.setUserId( userId ); 不需要设置用户 id，因为已经确认该数据属于该用户
        addressBookMapper.updateById( entity );
    }

    @Override
    public void deleteById( Long id, Long userId ){
        if ( addressBookMapper.countByIdAndUserId( id, userId ) != 1L ){
            throw new AddressBookUserNotMatchException( MessageConstant.ADDRESS_BOOK_USER_NOT_MATCH );
        }

        addressBookMapper.deleteById( id );
    }

    @Override
    public AddressBookVO getById( Long id ){
        AddressBookVO addressBookVO = addressBookMapper.selectVOById( id );
        return addressBookVO;
    }


    // ==================== 私有辅助方法 ====================

}
