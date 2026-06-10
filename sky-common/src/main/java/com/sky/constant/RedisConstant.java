package com.sky.constant;

import com.sky.exception.IllegalParameterException;

import java.time.Duration;

public class RedisConstant {
    // ==================== 常量 ====================
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
         * @param categoryId 菜品分类 id, <b>不能为空</b>, 否则抛出异常 {@link com.sky.exception.IllegalParameterException}
         * @return dish:category:categoryId
         */
        public static String formatRedisKey( Long categoryId ){
            if ( categoryId == null ){
                throw new IllegalParameterException( "分类 id 不能为空" );
            }
            return DISH_CACHE_PREFIX + categoryId;
        }

        private Dish(){}
    }





    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private RedisConstant(){}


    // ==================== 公有方法 ====================



    // ==================== 私有辅助方法 ====================

}
