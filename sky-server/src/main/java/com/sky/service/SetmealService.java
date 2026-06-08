package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 套餐管理服务
 *
 * @CreateTime 2026/5/28 20:11
 */
public interface SetmealService {
    /**
     * <b>管理端</b>: 新增套餐, 同时存储该套餐和相关菜品的关联关系
     */
    void saveWithRelation( SetmealDTO setmealDTO );

    /**
     * <b>管理端</b>: 条件分页查询套餐
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
     */
    List< SetmealVO > listEnabledByCategoryId( Long categoryId );

    /**
     * <b>用户端</b>: 查询套餐对应的套餐-菜品关联关系
     */
    List< SetmealDish > listRelationBySetmealId( Long id );
}
