package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.category.CategoryDTO;
import com.sky.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 分类对象转换映射, 将 category 分类实体、vo、dto 等对象进行互相转换
 *
 * @CreateTime 2026/9/27 13:35
 */
@Mapper( config = GlobalMappingConfiguration.class )
public interface CategoryConverter {

    /**
     * 将分类数据 dto 转换为实体对象
     *
     * @param categoryDTO 分类数据信息
     * @return 分类实体对象
     */
    // 注意 dto 去掉 id 字段后不能映射 id, 就需要手动忽略该字段
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "status", ignore = true )
    @Mapping( target = "createTime", ignore = true )
    @Mapping( target = "updateTime", ignore = true )
    @Mapping( target = "createUser", ignore = true )
    @Mapping( target = "updateUser", ignore = true )
    Category toEntity( CategoryDTO categoryDTO );
}
