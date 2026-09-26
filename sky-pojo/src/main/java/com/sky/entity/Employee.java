package com.sky.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 员工表主键 id */
    private Long id;
    /** 账号用户名 */
    private String username;
    /** 姓名 */
    private String name;
    /** 账号密码 */
    private String password;
    /** 电话号码 */
    private String phone;
    /** 性别 */
    private String sex;
    /** 身份证号码 */
    private String idNumber;
    /** 帐号启用状态 */
    private Integer status;
    /** 数据创建时间 */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    /** 更新时间 */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;
    /** 创建该员工数据的用户 */
    private Long createUser;
    /** 更新该员工数据的用户 */
    private Long updateUser;

}
