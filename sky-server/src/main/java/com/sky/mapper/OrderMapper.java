package com.sky.mapper;

import com.sky.dto.order.AdminOrderPageQueryDTO;
import com.sky.dto.order.OrderStatusCount;
import com.sky.dto.order.UserOrderPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.projection.order.PaymentTimeoutProjection;
import com.sky.projection.order.StatusProjection;
import com.sky.vo.order.OrderHistoryItemVO;
import com.sky.vo.report.TurnoverReportItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 查询某个订单的状态
     *
     * @param id 订单 id
     * @return 订单状态, 为 null 说明订单不存在
     */
    @Select( """
            SELECT status
            FROM orders
            WHERE id = #{id}
            """ )
    Integer selectStatusById( @Param( "id" ) Long id );

    /**
     * 查询某个订单的状态、支付状态
     *
     * @param id 订单 id
     * @return 订单状态 + 支付状态 record, 为 null 说明订单不存在
     */
    @Select( """
            SELECT status, pay_status
            FROM orders
            WHERE id = #{id}
            """ )
    StatusProjection selectStatusProjectionById( @Param( "id" ) Long id );

    /**
     * 管理端更新订单状态
     *
     * @param id 订单 id
     * @param status 订单状态, {@link com.sky.constant.OrderConstant.Status}
     * @return 修改行数, 0 => 订单不存在, 1 => 修改成功
     */
    @Update( """
            UPDATE orders
            SET status = #{status}
            WHERE id = #{id}
            """ )
    int updateStatusById( @Param( "id" ) Long id, @Param( "status" ) Integer status );

    /**
     * 管理端更新订单信息
     *
     * @param order 订单实体类, 建议业务类新建对象手动拼装、不需要修改的字段留空、避免覆盖成错误数值
     * @return 修改行数<br>
     *          0 => 订单不存在;<br>
     *          1 => 该订单修改成功
     */
    int updateById( @Param( "order" ) Orders order );

    /**
     * 根据订单状态、下单截止时间查询订单, 主要用于<b>查询超时的未支付订单</b>
     *
     * @param status   订单状态, {@link com.sky.enumeration.OrderStatus}
     * @param deadline 截止时间, 用于筛选下单时间早于该节点的订单, <b>创建时间早于该节点即为超时</b>
     * @return 投影对象列表
     */
    @Select( """
            SELECT id, status, pay_status, order_time
            FROM orders
            WHERE status = #{status} AND order_time < #{deadline}
            """ )
    List< PaymentTimeoutProjection > selectPaymentTimeoutProjectionsByStatusAndDeadline(
            @Param( "status" ) int status,
            @Param( "deadline" ) LocalDateTime deadline
    );

    /**
     * 批量更新订单
     *
     * @param orders 待更新的订单列表, <br>
     *               1. <b>列表必须存在元素</b>
     *               2. 建议新建订单对象、不修改的字段设置为空
     * @return 修改数据条数, 理论上 = {@code list.size}( 如果不含重复 id )
     */
    int updateBatchByIds( @Param( "orders" ) List< Orders > orders );

    /**
     * 查询特定状态的一批订单的 id
     *
     * @param status 订单状态, {@link com.sky.enumeration.OrderStatus}
     * @return 订单 id 列表, 订单状态错误、不存在时列表大小为 0
     */
    @Select( """
            SELECT id
            FROM orders
            WHERE status = #{status}
            """ )
    List< Long > selectIdsByStatus( @Param( "status" ) int status );

    /**
     * 查询某个订单的订单号
     *
     * @param id 订单 id
     * @return 订单号, 订单不存在时返回 null
     */
    @Select( """
            SELECT number
            FROM orders
            WHERE id = #{id}
            """ )
    String selectNumberById( @Param( "id" ) Long id );

    /**
     * 统计指定时间范围内, 每天的总营业额
     *
     * @param status 订单状态, <b>只查询已完成、没被取消的订单</b>{@link com.sky.enumeration.OrderStatus#COMPLETED}
     * @param begin        起始时间( 包含 )
     * @param endExclusive 截止时间( <b>不包含</b> ),
     *                     由调用方确认 begin <= endExclusive
     * @return 时间范围内, 每天的营业额数据项列表
     */
    List< TurnoverReportItemVO > sumAmountByStatusAndGroupByDate( @Param( "status" ) int status, @Param( "begin" ) LocalDate begin, @Param( "endExclusive" ) LocalDate endExclusive );
}
