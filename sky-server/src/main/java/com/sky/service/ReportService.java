package com.sky.service;

import com.sky.vo.report.OrderReportVO;
import com.sky.vo.report.SalesReportVO;
import com.sky.vo.report.TurnoverReportVO;
import com.sky.vo.report.UserReportVO;

import java.time.LocalDate;

/**
 * 数据统计报告服务
 *
 * @CreateTime 2026/9/19 19:00
 */
public interface ReportService {

    /**
     * 管理端统计指定时间范围内的营业额, <b>只查询已完成、没被取消的订单</b>
     *
     * @param begin 起始时间( 包含 )
     * @param end 截止时间( 包含 )
     * @return 时间范围内, 每天的营业额数据
     */
    TurnoverReportVO reportTurnoverByDate( LocalDate begin, LocalDate end );

    /**
     * 统计指定时间范围的用户量
     *
     * @param begin 起始时间( 包含 )
     * @param end 截止时间( 包含 )
     * @return 时间范围内, 每天的用户总量和用户增量
     */
    UserReportVO reportUserStatisticsByDate( LocalDate begin, LocalDate end );

    /**
     * 订单数据统计
     * <pre>
     *     1. 每天的订单量
     *     2. 每天的有效订单量
     *     3. 累计订单总量
     *     4. 累计有效订单量
     *     5. 累计订单完成率
     * </pre>
     *
     * @param begin 起始时间( 包含当天 )
     * @param end 截止时间( 包含当天 )
     * @return 订单统计数据
     */
    OrderReportVO reportOrderStatisticsByDate( LocalDate begin, LocalDate end );

    /**
     * 计算订单完成率公共逻辑, 订单完成率 = 有效订单数量 / 订单总数
     * <p>
     *     保留到小数点后 4 位, 由调用方或者前端转换为百分数<br>
     *     <b>由调用方确保参数有效, 并且有效订单数量、订单总数均为同一天或者同一时间范围</b>
     * </p>
     *
     * @param validOrderCount 有效订单数量( 已完成的订单 )
     * @param orderCount 订单总数
     * @return 订单完成率, <b>当订单量 = 0 时返回 0</b>
     */
    double calcOrderCompletionRate( long validOrderCount, long orderCount );

    /**
     * 统计时间范围内的销量, 主要记录排名前多少位的商品、销售量
     *
     * @param topN  排名前多少位
     * @param begin 起始时间( 包含当天 )
     * @param end 截止时间( 包含当天 )
     * @return 销量统计数据
     */
    SalesReportVO reportSalesStatistics( Integer topN, LocalDate begin, LocalDate end );
}
