package com.sky.service;

import com.sky.dto.order.AdminOrderCancelDTO;
import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.dto.order.OrderCancelDTO;
import com.sky.dto.order.OrdersRejectionDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.order.AdminOrderInfoVO;
import com.sky.vo.order.UserOrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderStatisticsVO;
import com.sky.vo.order.OrderSubmitVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public interface OrderService {
    /**
     * 用户端-用户下单、创建订单、返回订单必要数据
     *
     * @param userId 用户 id
     * @param ordersSubmitDTO 用户下单的订单数据
     * @return {@link com.sky.vo.order.OrderSubmitVO}, 返回创建订单后必要的订单信息
     */
    OrderSubmitVO createWithUserId( Long userId, OrdersSubmitDTO ordersSubmitDTO );

    /**
     * 用户端-支付订单，返回 prepay_id 给前端
     *
     * @param userId 用户 id
     * @param id 订单 id
     */
    OrderPaymentVO payWithIdAndUserId( Long id, Long userId );

    /**
     * 用户端-支付成功，修改订单信息、删除购物车数据
     *
     * @param userId 用户 id
     * @param id 订单 id
     */
    void paySuccessfullyWithIdAndUserId( Long id, Long userId );

    /**
     * 用户端-分页查询指定状态的历史订单
     *
     * @return 分页查询结果
     */
    PageResult pageByUserIdAndQuery( Long userId, UserOrderPageQueryDTO userOrderPageQueryDTO );

    /**
     * 用户端-查询订单所有信息
     *
     * @param userId 用户 id
     * @param id     订单 id
     * @return 订单详情 = order 部分展示字段 + orderDetail
     */
    UserOrderInfoVO getByIdAndUserId( Long id, Long userId );

    /**
     * 用户端-取消订单
     *
     * @param userId 用户 id
     * @param orderCancelDTO 用户取消订单的相关信息
     */
    void cancelWithUserId( Long userId, OrderCancelDTO orderCancelDTO );

    /**
     * 用户端-再来一单
     *
     * @param userId 用户 id
     * @param id     订单 id
     */
    void reorderByIdAndUserId( Long id, Long userId );

    /**
     * 管理端-条件分页查询订单
     *
     * @param orderPageQueryDTO 订单条件分页查询参数
     * @return 包装查询数据列表的分页结果
     */
    PageResult pageByQuery( AdminOrderPageQueryDTO orderPageQueryDTO );

    /**
     * 管理端统计<code>待接单、待派送、派送中</code>的订单数量
     *
     * @return <code>待接单、待派送、派送中</code>的订单数量视图对象
     */
    OrderStatisticsVO statistics(  );

    /**
     * 管理端查看订单详情
     *
     * @param id 订单 id
     * @return 订单详情 VO( 与用户端订单详情 VO 隔离 )
     */
    AdminOrderInfoVO getById(  Long id );

    /**
     * 管理端接受订单
     *
     * @param id 订单 id
     */
    void acceptById(  Long id );

    /**
     * 管理端拒绝订单
     *
     * @param id                订单 id
     * @param orderRejectionDTO 拒单信息
     */
    void rejectById( Long id, OrdersRejectionDTO orderRejectionDTO );

    /**
     * 管理端取消订单( 待派送、派送中 )
     *
     * @param id 订单 id
     */
    void cancelById( Long id, AdminOrderCancelDTO orderCancelDTO );

    /**
     * 管理端派送订单( 状态 = 待派送 )
     *
     * @param id 订单 id
     */
    void deliverById( Long id );

    /**
     * 管理端完成( 派送中的 )订单
     *
     * @param id 订单 id
     */
    void completeById( Long id );
}
