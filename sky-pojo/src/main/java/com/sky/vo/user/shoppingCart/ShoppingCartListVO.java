package com.sky.vo.user.shoppingCart;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 购物车视图对象 VO */
@ApiModel(description = "购物车视图对象 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartListVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 购物车中包含的所有菜品、套餐信息 */
    @ApiModelProperty(value = "购物车中包含的所有菜品、套餐信息")
    private List< ShoppingCartItemVO > items = new ArrayList<>();

    /** 购物车总金额 */
    @ApiModelProperty(value = "购物车总金额", example = "128.50")
    private BigDecimal totalAmount = BigDecimal.ZERO;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
