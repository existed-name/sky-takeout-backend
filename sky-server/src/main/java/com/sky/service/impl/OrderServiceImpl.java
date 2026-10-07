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
import com.sky.dto.user.order.UserOrderPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.DeliveryStatus;
import com.sky.enumeration.OrderPayStatus;
import com.sky.enumeration.OrderStatus;
import com.sky.enumeration.PaymentMethod;
import com.sky.enumeration.TablewareStatus;
import com.sky.enumeration.WebSocketMessageType;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.exception.baiduMap.DistanceLimitExceededException;
import com.sky.exception.order.OrderAcceptFailedException;
import com.sky.exception.order.cancel.OrderCancelFailedException;
import com.sky.exception.order.OrderCompleteFailedException;
import com.sky.exception.order.OrderDeliverFailedException;
import com.sky.exception.order.OrderDetailIncompleteException;
import com.sky.exception.order.OrderPaidException;
import com.sky.exception.order.OrderRejectFailedException;
import com.sky.exception.order.OrderUserNotMatchException;
import com.sky.exception.order.ReorderFailedException;
import com.sky.exception.shoppingCart.ShoppingCartEmptyException;
import com.sky.external.DirectionDTO;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.projection.addressBook.AddressBookOrderInfoProjection;
import com.sky.projection.shoppingCart.ShoppingCartOrderDetailProjection;
import com.sky.projection.user.UserOrderInfoProjection;
import com.sky.projection.order.OrderStatusProjection;
import com.sky.properties.ShopInfoProperties;
import com.sky.result.PageResult;
import com.sky.service.BaiduMapService;
import com.sky.service.OrderService;
import com.sky.utils.AssertUtils;
import com.sky.utils.ListUtils;
import com.sky.utils.NotFoundUtil;
import com.sky.utils.OrderCacheManager;
import com.sky.utils.PageUtil;
import com.sky.vo.order.AdminOrderInfoVO;
import com.sky.vo.order.OrderPageQueryItemVO;
import com.sky.vo.user.order.UserOrderHistoryItemVO;
import com.sky.vo.user.order.UserOrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderStatisticsVO;
import com.sky.vo.order.OrderSubmitVO;
import com.sky.vo.websocket.WebSocketMessageVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            补充精简的思路
            字段业务检验
            dto 转订单实体对象
            查出用户信息、地址信息, 组装完整订单实体
            同时根据地址信息判断是否超过派送距离
            再组装订单关联的商品信息
         */
        PaymentMethod.throwIfInvalid( ordersSubmitDTO.getPayMethod() );
        DeliveryStatus.throwIfInvalid( ordersSubmitDTO.getDeliveryStatus() );
        TablewareStatus.throwIfInvalid( ordersSubmitDTO.getTablewareStatus() );

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

        /*
            设置用户信息
            由于用户名可能为空, 直接用用户名来判空必定抛不存在异常
            然后又不想多做一次数据库查询( countById )
            所以选择查询投影对象, 对投影对象进行判空
         */
        UserOrderInfoProjection user = userMapper.selectOrderInfoProjectionById( userId );
        NotFoundUtil.throwIfNotFound( user, MessageConstant.USER_NOT_FOUND );
        entity.setUserId( userId );
        entity.setUserName( user.getName() );

        /*
            设置地址簿信息
            id, userId 双重定位 => 数据不存在 | 数据不匹配该用户
         */
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBookOrderInfoProjection addressBook = addressBookMapper.selectOrderInfoProjectionByIdAndUserId( addressBookId, userId );
        if ( addressBook == null ){
            NotFoundUtil.throwIfCountZero( addressBookMapper.countById( addressBookId ), MessageConstant.ADDRESS_BOOK_NOT_FOUND );
            throw new AddressBookUserNotMatchException( MessageConstant.ADDRESS_BOOK_USER_NOT_MATCH );
        }

        /*
            day09 新需求: 用户下单之前检验收货地址距离商家地址是否过远
         */
        String userCompleteAddress = concatAndValidateCompleteAddress( addressBook );

        // 继续设置地址簿信息
        entity.setConsignee( addressBook.getConsignee() );
        // 外卖单好像通常只标了细节地址, 没标完整的地址, TODO: 根据业务需求替换为完整地址
        entity.setAddress( addressBook.getDetail() );
        entity.setPhone( addressBook.getPhone() );

        /*
            设置订单号、订单状态、支付状态(未支付)、下单时间
         */
        LocalDateTime now = LocalDateTime.now();

        entity.setNumber( generateOrderNumber( now ) );
        entity.setStatus( OrderStatus.PENDING_PAYMENT.getValue() );
        entity.setPayStatus( OrderPayStatus.UN_PAID.getValue() );
        entity.setOrderTime( now );

        /*
            主键回填
         */
        orderMapper.insert( entity );
        Long orderId = entity.getId();
        AssertUtils.assertIdBackfilled(
                orderId, String.format( "用户创建订单时主键回填失败, 订单信息 = %s", entity.toString() )
        );

        /*
            找该用户的购物车数据，组装 orderDetail, 设置 orderId 外键
         */
        List< ShoppingCartOrderDetailProjection > shoppingCarts = shoppingCartMapper.selectOrderDetailProjectionByUserId( userId );
        if ( ListUtils.isEmpty( shoppingCarts ) ){
            throw new ShoppingCartEmptyException( MessageConstant.SHOPPING_CART_EMPTY );
        }
        List< OrderDetail > orderDetailList = orderConverter.toDetailList( shoppingCarts );
        orderDetailList.forEach( orderDetail -> orderDetail.setOrderId( orderId ) );
        orderDetailMapper.insertBatch( orderDetailList );

        /*
            * 教程在创建订单的时候就清空了用户购物车数据，
            * 我的想法是支付成功再清除, 暂时保留数据
            * AI: 教程的做法是简化版，很多真实外卖确实是下单就清（因为下单即锁定库存，购物车商品可能变价/售罄）
         */

        // 最后转 VO 回显
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
        /*
            补充思路整理
            创建订单 → 支付 → 支付成功, 拆开了 3 个动作分别负责一部分
            而支付成功负责修改订单状态、审计字段
            于是先检验状态是否已经是完成支付
            状态符合要求的、就修改字段
            然后通知商家接单
            同时清理用户购物车数据，避免干扰之后买东西
         */
        // 订单状态有 status 和 pay_status, 这里只看的 pay_status, TODO: 根据需求看 status 或者 2 个字段都看
        if ( orderMapper.countByIdAndPayStatus( id, OrderPayStatus.PAID.getValue() ) == 1L ){
            throw new OrderPaidException( MessageConstant.ORDER_PAID );
        }

        // 支付成功 → 修改订单信息(status,payStatus,checkoutTime) → 删除该用户所有购物车数据
        Orders build = Orders.builder()
                .id( id )
                .userId( userId )
                .status( OrderStatus.TO_BE_CONFIRMED.getValue() )
                .payStatus( OrderPayStatus.PAID.getValue() )
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
    public PageResult< UserOrderHistoryItemVO > pageByUserIdAndQuery( Long userId, UserOrderPageQueryDTO userOrderPageQueryDTO ){
        /*
            补充思路整理
            核心是查订单主体 + 关联信息
            开头做一些查询字段的有效判断
            然后查用户的订单, 拿到逻辑外键去查关联信息, 最后组装
            需要注意的是
              - 空订单列表则提前结束
              - 由于关联信息是直接设置到订单对象里面、并没有改变最后包装的对象列表,
                查询的订单对象仍然可以正常转为 Page,
         */
        // TODO: 如果用户端查询条件拓展，这里可能还要补充参数检验、提取检验方法
        if ( userOrderPageQueryDTO.hasStatus() ){
            OrderStatus.throwIfInvalid( userOrderPageQueryDTO.getStatus() );
        }

        /*
            查订单本体信息
         */
        // 只对 1 次 SQL 查询有效
        PageUtil.validateParamsAndStartPage(
                userOrderPageQueryDTO.getPage(), userOrderPageQueryDTO.getPageSize()
        );
        List< UserOrderHistoryItemVO > orderHistoryItemVOList = orderMapper.selectByUserIdAndQuery( userId, userOrderPageQueryDTO );
        // 空列表 => 用户不存在、用户没有订单
        if ( ListUtils.isEmpty( orderHistoryItemVOList ) ){
            return PageUtil.buildPageResult( orderHistoryItemVOList );
        }

        /*
            查关联订单明细
            先拿到订单列表的 id, 然后查订单明细, 根据订单 id 分组、组装
         */
        List< Long > ids = orderHistoryItemVOList.stream()
                .map( UserOrderHistoryItemVO::getId )
                .toList();
        List< OrderDetail > allOrderDetails = orderDetailMapper.selectByOrderIds( ids );
        if ( ListUtils.isEmpty( allOrderDetails ) ){
            /*
                理论上说，订单必须有东西才可以下单放到数据库，
                所以 orderDetail 为空, 说明 orderDetail 的逻辑外键设置错 | 之前下了空订单
                由于 id 是从查出的对象拿到的，id 一定是有效的
                但是为了不影响用户体验，不该抛异常，只再后台记录日志
             */
            log.error( "用户 id = {}, ids = {} 的订单列表，对应的订单明细为空, 可能是之前下了空订单，或者 orderDetail 的逻辑外键设置错误", userId, ids );
            return PageUtil.buildPageResult( orderHistoryItemVOList );
        }

        // 分组、组装
        Map< Long, List< OrderDetail > > orderIdToDetailsMap = allOrderDetails.stream()
                .collect( Collectors.groupingBy( OrderDetail::getOrderId ) );
        for ( UserOrderHistoryItemVO vo : orderHistoryItemVOList ){
            Long id = vo.getId();
            // 每个订单的 orderDetailList 不应该为空, 为空则手动 new 链表、记录日志
            List< OrderDetail > orderDetailList = orderIdToDetailsMap.getOrDefault(
                    id,
                    Collections.emptyList() // 更省内存（返回不可变单例，不需要创建新对象）
            );
            if ( ListUtils.isEmpty( orderDetailList ) ){
                log.error( "用户 id = {}, id = {} 的订单，对应的 orderDetail 为空, 可能是之前下了空订单，或者 orderDetail 的逻辑外键设置错", userId, id );
            }
            vo.setOrderDetailList( orderDetailList );
        }

        // TODO: 目前应该是前端自己计算展示商品数量, 后续可以修改接口、把商品总数量传给前端展示
        return PageUtil.buildPageResult( orderHistoryItemVOList );
    }

    @Transactional( readOnly = true )
    @Override
    public UserOrderInfoVO getByIdAndUserId( Long id, Long userId ){
        /*
            orderInfoVO = order 部分字段 + orderDetailList, 1 + 1 查询
            由于 UserOrderInfoVO 字段也太多( 只比实体对象少 7 个左右 )
            所以这里没有查 vo 而是查完整对象、然后 converter 映射
         */
        Orders order = orderMapper.selectByIdAndUserId( id, userId );
        if ( order == null ){
            throwIfNotExistsOrNotMatches( id );
        }

        UserOrderInfoVO orderInfoVO = orderConverter.toUserOrderInfoVO( order );
        List< OrderDetail > orderDetailList = orderDetailMapper.selectByOrderId( id );
        orderInfoVO.setOrderDetailList( orderDetailList );
        return orderInfoVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void cancelByIdAndUserId( Long id, Long userId, OrderCancelDTO orderCancelDTO ){
        /*
            补充思路整理
            取消订单本质是修改订单状态
            于是查询订单状态, 判断订单可不可以取消, 同时已付款的订单需要退款

            待付款、待接单(已付款) => 可以直接取消
            已接单(待派送)、派送中 => 不可直接取消、需要联系商家
            已完成、已取消 => 不可取消
         */
        // 前置判断: 查看订单状态，TODO: 对查出来的订单状态作有效判断, 无效则说明之前写入了错误的订单状态
        Integer status = orderMapper.selectStatusByIdAndUserId( id, userId );
        if ( status == null ){
            throwIfNotExistsOrNotMatches( id );
        }

        // 待付款/待接单: 可直接取消
        if ( OrderStatus.canUserCancel( status ) ){
            String cancelReason = orderCancelDTO.getCancelReason();
            // 取消 => 修改订单状态、支付状态(如果付款则退款)、取消原因(可选)、取消时间
            Orders build = Orders.builder()
                    .id( id )
                    .status( OrderStatus.CANCELLED.getValue() )
                    .userId( userId )
//                    .payStatus()
                    .cancelReason( cancelReason )
                    .cancelTime( LocalDateTime.now() )
                    .build();

            // 待接单 => 已付款 => 取消订单后退款
            // 附: int Integer 可以直接 == 比较, Integer 自动拆箱变成 int( 除非 Integer 为空 )
            if ( OrderStatus.TO_BE_CONFIRMED.getValue() == status ){
                // 由于并没有写商户支付功能，让前端模拟一个退款
                // TODO: 调微信支付接口退款
                refund( id );
                build.setPayStatus( OrderPayStatus.REFUND.getValue() );
            }

            // 由于最开始查询订单状态时已经判断了数据不存在/不匹配，所以这里不需要再判断
            orderMapper.updateByIdAndUserId( build );
            // 修改完成就结束了
            return;
        }

        // 其他订单状态无法直接取消, 抛异常
        throwCancelFailedExceptionByStatus( status );
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
    public PageResult< OrderPageQueryItemVO > pageByQuery( AdminOrderPageQueryDTO orderPageQueryDTO ){
        /*
            补充思路整理
            对查询参数进行业务检验
              - 如果传了订单状态就检验一下, 其他的都不用检验,
              - 也不需要检验结束时间在起始时间之前
            然后查订单
            观察原型看到, 对于已完成、已取消的订单, 还需要额外展示商品、数量
         */
        Integer orderStatus = orderPageQueryDTO.getStatus();
        if ( orderStatus != null ){
            OrderStatus.throwIfInvalid( orderStatus );
        }

        PageUtil.validateParamsAndStartPage( orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize() );
        // 自动分页, List 实际会被 PageHelper 转成 Page, 并且查出来的 List 不可能为 null
        Page< Orders > ordersPage = ( Page< Orders > ) orderMapper.selectByQuery( orderPageQueryDTO );
        // 注意: 第 1 次查询的 Page 带有 total 字段, 留到最后用来包装 PageResult
        List< OrderPageQueryItemVO > orderPageItemVOs = orderConverter.toPageVOList( ordersPage.getResult() );

        // 对于未完成的订单需要展示菜品数据
        if ( ! OrderStatus.isEnd( orderStatus ) ){
            /*
                我再回看原型界面, 其实它要展示的不是 orderDetail 订单细节,
                它只需要展示 "dishName * number; setmealName * number..." 字符串
                也就是把所有菜品、套餐名字和数量拼起来
                于是我认为单条 vo 数据不需要返回 orderDetails, 而是直接返回 orderDishesStr
             */

            List< Long > orderIds = orderPageItemVOs.stream()
                    .map( OrderPageQueryItemVO::getId )
                    .toList();
            List< OrderDetail > allOrderDetails = orderDetailMapper.selectByOrderIds( orderIds );

            /*
                这里查到的是待展示 orders 对应的所有 orderDetails,
                先按 orderId 分组, 每个组内拼接自己的 orderDishesStr
                然后遍历 orderPageItemVOs, 每个 VO 设置 orderDishesStr
             */
            Map< Long, List< OrderDetail > > orderIdToDetailsMap = allOrderDetails.stream()
                    .collect( Collectors.groupingBy( OrderDetail::getOrderId ) );
            // TODO: 这里暂时嵌套 for, 之后可以优化在 for 外面拼接每个 order 的 orderDishes
            for ( OrderPageQueryItemVO pageQueryVO : orderPageItemVOs ){
                pageQueryVO.setOrderDishes(
                        concatOrderDishes(
                                orderIdToDetailsMap.get( pageQueryVO.getId() )
                        )
                );
            }
        }

        return PageUtil.buildPageResult( ordersPage.getTotal(), orderPageItemVOs );
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
                .toBeConfirmed( getValidStatusCountFromMap( statusToCountMap, OrderStatus.TO_BE_CONFIRMED.getValue() ) )
                .confirmed( getValidStatusCountFromMap( statusToCountMap, OrderStatus.CONFIRMED.getValue() ) )
                .deliveryInProgress( getValidStatusCountFromMap( statusToCountMap, OrderStatus.DELIVERY_IN_PROGRESS.getValue() ) )
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
        NotFoundUtil.throwIfNotFound( order, MessageConstant.ORDER_NOT_FOUND );
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
            补充思路整理
            接受订单 → 状态修改为已接受、待派送
            业务考虑:
              - 可以被接受的 → 只有已付款( 待接单 )的订单可以接受
              - 不可以接受 → 待付款  |  已接单、派送中、已完成、已取消
              - 不存在的订单 → 由于涉及数值比较, 这里先进行存在性检验, 没有看 Mapper 修改行数
            审计字段: 无, 只需要修改订单状态
         */
        Integer orderStatus = orderMapper.selectStatusById( id );
        NotFoundUtil.throwIfNotFound( orderStatus, MessageConstant.ORDER_NOT_FOUND );

        // 排除待付款的订单
        if ( OrderStatus.PENDING_PAYMENT.getValue() == orderStatus ){
            throw new OrderAcceptFailedException( MessageConstant.ORDER_PENDING_PAYMENT );
        }

        // 排除已经接单、派送中、已完成、已取消的订单
        if ( OrderStatus.TO_BE_CONFIRMED.getValue() != orderStatus ){
            throw new OrderAcceptFailedException( MessageConstant.ORDER_HAS_ACCEPTED );
        }

        // 剩下的就是已付款、待接单的订单
        orderMapper.updateStatusById( id, OrderStatus.CONFIRMED.getValue() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void rejectById( Long id, OrdersRejectionDTO orderRejectionDTO ){
        /*
            补充思路整理
            查出订单状态, 判断是否可以拒单, 不可以则抛异常, 可以则修改订单信息

            拒绝订单 → 主要是修改订单状态, TODO: 提示用户端订单被拒绝
            可以拒绝 → 只有“待接单”状态可以拒单
              - 并且拒绝已付款的订单需要退款
            不可以拒绝 → 其他状态的订单, 待付款 | 已接单(待派送)、派送中、已完成、已取消

            * 涉及字段, 主要是审计字段
              - cancel_reason
              - status,
              - pay_status,
              - cancel_time 用户端取消订单 + 管理端拒绝/取消订单共用
         */
        // TODO: 没考虑再对查出来的 orderStatus、payStatus 判断有效性, 需要的话可以补一个
        OrderStatusProjection orderStatusProjection = orderMapper.selectStatusProjectionById( id );
        NotFoundUtil.throwIfNotFound( orderStatusProjection, MessageConstant.ORDER_NOT_FOUND );

        /*
              - 待付款 状态我觉得可以拒单, 虽然前端原型没有展示待付款订单, 但是语义上讲、只要没有接单那就可以拒单
        // 待付款: 拒单、无需退款
        if ( OrderStatus.PENDING_PAYMENT.getValue() == orderStatus ){
            orderMapper.updateStatusById( id, OrderStatus.CANCELLED.getValue() );
            // PayStatus 依然为 UN_PAID
            return;
        }
         */

        // 待接单: 已付款、拒单需要退款, 同步订单状态、支付状态、拒单原因
        if ( OrderStatus.TO_BE_CONFIRMED.getValue() == orderStatusProjection.getStatus() ){
            /*
                理论上待接单的订单已经完成了支付, 但是教程还是做了支付状态判断,
                我在想除了保险、还是保险
             */
            Orders build = Orders.builder()
                    .id( id )
                    .status( OrderStatus.CANCELLED.getValue() )
//                    .payStatus( OrderPayStatus.REFUND )
                    .rejectionReason( orderRejectionDTO.getRejectionReason() )
                    .cancelTime( LocalDateTime.now() )
                    .build();
            // 先退款、再写库, 防止写库失败导致退款失败、影响用户体验
            if ( OrderPayStatus.PAID.getValue() == orderStatusProjection.getPayStatus() ){
                refund( id );
                build.setPayStatus( OrderPayStatus.REFUND.getValue() );
            }
            orderMapper.updateById( build );
            return;
        }

        // 剩下的就是不可以拒单的: 1待付款 3已接单 4派送中 5已完成 6已取消
        throw new OrderRejectFailedException(
                MessageConstant.formOrderRejectFailedMessage(
                        OrderStatus.toChineseName( orderStatusProjection.getStatus() )
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
            * 涉及字段: status, pay_status, cancel_reason, cancel_time
         */
        OrderStatusProjection orderStatusProjection = orderMapper.selectStatusProjectionById( id );
        NotFoundUtil.throwIfNotFound( orderStatusProjection, MessageConstant.ORDER_NOT_FOUND );

        Integer orderStatus = orderStatusProjection.getStatus();
        if ( OrderStatus.canAdminCancel( orderStatus ) ){
            Orders build = Orders.builder()
                    .id( id )
                    .status( OrderStatus.CANCELLED.getValue() )
                    .cancelTime( LocalDateTime.now() )
                    .cancelReason( orderCancelDTO.getCancelReason() )
                    .build();
            Integer payStatus = orderStatusProjection.getPayStatus();
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
        NotFoundUtil.throwIfNotFound( orderStatus, MessageConstant.ORDER_NOT_FOUND );

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
        NotFoundUtil.throwIfNotFound( orderStatus, MessageConstant.ORDER_NOT_FOUND );

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



    // ==================== 私有辅助方法 ====================

    /**
     * 用户下单之前检验收货地址距离商家地址是否过远
     *
     * @param addressBook 地址簿数据, 必须包含省、市、区、详细地址, 用于拼接完整地址
     * @return 用户的完整地址, 格式为{@code xx省xx市xx区xx地址}
     * @throws DistanceLimitExceededException 当用户收货地址距离商家地址超过限制时抛出
     */
    private String concatAndValidateCompleteAddress( AddressBookOrderInfoProjection addressBook ){
        /*
            这里拼装的 `xx省xx市xx区 + 具体地址`, 获取配置文件的商家地址,
            然后调百度地图服务类获取 2 点距离
            如果最短距离都超过距离限制、那就拒绝派送
         */
        StringBuilder addressBuilder = new StringBuilder();
        addressBuilder.append( addressBook.getProvinceName() )
                .append( addressBook.getCityName() )
                .append( addressBook.getDistrictName() )
                .append( addressBook.getDetail() );
        String userCompleteAddress = addressBuilder.toString();

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
        return userCompleteAddress;
    }

    /**
     * 生成订单号
     * <p>
     *     格式：{@code {业务码}{日期}{7位序列}}, {@code {7位序列}} = 今天的订单数( 第 N 个订单 )
     *      <br>
     *      示例：TB20250619000001, CQ202606201234567
     * </p>
     * 教程用的 System.currentTimeMillis 高并发可能重叠
     *
     * @param now 当前的时间
     * @return 订单号字符串
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
     * <p>
     * 此时如果 id 对应的数据不存在，则抛出{@link NotFoundException}, 否则不做判断、直接抛{@link OrderUserNotMatchException}
     * </p>
     *
     * @param orderId 订单 id
     */
    private void throwIfNotExistsOrNotMatches( Long orderId ){
        NotFoundUtil.throwIfCountZero( orderMapper.countById( orderId ), MessageConstant.ORDER_NOT_FOUND );
        throw new OrderUserNotMatchException( MessageConstant.ORDER_USER_NOT_MATCH );
    }

    /**
     * <b>用户取消订单时</b>, 如果订单状态不支持取消订单, 则由业务方法调用该方法,
     * 根据不同订单状态设置不同的异常信息、然后抛出业务异常
     *
     * @param status 订单状态, <b>由调用方确认非空、有效</b>, 用于映射对应的异常信息
     * @throws IllegalArgumentException 当调用方传入的订单状态无效时抛出
     * @throws OrderCancelFailedException 预期抛出的业务异常, 用于提醒用户当前订单状态不支持取消订单
     */
    private void throwCancelFailedExceptionByStatus( Integer status ){
        // 由于 switch 的 case 分支必须是常量, getValue 方法算变量, 所以这里先转枚举、再分支判断情况
        OrderStatus orderStatus = OrderStatus.toInstance( status );
        String message = switch ( orderStatus ){
            case CANCELLED -> MessageConstant.ORDER_HAS_CANCELED;
            case COMPLETED -> MessageConstant.ORDER_COMPLETED_NOT_CANCELABLE;
            // 包括 CONFIRMED, DELIVERY_IN_PROGRESS
            default -> MessageConstant.ORDER_CANCEL_FAILED_PLEASE_CONTACT_MERCHANT;
        };
        throw new OrderCancelFailedException( message );
    }

    /**
     * 拼接该订单的菜品、套餐信息为 1 个字符串
     *
     * @param orderDetails 某个订单的详情数据列表, 由调用方保证非 null
     * @return 商品数量字符串, 模板为{@code dishName * number; setmealName * number...}
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
     * @param statusToCountMap 订单状态 → {@code < 状态, 数量 >}封装对象
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
     * 订单退款, TODO: 微信商户号 + 调用微信支付 API 实现退款
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
            log.error( "向商家发送信息时, 消息对象 json 序列化失败, 订单 id = {}, 消息对象 = {}", orderId, webSocketMessageVO );
            throw new IllegalStateException( e );
        }
        webSocketServer.sendToAllClients( json );
    }



}
