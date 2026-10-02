package com.sky.utils;

/**
 * 断言工具类, 抽取公共的检验、抛出异常逻辑
 *
 * @CreateTime 2026/10/2 6:11
 */
public class AssertUtils {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private AssertUtils(){}


    // ==================== 公有方法 ====================

    /**
     * 主键回填失败时抛出 JDK 原生异常
     *
     * @param id 插入实体对象后, mybatis 回填的主键 id
     * @param message 异常信息, 由调用方根据具体场景自定义, <br>
     *                比如{@code 插入套餐数据时, 主键回填失败, 套餐名称 = %s , 分类 id = %s}
     * @throws IllegalStateException 当 id 为 null 时抛出( 主键回填失败 )
     */
    public static void assertIdBackFilled( Long id, String message ){
        if ( id == null ){
            throw new IllegalStateException( message );
        }
    }


    // ==================== 私有辅助方法 ====================

}
