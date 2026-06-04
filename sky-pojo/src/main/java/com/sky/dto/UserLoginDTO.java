package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * C端用户登录DTO
 * <p>用于接收微信用户登录时提交的授权码</p>
 */
@Data
@ApiModel(value = "UserLoginDTO", description = "C端用户登录请求参数")
public class UserLoginDTO implements Serializable {

    /** 微信用户授权码 */
    @ApiModelProperty(value = "微信用户授权码", required = true, example = "auth_code_example")
    @NotBlank( message = "微信用户授权码不能为空" )
    private String code;

}
