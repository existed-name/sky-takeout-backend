package com.sky.vo.admin.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 营业额数据项
 *
 * @CreateTime 2026/9/17 8:13
 */
@ApiModel( description = "营业额数据项" )
@Data
public class TurnoverReportItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期 */
    @ApiModelProperty( value = "日期", example = "2026-09-17" )
    private LocalDate date;
    /** 当天的订单营业额 */
    @ApiModelProperty( value = "当天的订单营业额", example = "8848.44" )
    private BigDecimal turnover;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
