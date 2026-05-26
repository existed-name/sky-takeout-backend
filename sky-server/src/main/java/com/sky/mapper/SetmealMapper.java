package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     */
    @Select( """
            select count(id) from setmeal where category_id = #{categoryId}
            """)
    long countByCategoryId( @Param( "categoryId" ) Long categoryId );

    /**
     * 批量查询菜品关联的套餐
     * <br>
     * 用于删除菜品前检查是否有关联的套餐
     */
    long countByDishIds( @Param( "dishIds" ) List< Long> dishIds );
}