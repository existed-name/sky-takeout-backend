package com.sky.service;

import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public interface OrderService {
    /**
     * 用户下单、创建订单、返回订单必要数据
     *
     * @param userId 用户 id
     * @param ordersSubmitDTO 用户下单的订单数据
     * @return {@link com.sky.vo.order.OrderSubmitVO}, 返回创建订单后必要的订单信息
     */
    OrderSubmitVO createWithUserId( Long userId, OrdersSubmitDTO ordersSubmitDTO );

    /**
     * 支付订单，返回 prepay_id 给前端
     *
     * @param userId 用户 id
     * @param id 订单 id
     * @return
     */
    OrderPaymentVO payWithIdAndUserId( Long id, Long userId );

    /**
     * 支付成功，修改订单信息、删除购物车数据
     */
    void paySuccessfullyWithIdAndUserId( Long id, Long userId );
}
