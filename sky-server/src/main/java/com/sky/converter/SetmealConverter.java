package com.sky.converter;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.admin.setmeal.SetmealDTO;
import com.sky.entity.Setmeal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 套餐相关对象转换映射
 *
 * @CreateTime 2026/10/1 21:03
 */
@Mapper( config = GlobalMappingConfiguration.class )
public interface SetmealConverter {

    /**
     * 套餐 dto 转实体对象
     * <p>
     *     忽略字段: id、审计字段
     * </p>
     *
     * @param setmealDTO 套餐 dto
     * @return 实体对象
     */
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "createTime", ignore = true )
    @Mapping( target = "updateTime", ignore = true )
    @Mapping( target = "createUser", ignore = true )
    @Mapping( target = "updateUser", ignore = true )
    Setmeal toEntity( SetmealDTO setmealDTO );
}
