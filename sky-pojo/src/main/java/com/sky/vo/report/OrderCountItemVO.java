package com.sky.vo.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日订单数据项, 记录某天的订单数量
 *
 * @CreateTime 2026/9/19 9:41
 */
@ApiModel( description = "每日订单数据项" )
@Data
public class OrderCountItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期, yyyy-MM-dd */
    @ApiModelProperty( value = "日期", example = "2026-09-19" )
    private LocalDate date;
    /** 当天的订单数量 */
    @ApiModelProperty( value = "当天的订单数量", example = "188" )
    private Long count;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
