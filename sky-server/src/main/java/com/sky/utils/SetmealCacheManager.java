package com.sky.utils;

import com.sky.constant.RedisConstant;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务工具类: 提取套餐管理中的清除缓存业务方法, 由 SetmealService, DishService 调用
 *
 * @CreateTime 2026/6/12 13:14
 */
@Component
public class SetmealCacheManager {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final RedisCacheCleaner redisCacheCleaner;



    // ==================== 构造器 ====================
    public SetmealCacheManager( RedisCacheCleaner redisCacheCleaner ){
        this.redisCacheCleaner = redisCacheCleaner;
    }


    // ==================== 公有方法 ====================
    /**
     * 提取删除套餐、套餐关联菜品缓存的逻辑
     *
     * @param categoryId 套餐分类 id
     * @param setmealId 套餐 id
     */
    public void deleteRedisKey( Long categoryId, Long setmealId ){
        redisCacheCleaner.generateAndDeleteKey(
                RedisConstant.Setmeal::formatRedisKey,
                categoryId
        );
        redisCacheCleaner.generateAndDeleteKey(
                RedisConstant.Setmeal::formatDishRedisKey,
                setmealId
        );
    }

    /**
     * 提取删除套餐、套餐关联菜品缓存的逻辑
     * <p>
     *     <b>由调用方保证参数正确性</b>
     * </p>
     *
     * @param categoryIds 套餐分类 id
     * @param setmealIds 套餐 id
     */
    public void deleteRedisKeys( List< Long > categoryIds, List< Long > setmealIds ){
        redisCacheCleaner.generateAndDeleteKeys(
                RedisConstant.Setmeal::formatRedisKey,
                categoryIds
        );
        redisCacheCleaner.generateAndDeleteKeys(
                RedisConstant.Setmeal::formatDishRedisKey,
                setmealIds
        );
    }

    // ==================== 私有辅助方法 ====================

}
