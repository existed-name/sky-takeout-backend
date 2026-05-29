package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
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
    void insertBatch( @Param( "setmealDishes" ) List< SetmealDish > setmealDishes );

    /**
     * 查询多个套餐对应的所有 {@link com.sky.entity.SetmealDish} 关联关系对象, 用于组装 {@link com.sky.vo.SetmealVO}
     */
    List< SetmealDish > selectBySetmealIds( @Param( "setmealIds" ) List< Long> setmealIds );
}
