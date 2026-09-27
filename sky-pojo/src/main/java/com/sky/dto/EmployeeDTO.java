package com.sky.dto;

import com.sky.validation.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 员工数据传输对象
 * 用于员工信息的新增和修改操作
 */
@Data
@ApiModel( description = "员工对象 DTO" )
public class EmployeeDTO implements Serializable {

    /** 员工表id */
    @ApiModelProperty( value = "员工表id", example = "1" )
    @NotNull( message = "员工 id 不能为空", groups = ValidationGroups.Update.class )
    @Positive( message = "员工 id 必须是正整数", groups = ValidationGroups.Update.class )
    private Long id;

    /** 员工用户名(唯一) */
    @ApiModelProperty( value = "员工用户名(唯一)", example = "admin", required = true )
    @NotBlank( message = "员工用户名不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String username;

    /** 员工姓名 */
    @ApiModelProperty( value = "员工姓名", example = "张三", required = true )
    @NotBlank( message = "员工姓名不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String name;

    /** 员工手机号 */
    @ApiModelProperty( value = "员工手机号", example = "13800138000", required = true )
    @NotBlank( message = "员工手机号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Pattern( regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    /*
    ^1[3-9]\d{9}$ => 匹配以1开头、第二位是3-9、总共11位数字的手机号
    ^ 字符串开头
    1 必须以数字1开头
    [3-9] 第二位是3到9之间的数字
    \\d{9} 后面紧跟9位任意数字
    $ 字符串结尾
     */
    private String phone;

    /** 员工性别(1男,2女) */
    @ApiModelProperty( value = "员工性别(1男,2女)", example = "1", required = true )
    @NotBlank( message = "员工性别不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Pattern( regexp = "^[12]$", message = "员工性别只能是 1(男) 或 2(女)", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    /*
    ^ 字符串开头
    [12] 只能是数字1或2
    $ 字符串结尾
     */
    private String sex;

    /** 员工身份证号 */
    @ApiModelProperty( value = "员工身份证号", example = "110101199001011234", required = true )
    @NotBlank( message = "员工身份证号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Pattern( regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    /*
    |  或
    ^\\d{15}$  15位纯数字（老版身份证）
    ^\\d{18}$  18位纯数字
    ^\\d{17}(\\d\|X\|x)$  17位数字 + 最后一位是数字或X/x（18位身份证的标准格式）
     */
    private String idNumber;

}
