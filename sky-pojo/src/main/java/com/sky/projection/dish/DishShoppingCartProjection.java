package com.sky.projection.dish;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品购物车投影对象, 用于查询菜品的部分字段组装购物车数据
 *
 * @CreateTime 2026/10/5 10:18
 */
@Data
public class DishShoppingCartProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 菜品名称 */
    private String name;

    /** 菜品价格 */
    private BigDecimal price;

    /** 图片路径 */
    private String image;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
