package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AliOss 配置类，创建 AliOssUtil
 *
 * @CreateTime 2026/5/23 8:07
 */
@Configuration
public class AliOssConfiguration {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    /**
     * 创建、返回 AliOssUtil
     */
    @Bean // 注册为 Bean 对象，交给容器管理
    @ConditionalOnMissingBean // 条件创建: 容器已有对象，就不重复创建；否则创建对象
    public AliOssUtil getAliOssUtil( AliOssProperties aliOssProperties ){
        return new AliOssUtil(
            aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
    }


    // ==================== 私有辅助方法 ====================

}
