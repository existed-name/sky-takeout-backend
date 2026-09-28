package com.sky.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Redis 缓存清理公共逻辑
 * <p>
 *     由于依赖 RedisTemplate 注入, 所以需要实例化方法，而不是静态方法
 *     <br>
 *     同时注册为容器内容 Component
 * </p>
 *
 * @CreateTime 2026/6/11 21:14
 */
@Component
public class RedisCacheCleaner {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final RedisTemplate< String, Object > redisTemplate;


    // ==================== 构造器 ====================
    public RedisCacheCleaner( RedisTemplate< String, Object > redisTemplate ){this.redisTemplate = redisTemplate;}

    // ==================== 公有方法 ====================
    /**
     * 删除单个 key
     *
     * @param redisKey 由调用方确保正确性
     */
    public void delete( String redisKey ){
        if ( redisKey == null || redisKey.isBlank() ){
            return;
        }
        redisTemplate.delete( redisKey );
    }

    /**
     * 删除多个 key, 内部会对 key 列表去重，减少网络开销
     *
     * @param redisKeys 由调用方确保正确性
     */
    public void delete( List< String > redisKeys ){
        if ( redisKeys == null || redisKeys.isEmpty() ){
            return;
        }
        redisKeys = redisKeys.stream().distinct().toList();
        redisTemplate.delete( redisKeys );
    }

    /*
        观察 DishService、SetmealService 里面的删除缓存操作
        * 先调 RedisConstant 方法组装 RedisKey
        * 再交给 RedisTemplate 删
        * 那么变化最多的是组装 RedisKey => 调用方传方法引用，工具类用方法引用转成 RedisKey
     */

    /**
     * 调用方指定生成 Key 的方法、原始材料，该方法组装 Key 后删除
     *
     * @param redisKeyGenerator 方法引用，主要来自{@link com.sky.constant.RedisConstant}的组装 Key 方法
     *                          <br>
     *                          将 T 类型材料转换为 String 类型 Key
     * @param id 用来拼接 Key 的原始材料(比如 categoryId, setmealId 等等)，
     *          {@code RedisKey = 业务前缀:id},
     *           <b>为空时直接结束该方法, </b>
     * @param <T> 原始材料的类型
     * @throws IllegalArgumentException 当方法引用为空时
     */
    public < T > void generateAndDeleteKey( Function< T, String > redisKeyGenerator, T id ){
        validateRedisKeyGenerator( redisKeyGenerator );
        if ( id == null ){
            return;
        }
        String redisKey = redisKeyGenerator.apply( id );
        redisTemplate.delete( redisKey );
    }

    /**
     * 调用方指定生成 Key 的方法、多份原始材料，该方法批量组装 Key 后删除
     * <p>
     *     内部会对 key 列表去重，减少网络开销
     * </p>
     *
     * @param redisKeyGenerator 方法引用，主要来自{@link com.sky.constant.RedisConstant}的组装 Key 方法
     *                          <br>
     *                          将 T 类型材料转换为 String 类型 Key
     * @param ids 用来拼接 Key 的原始材料(比如 categoryId, setmealId 等等)，{@code RedisKey = 业务前缀:id}
     * @param <T> 原始材料的类型
     */
    public < T > void generateAndDeleteKeys( Function< T, String > redisKeyGenerator, List< T > ids ){
        validateRedisKeyGenerator( redisKeyGenerator );
        if ( ids == null || ids.isEmpty() ){
            return;
        }

        List< String > redisKeys = ids.stream()
                .map( redisKeyGenerator )
                .distinct()
                .toList();
        redisTemplate.delete( redisKeys );
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 检验 redisKeyGenerator(判空)
     * <p>
     *     当方法引用为空时抛出 {@code IllegalArgumentException}
     *
     * @param redisKeyGenerator 方法引用，主要来自{@link com.sky.constant.RedisConstant}的组装 Key 方法
     *                          <br>
     *                          将 T 类型材料转换为 String 类型 Key
     * @param <T> 原始材料的类型
     * @throws IllegalArgumentException 当方法引用为空时
     */
    private < T > void validateRedisKeyGenerator( Function< T, String > redisKeyGenerator ){
        // 我觉得方法引用应该单出来判断，因为用来生成 key 的材料可为空(不删就是)，但是方法引用一定要准确
        if ( redisKeyGenerator == null ){
            // 不过作为 common 的工具类，我在想应该抛业务异常还是 Java 原生异常
            // AI: common 工具类抛出标准异常，不污染业务异常体系
            throw new IllegalArgumentException( "方法引用 redisKeyGenerator 不能为空" );
        }
    }
}
