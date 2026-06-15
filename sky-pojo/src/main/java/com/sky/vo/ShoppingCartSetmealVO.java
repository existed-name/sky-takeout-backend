package com.sky.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 购物车中包含的套餐信息 VO */
@ApiModel(description = "购物车中包含的套餐信息 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartSetmealVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 套餐名称 */
    @ApiModelProperty(value = "套餐名称", example = "超值双人套餐")
    private String name;

    /** 套餐数量 */
    @ApiModelProperty(value = "套餐数量", example = "1")
    private Integer number;

    /** 套餐图片路径 */
    @ApiModelProperty(value = "套餐图片路径")
    private String image;

    /** 套餐单价 */
    @ApiModelProperty(value = "套餐单价", example = "88.00")
    private BigDecimal price;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
