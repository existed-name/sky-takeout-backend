package com.sky.vo.admin.employee;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 员工详细信息展示对象
 *
 * @CreateTime 2026/9/27 8:56
 */
@ApiModel( description = "员工详细信息展示对象" )
@Data
public class EmployeeInfoVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 员工表主键 id */
    @ApiModelProperty( value = "员工表id", example = "1" )
    private Long id;
    /** 账号用户名 */
    @ApiModelProperty( value = "员工账号用户名(唯一)", example = "admin" )
    private String username;
    /** 姓名 */
    @ApiModelProperty( value = "员工姓名", example = "张三" )
    private String name;
//    /** 账号密码 */
//    private String password;
    /** 电话号码 */
    @ApiModelProperty( value = "员工手机号", example = "13800138000" )
    private String phone;
    /** 性别 */
    @ApiModelProperty( value = "员工性别(1男,2女)", example = "1" )
    private String sex;
    /** 身份证号码 */
    @ApiModelProperty( value = "员工身份证号", example = "110101199001011234" )
    private String idNumber;
//    /** 帐号启用状态 */
//    @ApiModelProperty( value = "账号启用状态", example = "1" )
//    private Integer status;
//    /** 数据创建时间 */
//    @ApiModelProperty( value = "创建时间", example = "2026-09-26 21:10:35" )
//    private LocalDateTime createTime;
//    /** 更新时间 */
//    @ApiModelProperty( value = "更新时间", example = "2026-09-27 08:13:06" )
//    private LocalDateTime updateTime;
//    /** 创建该员工数据的用户 */
//    @ApiModelProperty( value = "创建该员工的管理员 id", example = "1" )
//    private Long createUser;
//    /** 更新该员工数据的用户 */
//    @ApiModelProperty( value = "更新该员工的管理员 id", example = "1" )
//    private Long updateUser;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
