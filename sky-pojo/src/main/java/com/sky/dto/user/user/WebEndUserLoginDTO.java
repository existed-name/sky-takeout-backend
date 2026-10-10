package com.sky.dto.user.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Web 用户端登录参数, 用于网页用户端体验业务流程
 *
 * @CreateTime 2026/10/10 12:52
 */
@Data
@Schema( description = "Web 用户端登录参数, 用于网页用户端体验业务流程" )
public class WebEndUserLoginDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 用户名, 用于拼接模拟的 openid */
    @Schema( description = "用户名, 用于拼接模拟的 openid", example = "张三", required = true )
    private String name;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
