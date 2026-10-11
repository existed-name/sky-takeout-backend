package com.sky.vo.admin.employee;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录视图对象
 * 用于员工登录成功后返回的令牌及用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "员工登录视图对象 VO")
public class EmployeeLoginVO implements Serializable {

    /** 员工id */
    @ApiModelProperty(value = "员工id", example = "1")
    private Long id;

    /** 用户名 */
    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;

    /** 姓名 */
    @ApiModelProperty(value = "姓名", example = "张三")
    private String name;

    /** jwt令牌 */
    @ApiModelProperty(value = "jwt令牌", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

}
