package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    /**
     * 添加某种菜品对应的口味
     */
    int insertBatch( @Param( "flavors" ) List< DishFlavor> flavors );

    /**
     * 根据多个菜品 id 查询对应的所有菜品口味
     * <br>
     * 注: 为了匹配原来的 Dish 链表顺序，不对 DishFlavor 进行排序
     */
    List< DishFlavor > selectByDishIdList( @Param( "dishIdList" ) List< Long> dishIdList );

    /**
     * 查询某种菜品对应的口味
     *
     * @param dishId 菜品 id
     * @return 该菜品关联的口味, 菜品不存在、无关联口味时返回空链表
     */
    @Select( """
            SELECT id, dish_id, name, value
            FROM dish_flavor
            WHERE dish_id = #{dishId}
            """ )
    List< DishFlavor > selectByDishId( @Param( "dishId" ) Long dishId );

    /**
     * 删除多个菜品对应的口味
     *
     * @param dishIds 菜品 id
     * @return 删除行数
     */
    int deleteBatchByDishIds( @Param( "dishIds" ) List< Long > dishIds );


    // ==================== 私有辅助方法 ====================

}
