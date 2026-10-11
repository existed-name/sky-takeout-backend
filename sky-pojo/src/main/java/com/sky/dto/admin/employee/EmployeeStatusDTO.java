package com.sky.dto.admin.employee;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 员工帐号状态修改信息
 *
 * @CreateTime 2026/9/27 6:39
 */
@ApiModel( description = "员工帐号状态修改信息" )
@Data
public class EmployeeStatusDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 账号启用状态 */
    @ApiModelProperty( value = "账号启用状态", example = "0", required = true )
    @NotNull( message = "账号启用状态不能为空" )
    private Integer status;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
