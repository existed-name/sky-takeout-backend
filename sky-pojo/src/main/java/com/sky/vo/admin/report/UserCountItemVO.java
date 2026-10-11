package com.sky.vo.admin.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 总用户量数据项, 记录某个日期的所有用户数量
 *
 * @CreateTime 2026/9/18 10:37
 */
@ApiModel( description = "总用户量数据项" )
@Data
public class UserCountItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 日期, yyyy-MM-dd */
    @ApiModelProperty( value = "日期", example = "2026-09-18" )
    private LocalDate date;
    /** 截止到当天的总用户量( 包括这一天 ) */
    @ApiModelProperty( value = "截止到当天的总用户量( 包括这一天 )", example = "68" )
    private Long total;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
