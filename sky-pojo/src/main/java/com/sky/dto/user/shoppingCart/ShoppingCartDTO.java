package com.sky.dto.user.shoppingCart;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 购物车数据传输对象
 * <p>
 *     用于用户端添加菜品/套餐到购物车时的请求参数封装
 * </p>
 */
@Data
@ApiModel(description = "购物车添加请求 DTO")
public class ShoppingCartDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 菜品id（与套餐id二选一必填） */
    @ApiModelProperty(value = "菜品id（添加菜品时必填）", example = "1", required = false )
    private Long dishId;

    /** 套餐id（与菜品id二选一必填） */
    @ApiModelProperty(value = "套餐id（添加套餐时必填）", example = "1", required = false )
    private Long setmealId;

    /** 菜品口味（如：微辣、加葱等） */
    @ApiModelProperty(value = "菜品口味(如果添加菜品时选择了口味, 则该字段存在)", example = "微辣", required = false )
    private String dishFlavor;

    /** 是否存在菜品 */
    public boolean hasDish(){
        return dishId != null;
    }

    /** 是否存在套餐 */
    public boolean hasSetmeal(){
        return setmealId != null;
    }

    /**
     * 菜品、套餐 id 都不存在 => 数据不完整
     */
    public boolean hasNoDishOrSetmeal(){
        return ! hasDish() && ! hasSetmeal();
    }

    /**
     * 菜品、套餐 id 都包含 => 不满足 2 选 1
     */
    public boolean hasDishAndSetmeal(){
        return hasDish() && hasSetmeal();
    }
}
