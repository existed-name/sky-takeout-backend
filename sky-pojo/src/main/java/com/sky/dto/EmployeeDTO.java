package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工对象 DTO
 */
@Data
@ApiModel( description = "员工对象 DTO" )
public class EmployeeDTO implements Serializable {
    /**
     * 员工表id
     */
    @ApiModelProperty( value = "员工表id" )
    private Long id;

    /**
     * 员工用户名(唯一)
     */
    @ApiModelProperty( value = "员工用户名(唯一)" )
    private String username;

    /**
     * 员工姓名
     */
    @ApiModelProperty( value = "员工姓名" )
    private String name;

    /**
     * 员工手机号
     */
    @ApiModelProperty( value = "员工手机号" )
    private String phone;

    /**
     * 员工性别(1男,2女)
     */
    @ApiModelProperty( value = "员工性别(1男,2女)" )
    private String sex;

    /**
     * 员工身份证号
     */
    @ApiModelProperty( value = "员工身份证号" )
    private String idNumber;

}
