package com.sky.convertor;

import com.sky.dto.AddressBookDTO;
import com.sky.entity.AddressBook;
import com.sky.vo.AddressBookVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 对象转换映射
 * <br>
 * 只有同名同类型的字段才会映射, 字段不一样不会不报错，自动跳过
 *
 * @CreateTime 2026/6/16
 */
@Mapper( componentModel = "spring" ) // 用 Spring 集成，可以生成 Spring Bean
public interface AddressBookConvertor {
    /**
     * DTO 转 entity
     */
    AddressBook toEntity( AddressBookDTO dto );

    /**
     * entity 转 VO
     */
    AddressBookVO toVO( AddressBook entity );

    /**
     * 批量 entity 转 VO
     */
    List< AddressBookVO > toVOList( List< AddressBook > entities );

}
