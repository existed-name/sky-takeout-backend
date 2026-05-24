package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 新增菜品
     */
    @AutoFill( OperationType.INSERT )
    @Insert( """
            INSERT INTO dish
            ( name, category_id, price, image, description, status, create_time, update_time, create_user, update_user )
            values ( #{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser} )
            """ )
    void insert( Dish dish );
}