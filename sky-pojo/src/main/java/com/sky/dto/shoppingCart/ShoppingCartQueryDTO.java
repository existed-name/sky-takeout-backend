package com.sky.dto.shoppingCart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车查询条件 DTO
 * <p>
 *     用于筛选购物车数据, 尤其是判断新增的购物车是否已有同类型的购物车数据
 * </p>
 * <p>
 *     <b>同一类型的 ShoppingCart</b>: 同一个用户, 并且 dishId、dishFlavor 相同( 完全相同的菜品 )或者 setmealId 相同( 同一个套餐 )
 * </p>
 * 完整购物车字段可见{@link com.sky.entity.ShoppingCart}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartQueryDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 用户id（必传，用于隔离不同用户的购物车数据） */
    private Long userId;

    /** 菜品id（与套餐id二选一，查询菜品时使用） */
    private Long dishId;

    /** 套餐id（与菜品id二选一，查询套餐时使用） */
    private Long setmealId;

    /** 口味（如：微辣、加葱等；同一菜品不同口味视为不同类型数据） */
    private String dishFlavor;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
