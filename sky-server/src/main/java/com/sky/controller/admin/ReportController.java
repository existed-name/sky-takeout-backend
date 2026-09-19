package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.report.OrderReportVO;
import com.sky.vo.report.SalesReportVO;
import com.sky.vo.report.TurnoverReportVO;
import com.sky.vo.report.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * 数据统计报告接口
 *
 * @CreateTime 2026/9/17 6:33
 */
@Validated
@Slf4j
@Api( tags = "数据统计报告接口" )
@RestController( "/adminReportController" )
@RequestMapping( "/admin/report" )
public class ReportController {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderService orderService;
    private final ReportService reportService;


    // ==================== 构造器 ====================
    public ReportController( OrderService orderService, ReportService reportService ){
        this.orderService = orderService;
        this.reportService = reportService;
    }


    // ==================== 公有方法 ====================

    /**
     * 统计指定时间范围内的营业额
     *
     * @param begin 起始时间( 包含 )
     * @param end 截止时间( 包含 )
     */
    @ApiOperation( "统计指定时间范围内的营业额" )
    @GetMapping( "/turnover-statistics" )
    public Result< TurnoverReportVO > turnoverStatistics(
            @RequestParam( name = "begin", required = true )
            /*
                iso = DateTimeFormat.ISO.DATE 会严格匹配 "yyyy-MM-dd",
                会拦截 DateTime 不能转成 Date
                这里手写格式, [ ] 里面的东西是选填
             */
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate begin,
            @RequestParam( name = "end", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate end
    ){
        Long empId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工查看时间范围 [ {} , {} ] 的营业额统计数据", empId, begin, end );
        TurnoverReportVO turnoverReportVO = reportService.reportTurnoverByDate( begin, end );
        return Result.success( turnoverReportVO );
    }

    /**
     * 统计指定时间范围的用户量
     */
    @ApiOperation( "统计指定时间范围的用户量" )
    @GetMapping( "/user-statistics" )
    public Result< UserReportVO > userStatistics(
            @RequestParam( name = "begin", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate begin,
            @RequestParam( name = "end", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate end
    ){
        Long empId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工查看时间范围 [ {} , {} ] 的用户量统计数据", empId, begin, end );
        UserReportVO userReportVO = reportService.reportUserStatisticsByDate( begin, end );
        return Result.success( userReportVO );
    }

    /**
     * 订单数据统计
     */
    @ApiOperation( "订单数据统计" )
    @GetMapping( "/order-statistics" )
    public Result< OrderReportVO > orderStatistics(
            @RequestParam( name = "begin", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate begin,
            @RequestParam( name = "end", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate end
    ){
        Long empId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工查看时间范围 [ {} , {} ] 的订单统计数据", empId, begin, end );
        OrderReportVO orderReportVO = reportService.reportOrderStatisticsByDate( begin, end );
        return Result.success( orderReportVO );
    }

    /**
     * 销量统计
     */
    @ApiOperation( "销量统计" )
    @GetMapping( "/sales-statistics" )
    public Result< SalesReportVO > salesStatistics(
            @RequestParam( name = "topN", required = false, defaultValue = "10" )
            @Positive( message = "topN 必须是正整数, 表示查询销量排名前 N 位的数据")
            Integer topN,
            @RequestParam( name = "begin", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate begin,
            @RequestParam( name = "end", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate end
    ){
        Long empId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工查看销量统计数据", empId );
        SalesReportVO salesReportVO = reportService.reportSalesStatistics( topN, begin, end );
        return Result.success( salesReportVO );
    }


    // ==================== 私有辅助方法 ====================

}
