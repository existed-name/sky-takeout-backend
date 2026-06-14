package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车查询条件 DTO
 * <p>
 *     用于封装查询购物车时的筛选条件，作为 {@link com.sky.mapper.ShoppingCartMapper#selectExistingItemByQuery} 方法的参数。
 *     查询条件之间为 AND 关系，各字段均为可选条件，为空时则不参与筛选。
 * </p>
 *
 * @see com.sky.entity.ShoppingCart
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

    /** 口味（如：微辣、加葱等；同一菜品不同口味视为不同记录） */
    private String dishFlavor;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
