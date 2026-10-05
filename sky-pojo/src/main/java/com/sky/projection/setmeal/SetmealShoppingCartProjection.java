package com.sky.projection.setmeal;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 套餐购物车投影对象, 用于查询套餐的部分字段组装购物车数据
 *
 * @CreateTime 2026/10/5 10:22
 */
@Data
public class SetmealShoppingCartProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 套餐名称 */
    private String name;

    /** 套餐价格 */
    private BigDecimal price;

    /** 图片路径 */
    private String image;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
