package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 菜品相关对象的转换映射类
 *
 * @CreateTime 2026/9/28 15:57
 */
@Mapper( config = GlobalMappingConfiguration.class )
public interface DishConverter {

    /**
     * 菜品 dto 转实体对象
     *
     * @param dto 前端传入的菜品 dto 信息
     * @return 菜品实体对象
     */
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "createTime", ignore = true )
    @Mapping( target = "updateTime", ignore = true )
    @Mapping( target = "createUser", ignore = true )
    @Mapping( target = "updateUser", ignore = true )
    Dish toEntity( DishDTO dto );
}
