package com.sky.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * 百度地图配置属性类，从 application.yaml 读取配置信息（AccessKey）
 * <p>
 *     配置前缀：sky.baidu-map
 * </p>
 * @CreateTime 2026/9/4 9:20
 */
@ConstructorBinding // 通过构造器绑定而不是 setter 设置
@ConfigurationProperties( prefix = "sky.baidu-map" )
@Getter // 只读、不可写
@AllArgsConstructor
public class BaiduMapProperties {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================
    /**
     * 百度地图 API 网址封装对象
     */
    @Getter
    @AllArgsConstructor
    public static class Url {
        /** 地理编码 API, 把具体地址转为经纬度 */
        private String geocoding;
        /** 路线规划 API, 主要用来拿到 2 个地址的路线距离 */
        private String direction;
    }


    // ==================== 实例变量 ====================
    /**
     * 百度地图 AccessKey
     */
    private String accessKey;

    /**
     * 百度地图 API 网址封装对象
     */
    private Url url;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
