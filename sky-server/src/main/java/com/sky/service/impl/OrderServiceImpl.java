package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.OrderConstant;
import com.sky.constant.StatusConstant;
import com.sky.convertor.OrderConvertor;
import com.sky.convertor.ShoppingCartConverter;
import com.sky.dto.order.OrderCancelDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.exception.order.CompletedOrderCannotCancelException;
import com.sky.exception.order.IncompleteOrderDetailException;
import com.sky.exception.order.InvalidOrderStatusException;
import com.sky.exception.order.OrderCannotCancelException;
import com.sky.exception.order.OrderHasCanceledException;
import com.sky.exception.order.OrderUserNotMatchException;
import com.sky.exception.order.ReorderFailedException;
import com.sky.exception.shoppingCart.EmptyShoppingCartException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.OrderCacheManager;
import com.sky.utils.PageUtil;
import com.sky.vo.AddressBookVO;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.order.OrderHistoryItemVO;
import com.sky.vo.order.OrderInfoVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderConvertor orderConvertor;
    private final UserMapper userMapper;
    private final AddressBookMapper addressBookMapper;
    private final OrderMapper orderMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderCacheManager orderCacheManager;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final ShoppingCartConverter shoppingCartConverter;


    // ==================== 构造器 ====================
    public OrderServiceImpl( OrderConvertor orderConvertor, UserMapper userMapper, AddressBookMapper addressBookMapper, OrderMapper orderMapper, ShoppingCartMapper shoppingCartMapper, OrderDetailMapper orderDetailMapper, OrderCacheManager orderCacheManager, DishMapper dishMapper, SetmealMapper setmealMapper, ShoppingCartConverter shoppingCartConverter ){
        this.orderConvertor = orderConvertor;
        this.userMapper = userMapper;
        this.addressBookMapper = addressBookMapper;
        this.orderMapper = orderMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.orderCacheManager = orderCacheManager;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.shoppingCartConverter = shoppingCartConverter;
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
        Orders entity = orderConvertor.toEntity( ordersSubmitDTO );

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
        List< OrderDetail > orderDetailList = orderConvertor.toDetailList( shoppingCartItemVOList );
        orderDetailList.forEach( orderDetail -> orderDetail.setOrderId( orderId ) );
        orderDetailMapper.insertBatch( orderDetailList );

        /*
            * 教程在创建订单的时候就清空了用户购物车数据，
            * 我的想法是支付成功再清除, 暂时保留数据
            * AI: 教程的做法是简化版，很多真实外卖确实是下单就清（因为下单即锁定库存，购物车商品可能变价/售罄）
         */

        // 转 VO
        return orderConvertor.toSubmitVO( entity );
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

        shoppingCartMapper.deleteByUserId( userId );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult pageByUserIdAndQuery( Long userId, UserOrderPageQueryDTO userOrderPageQueryDTO ){
        // TODO: 如果用户端查询条件拓展，这里可能还要补充参数检验、提取检验方法
        if ( userOrderPageQueryDTO.hasStatus() && ! OrderConstant.Status.isValidStatus( userOrderPageQueryDTO.getStatus() ) ){
            throw new InvalidOrderStatusException( MessageConstant.INVALID_ORDER_STATUS );
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
    public OrderInfoVO getByIdAndUserId( Long id, Long userId ){
        // orderInfoVO = order 部分字段 + orderDetailList, 1 + 1 查询
        Orders order = orderMapper.selectByIdAndUserId( id, userId );
        if ( order == null ){
            throwIfNotExistsOrNotMatches( id );
        }

        OrderInfoVO orderInfoVO = orderConvertor.toInfoVO( order );
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
                throw new IncompleteOrderDetailException( MessageConstant.INCOMPLETE_ORDER_DETAIL );
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
              - 但需要提前给 shoppingCarts 分组(放菜品、放套餐)
              - 并且还要合并
         */
        List< Dish > dishList = dishMapper.selectPartsByIds( dishIdList );
        List< Setmeal > setmealList = setmealMapper.selectByIds( setmealIdList );
        List< ShoppingCart > shoppingCartList = shoppingCartConverter.toEntities( oldOrderDetailList );

        /*
            * 拆分、设置最新 name, amount, image
            * MapStruct 不支持批量更新, 因为不知道列表的访问规则, 需要业务层循环更新
            * 最开始想的下标遍历 dishList, shoppingCartList
            * 但是问题来了 TODO: 如何确认同一个下标的 dish, shoppingCart 是对应的
              1. 长度对应
                * shoppingCartList 长度 = oldOrderDetailList
                * 已经确保 oldOrderDetailList 要么有 dishId 有么有 setmealId
                  - => oldOrderDetailList 长度 = dishIdList + setmealIdList
                  - => shoppingCartList 长度 = dishIdList + setmealIdList
                * 已经排除了 ids 对应商品不存在的情况
                  - => dishList 长度 = dishIdList, setmealList 长度 = setmealIdList
                  - => shoppingCartList 长度 = dishList + setmealList
                * ……
                * 反正 dishShoppingCarts 长度 = dishList, setmeal 同
              2. 顺序相同
                * filter/map 会保证原顺序
                * MapStruct 批量转换 for 循环也保证原顺序
         */
        List< ShoppingCart > dishShoppingCarts = shoppingCartList.stream()
                .filter( ShoppingCart::isDish ).toList();
        for ( int i = 0; i < dishList.size(); i++ ){
            Dish dish = dishList.get( i );
            ShoppingCart dishShoppingCart = dishShoppingCarts.get( i );
            shoppingCartConverter.updateFromDish( dish, dishShoppingCart );
        }

        List< ShoppingCart > setmealShoppingCarts = shoppingCartList.stream()
                .filter( ShoppingCart::isSetmeal ).toList();
        for ( int i = 0; i < setmealList.size(); i++ ){
            Setmeal setmeal = setmealList.get( i );
            ShoppingCart setmealShoppingCart = setmealShoppingCarts.get( i );
            shoppingCartConverter.updateFromSetmeal( setmeal, setmealShoppingCart );
        }

        // 整合、设置 createTime, userId
        List< ShoppingCart > completeShoppingCarts = new ArrayList<  >( dishShoppingCarts );
        completeShoppingCarts.addAll( setmealShoppingCarts );
        LocalDateTime now = LocalDateTime.now();
        completeShoppingCarts.forEach(
                shoppingCart -> {
                    shoppingCart.setUserId( userId );
                    shoppingCart.setCreateTime( now );
                }
        );
        shoppingCartMapper.insertBatch( completeShoppingCarts );
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
        if ( ! OrderConstant.Status.isValidStatus( status ) ){
            throw new InvalidOrderStatusException( MessageConstant.INVALID_ORDER_STATUS );
        }
        if ( OrderConstant.Status.CANCELLED.equals( status ) ){
            throw new OrderHasCanceledException( MessageConstant.ORDER_HAS_CANCELED );
        }
        if ( OrderConstant.Status.COMPLETED.equals( status ) ){
            throw new CompletedOrderCannotCancelException( MessageConstant.COMPLETED_ORDER_CANNOT_CANCEL );
        }
        if ( OrderConstant.Status.CONFIRMED.equals( status ) ||
                OrderConstant.Status.DELIVERY_IN_PROGRESS.equals( status ) ){
            throw new OrderCannotCancelException( MessageConstant.PLEASE_CONTACT_MERCHANT );
        }
    }


}
