package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.order.OrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;
import com.sky.vo.order.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 支付订单，返回 prepay_id 给前端，前端调微信 API，微信收到后再推送消息给后端
     *
     * <pre>
     *   点击结账 → 后端生成订单
     *     ↓
     *   点击支付 → 调用微信支付API → 拿到 prepay_id（预支付交易会话标识）
     *     ↓
     *   后端把支付参数返回给前端
     *     ↓
     *   前端调起微信支付SDK（弹出微信收银台）
     *     ↓
     *   用户在微信里完成支付
     *     ↓
     *   微信服务器 → 异步通知后端（支付结果） ← 这里需要内网穿透，暴露到公网(比如 cploar 工具)
     *     ↓
     *   后端更新订单状态
     * </pre>
     *
     */
    @ApiOperation( "支付订单" )
    @PostMapping( "/pay/{id}" )
    public Result< OrderPaymentVO > pay(
            @PathVariable( name = "id" )
            @NotNull( message = "订单 id 不能为空" )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户正在支付 id = {} 的订单", userId, id );
        OrderPaymentVO orderPaymentVO = orderService.payWithIdAndUserId( id, userId );
        return Result.success( orderPaymentVO );
    }

    /**
     * 支付回调通知: 前端调用第三方 API 完成支付，第三方通知后端
     * <br>
     * 本来该专门放到 PayNotifyController，然后暴露到公网上，但是这里简化
     */
    @ApiOperation( "支付回调通知" )
    @PostMapping( "/pay/success/{id}" )
    public Result< Void > paySuccess(
            @PathVariable( name = "id" )
            @NotNull( message = "订单 id 不能为空" )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户成功支付 id = {} 的订单", userId, id );
        orderService.paySuccessfullyWithIdAndUserId( id, userId );
        return Result.success(  );
    }

    /**
     * 分页查询指定状态的历史订单
     */
    @ApiOperation( "分页查询指定状态的历史订单" )
    @GetMapping( "/historyOrders" )
    public Result< PageResult > pageQuery( // ?page=x&pageSize=y&status=
               @Validated UserOrderPageQueryDTO userOrderPageQueryDTO ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户查询历史订单，查询参数: {}", userId, userOrderPageQueryDTO );
        PageResult pageResult = orderService.pageByUserIdAndQuery( userId, userOrderPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 查询订单详情
     */
    @ApiOperation( "查询订单详情" )
    @GetMapping( "/orderDetail/{id}" )
    public Result< OrderInfoVO > getInfo(
            @PathVariable( name = "id" )
            @NotNull( message = "订单 id 不能为空" )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户查询 id = {} 的订单信息", userId, id );
        OrderInfoVO orderInfoVO = orderService.getByIdAndUserId( id, userId );
        return Result.success( orderInfoVO );
    }


    // ==================== 私有辅助方法 ====================

}
