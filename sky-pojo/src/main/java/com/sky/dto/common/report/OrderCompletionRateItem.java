package com.sky.dto.common.report;

import lombok.Data;

import java.time.LocalDate;

/**
 * 订单完成率数据项,
 * 由 SQL 查出每天的有效订单数、总订单数, 计算完成率后返回该对象,
 * 仅用于 mapper、service 之间传递数据
 *
 * @CreateTime 2026/9/23 15:58
 */
@Data
public class OrderCompletionRateItem {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期, yyyy-MM-dd */
    private LocalDate date;
    /** 当天的订单完成率 = 当天有效订单数 / 当天订单总数, 保留 4 位小数 */
    private Double rate;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
