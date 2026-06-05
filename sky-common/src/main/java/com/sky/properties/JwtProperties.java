package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类，用于从 application.yml 中读取管理端和用户端的 JWT 相关配置。
 * <p>
 * 配置前缀：sky.jwt
 */
@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    // ==================== 管理端员工 JWT 配置 ====================

    /**
     * 管理端 JWT 签名密钥，用于生成和校验员工端令牌
     */
    private String adminSecretKey;

    /**
     * 管理端 JWT 令牌有效期(Time To Live)，单位：毫秒
     */
    private long adminTtl;

    /**
     * 管理端 JWT 令牌在请求头中的名称( 前端传参命名 )
     */
    private String adminTokenName;

    // ==================== 用户端微信用户 JWT 配置 ====================

    /**
     * 用户端 JWT 签名密钥，用于生成和校验微信用户端令牌
     */
    private String userSecretKey;

    /**
     * 用户端 JWT 令牌有效期(Time To Live)，单位：毫秒
     */
    private long userTtl;

    /**
     * 用户端 JWT 令牌在请求头中的名称( 前端传参命名 )
     */
    private String userTokenName;

}
