package com.sky.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 购物车中包含的菜品信息 VO */
@ApiModel(description = "购物车中包含的菜品信息 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartDishVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 菜品名称 */
    @ApiModelProperty(value = "菜品名称", example = "宫保鸡丁")
    private String name;

    /** 菜品数量 */
    @ApiModelProperty(value = "菜品数量", example = "2")
    private Integer number;

    /** 菜品图片路径 */
    @ApiModelProperty(value = "菜品图片路径")
    private String image;

    /** 菜品单价 */
    @ApiModelProperty(value = "菜品单价", example = "38.00")
    private BigDecimal price;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
