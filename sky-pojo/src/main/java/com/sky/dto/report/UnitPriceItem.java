package com.sky.dto.report;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每天的平均客单价数据项
 *
 * @CreateTime 2026/9/23 16:40
 */
@Data
public class UnitPriceItem {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期, yyyy-MM-dd */
    private LocalDate date;
    /** 当天的平均客单价 = 当天营业额 / 当天有效订单数, 保留 2 位小数 */
    private BigDecimal unitPrice;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
