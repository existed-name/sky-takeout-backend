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
//    @Scheduled( cron = "*/15 * * * * ?") // 测试使用
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

        // 注意批量更新时, where id in( .. ) 不能为空, 传入空列表会报错, 需要提前判断
        int size = paymentTimeoutOrders.size();
        if ( size == 0 ){
            log.info( "暂时没有支付超时的订单" );
            return;
        }

        List< Orders > ordersToBeCanceled = paymentTimeoutOrders.stream()
                .map( order -> Orders.builder()
                        .id( order.getId() )
                        .status( OrderStatus.CANCELLED.getValue() )
                        .cancelReason( CANCEL_REASON_PAYMENT_TIMEOUT )
                        .cancelTime( now )
                        .build()
                )
                .toList();

        orderMapper.updateBatchByIds( ordersToBeCanceled );
        log.info( "成功取消 {} 个支付超时的订单", size );
    }

    /**
     * 每天凌晨1点检查一次是否存在“派送中”的订单，如果存在则修改订单状态为“已完成”
     */
    @Transactional( rollbackFor = Exception.class )
//    @Scheduled( cron = "0 0 1 * * ?" )
    @Scheduled( cron = "*/15 * * * * ?" ) // 测试使用
    public void handleDeliveringOrders(){
        /*
            1. 查询派送中的订单
            2. 不存在则跳过、存在则批量设置状态已完成
              * 虽然总感觉不对, 理论上都该骑手送货、确认送货、同步订单状态
              * 不过没有骑手端、就只能靠店家确认( 有可能是店家管理员送货
              * 如果店家忘了确认完成订单、也就是靠定时任务处理
              * 但是如果特殊情况、实际上没有送到, 那就是业务的问题了...
            3. 涉及字段
              * status, id
              * delivery_time 送达时间 !? 那我设置 now 不炸了吗
         */
        LocalDateTime now = LocalDateTime.now();
        log.info( "正在处理派送中、忘记确认完成的订单, 当前时间 = {}", now );
        // 教程专门查询 1 个小时及以前的派送中订单, 可能是考虑 0 点 ~ 1 点之间还有人在送外卖...
        List< Long > deliveringOrderIds = orderMapper.selectIdsByStatus( OrderStatus.DELIVERY_IN_PROGRESS.getValue() );

        int size = deliveringOrderIds.size();
        if ( size == 0 ){
            log.info( "没有需要确认完成的订单" );
            return;
        }

        List< Orders > ordersToBeCompleted = deliveringOrderIds.stream()
                .map( id -> Orders.builder()
                        .id( id )
                        .status( OrderStatus.COMPLETED.getValue() )
                        .build()
                )
                .toList();
        orderMapper.updateBatchByIds( ordersToBeCompleted );
        log.info( "已确认完成 {} 个派送中的订单", size );
    }



    // ==================== 私有辅助方法 ====================

}
