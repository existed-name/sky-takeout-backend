package com.sky.constant;

import java.time.LocalDate;

/**
 * 时间、日期相关的常量
 *
 * @CreateTime 2026/9/19 18:35
 */
public class TimeConstant {
    // ==================== 常量 ====================
    public static final long ONE_DAY = 1;


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private TimeConstant(){}


    // ==================== 公有方法 ====================

    /**
     * 获取某个日期第 2 天的日期
     *
     * @param today 今天的日期
     * @return 第 2 天的日期
     */
    public static LocalDate getTomorrow( LocalDate today ){
        return today.plusDays( ONE_DAY );
    }



    // ==================== 私有辅助方法 ====================

}
