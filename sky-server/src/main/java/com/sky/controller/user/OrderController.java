package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.order.OrderCancelDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.dto.user.order.UserOrderPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.user.order.UserOrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping( "/user/orders" )
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
     * 用户下单、生成订单、返回订单必要数据
     */
    @ApiOperation( "用户下单并返回订单必要数据" )
    @PostMapping
    public Result< OrderSubmitVO > submit(
            @RequestBody @Validated
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
    @PostMapping( "/{id}/payment" )
    public Result< OrderPaymentVO > pay(
            @PathVariable( name = "id", required = true )
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
    @PostMapping( "/{id}/payment/success" )
    public Result< Void > paySuccess(
            @PathVariable( name = "id", required = true )
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
    @GetMapping( "/history" )
    public Result< PageResult > pageQuery( // ?page=x&pageSize=y&status=
               @Validated UserOrderPageQueryDTO userOrderPageQueryDTO
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户查询历史订单，查询参数: {}", userId, userOrderPageQueryDTO );
        PageResult pageResult = orderService.pageByUserIdAndQuery( userId, userOrderPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 查询订单详情
     */
    @ApiOperation( "查询订单详情" )
    @GetMapping( "/{id}" )
    public Result< UserOrderInfoVO > getInfo(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户查询 id = {} 的订单信息", userId, id );
        UserOrderInfoVO orderInfoVO = orderService.getByIdAndUserId( id, userId );
        return Result.success( orderInfoVO );
    }

    /**
     * 取消订单
     */
    @ApiOperation( "取消订单" )
    @PutMapping( "/cancel" )
    public Result< Void > cancel(
            @RequestBody @Validated
            @NotNull( message = "订单取消信息不能为空" )
            OrderCancelDTO orderCancelDTO
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户取消 id = {} 的订单", userId, orderCancelDTO.getId() );
        orderService.cancelWithUserId( userId, orderCancelDTO );
        return Result.success(  );
    }

    /**
     * 再来一单
     */
    @ApiOperation( "再来一单" )
    @PostMapping( "/repetition/{id}" )
    public Result< Void > reorder(
            @PathVariable( name = "id" )
            @NotNull( message = "订单 id 不能为空" )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户再来一单: id = {}", userId, id );
        orderService.reorderByIdAndUserId( id, userId );
        return Result.success(  );
    }

    /**
     * 用户催促商家接受订单
     */
    @ApiOperation( "用户催促商家接受订单" )
    @PostMapping( "/{id}/urge-accept" )
    public Result< Void > urgeAccept(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "userId = {} 的用户催促商家接受订单, 订单 id = {}", userId, id );
        orderService.urgeAcceptByIdAndUserId( id, userId );
        return Result.success(  );
    }


    // ==================== 私有辅助方法 ====================

}
