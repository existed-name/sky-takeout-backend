package com.sky.mapper;

import com.sky.entity.SetmealDish;
import com.sky.vo.admin.setmeal.SetmealVO;
import com.sky.vo.user.setmeal.UserSetmealDishItemVO;
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
     * 新增套餐和相关菜品的关联关系, 套餐本体信息由{@link com.sky.mapper.SetmealMapper}管理
     * <p>
     *     太好了，不用设置审计字段
     * </p>
     *
     * @param setmealDishes 套餐关联菜品信息
     * @return 插入数据行数
     */
    int insertBatch( @Param( "setmealDishes" ) List< SetmealDish > setmealDishes );

    /**
     * 查询多个套餐对应的所有 {@link com.sky.entity.SetmealDish} 关联关系对象, 用于组装 {@link SetmealVO}
     */
    List< SetmealDish > selectBySetmealIds( @Param( "setmealIds" ) List< Long> setmealIds );

    /**
     * 删除多个套餐对应的关联关系
     *
     * @param setmealIds 套餐 id 链表
     * @return 删除数据行数
     */
    int deleteBySetmealIds( @Param( "setmealIds" ) List< Long > setmealIds );

    /**
     * 查询某个套餐的关联菜品信息
     *
     * @param setmealId 套餐 id
     * @return 关联菜品信息列表
     */
    List< SetmealDish > selectBySetmealId( @Param( "setmealId" ) Long setmealId );

    /**
     * 删除某个套餐关联的所有菜品关系
     *
     * @param setmealId 套餐 id
     * @return 删除数据行数
     */
    int deleteBatchBySetmealId( @Param( "setmealId" ) Long setmealId );

    /**
     * 查询某个套餐包含的菜品信息
     * <p>
     *     数据项 = SetmealDish.copies + Dish.name、description、image
     * </p>
     * 排序规则: 无
     *
     * @param setmealId 套餐 id
     * @return 套餐关联菜品数据项列表
     */
    List< UserSetmealDishItemVO > selectDishItemVOListBySetmealId( @Param( "setmealId" ) Long setmealId );
}
