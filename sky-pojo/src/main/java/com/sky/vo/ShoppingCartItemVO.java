package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车中包含的套餐/菜品信息 VO，
 * <br>
 * 因为 ShoppingCartSetmealVO, ShoppingCartDishVO 字段一样，提取为同一个 VO
 *
 * @CreateTime 2026/6/15 10:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "购物车单条数据 VO")
public class ShoppingCartItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 数据库表 id */
    @ApiModelProperty(value = "数据库表 id", example = "1")
    private Long id;

    /** 菜品 id */
    @ApiModelProperty(value = "菜品 id", example = "1")
    private Long dishId;

    /** 套餐 id */
    @ApiModelProperty(value = "套餐 id", example = "1")
    private Long setmealId;

    /** 套餐/菜品名称 */
    @ApiModelProperty(value = "套餐/菜品名称", example = "超值双人套餐")
    private String name;

    /** 套餐/菜品数量 */
    @ApiModelProperty(value = "套餐/菜品数量", example = "1")
    private Integer number;

    /** 套餐/菜品图片路径 */
    @ApiModelProperty(value = "套餐/菜品图片路径")
    private String image;

    /** 套餐/菜品单价 */
    @ApiModelProperty(value = "套餐/菜品单价", example = "88.00")
    private BigDecimal price;

    /** 菜品口味(展示菜品会用) */
    @ApiModelProperty(value = "菜品口味", example = "微辣")
    private String dishFlavor;

    /** 加购时间 */
    @ApiModelProperty(value = "加购时间", example = "2026-06-15 10:30:00")
    private LocalDateTime createTime;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
