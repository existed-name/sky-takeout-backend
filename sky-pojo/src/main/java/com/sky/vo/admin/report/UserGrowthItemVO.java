package com.sky.vo.admin.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户增加量数据项, 记录某天相比昨天新增多少用户
 *
 * @CreateTime 2026/9/18 10:40
 */
@ApiModel( description = "用户增加量数据项" )
@Data
public class UserGrowthItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期, yyyy-MM-dd */
    @ApiModelProperty( value = "日期", example = "2026-09-18" )
    private LocalDate date;
    /** 当天新增的用户量 */
    @ApiModelProperty( value = "当天新增的用户量", example = "68" )
    private Long growth;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
