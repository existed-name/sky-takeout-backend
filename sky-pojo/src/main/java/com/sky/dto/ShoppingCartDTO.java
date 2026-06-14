package com.sky.dto;

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
    @ApiModelProperty(value = "菜品id（添加菜品时必填）", example = "1")
    private Long dishId;

    /** 套餐id（与菜品id二选一必填） */
    @ApiModelProperty(value = "套餐id（添加套餐时必填）", example = "1")
    private Long setmealId;

    /** 菜品口味（如：微辣、加葱等） */
    @ApiModelProperty(value = "菜品口味", example = "微辣")
    private String dishFlavor;

    /** 是否存在菜品 */
    public boolean hasDish(){
        return dishId != null;
    }

    /** 是否存在套餐 */
    public boolean hasSetmeal(){
        return setmealId != null;
    }
}
