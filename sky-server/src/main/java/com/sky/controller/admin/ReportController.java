package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.report.TurnoverReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 数据统计报告接口
 *
 * @CreateTime 2026/9/17 6:33
 */
@Slf4j
@Api( tags = "数据统计报告接口" )
@RestController( "/adminReportController" )
@RequestMapping( "/admin/report" )
public class ReportController {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderService orderService;


    // ==================== 构造器 ====================
    public ReportController( OrderService orderService ){
        this.orderService = orderService;
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
    public Result< TurnoverReportVO > reportTurnoverStatistics(
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
        TurnoverReportVO turnoverReportVO = orderService.reportTurnoverByDate( begin, end );
        return Result.success( turnoverReportVO );
    }


    // ==================== 私有辅助方法 ====================

}
