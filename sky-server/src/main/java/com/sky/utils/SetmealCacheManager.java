package com.sky.utils;

import com.sky.constant.RedisConstant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 业务工具类: 提取套餐管理中的清除缓存业务方法, 由 SetmealService, DishService 调用
 * <p>
 *     套餐缓存的业务逻辑:
 *     <ol>
 *     <li>完整套餐对象 = 套餐本体信息 + 套餐包含的菜品信息</li>
 *     <li>1 个分类 = 1 个套餐本体信息列表, 1 个套餐 = 1 个菜品信息列表</li>
 *     <li>于是修改 1 个套餐, 就清理 1 个分类 + 对应的菜品列表缓存</li>
 *     </ol>
 * </p>
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
     * 删除某个套餐关联的缓存, 内部调用{@link RedisCacheCleaner#generateAndDeleteKey}进行删除操作
     * <p>
     *     <b>由调用方保证参数正确性</b>
     * </p>
     *
     * @param categoryId 套餐所属的分类 id, 用来组装该分类的 RedisKey
     * @param setmealId 套餐 id, 用来组装该套餐对应的菜品列表 RedisKey
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
     * 批量删除多个套餐关联的缓存, 内部调用{@link RedisCacheCleaner#generateAndDeleteKey}进行删除操作
     * <p>
     *     <b>由调用方保证参数正确性</b>
     * </p>
     *
     * @param categoryIds 各个套餐所属的分类 id, 用来组装这些分类的 RedisKeys
     * @param setmealIds 各个套餐的 id, 用来组装这些套餐对应的菜品列表 RedisKeys
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
