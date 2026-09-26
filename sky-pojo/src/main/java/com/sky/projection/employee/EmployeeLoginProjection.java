package com.sky.projection.employee;

import lombok.Data;

/**
 * 员工登录信息投影类, 封装 mapper 查询的和员工登录功能相关的字段
 *
 * @CreateTime 2026/9/26 14:07
 */
@Data
public class EmployeeLoginProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 员工表主键 id */
    private Long id;
    /** 账号用户名 */
    private String username;
    /** 姓名 */
    private String name;
    /** 账号密码 */
    private String password;
//    /** 电话号码 */
//    private String phone;
//    /** 性别 */
//    private String sex;
//    /** 身份证号码 */
//    private String idNumber;
    /** 帐号启用状态 */
    private Integer status;
//    /** 数据创建时间 */
//    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
//    private LocalDateTime createTime;
//    /** 更新时间 */
//    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
//    private LocalDateTime updateTime;
//    /** 创建该员工数据的用户 */
//    private Long createUser;
//    /** 更新该员工数据的用户 */
//    private Long updateUser;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
