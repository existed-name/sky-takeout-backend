package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * 套餐管理服务
 *
 * @CreateTime 2026/5/28 20:11
 */
public interface SetmealService {
    /**
     * 管理端新增套餐, 保存套餐本体信息、关联菜品信息
     *
     * @param operatorId 显式传入操作者 id, 如果不使用 AOP 自动填写审计字段,
     *                   则使用该参数手动填写 create_user、update_user
     * @param setmealDTO 包含关联菜品信息的套餐数据
     */
    void saveWithRelation( Long operatorId, SetmealDTO setmealDTO );

    /**
     * 管理端条件分页查询套餐
     *
     * @param setmealPageQueryDTO 条件分页查询参数
     * @return 数据列表分页包装结果
     */
    PageResult page( SetmealPageQueryDTO setmealPageQueryDTO );

    /**
     * <b>管理端</b>: 批量删除套餐
     */
    void deleteBatch( List< Long > ids );

    /**
     * <b>管理端</b>: 根据 id 查询套餐
     */
    SetmealVO getInfo( Long id );

    /**
     * <b>管理端</b>: 修改套餐信息
     */
    void update( SetmealDTO setmealDTO );

    /**
     * <b>管理端</b>: 修改套餐状态(起售/停售)
     */
    void updateStatusById( Long id, Integer status );

    /**
     * <b>管理端</b>: 根据套餐分类 id 查询对应的套餐
     */
    List< SetmealVO > listByCategoryId( Long categoryId );

    /**
     * <b>用户端</b>: 根据套餐分类 id 查询<b>起售的</b>套餐
     * <p>
     * SetmealVO = Setmeal + categoryName + SetmealDish, 而这里不需要关联关系 SetmealDish
     */
    List< SetmealVO > listEnabledByCategoryId( Long categoryId );

    /**
     * <b>用户端</b>: 查询套餐包含的菜品信息
     * <br>
     * DishItemVO = SetmealDish + Dish
     */
    List< DishItemVO > listDishItemVOById( Long id );
}
