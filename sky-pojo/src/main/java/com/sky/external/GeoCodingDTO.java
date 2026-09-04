package com.sky.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 百度地图将具体位置地理编码后拿到的经度纬度包装对象
 *
 * @CreateTime 2026/9/4 10:48
 */
// 解析 json 字符串时忽略
@JsonIgnoreProperties( ignoreUnknown = true )
public class GeoCodingDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 精度 lng */
    private float longitude;
    /** 维度 lat */
    private float latitude;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
