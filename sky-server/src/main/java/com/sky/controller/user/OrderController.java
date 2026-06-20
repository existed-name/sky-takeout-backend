package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 用户端-订单管理接口
 *
 * @CreateTime 2026/6/19 9:09
 */
@Slf4j
@Validated
@Api( tags = "用户端-订单管理接口" )
@RestController( "userOrderController" )
@RequestMapping( "/user/order" )
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
     * 用户下单、生成订单、返回订单必要数据
     */
    @ApiOperation( "用户下单并返回订单必要数据" )
    @PostMapping( "/submit" )
    public Result< OrderSubmitVO > submit(
            @RequestBody @Validated // 虽然 RequestBody 自动判空，但是异常信息不太友好，还是手动 NotNull 判空
            @NotNull( message = "提交的订单信息不能为空" )
            OrdersSubmitDTO ordersSubmitDTO
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户创建订单: {}", userId, ordersSubmitDTO );
        OrderSubmitVO orderSubmitVO = orderService.createWithUserId( userId, ordersSubmitDTO );
        return Result.success( orderSubmitVO );
    }


    // ==================== 私有辅助方法 ====================

}
