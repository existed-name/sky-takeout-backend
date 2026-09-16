package com.sky.controller.test;

import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

/**
 * 用户端订单服务测试接口
 * <p>
 *     没有注册进{@link com.sky.config.WebMvcConfiguration#addInterceptors},
 *     访问时不会被拦截,只需传参就行、不用登录( 尤其是微信小程序 )方便后端测试
 * </p>
 *
 * @CreateTime 2026/9/15 9:24
 */
@Slf4j
@Profile( "dev" )
@RestController( "TestUserOrderController" )
@RequestMapping( "/test/orders" )
public class UserOrderController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderService orderService;


    // ==================== 构造器 ====================
    public UserOrderController( OrderService orderService ){
        this.orderService = orderService;
    }



    // ==================== 公有方法 ====================

    /**
     * 测试用户端支付成功服务
     */
    @ApiOperation( "测试用户端支付成功服务" )
    @PostMapping( "/{id}/pay/succeess" )
    public Result< Void > paySuccessfully(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "测试用户支付成功后修改订单状态、删除购物车数据、提醒商家接单, 订单 id = {}, 用户 id = {}", id, userId );
        orderService.paySuccessfullyWithIdAndUserId( id, userId );
        return Result.success();
    }

    /**
     * 测试用户催促商家接单
     */
    @ApiOperation( "测试用户催促商家接单" )
    @PostMapping( "/{id}/urge-accept" )
    public Result< Void > urgeAccept(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "测试用户催促商家接单, 订单 id = {}, 用户 id = {}", id, userId );
        orderService.urgeAcceptByIdAndUserId( id, userId );
        return Result.success();
    }


    // ==================== 私有辅助方法 ====================

}
