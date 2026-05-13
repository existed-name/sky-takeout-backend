package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel( description = "员工对象 DTO" )
public class EmployeeDTO implements Serializable {
    @ApiModelProperty( value = "员工表id" )
    private Long id;

    @ApiModelProperty( value = "员工用户名(唯一)" )
    private String username;

    @ApiModelProperty( value = "员工姓名" )
    private String name;

    @ApiModelProperty( value = "员工手机号" )
    private String phone;

    @ApiModelProperty( value = "员工性别" )
    private String sex;

    @ApiModelProperty( value = "员工身份证号" )
    private String idNumber;

}
