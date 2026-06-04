package com.sky.constant;

public class HttpClientConstant {
    // ==================== 常量 ====================
    /** 微信登录官网 API */
    public static final String WECHAT_LOGIN_URL = " https://api.weixin.qq.com/sns/jscode2session";
    /** 微信小程序 AppId */
    public static final String APP_ID = "wx59dfd0c5591a6b08";
    /** 微信小程序 App Secret */
    public static final String APP_SECRET = "3c5ad8e852bee30785007dc842b15b76";
    /** 授权类型 */
    public static final String GRANT_TYPE = "authorization_code";
    /** session_id 字符串常量, 用于 JSON 数据键值对映射 */
    public static final String SESSION_ID_STRING = "session_id";
    /** openid 字符串常量, 用于 JSON 数据键值对映射 */
    public static final String OPEN_ID_STRING = "openid";


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private HttpClientConstant(){}


    // ==================== 公有方法 ====================
    /**
     * 拼接请求参数
     *
     * @param jsCode
     *
     * @return <code>?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE</code>
     */
    public static String formatQueryParams( String jsCode ){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "?" )
                .append( "?appid=" ).append( APP_ID ).append( "&" )
                .append( "secret=" ).append( APP_SECRET ).append( "&" )
                .append( "js_code=" ).append( jsCode ).append( "&" )
                .append( "grant_type=" ).append( GRANT_TYPE );
        String format = String
                .format( "?appid=%s&secret=%s&js_code=%s&grant_type=%s",
                APP_ID, APP_SECRET, jsCode, GRANT_TYPE );
        return stringBuilder.toString();
    }


    // ==================== 私有辅助方法 ====================

}
