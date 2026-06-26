package com.sky.service;

import com.sky.dto.order.OrderCancelDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.order.OrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;

import javax.validation.constraints.NotNull;

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
    OrderInfoVO getByIdAndUserId( Long id, Long userId );

    /**
     * 用户端-取消订单
     *
     * @param userId 用户 id
     * @param orderCancelDTO 用户取消订单的相关信息
     */
    void cancelWithUserId( Long userId, OrderCancelDTO orderCancelDTO );
}
