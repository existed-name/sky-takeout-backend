package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 * <p>
 *     对应数据库表 shopping_cart，存储用户购物车中的菜品/套餐信息。
 *     每个用户可有多条记录，每条记录代表购物车中的一项（菜品或套餐）。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 商品名称（菜品名称或套餐名称） */
    private String name;

    /** 用户id（关联用户表） */
    private Long userId;

    /** 菜品id（与套餐id二选一） */
    private Long dishId;

    /** 套餐id（与菜品id二选一） */
    private Long setmealId;

    /** 口味（如：微辣、加葱等） */
    private String dishFlavor;

    /** 数量 */
    private Integer number;

    /** 单价金额 */
    private BigDecimal amount;

    /** 商品图片URL */
    private String image;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 该条购物车数据是不是菜品 */
    public boolean isDish(){
        return dishId != null;
    }

    /** 该条购物车数据是不是套餐 */
    public boolean isSetmeal(){
        return setmealId != null;
    }
}
