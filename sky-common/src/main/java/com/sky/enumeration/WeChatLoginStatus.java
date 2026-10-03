package com.sky.enumeration;

import com.sky.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信登录 API 响应状态, 记录响应码、对应的错误信息, 封装相关业务方法
 *
 * @see <a href="https://developers.weixin.qq.com/doc/oplatform/developers/errCode/#%E5%85%AC%E5%85%B1%E9%94%99%E8%AF%AF%E7%A0%81">微信登录官方错误码文档</a>
 * @CreateTime 2026/10/3 15:16
 */
@AllArgsConstructor
@Getter
public enum WeChatLoginStatus {

    /** 正常登录情况 1 */
    OK_1( 0, "ok", "请求成功" ),
    /** 正常登录情况 2 */
    OK_2( null, null, "请求成功" ),
    /** 系统错误 */
    SYSTEM_ERROR( -1, "system error", "系统繁忙，此时请开发者稍候再试" ),
    /** 微信用户授权码无效 */
    INVALID_CODE( 40029, "invalid code", "无效的 oauth_code" ),
    /** 重复登录 */
    CODE_BEEN_USED( 40163, "code been used", "oauth_code已使用" ),
    ;


    /** 响应错误码 */
    private final Integer errCode;
    /** 错误码对应的异常信息( 英文 ) */
    private final String errMsg;
    /** 中文描述 */
    private final String description;


    /**
     * 判断微信登录是否失败
     * <p>
     *     判断依据: 通过 API 登录失败会返回<code>errcode</code>和<code>errmsg</code>,
     *     <code>errcode</code>和<code>errmsg</code>为空则说明登录失败,
     *     同时限制了<code>errcode != 0</code>
     * </p>
     *
     * @param errorCode 微信登录响应码
     * @param errorMessage 微信登录响应错误信息
     * @return true 登录失败, false 登录成功
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信小程序登录官网</a>
     */
    public static boolean loginFailed( Integer errorCode, String errorMessage ){
        return errorCode != null && ! OK_1.getErrCode().equals( errorCode )
                && ! StringUtils.isBlank( errorMessage );
    }

    /**
     * 微信用户授权码是否无效, 比如过期失效
     *
     * @param errorCode 微信登录响应错误码, 可传入 null( null 通常表示登录正常 )
     * @return true 授权码无效, false 依旧有效
     */
    public static boolean isWeChatCodeInvalid( Integer errorCode ){
        return INVALID_CODE.getErrCode().equals( errorCode );
    }

    /**
     * 微信用户授权码是否已经用过了
     *
     * @param errorCode 微信登录响应错误码
     * @return true, 微信用户授权码已经被用过( 已经登录过了 ); false 未被使用过
     */
    public static boolean isWeChatCodeUsed( Integer errorCode ){
        return CODE_BEEN_USED.getErrCode().equals( errorCode );
    }
}
