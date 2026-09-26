package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务术语、业务名词相关常量、方法
 *
 * @CreateTime 2026/9/24 11:51
 */
@AllArgsConstructor
@Getter
public enum BusinessTerminology {

    /** 运营数据报表文件名 */
    BUSINESS_DATA_EXCEL( "运营数据报表" ),
    /*
        Excel 报表标题名称
     */
    /** 运营数据报表页 */
    BUSINESS_DATA_SHEET( "运营数据报表" ),
    /** 概览数据表页 */
    OVERVIEW_DATA_SHEET( "概览数据" ),
    /** 明细数据表页 */
    DETAIL_DATA_SHEET( "明细数据" ),

    /*
        Excel 单元格
     */
    /** 营业额 */
    TURNOVER( "营业额" ),
    /** 订单完成率 */
    ORDER_COMPLETION_RATE( "订单完成率" ),
    /** 新增用户量 */
    USER_GROWTH( "新增用户量" ),
    /** 有效订单数 */
    VALID_ORDER_COUNT( "有效订单数" ),
    /** 平均客单价 */
    UNIT_PRICE( "平均客单价" ),
    /** 日期 */
    DATE( "日期" )
    ;

    ;
    /** 该业务术语的名称 */
    private final String name;
}
