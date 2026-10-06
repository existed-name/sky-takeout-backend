package com.sky.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信 jscode2session 接口响应对象
 *
 * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信官方文档</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeChatSessionDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 用户唯一标识 */
    @JsonProperty( "openid" ) // 对应返回数据 openid
    private String openid;

    /** 会话密钥 */
    @JsonProperty( "session_key" ) // 对应返回数据 session_key
    private String sessionKey;

    /** 错误码 */
    @JsonProperty( "errcode" ) // 对应返回数据 errcode
    private Integer errorCode;

    /** 错误信息 */
    @JsonProperty( "errmsg" ) // 对应返回数据 errmsg
    private String errorMessage;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 判断微信登录是否失败
     * <p>
     *     判断依据: 通过 API 登录失败会返回<code>errcode</code>和<code>errmsg</code>, 由此判断登录失败
     * </p>
     *
     * @return true 登录失败, false 登录成功
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信小程序登录官网</a>
     * @deprecated 新代码请调用{@link com.sky.enumeration.WeChatLoginStatus#loginFailed}, 和 dto 解耦
     */
    public boolean loginFailed(){
        return errorCode != null && errorCode != 0 // 成功时 0
                &&  // 这里为了保险还补充了异常信息检验
                errorMessage != null && ! errorMessage.isBlank();
    }



    // ==================== 私有辅助方法 ====================

}
