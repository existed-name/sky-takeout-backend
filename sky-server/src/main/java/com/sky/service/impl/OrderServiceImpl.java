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
    /** 订单数量 */
    private int orderCount = 0;



    // ==================== 构造器 ====================
    public OrderServiceImpl( OrderConvertor orderConvertor, UserMapper userMapper, AddressBookMapper addressBookMapper, OrderMapper orderMapper, ShoppingCartMapper shoppingCartMapper, OrderDetailMapper orderDetailMapper ){
        this.orderConvertor = orderConvertor;
        this.userMapper = userMapper;
        this.addressBookMapper = addressBookMapper;
        this.orderMapper = orderMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.orderDetailMapper = orderDetailMapper;
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
//        orderId == null.if

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
     * 格式：{业务码}{日期}{6位序列}
     * <br>
     * 示例：TB20250619000001
     * <br>
     * 教程用的 System.currentTimeMillis 高并发可能重叠
     */
    private String generateOrderNumber( LocalDateTime now ){
        String businessCode = "CQ"; // "苍穹餐厅";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "yyyyMMdd" );
        StringBuilder orderNumber = new StringBuilder();
        orderNumber.append( businessCode )
                .append( now.format( dateTimeFormatter ) );

        // 由于要凑齐 6 位，所以需要数已有订单数有多少位
        int temp = orderCount, digitCount = 0;
        while ( temp > 0 ){
            temp /= 10;
            ++digitCount;
        }

        int requiredDigitCount = 6;
        if ( digitCount < requiredDigitCount ){
            orderNumber.append( // 编译器提示把 for 循环改成 String.repeat
                    "0".repeat( Math.max(
                            0, requiredDigitCount - digitCount )
                    )
            );
        }
        orderNumber.append( ++orderCount );

        return orderNumber.toString();
    }


}
