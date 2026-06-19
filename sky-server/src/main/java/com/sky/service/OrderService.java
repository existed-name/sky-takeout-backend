package com.sky.service;

import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.vo.order.OrderSubmitVO;

public interface OrderService {
    /**
     * 用户下单、创建订单、返回订单必要数据
     *
     * @param userId 用户 id
     * @param ordersSubmitDTO 用户下单的订单数据
     * @return {@link com.sky.vo.order.OrderSubmitVO}, 返回创建订单后必要的订单信息
     */
    OrderSubmitVO createWithUserId( Long userId, OrdersSubmitDTO ordersSubmitDTO );
}
