package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    @Insert( """
            INSERT INTO dish
            ( name, category_id, price, image, description, status, create_time, update_time, create_user, update_user )
            values ( #{dish.name}, #{dish.categoryId}, #{dish.price}, #{dish.image}, #{dish.description}, #{dish.status}, #{dish.createTime}, #{dish.updateTime}, #{dish.createUser}, #{dish.updateUser} )
            """ )
    int insert( @Param( "dish" ) Dish dish );

    /**
     * 条件分页查询 - PageHelper 执行分页操作
     */
    List< Dish> selectByQuery( DishPageQueryDTO dishPageQueryDTO );
}