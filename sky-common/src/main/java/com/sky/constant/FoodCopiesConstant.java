package com.sky.constant;

/**
 * 用户加购的菜品/套餐份数，相关常量
 *
 * @CreateTime 2026/6/14 11:18
 */
public class FoodCopiesConstant {
    // ==================== 常量 ====================
    /** 基础 1 份 */
    public static final Integer BASE_COPIES = 1;


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private FoodCopiesConstant(){}


    // ==================== 公有方法 ====================

    /**
     * 自增 1
     */
    public static int increment( int number ){
        return ++number;
    }

    /**
     * 自减 1
     */
    public static int decrement( int number ){
        return --number;
    }


    // ==================== 私有辅助方法 ====================

}
