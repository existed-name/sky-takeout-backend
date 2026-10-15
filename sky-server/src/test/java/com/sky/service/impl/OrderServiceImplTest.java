package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.converter.OrderConverter;
import com.sky.dto.admin.order.OrderRejectionDTO;
import com.sky.dto.user.order.OrderSubmitDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.enumeration.OrderPayStatus;
import com.sky.enumeration.OrderStatus;
import com.sky.exception.common.NotFoundException;
import com.sky.exception.baiduMap.DistanceLimitExceededException;
import com.sky.exception.order.accept.OrderAcceptFailedException;
import com.sky.external.DirectionDTO;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.projection.order.OrderStatusProjection;
import com.sky.properties.ShopInfoProperties;
import com.sky.service.BaiduMapService;
import com.sky.vo.user.addressBook.AddressBookVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {
    // ==================== 常量 ====================
    /** 被测方法入参：接单的订单 id；测试里用一个固定的、不存在的 id 即可 */
    private static final Long ORDER_ID = 100L;
    private static final String ORDER_REJECTION_REASON = "菜卖完了";

    /** createWithUserId 测试常量 */
    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_BOOK_ID = 1L;
    /** 配送距离限制: 5000 米 */
    private static final Double LIMIT_DISTANCE = 5000.0;
    /** 商家地址 */
    private static final String SHOP_ADDRESS = "北京市海淀区上地十街10号";


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** acceptById/rejectById 只需要 mock orderMapper */
    private OrderMapper orderMapper;

    /** createWithUserId 需要额外 mock */
    private BaiduMapService baiduMapService;
    private ShopInfoProperties shopInfoProperties;
    private UserMapper userMapper;
    private AddressBookMapper addressBookMapper;
    private OrderConverter orderConverter;
    private ShoppingCartMapper shoppingCartMapper;
    private OrderDetailMapper orderDetailMapper;

    /** 被测对象：用真实构造器 new 出来 */
    private OrderServiceImpl orderService;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    // ======== setUp ========
    /** 每个用例执行前都重建一套 mock + 被测对象，保证用例之间完全隔离 */
    @BeforeEach
    void setUp() {
        // 1. mock 出所有需要的接口
        orderMapper = mock( OrderMapper.class );
        baiduMapService = mock( BaiduMapService.class );
        userMapper = mock( UserMapper.class );
        addressBookMapper = mock( AddressBookMapper.class );
        orderConverter = mock( OrderConverter.class );
        shoppingCartMapper = mock( ShoppingCartMapper.class );
        orderDetailMapper = mock( OrderDetailMapper.class );

        // 2. new 出 ShopInfoProperties (构造器注入，immutable)
        shopInfoProperties = new ShopInfoProperties( SHOP_ADDRESS, LIMIT_DISTANCE );

        // 3. new 出被测类。OrderServiceImpl 构造器共 12 个参数，顺序是：
        //    ( orderConverter, userMapper, addressBookMapper, orderMapper,
        //      shoppingCartMapper, orderDetailMapper, orderCacheManager,
        //      dishMapper, setmealMapper, shoppingCartConverter,
        //      shopInfoProperties, baiduMapService )
        //    acceptById 只用第 4 个 orderMapper，createWithUserId 需要更多依赖。
        orderService = new OrderServiceImpl(
                orderConverter, userMapper, addressBookMapper, orderMapper,
                shoppingCartMapper, orderDetailMapper, null,
                null, null, null,
                shopInfoProperties, baiduMapService, null, null
        );
    }

    // ======== acceptById ========
    /**
     * 订单不存在：selectStatusById 返回 null => 抛 NotFoundException("订单不存在")
     * <p>
     *     顺便验证：只查了状态，绝没有去执行更新。
     * </p>
     */
    @Test
    void shouldThrowNotFoundException_whenOrderNotExists() {
        // Given 订单不存在：让 mapper 查状态返回 null
        when( orderMapper.selectStatusById( ORDER_ID ) )
               .thenReturn( null );

        // When 执行接单 + Then 断言抛出的是 NotFoundException 且 message 精确匹配
        assertThatThrownBy( () -> orderService.acceptById( ORDER_ID ) )
                .isInstanceOf( NotFoundException.class )
                .hasMessage( MessageConstant.ORDER_NOT_FOUND ); // "订单不存在"

        // 附加行为验证：确实查过一次状态；且根本不该去更新订单
        verify( orderMapper ).selectStatusById( ORDER_ID );
        verify( orderMapper, never() ).updateStatusById( anyLong(), anyInt() );
    }

    /**
     * 接受订单: 拒绝接受待付款状态的订单
     */
    @Test
    public void shouldThrowOrderAcceptFailedException_whenOrderPendingPayment(){
        // 设定: 只要查询订单状态就返回 待付款
        when( orderMapper.selectStatusById( ORDER_ID ) )
                .thenReturn( OrderStatus.PENDING_PAYMENT.getValue() );

        // 判断抛出异常是否为 OrderAcceptFailedException、对应场景的异常信息
        assertThatThrownBy( () -> orderService.acceptById( ORDER_ID ) )
                .isInstanceOf( OrderAcceptFailedException.class )
                .hasMessage( MessageConstant.ORDER_PENDING_PAYMENT );

        // 检查本次 mapper 是否执行了期望的行为
        verify( orderMapper ).selectStatusById( ORDER_ID );
        // 检查没有越过异常检查直接更新数据库 => 任何参数都算进来
        verify( orderMapper, never() )
                .updateStatusById( anyLong(), anyInt() );
    }

    /**
     * 接受订单: 已经接过了的订单不能重复接单
     */
    // @ValueSource 只接受编译期常量：枚举 getter 不行、OrderConstant.Status 已弃用、自定义常量又冗余，故直接硬编码
    @ParameterizedTest
    @ValueSource( ints = {
            3, //CONFIRMED, // OrderStatus.CONFIRMED,
            // 不能用 getter, 普通常量类又被标记弃用了, 自定义内部常量又不行, 暂时硬编码
            4, //DELIVERY_IN_PROGRESS, // OrderStatus.DELIVERY_IN_PROGRESS
            5, //COMPLETED, // OrderStatus.COMPLETED
            6, //CANCELLED, // OrderStatus.CANCELLED
    })
    public void shouldThrowOrderAcceptFailed_whenOrderHasAccepted( int status ){
        // 循环注入参数
        when( orderMapper.selectStatusById( ORDER_ID ) )
                .thenReturn( status );

        // 每个参数都应该抛 接单失败异常 + 状态不满足的异常信息
        assertThatThrownBy( () -> orderService.acceptById( ORDER_ID ) )
                // 之所以先判断异常类型、是因为先判断、先结束, 异常类型对不上肯定就不对了
                .isInstanceOf( OrderAcceptFailedException.class )
               .hasMessage( MessageConstant.ORDER_HAS_ACCEPTED );

        // 确认进行过 service 里面的查询操作
        verify( orderMapper ).selectStatusById( ORDER_ID );
        // 确认没有改动数据库
        verify( orderMapper, never() ).updateStatusById( anyLong(), anyInt() );
    }

    /**
     * 接受订单：订单状态 = 待接单 时能够正确接单
     */
    @Test
    public void shouldAcceptOrder_whenOrderIsToBeConfirmed(){
        // 设定订单状态 = 待接单
        when( orderMapper.selectStatusById( ORDER_ID ) )
                .thenReturn( OrderStatus.TO_BE_CONFIRMED.getValue() );

        /*
            只需要执行业务逻辑，无任何异常抛出，
            直接调用业务类方法就行、不需要再 assertThatThrownBy 判断异常
         */
        orderService.acceptById( ORDER_ID );

        // 进行过数据库查询、修改
        verify( orderMapper ).selectStatusById( ORDER_ID );
        // 并且修改参数固定为目标订单 id、修改后的状态
        verify( orderMapper ).updateStatusById( ORDER_ID, OrderStatus.CONFIRMED.getValue() );
        // 根据教程说的、检查无多余数据库操作
        verifyNoMoreInteractions( orderMapper );
    }

    // ======== rejectById ========
    /*
        个人初步理解
        单元测试虽然是后端自己写的、不过它更关注输入输出而不是过程
        => 用户输入什么值、后端应该输出什么结果
        => 检验过程主要就是看有没有操作数据库( 调用 mapper 哪些方法 )、有没有多余操作

        不过还会遇到其他情况、比如需要 mock service 方法内部查出来的数据
        => 所以输入实际上 = 前端传参 + service 需要查库的数据
        => 然后分情况 mock
     */
    @Test
    public void rejectById( ){
        /*
            Mock 方法的参数列表
            单参数直接用常量
            dto/对象手动 new
         */
        OrderRejectionDTO orderRejectionDTO = new OrderRejectionDTO();
        orderRejectionDTO.setRejectionReason( ORDER_REJECTION_REASON );

        /*
            Mock 数据库返回值
         */
        OrderStatusProjection orderStatusProjection = new OrderStatusProjection();
        orderStatusProjection.setStatus( OrderStatus.CONFIRMED.getValue() );
        orderStatusProjection.setPayStatus( OrderPayStatus.PAID.getValue() );

        when( orderMapper.selectStatusProjectionById( ORDER_ID ) )
                .thenReturn( orderStatusProjection );

        /*
            跑业务逻辑
            预期有异常就 assetThatThrownBy
            无就直接跑
         */
        orderService.rejectById( ORDER_ID, orderRejectionDTO );
    }

    // ======== createWithUserId ========

    /**
     * 距离超过限制: 最小距离 > limitDistance → 抛 DistanceLimitExceededException
     */
    @Test
    public void shouldThrowDistanceLimitExceededException_whenDistanceExceedsLimit(){
        // Given: Mock 前置依赖（用户、地址簿）
        mockPreconditionsForCreateWithUserId();

        // Mock 百度地图返回距离 8000m > 5000m 限制
        DirectionDTO directionDTO = new DirectionDTO();
        directionDTO.setDistances( List.of( 8000.0 ) );
        when( baiduMapService.listDistances( anyString(), anyString() ) )
                .thenReturn( directionDTO );

        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setAddressBookId( ADDRESS_BOOK_ID );

        // When + Then: 执行创建订单 → 抛 DistanceLimitExceededException
        assertThatThrownBy( () -> orderService.createWithUserId( USER_ID, dto ) )
                .isInstanceOf( DistanceLimitExceededException.class )
                .hasMessage( MessageConstant.formDistanceLimitExceededMessage( SHOP_ADDRESS, LIMIT_DISTANCE.intValue() ) );

        // Verify: 确实调用了百度地图服务
        verify( baiduMapService ).listDistances( anyString(), anyString() );
    }

    /**
     * 距离在限制内: 最小距离 < limitDistance → 不抛 DistanceLimitExceededException
     * <p>
     *     由于后续步骤（购物车、订单创建）需要更多 mock，
     *     本测试只验证"没有抛 DistanceLimitExceededException"（可能抛其他异常）。
     * </p>
     */
    @Test
    public void shouldNotThrowDistanceLimitExceededException_whenDistanceWithinLimit(){
        // Given: Mock 前置依赖
        mockPreconditionsForCreateWithUserId();

        // Mock 百度地图返回距离 3000m < 5000m 限制
        DirectionDTO directionDTO = new DirectionDTO();
        directionDTO.setDistances( List.of( 3000.0 ) );
        when( baiduMapService.listDistances( anyString(), anyString() ) )
                .thenReturn( directionDTO );

        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setAddressBookId( ADDRESS_BOOK_ID );

        // When + Then: 执行创建订单 → 不应该抛 DistanceLimitExceededException
        // 后续步骤可能抛其他异常，这里只验证距离检查通过
        try {
            orderService.createWithUserId( USER_ID, dto );
        } catch ( DistanceLimitExceededException e ){
            fail( "距离在限制内，不应抛 DistanceLimitExceededException" );
        } catch ( Throwable t ){
            // 其他异常（如 EmptyShoppingCartException）是预期的，忽略
        }

        verify( baiduMapService ).listDistances( anyString(), anyString() );
    }

    /**
     * 边界: 最小距离 == limitDistance → 不抛 DistanceLimitExceededException
     * <p>
     *     代码逻辑: {@code if ( Double.compare( minDistance, limitDistance ) > 0 )}
     *     当两者相等时，compare 返回 0，不满足 > 0，不抛异常。
     * </p>
     */
    @Test
    public void shouldNotThrowDistanceLimitExceededException_whenDistanceExactlyAtLimit(){
        // Given
        mockPreconditionsForCreateWithUserId();

        // Mock 百度地图返回距离正好 = 限制
        DirectionDTO directionDTO = new DirectionDTO();
        directionDTO.setDistances( List.of( LIMIT_DISTANCE ) ); // 5000m
        when( baiduMapService.listDistances( anyString(), anyString() ) )
                .thenReturn( directionDTO );

        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setAddressBookId( ADDRESS_BOOK_ID );

        // When + Then: 距离等于限制 → 不抛 DistanceLimitExceededException
        try {
            orderService.createWithUserId( USER_ID, dto );
        } catch ( DistanceLimitExceededException e ){
            fail( "距离等于限制，不应抛 DistanceLimitExceededException" );
        } catch ( Throwable t ){
            // 其他异常忽略
        }
    }

    /**
     * 空距离列表: distances 为空 → 抛 NoSuchElementException (来自 {@code .get()})
     * <p>
     *     代码逻辑: {@code distances.stream().min(...).get()}
     *     空列表 → min() 返回 Optional.empty() → get() 抛 NoSuchElementException。
     * </p>
     */
    @Test
    public void shouldThrowNoSuchElementException_whenDistanceListIsEmpty(){
        // Given
        mockPreconditionsForCreateWithUserId();

        // Mock 百度地图返回空距离列表
        DirectionDTO directionDTO = new DirectionDTO();
        directionDTO.setDistances( Collections.emptyList() );
        when( baiduMapService.listDistances( anyString(), anyString() ) )
                .thenReturn( directionDTO );

        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setAddressBookId( ADDRESS_BOOK_ID );

        // When + Then: 空距离列表 → 抛 NoSuchElementException
        assertThatThrownBy( () -> orderService.createWithUserId( USER_ID, dto ) )
                .isInstanceOf( NoSuchElementException.class );
    }

    // ==================== 私有辅助方法 ====================

    /**
     * Mock createWithUserId 的前置依赖（用户、地址簿、orderConverter）
     * <p>
     *     距离检查之前的步骤：
     *     1. orderConverter.toEntity(dto) → 返回一个空的 Orders 实体
     *     2. userMapper.selectById(userId) → 返回一个有效的 User
     *     3. addressBookMapper.selectVOByIdAndUserId(addressBookId, userId) → 返回一个有效的 AddressBookVO
     * </p>
     */
    private void mockPreconditionsForCreateWithUserId(){
        // Mock orderConverter.toEntity(dto)
        Orders orders = new Orders();
        when( orderConverter.toEntity( any( OrderSubmitDTO.class ) ) )
                .thenReturn( orders );

        // Mock userMapper.selectById(userId)
        User user = new User();
        user.setName( "测试用户" );
        when( userMapper.selectById( USER_ID ) )
                .thenReturn( user );

        // Mock addressBookMapper.selectVOByIdAndUserId(addressBookId, userId)
        AddressBookVO addressBookVO = new AddressBookVO();
        addressBookVO.setProvinceName( "北京市" );
        addressBookVO.setCityName( "海淀区" );
        addressBookVO.setDetail( "上地十街10号" );
        addressBookVO.setConsignee( "测试收货人" );
        addressBookVO.setPhone( "13800138000" );
        when( addressBookMapper.selectVOByIdAndUserId( ADDRESS_BOOK_ID, USER_ID ) )
                .thenReturn( addressBookVO );
    }
}
