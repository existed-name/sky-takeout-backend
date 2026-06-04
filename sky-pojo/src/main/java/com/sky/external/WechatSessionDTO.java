package com.sky.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信 jscode2session 接口响应对象
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html">微信官方文档</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WechatSessionDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 用户唯一标识 */
    private String openid;

    /** 会话密钥 */
    private String sessionKey;

    /** 错误码 */
    private Integer errorCode;

    /** 错误信息 */
    private String errorMessage;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
