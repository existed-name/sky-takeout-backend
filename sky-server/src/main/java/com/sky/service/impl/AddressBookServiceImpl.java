package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.convertor.AddressBookConvertor;
import com.sky.dto.AddressBookDTO;
import com.sky.vo.AddressBookVO;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    private final AddressBookMapper addressBookMapper;
    private final AddressBookConvertor addressBookConvertor;

    public AddressBookServiceImpl( AddressBookMapper addressBookMapper, AddressBookConvertor addressBookConvertor ){
        this.addressBookMapper = addressBookMapper;
        this.addressBookConvertor = addressBookConvertor;
    }
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    @Override
    public List< AddressBookVO > listByUserId( Long userId ){
        List< AddressBookVO > addressBookVOList = addressBookMapper.selectVOByUserId( userId );
        return addressBookVOList;
    }

    @Override
    public void saveWithUserId( AddressBookDTO addressBookDTO, Long userId ){
        AddressBook entity = addressBookConvertor.toEntity( addressBookDTO );
        // DTO、VO 没有 userId 入库前需要手动给 entity 设置 userId
        entity.setUserId( userId );
        addressBookMapper.insert( entity );
    }


    // ==================== 私有辅助方法 ====================

}
