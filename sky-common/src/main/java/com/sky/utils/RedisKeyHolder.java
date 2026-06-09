package com.sky.utils;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * 由 Service 调用，缓存内容时在该类记录出现过的 RedisKey
 * 
 * @CreateTime 2026/6/9 9:36
 */
public class RedisKeyHolder {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================
    /** 管理菜品模块的 RedisKey */
    public static final class Dish {

        /** 存储菜品分类 id 作为 RedisKey */
        @Getter
        private static final Set< String > redisKeySet = new HashSet<>();

        /**
         * 添加 RedisKey( 菜品分类 id )
         *
         * @param categoryId 菜品分类 id( 转为 String 类型 )
         */
        public static void addIfNotExists( String categoryId ){
            redisKeySet.add( categoryId );
        }

        /**
         * 删除所有 RedisKey
         */
        public static void removeAll(){
            // redisKeySet.removeAll( ); 是删除 set 和 传入集合的交集
            redisKeySet.clear();
        }
        
        private Dish(){}
    }
    
    


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private RedisKeyHolder(){}


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
