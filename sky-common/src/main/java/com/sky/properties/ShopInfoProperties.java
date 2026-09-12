package com.sky.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * 商家门店信息配置属性类, 主要记录门店具体地址
 *
 * @CreateTime 2026/9/8 6:41
 */
@ConstructorBinding // 构造器注入
// 找配置文件的 sky.shop, 同时需要启动类显式声明该类
@ConfigurationProperties( prefix = "sky.shop" )
@Getter // 只读
@AllArgsConstructor
public class ShopInfoProperties {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 商家具体地址, 比如{@code 北京市海淀区上地十街10号} */
    private String address;
    /** 送货距离限制, 单位 m 米 */
    private Double limitDistance;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
