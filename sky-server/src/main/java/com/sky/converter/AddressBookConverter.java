package com.sky.converter;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.user.addressBook.AddressBookDTO;
import com.sky.entity.AddressBook;
import com.sky.vo.user.addressBook.AddressBookVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 对象转换映射
 * <br>
 * 只有同名同类型的字段才会映射, 字段不一样不会不报错，自动跳过
 *
 * @CreateTime 2026/6/16
 */
@Mapper( config = GlobalMappingConfiguration.class )
public interface AddressBookConverter {
    /**
     * DTO 转 entity
     * <p>
     *      AddressBook 忽略字段: id, userId, concatAddress,
     *      <b>由调用方手动设置</b>
     * </p>
     */
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "userId", ignore = true )
    @Mapping( target = "concatAddress", ignore = true )
    AddressBook toEntity( AddressBookDTO dto );

    /**
     * entity 转 VO
     * <p>
     * <b>AddressBookVO 忽略字段</b>: 无
     * </p>
     */
    AddressBookVO toVO( AddressBook entity );

    /**
     * 批量 entity 转 VO, 该接口方法编译后循环调用{@link #toVO(AddressBook)}
     */
    List< AddressBookVO > toVOList( List< AddressBook > entities );

}
