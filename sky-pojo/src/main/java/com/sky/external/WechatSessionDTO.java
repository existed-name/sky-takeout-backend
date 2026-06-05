package com.sky.external;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("session_key") // 对应返回数据 session_key
    private String sessionKey;

    /** 错误码 */
    @JsonProperty( "errcode" ) // 对应返回数据 errcode
    private Integer errorCode;

    /** 错误信息 */
    @JsonProperty( "errmsg" ) // 对应返回数据 errmsg
    private String errorMessage;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
