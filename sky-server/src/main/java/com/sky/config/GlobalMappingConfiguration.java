package com.sky.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct 配置规则, 统一管理 {@link com.sky.converter}的 Converter 类的 @Mapper 配置
 *
 * @CreateTime 2026/7/2 13:34
 */
@MapperConfig(
    // 用 Spring 集成，编译后生成 @Component, 可用 @Autowired 注入 Spring Bean
    componentModel = "spring",
    // 源对象字段如果没有被用到，该怎么处理
    // 由于多数情况下源对象字段很多用不到，如果选择 ERROR 就必须手动指定忽略源对象字段、很多很麻烦
    // 所以通常 IGNORE, 只映射同名的属性、手动 Mapping 的属性
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    // 目标对象字段如果没有被赋值，该怎么处理
    // 因为目标对象通常需要填满，所以如果有字段没赋值就报错, 必须手动忽略部分字段, 确保所有字段都被处理
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public class GlobalMappingConfiguration {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
