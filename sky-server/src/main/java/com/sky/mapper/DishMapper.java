package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
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
            select count( * ) from dish where category_id = #{categoryId}
            """)
    long countByCategoryId( @Param( "categoryId" ) Long categoryId );

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
     * <br>
     * 联表查询 Dish + CategoryName，之后单独查 DishFlavorList，再业务层拼装 DishVOList
     * <br>
     * 排序规则: 菜品修改时间降序、菜品 id 降序
     */
    List< DishVO > selectDishVOByQueryWithoutFlavors( @Param( "dto" ) DishPageQueryDTO dishPageQueryDTO );

    /**
     * 批量查询某种状态的菜品数量(比如正在起售的菜品数量)
     * <br>
     * 用于删除菜品前检查是否起售
     * <br>
     * 更推荐 java.lang.Long，因为 count(*) 在 MySQL 里默认返回 BIGINT
     */
    long countByIdsAndStatus( @Param( "ids" ) List< Long> ids, @Param( "status" ) Integer status );

    /**
     * 批量删除菜品
     */
    int deleteBatch( @Param( "ids" ) List< Long> ids );


}