package com.sky.task;

import com.sky.entity.Orders;
import com.sky.enumeration.OrderStatus;
import com.sky.mapper.OrderMapper;
import com.sky.projection.order.PaymentTimeoutProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单模块定时任务, 提供以下功能
 * <pre>
 *    1. 检查、取消支付超时的订单
 *    2. 完成忘记确认的派送中订单
 * </pre>
 *
 * @CreateTime 2026/9/12 13:50
 */
@Slf4j
@Component
public class OrderTask {

    // ==================== 常量 ====================
    /** 支付超时的时间限制, 单位 min 分钟 */
    public static final long PAYMENT_TIMEOUT_MINUTE = 15;
    /** 订单取消原因: 支付超时 */
    public static final String CANCEL_REASON_PAYMENT_TIMEOUT = "订单支付超时, 已自动取消";


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderMapper orderMapper;


    // ==================== 构造器 ====================
    public OrderTask( OrderMapper orderMapper ){
        this.orderMapper = orderMapper;
    }


    // ==================== 公有方法 ====================

    /**
     * 每分钟检查是否存在支付超时的订单, 并取消这些订单
     * <p>
     *     本地测试可用更短时间间隔
     * </p>
     */
    @Transactional( rollbackFor = Exception.class )
    @Scheduled( cron = "0 */1 * * * ?" )
//    @Scheduled( cron = "*/15 * * * * ?")
    public void handlePaymentTimeoutOrders(){
        /*
            1. 直接查询超时的未支付订单、不再需要单独判断超时
              * 查询时指定订单状态、下单时间
            2. 取消这批超时订单
            3. 涉及字段
              * status / pay_status
              * order_time , id
              * cancel_reason, cancel_time
         */
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minusMinutes( PAYMENT_TIMEOUT_MINUTE );
        log.info( "正在处理支付超时的订单, 当前时间 = {}, 创建时间早于 {} 的未支付订单判定为超时", now, deadline );
        List< PaymentTimeoutProjection > paymentTimeoutOrders = orderMapper.selectPaymentTimeoutProjectionsByStatusAndDeadline( OrderStatus.PENDING_PAYMENT.getValue(), deadline );

        List< Orders > ordersToBeCanceled = paymentTimeoutOrders.stream()
                .map( order -> Orders.builder()
                        .id( order.getId() )
                        .status( OrderStatus.CANCELLED.getValue() )
                        .cancelReason( CANCEL_REASON_PAYMENT_TIMEOUT )
                        .cancelTime( now )
                        .build()
                )
                .toList();

//        orderMapper.updateBatchByIds( ordersToBeCanceled );
        // TODO: 优化为 1 次性修改整批订单, 比如 CASE WHEN
        for ( Orders order : ordersToBeCanceled ){
            orderMapper.updateById( order );
        }

        log.info( "成功取消 {} 个支付超时的订单", ordersToBeCanceled.size() );
    }



    // ==================== 私有辅助方法 ====================

}
