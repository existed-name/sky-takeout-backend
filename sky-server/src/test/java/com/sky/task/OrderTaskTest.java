package com.sky.task;

import com.sky.entity.Orders;
import com.sky.enumeration.OrderStatus;
import com.sky.mapper.OrderMapper;
import com.sky.projection.order.PaymentTimeoutProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * OrderTask 单元测试
 *
 * @CreateTime 2026/9/13
 */
public class OrderTaskTest {

    // ==================== 常量 ====================
    /** 测试订单 id */
    private static final Long ORDER_ID = 100L;
    /** 支付超时时间限制: 15 分钟 = 900 秒 */
    private static final long TIMEOUT_SECONDS = OrderTask.PAYMENT_TIMEOUT_MINUTE * 60;
    /** 取消原因常量 */
    private static final String CANCEL_REASON = OrderTask.CANCEL_REASON_PAYMENT_TIMEOUT;


    // ==================== 实例变量 ====================
    private OrderMapper orderMapper;
    private OrderTask orderTask;


    // ==================== 初始化 ====================
    @BeforeEach
    void setUp() {
        orderMapper = mock( OrderMapper.class );
        orderTask = new OrderTask( orderMapper );
    }


    // ==================== 测试方法 ====================

    /**
     * 场景 1: 订单下单时间超过 15 分钟，应该被取消
     * Given: 存在一个待付款订单，下单时间为 16 分钟前
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 应该被调用，且传入的订单状态为已取消
     */
    @Test
    void shouldCancelOrder_whenOrderTimeExceeds15Minutes() {
        // Given: 16 分钟前的订单
        LocalDateTime sixteenMinutesAgo = LocalDateTime.now().minusMinutes( 16 );
        PaymentTimeoutProjection projection = createProjection( ORDER_ID, sixteenMinutesAgo );

        when( orderMapper.selectPaymentTimeoutProjectionsByStatus( OrderStatus.PENDING_PAYMENT.getValue() ) )
                .thenReturn( List.of( projection ) );

        // When: 执行定时任务
        orderTask.handlePaymentTimeoutOrders();

        // Then: 验证 updateById 被调用，且参数正确
        ArgumentCaptor< Orders > captor = ArgumentCaptor.forClass( Orders.class );
        verify( orderMapper ).updateById( captor.capture() );

        Orders cancelledOrder = captor.getValue();
        assertThat( cancelledOrder.getId() ).isEqualTo( ORDER_ID );
        assertThat( cancelledOrder.getStatus() ).isEqualTo( OrderStatus.CANCELLED.getValue() );
        assertThat( cancelledOrder.getCancelReason() ).isEqualTo( CANCEL_REASON );
        assertThat( cancelledOrder.getCancelTime() ).isNotNull();
    }

    /**
     * 场景 2: 订单下单时间未超过 15 分钟，不应该被取消
     * Given: 存在一个待付款订单，下单时间为 14 分钟前
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 不应该被调用
     */
    @Test
    void shouldNotCancelOrder_whenOrderTimeIsLessThan15Minutes() {
        // Given: 14 分钟前的订单
        LocalDateTime fourteenMinutesAgo = LocalDateTime.now().minusMinutes( 14 );
        PaymentTimeoutProjection projection = createProjection( ORDER_ID, fourteenMinutesAgo );

        when( orderMapper.selectPaymentTimeoutProjectionsByStatus( OrderStatus.PENDING_PAYMENT.getValue() ) )
                .thenReturn( List.of( projection ) );

        // When: 执行定时任务
        orderTask.handlePaymentTimeoutOrders();

        // Then: 验证 updateById 未被调用
        verify( orderMapper, never() ).updateById( any( Orders.class ) );
    }

    /**
     * 场景 3: 边界情况 - 订单下单时间正好等于 15 分钟，不应该被取消（严格大于）
     * Given: 存在一个待付款订单，下单时间正好为 15 分钟前
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 不应该被调用（因为判断条件是 > 15*60 秒，等于不算超时）
     */
    @Test
    void shouldNotCancelOrder_whenOrderTimeIsExactly15Minutes() {
        // Given: 正好 15 分钟前的订单（900 秒）
        LocalDateTime exactly15MinutesAgo = LocalDateTime.now().minusMinutes( 15 );
        PaymentTimeoutProjection projection = createProjection( ORDER_ID, exactly15MinutesAgo );

        when( orderMapper.selectPaymentTimeoutProjectionsByStatus( OrderStatus.PENDING_PAYMENT.getValue() ) )
                .thenReturn( List.of( projection ) );

        // When: 执行定时任务
        orderTask.handlePaymentTimeoutOrders();

        // Then: 验证 updateById 未被调用
        verify( orderMapper, never() ).updateById( any( Orders.class ) );
    }

    /**
     * 场景 4: 没有待付款订单时，updateById 不被调用
     * Given: mapper 返回空列表
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 不应该被调用
     */
    @Test
    void shouldNotCallUpdate_whenNoOrdersReturned() {
        // Given: 没有待付款订单
        when( orderMapper.selectPaymentTimeoutProjectionsByStatus( OrderStatus.PENDING_PAYMENT.getValue() ) )
                .thenReturn( Collections.emptyList() );

        // When: 执行定时任务
        orderTask.handlePaymentTimeoutOrders();

        // Then: 验证 updateById 未被调用
        verify( orderMapper, never() ).updateById( any( Orders.class ) );
    }


    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的 PaymentTimeoutProjection 对象
     *
     * @param id        订单 id
     * @param orderTime 下单时间
     * @return 投影对象
     */
    private PaymentTimeoutProjection createProjection( Long id, LocalDateTime orderTime ) {
        PaymentTimeoutProjection projection = new PaymentTimeoutProjection();
        projection.setId( id );
        projection.setStatus( OrderStatus.PENDING_PAYMENT.getValue() );
        projection.setOrderTime( orderTime );
        projection.setPayStatus( 0 ); // 未支付
        return projection;
    }
}
