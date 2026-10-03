package com.sky.dto.shop;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 店铺营业状态修改信息
 *
 * @CreateTime 2026/10/3 9:07
 */
@ApiModel( description = "店铺营业状态修改信" )
@Data
public class ShopStatusDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /**
     * 店铺营业状态: 0 表示打烊, 1 表示营业中
     */
    @ApiModelProperty( value = "店铺营业状态: 0 表示打烊, 1 表示营业中", example = "1", required = true )
    @NotNull( message = "店铺营业状态不能为空" )
    private Integer status;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
