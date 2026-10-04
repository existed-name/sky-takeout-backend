package com.sky.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties( prefix = "sky.wechat" )
@Getter
@AllArgsConstructor
public class WeChatProperties {

    /**
     * 微信官方接口 URL
     */
    @Getter
    @AllArgsConstructor
    public static class Url {
        /** 微信登录接口 URL */
        private String login;
    }
    /** 微信官方接口 URL 封装对象 */
    private Url url;

    /** 小程序的appid */
    private String appid;
    /** 小程序的秘钥 */
    private String secret;
    /** 商户号 */
    private String mchid;
    /** 商户API证书的证书序列号 */
    private String mchSerialNo;
    /** 商户私钥文件 */
    private String privateKeyFilePath;
    /** 证书解密的密钥 */
    private String apiV3Key;
    /** 平台证书 */
    private String weChatPayCertFilePath;
    /** 支付成功的回调地址 */
    private String notifyUrl;
    /** 退款成功的回调地址 */
    private String refundNotifyUrl;

}
