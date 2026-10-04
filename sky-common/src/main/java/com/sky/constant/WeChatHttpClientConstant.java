package com.sky.constant;

import org.apache.http.client.utils.URIBuilder;

/**
 * 微信登录相关请求常量
 * <p>
 *     TODO: 整理为枚举类
 * </p>
 *
 * @UpdateTime 2026/10/3 20:58
 */
public class WeChatHttpClientConstant {
    // ==================== 常量 ====================
    /** 微信登录官网 API */
    public static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    /** 授权类型 */
    public static final String DEFAULT_GRANT_TYPE = "authorization_code";

    /** 变量名对应的字符串值 */
    public static final class StringValue {
        /** AppId 字符串常量 */
        public static final String APP_ID = "appid";
        /** secret 字符串常量 */
        public static final String SECRET = "secret";
        /** js_code 字符串常量 */
        public static final String JS_CODE = "js_code";
        /** grant_type 字符串常量 */
        public static final String GRANT_TYPE = "grant_type";
        /** session_key 字符串常量, 用于 JSON 数据键值对映射 */
        public static final String SESSION_KEY = "session_key";
        /** openid 字符串常量, 用于 JSON 数据键值对映射 */
        public static final String OPEN_ID = "openid";

        private StringValue(){}
    }


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private WeChatHttpClientConstant(){}


    // ==================== 公有方法 ====================
    /**
     * 拼接请求参数
     * <p>
     * grantType 授权类型使用默认的{@link WeChatHttpClientConstant#DEFAULT_GRANT_TYPE}
     *
     * @param appId 开发者小程序 id
     * @param appSecret 开发者小程序密钥
     * @param jsCode 用户通过 wx.login 接口获得的临时登录凭证 code
     *
     * @return <code>?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE</code>
     */
    public static String formatQueryParams( String appId, String appSecret, String jsCode ){
        return formatQueryParams( appId, appSecret, jsCode, DEFAULT_GRANT_TYPE );
    }

    /**
     * 拼接请求参数
     *
     * @param appId 开发者小程序 id
     * @param appSecret 开发者小程序密钥
     * @param jsCode 用户通过 wx.login 接口获得的临时登录凭证 code
     * @param grantType 授权类型，此处只需填写<code>authorization_code</code>
     *
     * @return <code>?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE</code>
     */
    public static String formatQueryParams( String appId, String appSecret, String jsCode, String grantType ){
        /*
        // 方法 1 StringBuilder 拼接
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "?" )
                .append( "appid=" ).append( appId ).append( "&" )
                .append( "secret=" ).append( appSecret ).append( "&" )
                .append( "js_code=" ).append( jsCode ).append( "&" )
                .append( "grant_type=" ).append( grantType );
        // 方法 2 格式字符串
        String format = String
                .format( "?appid=%s&secret=%s&js_code=%s&grant_type=%s",
                appId, appSecret, jsCode, grantType );
         */
        // 推荐方法 3 URIBuilder 构建请求参数 / 请求路径
        URIBuilder uriBuilder = new URIBuilder();
        URIBuilder uri = uriBuilder.addParameter( StringValue.APP_ID, appId )
                .addParameter( StringValue.SECRET, appSecret )
                .addParameter( StringValue.JS_CODE, jsCode )
                .addParameter( StringValue.GRANT_TYPE, grantType );
        return uri.toString();
    }


    // ==================== 私有辅助方法 ====================

}
