package com.sky.service;

import com.sky.vo.admin.workspace.DataOverviewVO;
import com.sky.vo.admin.workspace.DishOverviewVO;
import com.sky.vo.admin.workspace.OrderOverviewVO;
import com.sky.vo.admin.workspace.SetmealOverviewVO;

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
    DataOverviewVO overviewDataByTimeRange( LocalDate begin, LocalDate end );

    /**
     * 订单数据总览, <b>没有筛选时间范围、某天日期</b>
     *
     * @return 各个状态的订单数量
     */
    OrderOverviewVO overviewOrders();

    /**
     * 菜品数据总览
     *
     * @return 起售、停售菜品数量
     */
    DishOverviewVO overviewDishes();

    /**
     * 套餐数据总览
     *
     * @return 起售、停售套餐数量
     */
    SetmealOverviewVO overviewSetmeals();
}
