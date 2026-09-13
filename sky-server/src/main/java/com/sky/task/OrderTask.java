package com.sky.task;

import com.sky.entity.Orders;
import com.sky.enumeration.OrderPayStatus;
import com.sky.enumeration.OrderStatus;
import com.sky.mapper.OrderMapper;
import com.sky.projection.order.PaymentTimeoutProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     */
    @Transactional( rollbackFor = Exception.class )
    @Scheduled( cron = "0 */1 * * * ?" )
//    @Scheduled( cron = "*/15 * * * * ?") 本地测试可用更短时间间隔
    public void handlePaymentTimeoutOrders(){
        /*
            1. 查询订单状态待付款 / 支付状态未支付的订单
              * 处理到位的情况只会查出最近的订单、不会查出老旧数据
            2. 创建订单时间到现在时间间隔有无超时
              * 超时则取消
              * 可以攒够一批、一起取消、一次性修改数据库
            3. 涉及字段
              * status / pay_status
              * order_time , id
              * cancel_reason, cancel_time
         */
        List< PaymentTimeoutProjection > unpaidOrders = orderMapper.selectPaymentTimeoutProjectionsByStatus( OrderStatus.PENDING_PAYMENT.getValue() );
        LocalDateTime now = LocalDateTime.now();
        log.info( "正在处理支付超时的订单, 当前时间 = {}", now );

        /*
        ArrayList< Long > orderIdsToBeCanceled = new ArrayList<>();
        for ( Orders order : unpaidOrders ){
            LocalDateTime orderTime = order.getOrderTime();
            Duration between = Duration.between( orderTime, now );
            long seconds = between.toSeconds();
            if ( seconds > PAYMENT_TIMEOUT_MINUTE * 60 ){
                orderIdsToBeCanceled.add( order.getId() );
            }
        }
         */

        List< Orders > ordersToBeCanceled = unpaidOrders.stream()
                .filter( order -> {
                    LocalDateTime orderTime = order.getOrderTime();
                    Duration between = Duration.between( orderTime, now );
                    // 为了避免直接转分钟丢失部分时间, 转成秒来比较
                    long seconds = between.toSeconds();
                    return ( seconds > PAYMENT_TIMEOUT_MINUTE * 60 );
                } )
                .map( order -> Orders.builder()
                        .id( order.getId() )
                        .status( OrderStatus.CANCELLED.getValue() )
                        .cancelReason( CANCEL_REASON_PAYMENT_TIMEOUT )
                        .cancelTime( now )
                        .build()
                )
                .toList();

//        orderMapper.updateBatchByIds( ordersToBeCanceled );
        for ( Orders order : ordersToBeCanceled ){
            orderMapper.updateById( order );
        }

        log.info( "成功取消 {} 个支付超时的订单", ordersToBeCanceled.size() );
    }



    // ==================== 私有辅助方法 ====================

}
