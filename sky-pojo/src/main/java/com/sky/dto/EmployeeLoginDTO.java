package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 员工登录数据传输对象
 * @UpdateTime 2026-09-26
 */
@Data
@ApiModel(description = "员工登录 DTO")
public class EmployeeLoginDTO implements Serializable {

    /** 用户名 */
    @ApiModelProperty( value = "用户名", example = "admin", required = true )
    @NotBlank( message = "用户名不能为空" )
    private String username;

    /** 密码 */
    @ApiModelProperty( value = "密码", example = "123456", required = true )
    @NotBlank( message = "密码不能为空" )
    private String password;

}
