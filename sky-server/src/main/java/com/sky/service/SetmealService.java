package com.sky.service;

import com.sky.dto.SetmealDTO;

/**
 * 套餐管理服务
 *
 * @CreateTime 2026/5/28 20:11
 */
public interface SetmealService {
    /**
     * 新增套餐, 同时存储该套餐和相关菜品的关联关系
     */
    void saveWithRelation( SetmealDTO setmealDTO );
}
