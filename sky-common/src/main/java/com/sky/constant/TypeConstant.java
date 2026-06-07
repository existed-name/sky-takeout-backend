package com.sky.constant;

import java.util.Map;

/**
 * 外卖分成几种类型: 菜品/套餐/... 用 type 表示
 * <p>
 * 而某种类型(菜品/套餐)，又可以细分类，叫做 category，<b>注意区分 category, type</b>
 *
 * @CreateTime 2026/6/7 17:12
 */
public class TypeConstant {
    // ==================== 常量 ====================
    /** 菜品类型用 1 表示 */
    public static final Integer DISH_TYPE = 1;
    /** 套餐类型用 2 表示 */
    public static final Integer SETMEAL_TYPE = 2;
    /** 菜品类型 */
    public static final String DISH_TYPE_DESCRIPTION = "菜品类型";
    /** 套餐类型 */
    public static final String SETMEAL_TYPE_DESCRIPTION = "套餐类型";
    /** 所有类型 */
    public static final String ALL_TYPE_DESCRIPTION = "所有类型";
    /** 未知类型( 无效 type ) */
    public static final String UNKNOWN_TYPE_DESCRIPTION = "未知类型";
    /** 有效数字 -> 业务描述 */
    public static final Map< Integer, String > TYPE_TO_DESCRIPTION_MAP = Map.ofEntries(
            Map.entry( DISH_TYPE, DISH_TYPE_DESCRIPTION ),
            Map.entry( SETMEAL_TYPE, SETMEAL_TYPE_DESCRIPTION )
    );


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private TypeConstant(){}


    // ==================== 公有方法 ====================
    /**
     * 有效类型
     */
    public static boolean isValidType( Integer type ){
        return type == null // 查询所有类型
                || TYPE_TO_DESCRIPTION_MAP.containsKey( type );
    }

    /**
     * 数字 -> 业务描述
     */
    public static String toDescription( Integer type ){
        return type == null ? ALL_TYPE_DESCRIPTION // 查询所有类型
                : TYPE_TO_DESCRIPTION_MAP.getOrDefault( type, UNKNOWN_TYPE_DESCRIPTION );
    }



    // ==================== 私有辅助方法 ====================

}
