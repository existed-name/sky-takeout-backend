package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.AddressBookDTO;
import com.sky.entity.AddressBook;
import com.sky.vo.AddressBookVO;
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
     * <b>AddressBook 忽略字段</b>: userId, concatAddress
     * <p>
     *     注: <b>创建时 id = null, 修改时 id 非空</b>, 都映射到 AddressBook
     * </p>
     */
    @Mapping( target = "userId", ignore = true )
    @Mapping( target = "concatAddress", ignore = true )
    AddressBook toEntity( AddressBookDTO dto );

    /**
     * entity 转 VO
     * <p>
     * <b>AddressBookVO 忽略字段</b>: 无
     */
    AddressBookVO toVO( AddressBook entity );

    /**
     * 批量 entity 转 VO, 该接口方法编译后循环调用{@link #toVO(AddressBook)}
     */
    List< AddressBookVO > toVOList( List< AddressBook > entities );

}
