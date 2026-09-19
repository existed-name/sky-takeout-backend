package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.OrderConstant;
import com.sky.constant.StatusConstant;
import com.sky.convertor.OrderConverter;
import com.sky.convertor.ShoppingCartConverter;
import com.sky.dto.order.AdminOrderCancelDTO;
import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.dto.order.OrderCancelDTO;
import com.sky.dto.order.OrderStatusCount;
import com.sky.dto.order.OrdersRejectionDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.enumeration.OrderPayStatus;
import com.sky.enumeration.OrderStatus;
import com.sky.enumeration.WebSocketMessageType;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.exception.baiduMap.DistanceLimitExceededException;
import com.sky.exception.common.TimeRangeInvalidException;
import com.sky.exception.order.OrderAcceptFailedException;
import com.sky.exception.order.OrderCancelFailedException;
import com.sky.exception.order.OrderCompleteFailedException;
import com.sky.exception.order.OrderDeliverFailedException;
import com.sky.exception.order.OrderFinishedCannotCancelException;
import com.sky.exception.order.OrderDetailIncompleteException;
import com.sky.exception.order.OrderRejectFailedException;
import com.sky.exception.order.OrderStatusInvalidException;
import com.sky.exception.order.OrderCannotCancelException;
import com.sky.exception.order.OrderHasCanceledException;
import com.sky.exception.order.OrderUserNotMatchException;
import com.sky.exception.order.ReorderFailedException;
import com.sky.exception.shoppingCart.EmptyShoppingCartException;
import com.sky.external.DirectionDTO;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.projection.order.StatusProjection;
import com.sky.properties.ShopInfoProperties;
import com.sky.result.PageResult;
import com.sky.service.BaiduMapService;
import com.sky.service.OrderService;
import com.sky.utils.OrderCacheManager;
import com.sky.utils.PageUtil;
import com.sky.vo.AddressBookVO;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.order.AdminOrderInfoVO;
import com.sky.vo.order.OrderPageQueryVO;
import com.sky.vo.order.OrderHistoryItemVO;
import com.sky.vo.order.UserOrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderStatisticsVO;
import com.sky.vo.order.OrderSubmitVO;
import com.sky.vo.report.OrderCountItemVO;
import com.sky.vo.report.OrderReportVO;
import com.sky.vo.report.OrderValidCountItemVO;
import com.sky.vo.report.TurnoverReportItemVO;
import com.sky.vo.report.TurnoverReportVO;
import com.sky.vo.report.UserCountItemVO;
import com.sky.vo.report.UserGrowthItemVO;
import com.sky.vo.report.UserReportVO;
import com.sky.vo.websocket.WebSocketMessageVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderConverter orderConverter;
    private final UserMapper userMapper;
    private final AddressBookMapper addressBookMapper;
    private final OrderMapper orderMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderCacheManager orderCacheManager;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final ShoppingCartConverter shoppingCartConverter;
    private final ShopInfoProperties shopInfoProperties;
    private final BaiduMapService baiduMapService;
    private final WebSocketServer webSocketServer;
    private final ObjectMapper objectMapper;


    // ==================== 构造器 ====================
    public OrderServiceImpl( OrderConverter orderConverter, UserMapper userMapper, AddressBookMapper addressBookMapper, OrderMapper orderMapper, ShoppingCartMapper shoppingCartMapper, OrderDetailMapper orderDetailMapper, OrderCacheManager orderCacheManager, DishMapper dishMapper, SetmealMapper setmealMapper, ShoppingCartConverter shoppingCartConverter, ShopInfoProperties shopInfoProperties, BaiduMapService baiduMapService, WebSocketServer webSocketServer, ObjectMapper objectMapper ){
        this.orderConverter = orderConverter;
        this.userMapper = userMapper;
        this.addressBookMapper = addressBookMapper;
        this.orderMapper = orderMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.orderCacheManager = orderCacheManager;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.shoppingCartConverter = shoppingCartConverter;
        this.shopInfoProperties = shopInfoProperties;
        this.baiduMapService = baiduMapService;
        this.webSocketServer = webSocketServer;
        this.objectMapper = objectMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public OrderSubmitVO createWithUserId( Long userId, OrdersSubmitDTO ordersSubmitDTO ){
        /*
         * 看看组装完整的 Orders 需要哪些东西
         * dto = addressBookId + payMethod + remark + estimatedDeliveryTime 预计送达时间
         *        + deliveryStatus 配送时间选择 + tablewareNumber + tablewareStatus
         *        + packAmount + amount
         * entity = 订单信息 dto + number + status 订单状态
         *           + 支付信息 payStatus
         *           + 用户信息 userId + userName 用户名称
         *           + 地址信息 phone + address 具体地址 + consignee 收货人名称
         *           + 审计字段 orderTime + checkoutTime + cancelTime
         *           + 取消订单 cancelReason + rejectionReason +
         * 只需要复制 dto + 查用户信息 + 查地址信息 + 设置订单号、订单状态、支付状态(未支付)、下单时间
         *       => 复制 dto, user, addressBook + setNumber/Status/PayStatus/OrderTime
         *          对了还有
         * 另外还有关联关系 OrderDetail, 相当于 ShoppingCart 把 userId 换成 orderId、去掉 createTime( Orders 已经有了 )
         */
        /*
            感觉直接用 MapStruct 把 dto, user, addressBook 3 个对象组成 1 个 entity
            会有点乱，要手动映射 source、target
            而且很多字段用不到、还可能重复覆盖同名字段
            所以就只复制 dto，其他手动 set
            * 另外比较奇怪: Orders.amount 实收金额是总金额??为什么还有 packAmount 但是没有 deliveryAmount ?
         */
        Orders entity = orderConverter.toEntity( ordersSubmitDTO );

        // 设置用户信息
        entity.setUserId( userId );
        User user = userMapper.selectById( userId );
        if ( user == null ){ // 虽然但是，还是判空保险一点
            throw new NotFoundException( MessageConstant.USER_NOT_FOUND );
        }
        entity.setUserName( user.getName() );

        // 设置地址簿信息
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBookVO addressBookVO = addressBookMapper.selectVOByIdAndUserId( addressBookId, userId );
        // TODO: 对了，前端会自动选择默认地址簿???
        // id, userId 双重定位 => id 对应数据不存在 | 数据不匹配该用户
        if ( addressBookVO == null ){
            if ( addressBookMapper.countById( addressBookId ) == 0 ){
                throw new NotFoundException( MessageConstant.ADDRESS_BOOK_NOT_FOUND );
            }
            throw new AddressBookUserNotMatchException( MessageConstant.ADDRESS_BOOK_USER_NOT_MATCH );
        }

        /*
            day09 新需求: 用户下单之前检验收货地址距离商家地址是否过远
            这里拼装的 `xx省xx市xx具体地址`, 获取配置文件的商家地址,
            然后调百度地图服务类获取 2 点距离
            如果最短距离都超过距离限制、那就拒绝派送
         */
        String userCompleteAddress = String.format( "%s%s%s",
                addressBookVO.getProvinceName(),
                addressBookVO.getCityName(),
                addressBookVO.getDetail() );
        String shopAddress = shopInfoProperties.getAddress();
        DirectionDTO directionDTO = baiduMapService.listDistances( shopAddress, userCompleteAddress );
        List< Double > distances = directionDTO.getDistances();
        Double minDistance = distances.stream()
                // Double::compare
                .min( ( a, b ) -> Double.compare( a, b ) )
                // TODO: 补充存在性判断( 理论上 baiduMapService 里面响应状态正常就有 distance、否则就抛异常, 但是不能完全确保遗漏 )
                .get();
        if ( Double.compare( minDistance, shopInfoProperties.getLimitDistance() ) > 0 ){
            throw new DistanceLimitExceededException( MessageConstant.formDistanceLimitExceededMessage( shopInfoProperties.getLimitDistance() ) );
        }

        // 继续设置信息、创建订单
        entity.setConsignee( addressBookVO.getConsignee() );
        entity.setAddress( addressBookVO.getDetail() );
        entity.setPhone( addressBookVO.getPhone() );

        // 设置订单号、订单状态、支付状态(未支付)、下单时间
        LocalDateTime now = LocalDateTime.now();

        entity.setNumber( generateOrderNumber( now ) );
        entity.setStatus( OrderConstant.Status.PENDING_PAYMENT );
        entity.setPayStatus( OrderConstant.PayStatus.UN_PAID );
        entity.setOrderTime( now );

        // 主键回填
        orderMapper.insert( entity );
        Long orderId = entity.getId();
        if ( orderId == null ){
            throw new IllegalStateException( "用户下单、Mapper 插入 Order 时主键回填失败" );
        }

        // 查关联菜品、套餐数据，组装 orderDetail, 设置 orderId 外键
        List< ShoppingCartItemVO > shoppingCartItemVOList = shoppingCartMapper.selectItemVOListByUserId( userId );
        if ( shoppingCartItemVOList == null || shoppingCartItemVOList.isEmpty() ){
            throw new EmptyShoppingCartException( MessageConstant.SHOPPING_CART_IS_NULL );
        }
        List< OrderDetail > orderDetailList = orderConverter.toDetailList( shoppingCartItemVOList );
        orderDetailList.forEach( orderDetail -> orderDetail.setOrderId( orderId ) );
        orderDetailMapper.insertBatch( orderDetailList );

        /*
            * 教程在创建订单的时候就清空了用户购物车数据，
            * 我的想法是支付成功再清除, 暂时保留数据
            * AI: 教程的做法是简化版，很多真实外卖确实是下单就清（因为下单即锁定库存，购物车商品可能变价/售罄）
         */

        // 转 VO
        return orderConverter.toSubmitVO( entity );
    }

//    @Transactional( rollbackFor = Exception.class )
    @Override
    public OrderPaymentVO payWithIdAndUserId( Long id, Long userId ){
        /*
            * 用户点击支付 → 后端调微信支付 API → 后端拿到预支付标识 prepay_id 等等参数
              → 拿给前端 → 前端调微信 JSAPI 确认支付 → 后端把某个路径暴露到公网 → 收到微信通知
            * 本来想模拟支付参数传给前端，前端收到参数，然后模拟一个确认支付界面，再通知后端
            * 但是不知道该传什么参数(OrderPaymentVO 是教程自带的),就略过，直接前端调该接口 → 确认支付 → 通知后端
         */
        return new OrderPaymentVO();
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void paySuccessfullyWithIdAndUserId( Long id, Long userId ){
        // AI 提醒: 避免重复修改，已经支付的就不需要修改了
        // 检查 payStatus、status, TODO: 这里只看是不是已支付，可能还需要补充额外判断
        if ( orderMapper.countByIdAndPayStatus( id, OrderConstant.PayStatus.PAID ) == 1L ){
            return;
        }

        // 支付成功 → 修改订单信息(status,payStatus,checkoutTime) → 删除该用户所有购物车数据
        Orders build = Orders.builder()
                .id( id )
                .userId( userId )
                .status( OrderConstant.Status.TO_BE_CONFIRMED )
                .payStatus( OrderConstant.PayStatus.PAID )
                .checkoutTime( LocalDateTime.now() )
                .build();
        int row = orderMapper.updateByIdAndUserId( build );
        // id, userId 定位 → 修改行数为 0 → 数据不存在 | 数据不属于该用户
        if ( row == 0 ){
            throwIfNotExistsOrNotMatches( id );
        }

        /*
            day10
            * 用户支付成功后, websocket 服务端提醒商家界面客户端接单
            * 暂不清楚需不需要专门的 WebSocketService、先整段代码写进来
         */
        remindMerchantToAcceptOrder( id );

        shoppingCartMapper.deleteByUserId( userId );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult pageByUserIdAndQuery( Long userId, UserOrderPageQueryDTO userOrderPageQueryDTO ){
        // TODO: 如果用户端查询条件拓展，这里可能还要补充参数检验、提取检验方法
        if ( userOrderPageQueryDTO.hasStatus() && ! OrderConstant.Status.isValid( userOrderPageQueryDTO.getStatus() ) ){
            throw new OrderStatusInvalidException( MessageConstant.INVALID_ORDER_STATUS );
        }

        // 只对 1 次 SQL 查询有效
        PageUtil.validateParamsAndStartPage(
                userOrderPageQueryDTO.getPage(), userOrderPageQueryDTO.getPageSize()
        );
        // TODO: MyBatis 返回列表理论上不会 null, 但可以补充判空，难点在处理 Page.total
        List< OrderHistoryItemVO > orderHistoryItemVOList = orderMapper.selectByUserIdAndQuery( userId, userOrderPageQueryDTO );

        // 转 Page 是为了拿到 total 数据库里面总共的数据条数
        Page< OrderHistoryItemVO > orderHistoryItemVOPage = ( Page< OrderHistoryItemVO > ) orderHistoryItemVOList;
        long total = orderHistoryItemVOPage.getTotal();

        // 目标订单不存在
        if ( orderHistoryItemVOList.isEmpty() ){
            // 不可能是 user 不存在的情况, 因为用户成功登录 + ThreadLocal 拿到 userId
            return PageUtil.buildPageResult( total, orderHistoryItemVOList );
        }

        List< Long > ids = orderHistoryItemVOList.stream().map( OrderHistoryItemVO::getId ).toList();
        List< OrderDetail > allOrderDetails = orderDetailMapper.selectByOrderIds( ids );
        if ( allOrderDetails == null || allOrderDetails.isEmpty() ){
            // 理论上说，订单必须有东西才可以下单放到数据库，所以 orderDetail 为空
            // 说明 orderDetail 的逻辑外键设置错 | 之前下了空订单 | 由于 id 是从查出的对象拿到的，id 一定是有效的
            // 但是为了不影响用户体验，不该抛异常，只再后台记录日志
            log.error( "ids = {} 的订单列表，对应的 orderDetail 为空, 可能是之前下了空订单，或者 orderDetail 的逻辑外键设置错",
                    ids ); // lombok 重写 toString
            return PageUtil.buildPageResult( total, orderHistoryItemVOList );
        }

        // 分组、组装
        Map< Long, List< OrderDetail > > orderIdToDetailsMap = allOrderDetails.stream().collect( Collectors.groupingBy( OrderDetail::getOrderId ) );
        for ( OrderHistoryItemVO vo : orderHistoryItemVOList ){
            Long id = vo.getId();
            // 每个订单的 orderDetailList 不应该为空, 为空则手动 new 链表、记录日志
            List< OrderDetail > orderDetailList = orderIdToDetailsMap.getOrDefault( id,
                    Collections.emptyList() // 更省内存（返回不可变单例，不需要创建新对象）
            );
            if ( orderDetailList.isEmpty() ){
                log.error( "id = {} 的订单，对应的 orderDetail 为空, 可能是之前下了空订单，或者 orderDetail 的逻辑外键设置错", id );
            }
            vo.setOrderDetailList( orderDetailList );
        }

        // 我自定义的 VO 之前想设置 totalNumber 每个订单的商品总数，但有点多余，就封装方法直接拿列表长度了
        // TODO: 当前是前端调 VO 自带方法计算 totalNumber，后续看情况修改为字段 + 业务类设置字段
        return PageUtil.buildPageResult( total, orderHistoryItemVOList );
    }

    @Transactional( readOnly = true )
    @Override
    public UserOrderInfoVO getByIdAndUserId( Long id, Long userId ){
        // orderInfoVO = order 部分字段 + orderDetailList, 1 + 1 查询
        Orders order = orderMapper.selectByIdAndUserId( id, userId );
        if ( order == null ){
            throwIfNotExistsOrNotMatches( id );
        }

        UserOrderInfoVO orderInfoVO = orderConverter.toInfoVO( order );
        List< OrderDetail > orderDetailList = orderDetailMapper.selectByOrderId( id );
        // 这里不会空指针，因为前面判空时已经抛了异常
        orderInfoVO.setOrderDetailList( orderDetailList );
        return orderInfoVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void cancelWithUserId( Long userId, OrderCancelDTO orderCancelDTO ){
        // 前置判断: 查看订单状态，未付款/待接单(已付款) => 可以直接取消，已接单/派送中 => 不可直接取消、需要联系商家
        Long id = orderCancelDTO.getId();
        Orders orderStatus = orderMapper.selectStatusByIdAndUserId( id, userId );
        if ( orderStatus == null ){
            throwIfNotExistsOrNotMatches( id );
        }

        Integer status = orderStatus.getStatus();
        // 待付款/待接单: 可直接取消
        if ( OrderConstant.Status.PENDING_PAYMENT.equals( status ) ||
                OrderConstant.Status.TO_BE_CONFIRMED.equals( status ) ){
            String cancelReason = orderCancelDTO.getCancelReason();
            // 取消 => 修改订单状态、支付状态(如果付款则退款)、取消原因(可选)、取消时间
            Orders build = Orders.builder()
                    .id( id )
                    .status( OrderConstant.Status.CANCELLED )
                    .userId( userId )
//                    .payStatus()
                    .cancelReason( cancelReason )
                    .cancelTime( LocalDateTime.now() )
                    .build();

            // 待接单 => 已付款 => 取消订单后退款
            if ( OrderConstant.Status.TO_BE_CONFIRMED.equals( status ) ){
                // 由于并没有写商户支付功能，让前端模拟一个退款
                // TODO: 调微信支付接口退款
                build.setPayStatus( OrderConstant.PayStatus.REFUND );
            }

            // 由于最开始查询订单状态时已经判断了数据不存在/不匹配，所以这里不需要再判断
            orderMapper.updateByIdAndUserId( build );
            // 修改完成就结束了
            return;
        }

        // 其他订单状态无法直接取消, 抛异常
        handleOrderStatusIfOrderNotCanceled( id, userId, status );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void reorderByIdAndUserId( Long id, Long userId ){
        // 先排除订单不存在、数据不匹配的情况
        if ( orderMapper.countByIdAndUserId( id, userId ) == 0L ){
            throwIfNotExistsOrNotMatches( id );
        }

        /*
            * 创建订单，拿到订单 id 作为 orderDetail 逻辑外键
            * 其实我想直接调 service 的 createWithUserId
            * 不过创建订单用完整 builder 可以清晰晓得需要哪些字段
              - 然而直到这里我才发觉，`再来一单`只有 id, userId, 没有用户自己填写的字段
              - 于是推测教程是把旧订单完全复制到新订单、修改部分字段
              - 如果我来设计: 再来一单 → 选择地址 → 备注、餐具、派送时间 → 支付
              - 那不就是 再来一单 → 新建订单 + 用复制的购物车数据?
            * TODO: 那我再整理思路: 找旧的订单明细 → 复制到新的购物车数据 → 交给前端调用 createWithUserId + 加载购物车
            * TODO: 改前端/根据需求修改逻辑
         */
        List< OrderDetail > oldOrderDetailList = orderDetailMapper.selectByOrderId( id );
        if ( oldOrderDetailList == null || oldOrderDetailList.isEmpty() ){
            log.error( "orderId = {} 的订单的关联菜品/套餐数据不存在, 可能是误删或创建订单时没有设置逻辑外键", id );
            throw new ReorderFailedException( MessageConstant.ORDER_DETAIL_NOT_FOUND );
        }

        /*
            orderDetail => dishId + dishFlavor + setmealId
                - 哪个 id 存在就说明买的哪种商品
                - dish 由 dishId, dishFlavor 定位
                - 防御逻辑外键都不存在的情况, TODO: 假如确定不可能有这种情况, 可以省略该步检查
         */
        for ( OrderDetail orderDetail : oldOrderDetailList ){
            if ( ! orderDetail.isDish() && ! orderDetail.isSetmeal() ){
                log.error( "id = {}, orderId = {} 的 orderDetail 没有包含 dishId, setmealId, 可能是字段映射失败"
                        , orderDetail.getId(), id );
                throw new OrderDetailIncompleteException( MessageConstant.INCOMPLETE_ORDER_DETAIL );
            }
        }

        /*
            * 注意需要检查起售状态，停售菜品/套餐不能卖
            * 思路: 批量查询
              - 先整合该单所有 dishId, setmealId
              - 然后查出所有关联的 dish, setmeal
              - 检查 status
              - 都起售，那就可以复制 orderDetail 到新订单, 但是 amount 要改为查出来的菜品/套餐实时价格(现价)
            * 另外还有被删除(下架)的情况
         */
        List< Long > dishIdList = oldOrderDetailList.stream()
                .filter( OrderDetail::isDish )
                .map( OrderDetail::getDishId )
                .distinct() // 注意去重
                .toList();
        // 突然发现: 判断存在性、直接 count 看目标字段数量就行了、不需要查出来 + 遍历判断
        if ( dishMapper.countByIdsAndStatus(
                dishIdList, StatusConstant.DISABLE ) > 0L ){
            throw new ReorderFailedException( MessageConstant.REORDER_FAILED_BECAUSE_DISH_OUT_OF_SALE );
        }
        // TODO: 注意 id IN( .. ) 是集合，会去重，idList 也需要去重, 防止统计对不上
        if ( ( long ) dishIdList.size() > dishMapper.countByIds( dishIdList ) ){
            throw new ReorderFailedException( MessageConstant.REORDER_FAILED_BECAUSE_DISH_REMOVED );
        }

        List< Long > setmealIdList = oldOrderDetailList.stream()
                .filter( OrderDetail::isSetmeal )
                .map( OrderDetail::getSetmealId )
                .distinct()
                .toList();
        if ( setmealMapper.countByIdsAndStatus(
                setmealIdList, StatusConstant.DISABLE ) > 0L ){
            throw new ReorderFailedException( MessageConstant.REORDER_FAILED_BECAUSE_SETMEAL_OUT_OF_SALE );
        }
        if ( ( long ) setmealIdList.size() > setmealMapper.countByIds( setmealIdList ) ){
            throw new ReorderFailedException( MessageConstant.REORDER_FAILED_BECAUSE_SETMEAL_REMOVED );
        }

        /*
            * 通过起售状态检验后，就可以复制旧的 orderDetail 到新的 shoppingCart
            * 需要换新的字段: orderId/userId, name, amount, image, createTime
            * 复用字段: dishId, dishFlavor, setmealId, number
            * TODO: 为了展示最新数据(name, amount, image)我又查的 dish, setmeal, 如果确定不变那可以不查/只查个别字段
            * 复制思路:
              - MapStruct 批量转换 orderDetail → shoppingCart
              - 利用 @MappingTarget 修改 shoppingCarts, 分别把 dish / setmeal 设进去
              - 遍历 shoppingCarts、根据 dishId/setmealId 定位关联商品
            * TODO: 还要看业务需不需要设置最新 name, amount, image
            * MapStruct 不支持批量更新, 需要业务层循环单次更新
            * AI 提醒优化:
              - 我想的遍历 shoppingCarts 内部再遍历寻找 dish/setmeal
              - 但毕竟是 id 定位, 用 Map 直接就可以获取对应的商品
              - 所以可优化为外部组装商品 Map, 遍历 shoppingCarts 时直接拿 Map 数据
         */
        List< ShoppingCart > shoppingCartList = shoppingCartConverter.toEntities( oldOrderDetailList );

        List< Dish > dishList = dishMapper.selectPartsByIds( dishIdList );
        Map< Long, Dish > idToDishMap = dishList.stream()
                .collect( Collectors.toMap(
                                Dish::getId, // 指定 key
                                dish -> dish // 指定 value
                )
        );
        List< Setmeal > setmealList = setmealMapper.selectByIds( setmealIdList );
        Map< Long, Setmeal > idToSetmealMap = setmealList.stream()
                .collect(
                        Collectors.toMap(
                                Setmeal::getId, setmeal -> setmeal
                        )
                );

        LocalDateTime now = LocalDateTime.now();
        for ( ShoppingCart shoppingCart : shoppingCartList ){
            if ( shoppingCart.isDish() ){
                // dishList, setmealList 相同 id 只有 1 个对象
                Dish source = idToDishMap.get( shoppingCart.getDishId() );
                shoppingCartConverter.updateFromDish( source, shoppingCart );
            } else {
                Setmeal source = idToSetmealMap.get( shoppingCart.getSetmealId() );
                shoppingCartConverter.updateFromSetmeal( source, shoppingCart );
            }
            shoppingCart.setUserId( userId );
            shoppingCart.setCreateTime( now );
        }
        shoppingCartMapper.insertBatch( shoppingCartList );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult pageByQuery( AdminOrderPageQueryDTO orderPageQueryDTO ){
        /*
            参数基本格式检验交给 Controller、参数检验注解
            参数业务检验: 订单状态有效性, 由于是选填、只有非 null( 传了 status )才做判断
            查询条件交给 Mapper, 细节是查询排序 + 如果查完整对象则 MapStruct 转换
         */
        PageUtil.validateParamsAndStartPage( orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize() );

        Integer orderStatus = orderPageQueryDTO.getStatus();
        if ( orderStatus != null && ! OrderConstant.Status.isValid( orderStatus ) ){
            throw new OrderStatusInvalidException( MessageConstant.INVALID_ORDER_STATUS );
        }

        // 自动分页, List 实际会被 PageHelper 转成 Page, 并且查出来的 List 不可能为 null
        Page< Orders > ordersPage = ( Page< Orders > ) orderMapper.selectByQuery( orderPageQueryDTO );
        List< OrderPageQueryVO > orderPageVOList = orderConverter.toPageVOList( ordersPage.getResult() );

        // 对于未完成的订单需要展示菜品数据
        // 本来 orderStatus 为 null 是不算 Finished 的, 但是内部 Set.of 的 Set 不允许 contains 传 null, 所以需要判空
        if ( orderStatus != null && ! OrderConstant.Status.isFinished( orderStatus ) ){
            /*
                我再回看原型界面, 其实它要展示的不是 orderDetail 订单细节,
                它只需要展示 "dishName * number; setmealName * number..." 字符串
                也就是把所有菜品、套餐名字和数量拼起来
                于是我认为单条 vo 数据不需要返回 orderDetails, 而是直接返回 orderDishesStr
             */

            List< Long > orderIds = orderPageVOList.stream()
                    .map( OrderPageQueryVO::getId )
                    .toList();
            List< OrderDetail > allOrderDetails = orderDetailMapper.selectByOrderIds( orderIds );

            /*
                这里查到的是待展示 orders 对应的所有 orderDetails,
                先按 orderId 分组, 每个组内拼接自己的 orderDishesStr
                然后遍历 orderPageVOList, 每个 VO 设置 orderDishesStr
             */
            Map< Long, List< OrderDetail > > orderIdToDetailsMap = allOrderDetails.stream()
                    .collect( Collectors.groupingBy( OrderDetail::getOrderId ) );
            // TODO: 这里暂时嵌套 for, 之后可以优化在 for 外面拼接每个 order 的 orderDishes
            for ( OrderPageQueryVO pageQueryVO : orderPageVOList ){
                pageQueryVO.setOrderDishes(
                        concatOrderDishes(
                                orderIdToDetailsMap.get(
                                        pageQueryVO.getId()
                                )
                        )
                );
            }
        }

        return PageUtil.buildPageResult( ordersPage.getTotal(), orderPageVOList );
    }

    @Transactional( readOnly = true )
    @Override
    public OrderStatisticsVO statistics( ){
        Map< Integer, OrderStatusCount > statusToCountMap = orderMapper.countGroupByStatus();

        /*
        // group by 只保留数据库存在的 status, 不存在则不会包装返回、导致直接访问该 status 的 count 出现 null 而
        statusToCountMap.forEach( ( k, v ) -> {
            log.info( "k = {}, v = {}", k, v );
        } );
         */

        OrderStatisticsVO build = OrderStatisticsVO.builder()
                .toBeConfirmed( getValidStatusCountFromMap( statusToCountMap, OrderConstant.Status.TO_BE_CONFIRMED ) )
                .confirmed( getValidStatusCountFromMap( statusToCountMap, OrderConstant.Status.CONFIRMED ) )
                .deliveryInProgress( getValidStatusCountFromMap( statusToCountMap, OrderConstant.Status.DELIVERY_IN_PROGRESS ) )
                .build();
        return build;
    }

    @Transactional( readOnly = true )
    @Override
    public AdminOrderInfoVO getById( Long id ){
        /*
            思路: vo = orderEntity + orderDetailList,
            分 2 次查再组装
            由于 vo 字段接近( 等于 )实体, 我考虑的先查整个实体、交给 converter 按需转换,
            之后改字段只需要改 converter
         */
        Orders order = orderMapper.selectById( id );
        if ( order == null ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }
        // 如果 order == null, converter 后仍然返回 null
        AdminOrderInfoVO adminOrderInfoVO = orderConverter.toAdminInfoVO( order );
        List< OrderDetail > orderDetailList = orderDetailMapper.selectByOrderId( id );
        adminOrderInfoVO.setOrderDetailList( orderDetailList );
        return adminOrderInfoVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void acceptById( Long id ){
        /*
            业务考虑:
            * 不存在的订单报错提醒 → 可以直接看 Mapper.update 返回的修改行数
            * 理论上待接单的订单已经付款( OrderConstant.PayStatus.PAID ), 这里没有再多考虑支付状态
            * 已经接单的订单不能重复接单 →
              - mapper 只对待接单状态的订单进行修改, 但不能显式提醒
              - 或者显式查询该订单的状态、进行判断
            * 全部丢在 mapper 处理也是可行的, 但不够清晰, 于是选择先查状态
         */
        Integer orderStatus = orderMapper.selectStatusById( id );
        if ( orderStatus == null ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }

        // 待付款
        if ( OrderConstant.Status.PENDING_PAYMENT.equals( orderStatus ) ){
            throw new OrderAcceptFailedException( MessageConstant.ORDER_PENDING_PAYMENT );
        }

        // 已付款 & 已接单
        if ( ! OrderConstant.Status.TO_BE_CONFIRMED.equals( orderStatus ) ){
            throw new OrderAcceptFailedException( MessageConstant.ORDER_HAS_ACCEPTED );
        }

        // 之前已判空, 修改行数应该恒为 1
        int updatedRow = orderMapper.updateStatusById( id, OrderConstant.Status.CONFIRMED );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void rejectById( Long id, OrdersRejectionDTO orderRejectionDTO ){
        /*
            * 业务规则
              - 订单不存在、已接单不能拒单( 那个是取消订单 )
              * 拒绝已付款的订单需要退款
              - 只有订单处于“待接单”状态时可以执行拒单操作
              - 拒单时需要指定拒单原因 rejection_reason
              - 已完成不能拒单
            * 没考虑再对查出来的 orderStatus 判断有效性
            * 审计字段:
              - status,
              - pay_status,
              - cancel_time 用户端取消订单 + 管理端拒绝/取消订单共用
         */
        StatusProjection statusProjection = orderMapper.selectStatusProjectionById( id );
        if ( statusProjection == null ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }

        /*
              - 待付款 状态我觉得可以拒单, 虽然前端原型没有展示待付款订单, 但是语义上讲、只要没有接单那就可以拒单
        // 待付款: 拒单、无需退款
        if ( OrderConstant.Status.PENDING_PAYMENT.equals( orderStatus ) ){
            orderMapper.updateStatusById( id, OrderConstant.Status.CANCELLED );
            // PayStatus 依然为 UN_PAID
            return;
        }
         */

        // 待接单: 已付款、拒单需要退款, 同步订单状态、支付状态、拒单原因
        if ( OrderConstant.Status.TO_BE_CONFIRMED.equals( statusProjection.getStatus() ) ){
            /*
                理论上待接单的订单已经完成了支付, 但是教程还是做了支付状态判断,
                我在想除了保险、还是保险 => 未支付/已退款就不进行退款
                ( 原型也暂时没展示要拒绝待付款的订单 )
             */
            Orders build = Orders.builder()
                    .id( id )
                    .status( OrderConstant.Status.CANCELLED )
//                    .payStatus( OrderConstant.PayStatus.REFUND )
                    .rejectionReason( orderRejectionDTO.getRejectionReason() )
                    .cancelTime( LocalDateTime.now() )
                    .build();
            // 先退款、再写库, 防止写库失败导致退款失败、影响用户体验
            if ( OrderConstant.PayStatus.PAID.equals( statusProjection.getPayStatus() ) ){
                refund( id );
                build.setPayStatus( OrderConstant.PayStatus.REFUND );
            }
            orderMapper.updateById( build );
            return;
        }

        // 剩下的就是不可以拒单的: 1待付款 3已接单 4派送中 5已完成 6已取消
        throw new OrderRejectFailedException(
                MessageConstant.formOrderRejectFailedMessage(
                        OrderConstant.Status.toChineseName( statusProjection.getStatus() )
                )
        );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void cancelById( Long id, AdminOrderCancelDTO orderCancelDTO ){
        /*
            * 业务规则
              - 待接单 → 接单 / 拒单
              - 待派送、派送中 的订单 → 取消
              - 已完成 → 退单
              - 取消肯定需要检查支付状态 → 退款
            * 思路
              - 存在性检验
              - 状态检验、满足要求的才可以取消
              - 支付状态判断、付了钱就退款
            * 涉及字段: status, pay_status, cancel_reason, cancel_timed
         */
        StatusProjection statusProjection = orderMapper.selectStatusProjectionById( id );
        if ( statusProjection == null ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }

        Integer orderStatus = statusProjection.getStatus();
        if ( OrderStatus.allowCancel( orderStatus ) ){
            Orders build = Orders.builder()
                    .id( id )
                    .status( OrderStatus.CANCELLED.getValue() )
                    .cancelTime( LocalDateTime.now() )
                    .cancelReason( orderCancelDTO.getCancelReason() )
                    .build();
            Integer payStatus = statusProjection.getPayStatus();
            if ( OrderPayStatus.PAID.getValue() == payStatus ){
                refund( id );
                build.setPayStatus( OrderPayStatus.REFUND.getValue() );
            }
            orderMapper.updateById( build );
            return;
        }

        throw new OrderCancelFailedException(
                MessageConstant.formOrderCancelFailedMessage(
                        OrderStatus.toChineseName( orderStatus )
                )
        );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deliverById( Long id ){
        /*
            * 业务规则
              - 只对 `已接单, 待派送` 状态的订单进行派送
            * 涉及字段
              - status
         */
        Integer orderStatus = orderMapper.selectStatusById( id );
        if ( orderStatus == null ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }

        if ( OrderStatus.CONFIRMED.getValue() == orderStatus ){
            deliver( id );
            orderMapper.updateStatusById( id, OrderStatus.DELIVERY_IN_PROGRESS.getValue() );
        }

        throw new OrderDeliverFailedException(
                MessageConstant.formOrderDeliverFailedMessage(
                        OrderStatus.toChineseName( orderStatus )
                )
        );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void completeById( Long id ){
        /*
            * 业务规则:
              - 派送中的订单才可以确认完成
            * 涉及字段
              - status, delivery_time 送达时间
         */
        Integer orderStatus = orderMapper.selectStatusById( id );
        if ( orderStatus == null ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }

        if ( OrderStatus.DELIVERY_IN_PROGRESS.getValue() == orderStatus ){
            complete( id );
            orderMapper.updateStatusById( id, OrderStatus.COMPLETED.getValue() );
        }

        throw new OrderCompleteFailedException(
                MessageConstant.formOrderCompleteFailedMessage(
                        OrderStatus.toChineseName( orderStatus )
                )
        );
    }

    @Transactional( readOnly = true )
    @Override
    public void urgeAcceptByIdAndUserId( Long id, Long userId ){
        if ( orderMapper.countByIdAndUserId( id, userId ) == 0L ){
            throwIfNotExistsOrNotMatches( id );
        }
        // TODO: 增加订单状态判断, 待接单则催促接单、待派送则催促派送、其他状态不考虑催单
        String orderNumber = orderMapper.selectNumberById( id );
        String content = String.format( "用户提醒接单, 订单号: %s, 请及时处理", orderNumber );
        int messageType = WebSocketMessageType.USER_URGENT_REQUEST.getType();
        sendMessageToMerchant( messageType, id, content );
    }

    @Transactional( readOnly = true )
    @Override
    public TurnoverReportVO reportTurnoverByDate( LocalDate begin, LocalDate end ){
        throwIfBeginIsAfterEnd( begin, end );

        // sql 筛选时排除 end 截止日期, 所以为了能够查到 end 这一天, 就把截止时间设为 end + 1
        LocalDate endDateExclusive = end.plusDays( 1 );
        List< TurnoverReportItemVO > itemVOList = orderMapper.sumAmountByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(), begin, endDateExclusive );
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .items( itemVOList )
                .build();
        return turnoverReportVO;
    }

    @Transactional( readOnly = true )
    @Override
    public UserReportVO reportUserStatisticsByDate( LocalDate begin, LocalDate end ){
        throwIfBeginIsAfterEnd( begin, end );
        /*
            * 每天的用户增量可以一次性在数据库查到
              - 但是如果某天没有新增用户( growth = 0 )、就会忽略那一天的数据项
              - TODO: 如果业务要求后端提供每天的用户增量( 包括 0 )、就需要补充日期和 0
            * 而每天的用户总量暂时考虑循环单次查询
              - 需要拿到[ begin, end ]时间范围内各个日期去查询
              - TODO: 优化为一次性查出时间范围内每天的总用户量
         */
        List< UserCountItemVO > userCountItemVOList = new ArrayList<>();
        for(
                // 用临时变量避免修改 begin、影响方法内其他用到 begin 的地方
                LocalDate targetDate = begin;
             ! targetDate.isAfter( end );
             // 怪不得之前第 1 轮循环后卡住了, 原来是忘了加日期需要复制给原来的变量
             targetDate = targetDate.plusDays( 1 ) )
        {
            // 注意: sql 查询会排除参数所在的日期, 为了保留这一天、需要给传进去的日期参数 + 1 天
            LocalDate dateExclusive = targetDate.plusDays( 1 );
            UserCountItemVO userCountItemVO = userMapper.countAllBeforeDate( dateExclusive );
            // mapper 只查了用户量, 业务类设置对应的日期
            userCountItemVO.setDate( targetDate );
            userCountItemVOList.add( userCountItemVO );
        }

        LocalDate endExclusive = end.plusDays( 1 );
        List< UserGrowthItemVO > userGrowthItemVOList = userMapper.countGroupByDate( begin, endExclusive );

        UserReportVO userReportVO = UserReportVO.builder()
                .userCountList( userCountItemVOList )
                .userGrowthList( userGrowthItemVOList )
                .build();
        return userReportVO;
    }

    @Transactional( readOnly = true )
    @Override
    public OrderReportVO reportOrderStatisticsByDate( LocalDate begin, LocalDate end ){
        throwIfBeginIsAfterEnd( begin, end );
        /*
             1. 每天的订单量
             2. 每天的有效订单量
             3. 累计订单总量
             4. 累计有效订单量
             5. 累计订单完成率
         */
        LocalDate endExclusive = end.plusDays( 1 );
        List< OrderCountItemVO > orderCountItemVOList = orderMapper.countGroupByDate( begin, endExclusive );
        List< OrderValidCountItemVO > orderValidCountItemVOList = orderMapper.countByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(), begin, endExclusive
        );
        long totalOrderCount = orderMapper.countAll();
        long totalValidOrderCount = orderMapper.countByStatus( OrderStatus.COMPLETED.getValue() );

        // 这里保留 4 位小数, 交给前端转为百分数, 比如 0.8222 → 82.22%
        double orderCompletionRate = totalValidOrderCount * 1.0 / totalOrderCount;
        String format = String.format( "%.4f", orderCompletionRate );
        orderCompletionRate = Double.parseDouble( format );

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .orderCountList( orderCountItemVOList )
                .validOrderCountList( orderValidCountItemVOList )
                .totalOrderCount( totalOrderCount )
                .validOrderCount( totalValidOrderCount )
                .orderCompletionRate( orderCompletionRate )
                .build();
        return orderReportVO;
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 生成订单号
     * <br>
     * 格式：{业务码}{日期}{7位序列}
     * <br>
     * 示例：TB20250619000001, CQ202606201234567
     * <br>
     * 教程用的 System.currentTimeMillis 高并发可能重叠
     */
    private String generateOrderNumber( LocalDateTime now ){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( OrderConstant.OrderNumber.DATE_PATTERN );
        String formatDate = now.format( dateTimeFormatter );

        /*
            * 由于要凑齐位数，需要数已有订单数有多少位
            * 选用缓存持久化订单数
            * 并且已有订单数应该包含该次的创建订单，也就是在原基础上 + 1
            * 所以用 increment 自增 + 利用返回值
         */
        long orderCount = orderCacheManager.nextSequence( formatDate );
        long temp = orderCount;
        int digitCount = 0;
        while ( temp > 0 ){
            temp /= 10;
            ++digitCount;
        }

        int requiredDigitCount = OrderConstant.OrderNumber.SERIAL_NUMBER_DIGIT_COUNT;
        // TODO: 不过这里有隐藏问题，如果超过需要的位数，会直接拼接，导致序列号多出几位(但只是格式不整齐，不影响功能)
        String extraSequnceString = "";
        if ( digitCount < requiredDigitCount ){
            // 编译器提示把 for 循环改成 String.repeat
            extraSequnceString = "0".repeat(
                    Math.max( 0, requiredDigitCount - digitCount )
            );
        }

        StringBuilder orderNumber = new StringBuilder();
        orderNumber.append( OrderConstant.OrderNumber.BUSINESS_CODE )
                .append( formatDate )
                .append( extraSequnceString )
                .append( orderCount );
        return orderNumber.toString();
    }

    /**
     * <b>仅用于 orderMapper id, userId 双重定位失败的情况</b> => 修改行数为 0 | 查出对象为空
     * <br>
     * 此时如果 id 对应的数据不存在，则抛出{@link NotFoundException}, 否则不做判断、直接抛{@link OrderUserNotMatchException}
     *
     * @param orderId 订单 id
     */
    private void throwIfNotExistsOrNotMatches( Long orderId ){
        throwIfNotExists( orderId );
        throw new OrderUserNotMatchException( MessageConstant.ORDER_USER_NOT_MATCH );
    }

    /**
     * 如果 id 对应的订单不存在，抛出{@link NotFoundException}
     *
     * @param orderId 订单 id
     */
    private void throwIfNotExists( Long orderId ){
        if ( orderMapper.countById( orderId ) == 0 ){
            throw new NotFoundException( MessageConstant.ORDER_NOT_FOUND );
        }
    }

    /**
     * 用于{@link #cancelWithUserId}方法检验订单状态是否满足取消订单的要求，不满足要求则抛出异常
     *
     * @param id 订单 id
     * @param userId 用户 id
     * @param status 订单状态
     */
    private void handleOrderStatusIfOrderNotCanceled( Long id, Long userId, Integer status ){
        if ( status == null ){
            throw new IllegalStateException(
                    String.format( "id = %s, userId = %s 的订单状态为 null, 可能是下单时没有设置订单状态",
                            id, userId
                    )
            );
        }
        if ( ! OrderConstant.Status.isValid( status ) ){
            throw new OrderStatusInvalidException( MessageConstant.INVALID_ORDER_STATUS );
        }
        if ( OrderConstant.Status.CANCELLED.equals( status ) ){
            throw new OrderHasCanceledException( MessageConstant.ORDER_HAS_CANCELED );
        }
        if ( OrderConstant.Status.COMPLETED.equals( status ) ){
            throw new OrderFinishedCannotCancelException( MessageConstant.COMPLETED_ORDER_CANNOT_CANCEL );
        }
        if ( OrderConstant.Status.CONFIRMED.equals( status ) ||
                OrderConstant.Status.DELIVERY_IN_PROGRESS.equals( status ) ){
            throw new OrderCannotCancelException( MessageConstant.PLEASE_CONTACT_MERCHANT );
        }
    }

    /**
     * 拼接该订单的菜品、套餐信息为 1 个字符串
     *
     * @param orderDetails 同一个订单的各个菜品详情数据
     * @return 菜品/套餐字符串, 模板为<code>dishName * number; setmealName * number...</code>
     */
    private String concatOrderDishes( List< OrderDetail > orderDetails ){
        return orderDetails.stream()
                .map( orderDetail -> String.format(
                        "%s * %d; ", orderDetail.getName(), orderDetail.getNumber()
                ) )
                .collect( Collectors.joining() );
    }

    /**
     * 从 Mapper 返回的 Map 拿取某种状态的订单数量, 同时作判空处理
     * <br>
     * 由调用方保证数据非空、合法
     *
     * @param statusToCountMap 订单状态 → < 状态, 数量 >封装对象
     * @param orderStatus 目标订单状态
     * @return 该状态的订单数量
     */
    private Long getValidStatusCountFromMap( Map< Integer, OrderStatusCount > statusToCountMap, Integer orderStatus ){
        OrderStatusCount orderStatusCount = statusToCountMap.get( orderStatus );
        if ( orderStatusCount == null ){
            return 0L;
        }

        Long count = orderStatusCount.getCount();
        return count == null ? 0L : count;
    }

    /**
     * 订单退款
     *
     * @param id 订单 id, 由调用方保证合法
     */
    private void refund( Long id ){
        log.info( "id = {} 的订单正在退款", id );
    }

    /**
     * 派送订单, {@link #deliverById(Long id)}
     *
     * @param id 订单 id
     */
    private void deliver( Long id ){
        log.info( "正在派送 id = {} 的订单", id );
    }

    /**
     * 确认完成派送中的订单, {@link #completeById(Long id)}
     *
     * @param id 订单 id
     */
    private void complete( Long id ){
        log.info( "确认完成派送中的订单, 订单 id = {}", id );
    }

    /**
     * 用户支付成功后, websocket 服务端提醒商家界面客户端接单
     *
     * @param id 订单 id, 由调用方确保有效
     */
    private void remindMerchantToAcceptOrder( Long id ){
        String orderNumber = orderMapper.selectNumberById( id );
        String content = String.format( "您有一个订单待处理, 订单号: %s, 请及时处理", orderNumber );
        int messageType = WebSocketMessageType.NEW_ORDER_ALERT.getType();
        sendMessageToMerchant( messageType, id, content );
    }

    /**
     * 将传入的各参数组装为消息对象{@link WebSocketMessageVO}, json 序列化后
     * 通过{@link WebSocketServer#sendToAllClients( String message ))}向商家发送提醒消息
     *
     * @param messageType 消息类型, {@link WebSocketMessageType}
     * @param orderId 订单 id
     * @param content 消息内容, 包含了订单号
     * @throws IllegalStateException 当消息对象 json 序列化失败时抛出
     */
    private void sendMessageToMerchant( int messageType, Long orderId, String content ){
        WebSocketMessageVO webSocketMessageVO = WebSocketMessageVO.builder()
                .type( messageType )
                .orderId( orderId )
                .content( content )
                .build();
        String json = "";
        try {
            json = objectMapper.writeValueAsString( webSocketMessageVO );
        } catch ( JsonProcessingException e ){
            log.error( "向商家发送信息时, 消息对象 json 序列化失败, 消息对象 = {}", webSocketMessageVO );
            throw new IllegalStateException( e );
        }
        webSocketServer.sendToAllClients( json );
    }

    /**
     * 起始时间不能超过截止时间, 否则抛业务异常
     *
     * @param begin 起始时间
     * @param end 截止时间
     * @throws TimeRangeInvalidException 当起始时间超过截止时间时
     */
    private void throwIfBeginIsAfterEnd( LocalDate begin, LocalDate end ){
        if ( begin.isAfter( end ) ){
            /*
                begin.compareTo( end ) > 0
                我想的话也可以不抛异常, 查数据库的时候交换 2 个日期参数的位置, 或者互换数值
                不过 ai 建议显式抛异常, 除非业务允许互换数值
             */
            throw new TimeRangeInvalidException( MessageConstant.BEGIN_TIME_GREATER_THAN_END_TIME );
        }
    }


}
