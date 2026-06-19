package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    /**
     * 用户下单、创建订单、主键回填
     */
    @Insert( """
            INSERT INTO orders
            ( number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, phone, address, user_name, consignee, cancel_reason, rejection_reason, cancel_time, estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number, tableware_status )
            VALUES ( #{order.number}, #{order.status}, #{order.userId}, #{order.addressBookId}, #{order.orderTime}, #{order.checkoutTime}, #{order.payMethod}, #{order.payStatus}, #{order.amount}, #{order.remark}, #{order.phone}, #{order.address}, #{order.userName}, #{order.consignee}, #{order.cancelReason}, #{order.rejectionReason}, #{order.cancelTime}, #{order.estimatedDeliveryTime}, #{order.deliveryStatus}, #{order.deliveryTime}, #{order.packAmount}, #{order.tablewareNumber}, #{order.tablewareStatus} )
            """ )
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    int insert( @Param( "order" ) Orders order );
}
