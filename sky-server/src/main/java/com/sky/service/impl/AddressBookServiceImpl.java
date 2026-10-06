package com.sky.service.impl;

import com.sky.constant.AddressBookConstant;
import com.sky.constant.MessageConstant;
import com.sky.convertor.AddressBookConverter;
import com.sky.dto.AddressBookDTO;
import com.sky.enumeration.AddressBookLabel;
import com.sky.enumeration.Gender;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.utils.NotFoundUtil;
import com.sky.vo.AddressBookVO;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import com.sky.vo.user.addressBook.AddressBookDetailVO;
import com.sky.vo.user.addressBook.AddressBookListItemVO;
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
    public List< AddressBookListItemVO > listByUserId( Long userId ){
        List< AddressBookListItemVO > addressBookListItemVOs = addressBookMapper.selectItemVOListByUserId( userId );
        return addressBookListItemVOs;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithUserId( Long userId, AddressBookDTO addressBookDTO ){
        /*
            实现思路
            业务检验:
              - 大部分都在 dto 字段检验过了
              - 这里自己补了性别、地址标签检验
            dto 转实体对象
            设置实体对象缺失的字段:
              - userId
              - 没有审计字段
              - 是否为默认地址, 另外我没有考虑把首次创建的地址设为默认地址, 交给用户自己设置
         */
        Gender.throwIfInvalid( Integer.valueOf( addressBookDTO.getSex() ) );
        String label = addressBookDTO.getLabel();
        if ( label != null ){
            AddressBookLabel.throwIfInvalid( Integer.valueOf( label ) );
        }

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
    public void updateByIdAndUserId( Long id, Long userId, AddressBookDTO addressBookDTO ){
        /*
            思路类似新增地址簿
            对部分字段进行业务检验: 性别、标签
            然后写入数据库时用 id、userId 定位, 判断数据存在性、是否属于当前用户
         */
        Gender.throwIfInvalid( Integer.valueOf( addressBookDTO.getSex() ) );
        String label = addressBookDTO.getLabel();
        if ( label != null ){
            AddressBookLabel.throwIfInvalid( Integer.valueOf( label ) );
        }

        AddressBook entity = addressBookConverter.toEntity( addressBookDTO );
        entity.setId( id );
        entity.setUserId( userId );

        int row = addressBookMapper.updateByIdAndUserId( entity );
        validateUpdatedRowById( id, row );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteByIdAndUserId( Long id, Long userId ){
        // 直接用 id + userId 定位数据，根据修改行数判断数据不存在、数据不匹配
        int row = addressBookMapper.deleteByIdAndUserId( id, userId );
        validateUpdatedRowById( id, row );
    }

    @Transactional( readOnly = true )
    @Override
    public AddressBookDetailVO getByIdAndUserId( Long id, Long userId ){
        AddressBookDetailVO addressBookDetailVO = addressBookMapper.selectDetailVOByIdAndUserId( id, userId );
        if ( addressBookDetailVO == null ){
            throwIfNotFoundOrNotMatch( id );
        }
        return addressBookDetailVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void setDefaultByIdAndUserId( Long id, Long userId ){
        /*
            * 由于只能有 1 个默认地址，所以需要先把之前的默认地址设为非默认，再设置现在的地址为默认
            * 撤销之前的默认地址: user_id + is_default = 1 定位
            * 再使用 id、userId 把目标地址修改为默认地址
            * 然后根据修改行数判断数据不存在、不匹配当前用户
            * 然而 2 次修改都会返回修改行数
              - 如果先撤销, 可能因为之前并没有默认地址, 导致拿到的修改行数 = 0, 进而干扰判断
            * 所以先设置默认地址, 用 id、userId 定位并检验修改行数
            * 再把其他地址设为非默认地址
              - 优化点在加上 where is_default = 1 只重置旧的默认地址
         */
        int updatedRow = addressBookMapper.updateIsDefaultByIdAndUserId( id, userId, AddressBookConstant.IS_DEFAULT );
        validateUpdatedRowById( id, updatedRow );
        addressBookMapper.resetDefaultByIdAndUserId( id, userId, AddressBookConstant.IS_DEFAULT );
    }



    // ==================== 私有辅助方法 ====================

    /**
     * <b>mapper 使用 id、userId 定位数据并修改后</b>, 根据修改行数判断异常: id 对应的数据不存在、或者跟当前用户不匹配
     * <p>
     *     当修改行数 = 0 时, 调用{@link #throwIfNotFoundOrNotMatch}抛出异常
     * </p>
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
     * 当 id 对应的数据不存在、或者跟当前用户不匹配时, 抛出业务异常
     * <p>
     *     使用前提: <b>mapper 已经使用了 id、userId 共同确定修改行数 = 0 或者查询对象 = null</b>,
     * </p>
     * <b>该方法一定会抛出异常</b>, 在使用前提下, 只要数据不存在, 就可以断定数据不匹配
     *
     * @param id 数据库表 id, 用于判断该数据是否存在
     * @throws NotFoundException 当数据不存在时抛出
     * @throws AddressBookUserNotMatchException 当数据存在, 也就是数据不属于该用户时抛出,
     */
    private void throwIfNotFoundOrNotMatch( Long id ){
        // 理论上 id, userId 定位到同一条数据，然而修改行数为 0
        // 说明 id 跟 userId 对不上 => id 错误(无效)或者 userId 错误(该数据不属于当前用户)
        NotFoundUtil.throwIfCountZero( addressBookMapper.countById( id ), MessageConstant.ADDRESS_BOOK_NOT_FOUND );
        throw new AddressBookUserNotMatchException( MessageConstant.ADDRESS_BOOK_USER_NOT_MATCH );
    }

}
