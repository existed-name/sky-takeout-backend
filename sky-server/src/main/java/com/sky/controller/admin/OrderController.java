package com.sky.controller.admin;

import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController( "adminOrderController" )
@RequestMapping( "/admin/orders" )
@Api( tags = "管理端订单管理接口" )
public class OrderController {
    private final OrderService orderService;

    public OrderController( OrderService orderService ){
        this.orderService = orderService;
    }
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


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




    // ==================== 私有辅助方法 ====================

}
