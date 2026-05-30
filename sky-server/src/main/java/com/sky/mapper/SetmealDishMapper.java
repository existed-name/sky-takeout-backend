package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 管理套餐、菜品的关联关系
 *
 * @CreateTime 2026/5/28 20:40
 */
@Mapper
public interface SetmealDishMapper {
    /**
     * 新增套餐和相关菜品的关联关系
     * <br>
     * 注意: 由{@link com.sky.mapper.SetmealMapper}存储该套餐
     */
    // 太好了，不用设置审计字段
    int insertBatch( @Param( "setmealDishes" ) List< SetmealDish > setmealDishes );

    /**
     * 查询多个套餐对应的所有 {@link com.sky.entity.SetmealDish} 关联关系对象, 用于组装 {@link com.sky.vo.SetmealVO}
     */
    List< SetmealDish > selectBySetmealIds( @Param( "setmealIds" ) List< Long> setmealIds );

    /**
     * 删除多个套餐对应的关联关系
     */
    int deleteBySetmealIds( @Param( "setmealIds" ) List< Integer> setmealIds );

    /**
     * 查询某个套餐和菜品关联关系
     */
    @Select( """
            SELECT id, setmeal_id, dish_id, name, price, copies
            FROM setmeal_dish
            WHERE setmeal_id = #{setmealId}
            """)
    List< SetmealDish > selectBySetmealId( @Param( "setmealId" ) Long setmealId );
}
