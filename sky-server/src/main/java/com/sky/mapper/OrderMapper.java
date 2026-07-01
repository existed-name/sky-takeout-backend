package com.sky.mapper;

import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.order.OrderHistoryItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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



    /**
     * 根据 id、userId 修改订单信息, 修改前会对传入对象的字段判空
     * <br>
     * 不会修改:
     *      <pre>
     *      - 标识符: number, user_id, user_name
     *      - 部分审计字段: order_time
     *      - 支付信息: amount, pay_method, pack_amount
     *      - 收货地址(虽然说快递可以改,外卖以后也可以支持换地址): address_book_id, phone, address, consignee
     *      - 用户备注:
     *        * remark
     *        * 餐具 tableware_number, tableware_status
     *        * 送达时间 estimated_delivery_time, delivery_status
     *      </pre>
     * @return 修改行数: <br>
     *          * 0 => <b>id 对应的数据不存在 | 数据不属于该用户</b>(排除用户不存在的情况); <br>
     *          * 1 => 正常
     */
    int updateByIdAndUserId( @Param( "order" ) Orders order );

    /**
     * 判断该 id 对应的数据是否存在
     *
     * @return 统计数据<br>
     *          * 0 => <b>该 id 对应的数据不存在</b><br>
     *          * 1 => 存在
     */
    @Select( """
            SELECT count( * )
            FROM orders
            WHERE id = #{id}
            """ )
    int countById( @Param( "id" ) Long id );

    /**
     * 判断该订单是否为特定的支付状态(比如已支付)
     *
     * @param payStatus 支付状态,{@link com.sky.constant.OrderConstant.PayStatus}
     * @return 统计数据<br>
     *          * 0 => <b>该订单支付状态 ≠ 指定的支付状态</b>(比如不是已支付)<br>
     *          * 1 => 该订单支付状态 = 指定的支付状态(比如已支付)
     */
    @Select( """
            SELECT count( * )
            FROM orders
            WHERE id = #{id} AND pay_status = #{payStatus}
            """ )
    long countByIdAndPayStatus( @Param( "id" ) Long id, @Param( "payStatus" ) Integer payStatus );

    /**
     * 条件查询该用户的历史订单 VO, 分页操作由{@link com.sky.utils.PageUtil}完成
     * <br>
     * 排序规则: 按照时间降序排列、时间相同按照 id 倒序排列，确保用户看到最新订单
     * <br>
     * TODO: 修改查询参数后，SQL 也需要修改
     */
    List< OrderHistoryItemVO > selectByUserIdAndQuery( @Param( "userId" ) Long userId, @Param( "query" ) UserOrderPageQueryDTO userOrderPageQueryDTO );

    /**
     * 查询完整订单实体类
     *
     * @return 完整 order，<b>由调用方决定保留哪些字段</b><br>
     *          如果返回 null => <b>id 对应的数据不存在 | 数据不属于该用户</b>(排除用户不存在的情况)
     */
    Orders selectByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 查询订单状态
     *
     * @return <b>只含 status 字段</b>的 order<br>
     *          如果返回 null => <b>id 对应的数据不存在 | 数据不属于该用户</b>(排除用户不存在的情况)
     */
    @Select( """
            SELECT status
            FROM orders
            WHERE id = #{id} AND user_id = #{userId}
            """ )
    Orders selectStatusByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 判断该数据的存在性、匹配性
     *
     * @return 定位的数据条数: <br>
     *          * 0 => <b>id 对应的数据不存在 | 数据不属于该用户</b>(排除用户不存在的情况); <br>
     *          * 1 => 正常
     */
    @Select( """
            SELECT count( * )
            FROM orders
            WHERE id = #{id} AND user_id = #{userId}
            """ )
    long countByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );
}
