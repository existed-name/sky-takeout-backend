package com.sky.constant;

import java.time.Duration;

public class RedisConstant {
    // ==================== 常量 ====================
    /** 命名 redisKey 的分隔符, 比如 [模块]:[子模块]:[key] */
    public static final String DELIMITER = ":";

    /**
     * 店铺模块相关的 Redis 常量, 比如店铺状态的 Key
     */
    public static final class Shop {
        /** 店铺营业状态 Key */
        public static final String SHOP_STATUS = "shop:status"; // "Shop-Status";
        /** 缓存有效时间 - 4 hours */
        public static final Duration CACHE_TTL = Duration.ofHours( 4 );

        private Shop(){}
    }

    /**
     * 菜品模块相关的 Redis 常量, 比如 Key 前缀、缓存有效时间
     */
    public static final class Dish {
        /**
         * 缓存菜品时的业务前缀, 拼接 categoryId 后组成完整 RedisKey
         */
        public static final String DISH_CACHE_PREFIX = "dish:category:";

        /**
         * 缓存有效时间 60 min
         */
        public static final Duration CACHE_TTL = Duration.ofMinutes( 60 );

        /**
         * 组装菜品缓存 RedisKey = {@link com.sky.constant.RedisConstant.Dish#DISH_CACHE_PREFIX} + categoryId
         *
         * @param categoryId 菜品分类 id, <b>不能为空</b>, 否则抛出异常
         * @return {@code dish:category:{categoryId}}
         */
        public static String formatRedisKey( Long categoryId ){
            return RedisConstant.formatRedisKey( DISH_CACHE_PREFIX, categoryId );
        }

        private Dish(){}
    }

    /**
     * 套餐模块相关的 Redis 常量, 比如 Key 前缀、缓存有效时间
     */
    public static class Setmeal {
        /** 缓存套餐时的业务前缀, 拼接 categoryId 后组成完整 RedisKey */
        public static final String SETMEAL_CACHE_PREFIX = "setmeal:category:";
        /** 缓存 套餐包含的所有菜品 的业务前缀, 拼接 setmealId 后组成完整 RedisKey */
        public static final String SETMEAL_DISH_CACHE_PREFIX = "setmeal:dish:";
        /** 套餐 - 缓存有效时间 60 min */
        public static final Duration CACHE_TTL = Duration.ofMinutes( 60 );
        /** 套餐关联菜品 - 缓存有效时间 60 min */
        public static final Duration DISH_CACHE_TTL = Duration.ofMinutes( 60 );

        /**
         * 组装套餐缓存 RedisKey = {@link com.sky.constant.RedisConstant.Setmeal#SETMEAL_CACHE_PREFIX} + categoryId
         *
         * @param categoryId 套餐分类 id, <b>不能为空</b>, 否则抛出异常 {@code IllegalArgumentException}
         * @return {@code setmeal:category:{categoryId}}
         */
        public static String formatRedisKey( Long categoryId ){
            return RedisConstant.formatRedisKey( SETMEAL_CACHE_PREFIX, categoryId );
        }

        /**
         * 组装套餐关联菜品的 RedisKey = {@link com.sky.constant.RedisConstant.Setmeal#SETMEAL_DISH_CACHE_PREFIX} + setmealId
         *
         * @param setmealId 套餐 id, <b>不能为空</b>
         * @return {@code setmeal:dish:{setmealId}}
         * @throws IllegalArgumentException 当套餐 id 为空时抛出
         */
        public static String formatDishRedisKey( Long setmealId ){
            return RedisConstant.formatRedisKey( SETMEAL_DISH_CACHE_PREFIX, setmealId );
        }

        private Setmeal(){}
    }

    /**
     * 订单号相关的 Redis 常量, 主要是<b>订单序列号(订单数量)</b>的 Key 前缀、缓存有效时间
     */
    public static final class OrderNumber {
        /** 缓存订单数量的业务前缀, 拼接日期后组成完整 RedisKey */
        public static final String ORDER_COUNT_CACHE_PREFIX = "order:count:";
        /** 订单数量 - 缓存有效时间 3 天，足够时间查看 */
        public static final Duration CACHE_TTL = Duration.ofDays( 3 );

        /**
         * 组装订单数量缓存 RedisKey = {@link com.sky.constant.RedisConstant.OrderNumber#ORDER_COUNT_CACHE_PREFIX} + formatDate
         *
         * @param formatDate 格式化日期字符串，非空，<b>需要调用方确保格式正常</b><br>
         *                   只要有参数为空就抛出异常 {@code IllegalArgumentException}
         * @return {@code order:count:{formatDate}}
         */
        public static String formatRedisKey( String formatDate ){
            if ( formatDate == null || formatDate.isBlank() ){
                throw new IllegalArgumentException( MessageConstant.FORMAT_DATE_IS_BLANK );
            }
            return ORDER_COUNT_CACHE_PREFIX + formatDate;
        }


        private OrderNumber(){}
    }




    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private RedisConstant(){}


    // ==================== 公有方法 ====================



    // ==================== 私有辅助方法 ====================
    /**
     * 提取公共组装 RedisKey 逻辑
     *
     * @param cachePrefix 缓存的 RedisKey 业务前缀
     * @param categoryId 套餐/菜品分类 id, <b>不能为空</b>, 否则抛出异常 {@code IllegalArgumentException}
     * @return cachePrefix:categoryId
     */
    private static String formatRedisKey( String cachePrefix, Long categoryId ){
        if ( categoryId == null ){
            throw new IllegalArgumentException( "分类 id 不能为空" );
        }
        return cachePrefix + categoryId;
    }

}
