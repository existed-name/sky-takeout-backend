package com.sky.service;

import com.sky.vo.workspace.DataOverviewVO;

import java.time.LocalDate;

/**
 * 管理端工作台服务
 *
 * @CreateTime 2026/9/20 9:21
 */
public interface WorkspaceService {
    /**
     * 统计指定时间范围的运营数据
     *
     * @param begin 起始时间( 包含当天 )
     * @param end 截止时间( 包含当天 )
     * @return 统计数据封装对象
     */
    DataOverviewVO getDataOverviewByDate( LocalDate begin, LocalDate end );
}
