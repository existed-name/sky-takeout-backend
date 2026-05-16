package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工数据传输对象
 * 用于员工信息的新增和修改操作
 */
@Data
@ApiModel(description = "员工对象 DTO")
public class EmployeeDTO implements Serializable {

    /** 员工表id */
    @ApiModelProperty(value = "员工表id", example = "1")
    private Long id;

    /** 员工用户名(唯一) */
    @ApiModelProperty(value = "员工用户名(唯一)", example = "admin")
    private String username;

    /** 员工姓名 */
    @ApiModelProperty(value = "员工姓名", example = "张三")
    private String name;

    /** 员工手机号 */
    @ApiModelProperty(value = "员工手机号", example = "13800138000")
    private String phone;

    /** 员工性别(1男,2女) */
    @ApiModelProperty(value = "员工性别(1男,2女)", example = "1")
    private String sex;

    /** 员工身份证号 */
    @ApiModelProperty(value = "员工身份证号", example = "110101199001011234")
    private String idNumber;

}
