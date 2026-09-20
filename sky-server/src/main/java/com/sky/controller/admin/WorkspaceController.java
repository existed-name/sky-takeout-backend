package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.workspace.DataOverviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端工作台数据统计接口
 *
 * @CreateTime 2026/9/20 8:59
 */
@Slf4j
@Validated
@Api( tags = "管理端工作台数据统计接口" )
@RestController
@RequestMapping( "/admin/workspace" )
public class WorkspaceController {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final WorkspaceService workspaceService;


    // ==================== 构造器 ====================
    public WorkspaceController( WorkspaceService workspaceService ){
        this.workspaceService = workspaceService;
    }


    // ==================== 公有方法 ====================

    /**
     * 某天的运营数据一览
     *
     * @param date 指定日期
     */
    @ApiOperation( "某天的运营数据一览" )
    @GetMapping( "/overview/data" )
    public Result< DataOverviewVO > overviewData(
            @RequestParam( name = "date", required = true )
            @DateTimeFormat( pattern = "yyyy-MM-dd[ HH:mm:ss]" )
            LocalDate date
    ){
        Long empId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员查看日期 = {} 的工作台运营数据", empId, date );
        DataOverviewVO dataOverviewVO = workspaceService.getDataOverviewByDate( date, date );
        return Result.success( dataOverviewVO );
    }


    // ==================== 私有辅助方法 ====================

}
