package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.order.AdminOrderCancelDTO;
import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.dto.order.OrdersRejectionDTO;
import com.sky.enumeration.OperationType;
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
import org.springframework.web.bind.annotation.RequestBody;
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
    @LogOperation( description = "接受订单", operationType = OperationType.UPDATE )
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

    /**
     * 拒绝订单,
     * <p>
     *     RESTful 设计, 请求路径
     *     <code>/orders/{id}/reject</code> 定位资源和动作,
     *     dto 传递信息, dto 不需要再带 id
     * </p>
     */
    @ApiOperation( "拒绝订单" )
    @PutMapping( "/{id}/reject" )
    @LogOperation( description = "拒绝订单", operationType = OperationType.UPDATE )
    public Result< Void > reject(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id,
            @RequestBody @Valid
            OrdersRejectionDTO orderRejectionDTO
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员拒绝 id = {} 的订单, 拒单信息 = {}", adminId, id, orderRejectionDTO );
        orderService.rejectById( id, orderRejectionDTO );
        return Result.success(  );
    }

    /**
     * 处理拒绝订单缺失 id 路径变量的问题
     * <p>
     *     问题描述: 缺失<code>{id}</code>请求路径缩写为<code>/admin/orders/reject</code>,
     *     而刚好存在接口<code>GET /admin/orders/{id}</code>, 于是把<code>reject</code>当作 id,
     *     结果请求方法不匹配
     * </p>
     */
    @ApiOperation( "处理拒绝订单缺失 id 路径变量的问题" )
    @RequestMapping( "/reject" )
    public Result< String > handleRejectOrderMissingId(){
        return Result.error( "拒绝订单 id 不能为空" );
    }

    /**
     * 取消订单( 待派送、派送中 )
     *
     * @param id 订单状态
     */
    @ApiOperation( "取消订单" )
    @PutMapping( "/{id}/cancel" )
    @LogOperation( description = "取消订单", operationType = OperationType.UPDATE )
    public Result< Void > cancel(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id,
            @RequestBody @Valid
            AdminOrderCancelDTO orderCancelDTO
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员取消 id = {} 的订单, 取消信息 = {}", adminId, id, orderCancelDTO );
        orderService.cancelById( id, orderCancelDTO );
        return Result.success(  );
    }

    /**
     * 处理取消订单时缺失 id 路径变量的情况
     */
    @ApiOperation( "处理取消订单缺失 id 路径变量的情况" )
    @RequestMapping( "/cancel" )
    public Result< String > handleCancelOrderMissingId(){
        return Result.error( "取消订单 id 不能为空" );
    }

    /**
     * 派送订单
     */
    @ApiOperation( "派送订单" )
    @PutMapping( "/{id}/deliver" )
    @LogOperation( description = "派送订单", operationType = OperationType.UPDATE )
    public Result< Void > deliver(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员派送 id = {} 的订单", adminId, id );
        orderService.deliverById( id );
        return Result.success(  );
    }

    /**
     * 处理派送订单时缺失 id 路径变量的情况
     */
    @ApiOperation( "处理派送订单缺失 id 路径变量的情况" )
    @RequestMapping( "/deliver" )
    public Result< String > handleDeliverOrderMissingId(){
        return Result.error( "派送订单的 id 不能为空" );
    }


    // ==================== 私有辅助方法 ====================

}
