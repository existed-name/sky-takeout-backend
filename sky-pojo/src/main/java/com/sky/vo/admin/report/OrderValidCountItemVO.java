package com.sky.vo.admin.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每天的有效订单数据项
 *
 * @CreateTime 2026/9/19 9:44
 */
@ApiModel( description = "每天的有效订单数据项" )
@Data
public class OrderValidCountItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期, yyyy-MM-dd */
    @ApiModelProperty( value = "日期", example = "2026-09-19" )
    private LocalDate date;
    /** 当天的有效订单数量 */
    @ApiModelProperty( value = "当天的有效订单数量", example = "138" )
    private Long count;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
