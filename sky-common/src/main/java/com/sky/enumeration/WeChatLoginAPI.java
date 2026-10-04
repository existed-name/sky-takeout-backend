package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.client.utils.URIBuilder;

/**
 * 微信登录 API 相关常量、方法, 包括请求路径、请求参数
 *
 * @CreateTime 2026/10/4 9:13
 */
@Getter
@AllArgsConstructor
public enum WeChatLoginAPI {

    /**
     * 微信登录官网 API
     *
     * @deprecated 目前已在配置文件中管理官网 URL, 请从配置类{@link com.sky.properties.WeChatProperties}中获取 URL
     */
    URL( "https://api.weixin.qq.com/sns/jscode2session" ),
    /** 默认授权类型, 作为请求参数拼接到请求 URI 中 */
    DEFAULT_GRANT_TYPE( "authorization_code" ),
    ;

    /** 请求路径、请求参数的值 */
    private final String value;


    /**
     * 拼接请求参数
     * <p>
     * grantType 授权类型使用默认的{@link #DEFAULT_GRANT_TYPE}
     *
     * @param appId 开发者小程序 id
     * @param appSecret 开发者小程序密钥
     * @param jsCode 用户通过 wx.login 接口获得的临时登录凭证 code
     *
     * @return 格式如下的字符串: <code>?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE</code>
     */
    public static String formatQueryParams( String appId, String appSecret, String jsCode ){
        return formatQueryParams( appId, appSecret, jsCode, DEFAULT_GRANT_TYPE.getValue() );
    }

    /**
     * 拼接请求参数
     *
     * @param appId 开发者小程序 id
     * @param appSecret 开发者小程序密钥
     * @param jsCode 用户通过 wx.login 接口获得的临时登录凭证 code
     * @param grantType 授权类型，此处只需填写<code>authorization_code</code>
     *
     * @return 格式如下的字符串: <code>?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE</code>
     */
    public static String formatQueryParams( String appId, String appSecret, String jsCode, String grantType ){
        URIBuilder uriBuilder = new URIBuilder();
        URIBuilder uri = uriBuilder.addParameter( WeChatLoginParamName.APP_ID.getParamName(), appId )
                .addParameter( WeChatLoginParamName.SECRET.getParamName(), appSecret )
                .addParameter( WeChatLoginParamName.JS_CODE.getParamName(), jsCode )
                .addParameter( WeChatLoginParamName.GRANT_TYPE.getParamName(), grantType );
        return uri.toString();
    }
}
