package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.OrderConstant;
import com.sky.convertor.OrderConvertor;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.exception.AddressBookUserNotMatchException;
import com.sky.exception.NotFoundException;
import com.sky.exception.shoppingCart.EmptyShoppingCartException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.utils.OrderCacheManager;
import com.sky.vo.AddressBookVO;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.order.OrderSubmitVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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



    // ==================== 构造器 ====================
    public OrderServiceImpl( OrderConvertor orderConvertor, UserMapper userMapper, AddressBookMapper addressBookMapper, OrderMapper orderMapper, ShoppingCartMapper shoppingCartMapper, OrderDetailMapper orderDetailMapper, OrderCacheManager orderCacheManager ){
        this.orderConvertor = orderConvertor;
        this.userMapper = userMapper;
        this.addressBookMapper = addressBookMapper;
        this.orderMapper = orderMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.orderCacheManager = orderCacheManager;
    }


    // ==================== 公有方法 ====================
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
        return orderConvertor.toVO( entity );
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


}
