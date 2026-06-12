package com.sky.utils;

import com.sky.constant.RedisConstant;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务工具类: 提取菜品管理中的清除缓存业务方法
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @since 3.0.0
 * @CreateTime 2026/6/12 14:21
 */
@Component
public class DishCacheManager {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final RedisCacheCleaner redisCacheCleaner;



    // ==================== 构造器 ====================
    public DishCacheManager( RedisCacheCleaner redisCacheCleaner ){
        this.redisCacheCleaner = redisCacheCleaner;
    }


    // ==================== 公有方法 ====================
    /**
     * 删除特定的菜品 RedisKey = {@code 业务前缀:分类 id}
     */
    public void deleteRedisKey( Long categoryId ){
        redisCacheCleaner.generateAndDeleteKey(
                RedisConstant.Dish::formatRedisKey,
                categoryId
        );
    }

    /**
     * 批量删除特定的菜品 RedisKey = {@code 业务前缀:分类 id}
     */
    public void deleteRedisKeys( List< Long > categoryIdList ){
        redisCacheCleaner.generateAndDeleteKeys(
                RedisConstant.Dish::formatRedisKey,
                categoryIdList
        );
    }

    // ==================== 私有辅助方法 ====================

}
