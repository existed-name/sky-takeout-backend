package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.common.TimeRangeInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 存放时间、日期相关的常量和公用方法
 *
 * @CreateTime 2026/9/19 18:35
 */
@Getter
@AllArgsConstructor
public enum Time {
    // ==================== 常量 ====================
    ONE_DAY( 1L );


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /**
     * 枚举成员代表的天数,
     * 以后拓展其他字段时, 如果枚举成员不需要改字段、可置空
     */
    private final Long days;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 获取某个日期第 2 天的日期
     *
     * @param today 今天的日期
     * @return 第 2 天的日期
     */
    public static LocalDate getTomorrow( LocalDate today ){
        return today.plusDays( ONE_DAY.getDays() );
    }

    /**
     * 起始时间不能超过截止时间, 否则抛业务异常
     *
     * @param begin 起始时间
     * @param end 截止时间
     * @throws TimeRangeInvalidException 当起始时间超过截止时间时
     */
    public static void throwIfBeginIsAfterEnd( LocalDate begin, LocalDate end ){
        if ( begin.isAfter( end ) ){
            /*
                begin.compareTo( end ) > 0
                我想的话也可以不抛异常, 查数据库的时候交换 2 个日期参数的位置, 或者互换数值
                不过 ai 建议显式抛异常, 除非业务允许互换数值
             */
            throw new TimeRangeInvalidException( MessageConstant.BEGIN_TIME_GREATER_THAN_END_TIME );
        }
    }


    // ==================== 私有辅助方法 ====================

}
