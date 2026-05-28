package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
