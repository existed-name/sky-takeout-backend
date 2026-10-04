package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信登录接口的请求参数名称
 * 
 * <p>
 *     注意是{@code 参数名称}而不是{@code 参数值}
 * </p>
 *
 * @CreateTime 2026/10/4 9:50
 * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信登录接口官方文档</a>
 */
@Getter
@AllArgsConstructor
public enum WeChatLoginParamName {

    /** 小程序 appId, 具体值从配置文件注入 */
    APP_ID( "appid" ),
    /** 小程序 appSecret, 具体值从配置文件注入 */
    SECRET( "secret" ),
    /** 微信用户授权码, 具体值由小程序端返回后端 */
    JS_CODE( "js_code" ),
    /** 授权类型, 默认值为{@link WeChatLoginAPI#DEFAULT_GRANT_TYPE} */
    GRANT_TYPE( "grant_type" ),
    ;

    /** 请求路径、请求参数的值 */
    private final String paramName;

}
