package com.sky.dto.dish;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 菜品启用状态修改信息
 *
 * @CreateTime 2026/9/30 16:07
 */
@ApiModel( description = "菜品启用状态修改信息" )
@Data
public class DishStatusDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /**
     * 起售状态 0表示停售 1表示起售
     */
    @ApiModelProperty( value = "起售状态 0表示停售 1表示起售", example = "1", required = true )
    @NotNull( message = "菜品起售状态不能为空" )
    private Integer status;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
