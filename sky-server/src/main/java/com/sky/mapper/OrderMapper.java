package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.dto.order.OrderStatusCount;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.order.OrderHistoryItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    /**
     * 管理端条件查询订单, 分页操作由{@link com.sky.utils.PageUtil}完成
     * <br>
     * 排序规则: 按照下单时间降序排列、时间相同按照 id 倒序排列，管理端优先看最新订单
     * <br>
     * 不过排序也有问题: 理论上顾客应该先到先处理( 时间升序排列 ), 但是这样会把老旧订单也排在前面,
     * 想要查询全部、查询已完成/取消订单为时间降序, 而中间状态订单为时间升序,
     * 可以在 Mapper 对 status 分值处理对应的排序策略
     * <br>
     * TODO: 优化排序规则 + 修改查询参数后，SQL 也需要修改
     *
     * @return 完整的订单对象列表
     */
    List< Orders > selectByQuery( @Param( "query" ) AdminOrderPageQueryDTO orderPageQueryDTO );

    /**
     * 管理端统计某种状态的订单数量
     *
     * @param status 订单状态, {@link com.sky.constant.OrderConstant.Status}
     * @return 订单数量
     */
    @Select( """
            SELECT count( * )
            FROM orders
            WHERE status = #{status}
            """ )
    Long countByStatus( @Param( "status" ) Integer status );

    /**
     * 管理端统计各种状态的订单数量
     * <p>
     *     这种报表查询默认返回<code>List< Map< String, Object > ></code>每行作为 1 个 Map<br>
     *     使用<code>@MapKey</code>必须确保 Value 能够 get 拿取 Key,
     *     直接<code>Map< Integer, Long ></code>会因为 Long 没有 getStatus 而报错<br>
     *     这里用 status 作为主键、每行数据封装成{@link OrderStatusCount}, 方便拿取数据、管理字段
     * </p>
     *
     * @return 记录<code>orderStatus → {@link OrderStatusCount}</code>的 Map
     */
    @Select( """
            SELECT status, count( * ) AS count
            FROM orders
            GROUP BY status
            """ )
    @MapKey( "status" )
    Map< Integer, OrderStatusCount > countGroupByStatus();

    /**
     * 查询某个订单实体对象
     *
     * @param id 订单 id
     * @return 完整的订单实体对象, 不存在时返回 null
     */
    Orders selectById( @Param( "id" ) Long id );
}
