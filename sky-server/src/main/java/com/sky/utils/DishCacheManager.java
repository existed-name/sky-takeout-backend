package com.sky.utils;

import com.sky.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务工具类: 提取菜品管理中的清除缓存业务方法
 *
 * @CreateTime 2026/6/12 14:21
 */
@Slf4j
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
     *
     * @param categoryId 菜品所属的分类 id, 用于拼接 RedisKey,
     *                   <b>为空时无法删除缓存, 打印日志后结束该方法</b>
     */
    public void deleteRedisKey( Long categoryId ){
        if ( categoryId == null ){
            log.warn( "删除某个分类对应的菜品缓存时, 分类 id 为 null, 无法拼接对应的 Redis Key, 可能导致旧缓存没被删除" );
            return;
        }
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
