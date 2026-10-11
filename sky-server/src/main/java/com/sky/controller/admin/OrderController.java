package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.admin.order.AdminOrderCancelDTO;
import com.sky.dto.admin.order.AdminOrderPageQueryDTO;
import com.sky.dto.admin.order.OrderRejectionDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.admin.order.AdminOrderInfoVO;
import com.sky.vo.admin.order.OrderPageQueryItemVO;
import com.sky.vo.admin.order.OrderStatisticsVO;
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
    public Result< PageResult< OrderPageQueryItemVO > > page(
            @Valid AdminOrderPageQueryDTO orderPageQueryDTO
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员条件分页查询订单, 查询参数 = {}", adminId, orderPageQueryDTO );
        PageResult< OrderPageQueryItemVO > pageResult = orderService.pageByQuery( orderPageQueryDTO );
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
    @PutMapping( "/{id}/acceptance" )
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
     * 拒绝订单
     */
    @ApiOperation( "拒绝订单" )
    @PutMapping( "/{id}/rejection" )
    @LogOperation( description = "拒绝订单", operationType = OperationType.UPDATE )
    public Result< Void > reject(
            @PathVariable( name = "id", required = true )
            @Positive( message = "订单 id 必须是正整数" )
            Long id,
            @RequestBody @Valid
            OrderRejectionDTO orderRejectionDTO
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
     * <p>
     *     AI 给的几种方案和我的考虑
     *     <ul>
     *         <li>给路径变量加上正则表达式约束{@code @PutMapping("/{id:\\d+}")}: 我感觉不清晰、路径字符串太长</li>
     *         <li>请求路径移到最后: 也就是教程的操作, 但并不能解决问题, 而且 id 应该移到前面来定位资源</li>
     *         <li>改为请求参数: 也不符合 RESTful 语义</li>
     *         <li>兜底端点: 我这种操作, 但是会写很多重复方法</li>
     *     </ul>
     *     我的思考: 不需要兜底端点, 正常情况不会漏传路径变量, 而且路径缩写导致匹配端点失败, 问题也不大,
     *     除非匹配到了其他端点、做了错误操作; 那么最后还是正常的写就行, 除非必须要求处理漏传路径变量的问题
     * </p>
     * <p>
     * 我觉得问题本质是: 传参不带路径变量时，
     * SpringBoot没有把 null 绑定到请求路径变量上面、进而无法让 @PathVariable 之类的注解进行检验，
     * 进而导致请求路径缩写;
     * 不过 ai 说 SpringBoot 也没有这种处理机制...
     * 就不管了
     * </p>
     */
    @ApiOperation( "处理拒绝订单缺失 id 路径变量的问题" )
    @PutMapping( "/rejection" )
    public Result< String > handleRejectOrderMissingId(){
        return Result.error( "拒绝订单 id 不能为空" );
    }

    /**
     * 取消订单( 待派送、派送中 )
     *
     * @param id 订单状态
     */
    @ApiOperation( "取消订单" )
    @PutMapping( "/{id}/cancelation" )
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
    @PutMapping( "/cancelation" )
    public Result< String > handleCancelOrderMissingId(){
        return Result.error( "取消订单 id 不能为空" );
    }

    /**
     * 派送订单
     */
    @ApiOperation( "派送订单" )
    @PutMapping( "/{id}/delivery" )
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
    @PutMapping( "/delivery" )
    public Result< String > handleDeliverOrderMissingId(){
        return Result.error( "派送订单的 id 不能为空" );
    }

    /**
     * 完成订单
     */
    @ApiOperation( "完成订单" )
    @PutMapping( "/{id}/completion" )
    @LogOperation( description = "完成订单", operationType = OperationType.UPDATE )
    public Result< Void > complete(
        @PathVariable( name = "id", required = true )
        @Positive( message = "订单 id 必须是正整数" )
        Long id
    ){
        Long adminId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员完成 id = {} 的订单", adminId, id );
        orderService.completeById( id );
        return Result.success(  );
    }

    /**
     * 处理完成订单时缺失 id 路径变量的情况
     */
    @ApiOperation( "处理完成订单缺失 id 路径变量的情况" )
    @PutMapping( "/completion" )
    public Result< String > handleCompleteOrderMissingId(){
        return Result.error( "完成订单的 id 不能为空" );
    }


    // ==================== 私有辅助方法 ====================

}
