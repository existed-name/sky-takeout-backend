package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.order.AdminOrderInfoVO;
import com.sky.vo.order.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController( "adminOrderController" )
@RequestMapping( "/admin/orders" )
@Api( tags = "管理端订单管理接口" )
public class OrderController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderService orderService;



    // ==================== 构造器 ====================
    public OrderController( OrderService orderService ){
        this.orderService = orderService;
    }


    // ==================== 公有方法 ====================

    /**
     * 条件分页查询订单
     */
    @GetMapping
    @ApiOperation( value = "条件分页查询订单" )
    public Result< PageResult > page(
            @NotNull( message = "订单条件分页查询参数不能为空" )
            @Valid AdminOrderPageQueryDTO orderPageQueryDTO
    ){
        log.info( "管理端条件分页查询订单, 查询参数: {}", orderPageQueryDTO );
        PageResult pageResult = orderService.pageByQuery( orderPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 统计<code>待接单、待派送、派送中</code>的订单数量
     */
    @ApiOperation( "统计待接单、待派送、派送中的订单数量" )
    @GetMapping( "/statistics" )
    public Result< OrderStatisticsVO > statistics(){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员统计待接单、待派送、派送中的订单数量", adminId );
        OrderStatisticsVO orderStatisticsVO = orderService.statistics(  );
        return Result.success( orderStatisticsVO );
    }

    /**
     * 查询订单详情
     * <p>
     *     注意: <b>不带 id 参数发送请求会落到条件分页查询</b>
     * </p>
     */
    @ApiOperation( "查询订单详情" )
    @GetMapping( "/{id}" )
    public Result< AdminOrderInfoVO > getInfo(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员查看 id = {} 的订单详情", adminId, id );
        AdminOrderInfoVO orderInfoVO = orderService.getById( id );
        return Result.success( orderInfoVO );
    }

    /**
     * 接受订单, <br>
     * 跟教程的<code>confirm</code>区分开,
     * 因为 confirm 只是确认、可能多种含义,
     * 而 accept 接受、履行相关职责
     */
    @ApiOperation( "接受订单" )
    @PutMapping( "/{id}/accept" )
    public Result< Void > accept(
        @PathVariable( name = "id", required = true )
        @Positive( message = "订单 id 必须是正整数" )
        Long id
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员确认接受 id = {} 的订单", adminId, id );
        orderService.acceptById( id );
        return Result.success(  );
    }




    // ==================== 私有辅助方法 ====================

}
