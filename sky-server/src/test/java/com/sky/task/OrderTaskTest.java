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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderTask 单元测试
 * <p>
 *     源码逻辑: 直接查询超时未支付订单( SQL WHERE order_time &lt; deadline )，
 *     不再做 Java 内存过滤。测试重点: 查出来的订单是否被正确取消、deadline 传参是否正确。
 * </p>
 *
 * @CreateTime 2026/9/13
 */
public class OrderTaskTest {

    // ==================== 常量 ====================
    /** 测试订单 id */
    private static final Long ORDER_ID_1 = 100L;
    private static final Long ORDER_ID_2 = 200L;
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
     * 场景 1: 存在超时订单 → 每条都调 updateById，字段正确
     * Given: mapper 返回 1 条超时订单
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 被调用 1 次，status=CANCELLED, cancelReason 正确
     */
    @Test
    void shouldCancelOrders_whenTimeoutOrdersExist() {
        // Given: 1 条超时订单
        PaymentTimeoutProjection projection = createProjection( ORDER_ID_1 );
        when( orderMapper.selectPaymentTimeoutProjectionsByStatusAndDeadline(
                anyInt(), any( LocalDateTime.class ) ) )
                .thenReturn( List.of( projection ) );

        // When
        orderTask.handlePaymentTimeoutOrders();

        // Then: 验证 updateById 被调用，参数正确
        ArgumentCaptor< Orders > captor = ArgumentCaptor.forClass( Orders.class );
        verify( orderMapper ).updateById( captor.capture() );

        Orders cancelled = captor.getValue();
        assertThat( cancelled.getId() ).isEqualTo( ORDER_ID_1 );
        assertThat( cancelled.getStatus() ).isEqualTo( OrderStatus.CANCELLED.getValue() );
        assertThat( cancelled.getCancelReason() ).isEqualTo( CANCEL_REASON );
        assertThat( cancelled.getCancelTime() ).isNotNull();
    }

    /**
     * 场景 2: 无超时订单 → updateById 不调用
     * Given: mapper 返回空列表
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 从未被调用
     */
    @Test
    void shouldNotCallUpdate_whenNoTimeoutOrders() {
        // Given: 无超时订单
        when( orderMapper.selectPaymentTimeoutProjectionsByStatusAndDeadline(
                anyInt(), any( LocalDateTime.class ) ) )
                .thenReturn( Collections.emptyList() );

        // When
        orderTask.handlePaymentTimeoutOrders();

        // Then
        verify( orderMapper, never() ).updateById( any( Orders.class ) );
    }

    /**
     * 场景 3: deadline 传参正确 → 传给 mapper 的 deadline ≈ now - 15 分钟
     * Given: mapper 返回空列表
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: 验证第二个参数 LocalDateTime 在 now-15min 附近（±2 秒容差）
     */
    @Test
    void shouldPassCorrectDeadline_whenQueryingTimeoutOrders() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now().minusMinutes( OrderTask.PAYMENT_TIMEOUT_MINUTE );
        when( orderMapper.selectPaymentTimeoutProjectionsByStatusAndDeadline(
                anyInt(), any( LocalDateTime.class ) ) )
                .thenReturn( Collections.emptyList() );

        // When
        orderTask.handlePaymentTimeoutOrders();
        LocalDateTime afterCall = LocalDateTime.now().minusMinutes( OrderTask.PAYMENT_TIMEOUT_MINUTE );

        // Then: 捕获 deadline 参数，验证在合理范围内
        ArgumentCaptor< LocalDateTime > deadlineCaptor = ArgumentCaptor.forClass( LocalDateTime.class );
        verify( orderMapper ).selectPaymentTimeoutProjectionsByStatusAndDeadline(
                eq( OrderStatus.PENDING_PAYMENT.getValue() ), deadlineCaptor.capture() );

        LocalDateTime deadline = deadlineCaptor.getValue();
        assertThat( deadline ).isAfter( beforeCall.minusSeconds( 2 ) );
        assertThat( deadline ).isBefore( afterCall.plusSeconds( 2 ) );
    }

    /**
     * 场景 4: 多条超时订单 → 每条各调一次 updateById
     * Given: mapper 返回 2 条超时订单
     * When: 调用 handlePaymentTimeoutOrders()
     * Then: updateById 被调用 2 次，各自的 id 正确
     */
    @Test
    void shouldCancelEachOrder_whenMultipleTimeoutOrdersExist() {
        // Given: 2 条超时订单
        PaymentTimeoutProjection p1 = createProjection( ORDER_ID_1 );
        PaymentTimeoutProjection p2 = createProjection( ORDER_ID_2 );
        when( orderMapper.selectPaymentTimeoutProjectionsByStatusAndDeadline(
                anyInt(), any( LocalDateTime.class ) ) )
                .thenReturn( List.of( p1, p2 ) );

        // When
        orderTask.handlePaymentTimeoutOrders();

        // Then: updateById 被调 2 次
        ArgumentCaptor< Orders > captor = ArgumentCaptor.forClass( Orders.class );
        verify( orderMapper, times( 2 ) ).updateById( captor.capture() );

        List< Orders > cancelledOrders = captor.getAllValues();
        assertThat( cancelledOrders ).hasSize( 2 );
        assertThat( cancelledOrders.get( 0 ).getId() ).isEqualTo( ORDER_ID_1 );
        assertThat( cancelledOrders.get( 1 ).getId() ).isEqualTo( ORDER_ID_2 );
        // 两条都应该是取消状态
        cancelledOrders.forEach( o -> {
            assertThat( o.getStatus() ).isEqualTo( OrderStatus.CANCELLED.getValue() );
            assertThat( o.getCancelReason() ).isEqualTo( CANCEL_REASON );
        } );
    }


    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的 PaymentTimeoutProjection 对象
     */
    private PaymentTimeoutProjection createProjection( Long id ) {
        PaymentTimeoutProjection projection = new PaymentTimeoutProjection();
        projection.setId( id );
        projection.setStatus( OrderStatus.PENDING_PAYMENT.getValue() );
        projection.setOrderTime( LocalDateTime.now().minusMinutes( 20 ) );
        projection.setPayStatus( 0 );
        return projection;
    }
}
