package com.sky.dto.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 公用分页查询参数 dto
 *
 * @CreateTime 2026/9/29 20:36
 */
@Data
@ApiModel( description = "公用分页查询参数 dto" )
public class PageQueryDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 页码 */
    @ApiModelProperty( value = "起始页码", example = "1", required = true )
    @NotNull( message = "起始页码不能为空" )
    @Positive( message = "起始页码必须是正整数" )
    private Integer page;

    /** 每页显示记录数 */
    @ApiModelProperty( value = "每页记录数", example = "10", required = true )
    @NotNull( message = "每页数据条数不能为空" )
    @Positive( message = "每页数据条数必须是正整数" )
    private Integer pageSize;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
