package com.sky.utils;

import com.sky.exception.NotFoundException;

/**
 * 数据不存在工具类, 提取数据不存在的通用处理逻辑
 *
 * @CreateTime 2026/9/27 16:04
 */
public class NotFoundUtil {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private NotFoundUtil(){}


    // ==================== 公有方法 ====================

    /**
     * 根据 id 查询到的对象为 null 时( 不存在 )抛出业务异常
     *
     * @param selectResult mapper 查询结果对象
     * @param message 抛出异常的信息
     * @throws NotFoundException 当查询对象为 null 时抛出
     */
    public static void throwIfNotFound( Object selectResult, String message ){
        if ( selectResult == null ){
            throw new NotFoundException( message );
        }
    }

    /**
     * 根据 id 修改数据, 修改行数为 0 时( 数据不存在 )抛出业务异常
     *
     * @param row 修改行数
     * @param message 抛出的异常信息, 由调用方在{@link com.sky.constant.MessageConstant}中指定
     * @throws NotFoundException 当修改行数为 0 时抛出
     */
    public static void throwIfNotUpdated( int row, String message ){
        if ( row == 0 ){
            throw new NotFoundException( message );
        }
    }

    /**
     * 根据 id 计数, 计数 = 0 说明该条数据不存在, 抛出业务异常
     *
     * @param countById 计数结果, 基本数据类型、而不是包装类型
     * @param message 异常信息
     * @throws NotFoundException 计数结果 = 0 时抛出
     */
    public static void throwIfCountZero( long countById, String message ){
        if ( countById == 0L ){
            throw new NotFoundException( message );
        }
    }


    // ==================== 私有辅助方法 ====================

}
