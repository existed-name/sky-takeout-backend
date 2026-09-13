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
 *     覆盖两个定时任务方法:
 *     1. handlePaymentTimeoutOrders: 支付超时取消 (场景 1~4)
 *     2. handleDeliveringOrders: 派送中自动完成 (场景 5~8)
 *     测试重点: 查询参数正确性、空列表 guard、批量更新字段映射。
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

        // Then: 验证 updateBatchByIds 被调用，参数正确
        @SuppressWarnings( "unchecked" )
        ArgumentCaptor< List< Orders > > captor = ArgumentCaptor.forClass( List.class );
        verify( orderMapper ).updateBatchByIds( captor.capture() );

        List< Orders > batch = captor.getValue();
        assertThat( batch ).hasSize( 1 );
        Orders cancelled = batch.get( 0 );
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
        verify( orderMapper, never() ).updateBatchByIds( any() );
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

        // Then: updateBatchByIds 被调 1 次，批次含 2 条订单
        @SuppressWarnings( "unchecked" )
        ArgumentCaptor< List< Orders > > captor = ArgumentCaptor.forClass( List.class );
        verify( orderMapper ).updateBatchByIds( captor.capture() );

        List< Orders > batch = captor.getValue();
        assertThat( batch ).hasSize( 2 );
        assertThat( batch.get( 0 ).getId() ).isEqualTo( ORDER_ID_1 );
        assertThat( batch.get( 1 ).getId() ).isEqualTo( ORDER_ID_2 );
        // 两条都应该是取消状态
        batch.forEach( o -> {
            assertThat( o.getStatus() ).isEqualTo( OrderStatus.CANCELLED.getValue() );
            assertThat( o.getCancelReason() ).isEqualTo( CANCEL_REASON );
        } );
    }


    /**
     * 场景 5: 存在派送中订单 → 批量更新为已完成
     * Given: mapper 返回 1 条派送中订单 id
     * When: 调用 handleDeliveringOrders()
     * Then: updateBatchByIds 被调用，status=COMPLETED
     */
    @Test
    void shouldCompleteOrders_whenDeliveringOrdersExist() {
        // Given: 1 条派送中订单
        when( orderMapper.selectIdsByStatus( OrderStatus.DELIVERY_IN_PROGRESS.getValue() ) )
                .thenReturn( List.of( ORDER_ID_1 ) );

        // When
        orderTask.handleDeliveringOrders();

        // Then
        @SuppressWarnings( "unchecked" )
        ArgumentCaptor< List< Orders > > captor = ArgumentCaptor.forClass( List.class );
        verify( orderMapper ).updateBatchByIds( captor.capture() );

        List< Orders > batch = captor.getValue();
        assertThat( batch ).hasSize( 1 );
        assertThat( batch.get( 0 ).getId() ).isEqualTo( ORDER_ID_1 );
        assertThat( batch.get( 0 ).getStatus() ).isEqualTo( OrderStatus.COMPLETED.getValue() );
    }

    /**
     * 场景 6: 无派送中订单 → 不调用 updateBatchByIds
     * Given: mapper 返回空列表
     * When: 调用 handleDeliveringOrders()
     * Then: updateBatchByIds 从未被调用
     */
    @Test
    void shouldNotCallUpdate_whenNoDeliveringOrders() {
        // Given
        when( orderMapper.selectIdsByStatus( OrderStatus.DELIVERY_IN_PROGRESS.getValue() ) )
                .thenReturn( Collections.emptyList() );

        // When
        orderTask.handleDeliveringOrders();

        // Then
        verify( orderMapper, never() ).updateBatchByIds( any() );
    }

    /**
     * 场景 7: 多条派送中订单 → 全部更新为已完成
     * Given: mapper 返回 2 条派送中订单 id
     * When: 调用 handleDeliveringOrders()
     * Then: updateBatchByIds 被调 1 次，批次含 2 条，status 均为 COMPLETED
     */
    @Test
    void shouldCompleteEachOrder_whenMultipleDeliveringOrdersExist() {
        // Given: 2 条派送中订单
        when( orderMapper.selectIdsByStatus( OrderStatus.DELIVERY_IN_PROGRESS.getValue() ) )
                .thenReturn( List.of( ORDER_ID_1, ORDER_ID_2 ) );

        // When
        orderTask.handleDeliveringOrders();

        // Then
        @SuppressWarnings( "unchecked" )
        ArgumentCaptor< List< Orders > > captor = ArgumentCaptor.forClass( List.class );
        verify( orderMapper ).updateBatchByIds( captor.capture() );

        List< Orders > batch = captor.getValue();
        assertThat( batch ).hasSize( 2 );
        assertThat( batch.get( 0 ).getId() ).isEqualTo( ORDER_ID_1 );
        assertThat( batch.get( 1 ).getId() ).isEqualTo( ORDER_ID_2 );
        batch.forEach( o ->
                assertThat( o.getStatus() ).isEqualTo( OrderStatus.COMPLETED.getValue() )
        );
    }

    /**
     * 场景 8: status 传参正确 → 传给 mapper 的是 DELIVERY_IN_PROGRESS
     * Given: mapper 返回空列表
     * When: 调用 handleDeliveringOrders()
     * Then: selectIdsByStatus 参数 = DELIVERY_IN_PROGRESS
     */
    @Test
    void shouldPassCorrectStatus_whenQueryingDeliveringOrders() {
        // Given
        when( orderMapper.selectIdsByStatus( anyInt() ) ).thenReturn( Collections.emptyList() );

        // When
        orderTask.handleDeliveringOrders();

        // Then
        verify( orderMapper ).selectIdsByStatus( OrderStatus.DELIVERY_IN_PROGRESS.getValue() );
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
