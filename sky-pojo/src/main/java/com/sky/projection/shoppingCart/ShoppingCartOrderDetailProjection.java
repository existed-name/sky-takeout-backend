package com.sky.projection.shoppingCart;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车订单明细投影对象, 用于查询组装订单明细{@link com.sky.entity.OrderDetail}需要的字段,
 * 并通过{@link com.sky.converter.OrderConverter}映射为订单明细实体对象
 *
 * @CreateTime 2026/10/6 20:13
 */
@Data
public class ShoppingCartOrderDetailProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

//    /** 主键 */
//    private Long id;

    /** 商品名称（菜品名称或套餐名称） */
    private String name;

//    /** 用户id（关联用户表） */
//    private Long userId;

    /** 菜品id（与套餐id二选一） */
    private Long dishId;

    /** 套餐id（与菜品id二选一） */
    private Long setmealId;

    /** 口味（如：微辣、加葱等）, 和菜品一起出现、或者为空  */
    private String dishFlavor;

    /** 商品份数 */
    private Integer number;

    /** 单价金额 */
    private BigDecimal amount;

    /** 商品图片URL */
    private String image;

//    /** 创建时间 */
//    private LocalDateTime createTime;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
