package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     */
    @Select( """
            select count(id) from dish where category_id = #{categoryId}
            """)
    Integer countByCategoryId( @Param( "categoryId" ) Long categoryId );

}