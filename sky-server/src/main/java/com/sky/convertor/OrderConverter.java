package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.user.order.OrderSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.projection.shoppingCart.ShoppingCartOrderDetailProjection;
import com.sky.vo.admin.order.AdminOrderInfoVO;
import com.sky.vo.admin.order.OrderPageQueryItemVO;
import com.sky.vo.user.order.UserOrderInfoVO;
import com.sky.vo.admin.order.OrderSubmitVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 订单对象转换映射(entity, vo, dto)
 * <pre>
 *     <code>
         VO 字段数 << 实体字段数？（比如 < 50%）
                 │
                 ├─ 是 ──→ Mapper 直接查 VO（列表、轻量场景）
                 │
                 └─ 否 ──→ VO 接近实体？
                             │
                             ├─ 是 ──→ 有复杂计算字段？
                             │         │
                             │         ├─ 是 ──→ 查实体 + Service 手动组装
                             │         └─ 否 ──→ 查实体 + MapStruct 转换
                             │                   （用 ignore 排除敏感字段）
                             │
                             └─ 否 ──→ 多个 VO 差异大？
                                         │
                                         ├─ 是 ──→ 各场景独立 Mapper 方法
                                         └─ 否 ──→ 查实体 + MapStruct
 *     </code>
 * </pre>
 *
 * @CreateTime 2026/6/19 10:16
 */
@Mapper( config = GlobalMappingConfiguration.class )
public interface OrderConverter {
    /**
     * 用户下单 dto 转订单实体对象
     *
     * @param submitDTO 订单创建信息
     * @return 订单实体对象, 只包含 dto 的字段, 其他字段由业务类设置
     */
    @BeanMapping( ignoreByDefault = true )
    @Mapping( source = "submitDTO.addressBookId", target = "addressBookId" )
    @Mapping( source = "submitDTO.payMethod", target = "payMethod" )
    @Mapping( source = "submitDTO.remark", target = "remark" )
    @Mapping( source = "submitDTO.estimatedDeliveryTime", target = "estimatedDeliveryTime" )
    @Mapping( source = "submitDTO.deliveryStatus", target = "deliveryStatus" )
    @Mapping( source = "submitDTO.tablewareNumber", target = "tablewareNumber" )
    @Mapping( source = "submitDTO.tablewareStatus", target = "tablewareStatus" )
    @Mapping( source = "submitDTO.packAmount", target = "packAmount" )
    @Mapping( source = "submitDTO.amount", target = "amount" )
    Orders toEntity( OrderSubmitDTO submitDTO );


    /**
     * 用户下单后，订单实体转 vo 用于展示支付订单界面
     *
     * @param order 创建好的订单实体对象
     * @return 用户下单的回显信息,
     *          <b>映射时忽略的字段</b>: 无, 全部映射
     */
    @Mapping( source = "order.number", target = "orderNumber" )
    @Mapping( source = "order.amount", target = "orderAmount" )
    OrderSubmitVO toSubmitVO( Orders order );

    /**
     * 用户购物车数据转为订单明细, 2 种对象记录的内容基本一样
     *
     * @param shoppingCart 购物车数据, 此处为专门的投影对象
     * @return 订单明细实体对象, <b>映射时忽略的字段</b>: id, orderId
     */
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "orderId", ignore = true )
    OrderDetail toDetail( ShoppingCartOrderDetailProjection shoppingCart );

    /**
     * 用户购物车数据批量转为订单明细, 该接口方法编译后循环调用{@link #toDetail(ShoppingCartOrderDetailProjection)}
     *
     * @param shoppingCarts 购物车数据列表, 此处为专门的投影对象列表
     * @return 订单明细列表
     */
    List< OrderDetail > toDetailList( List< ShoppingCartOrderDetailProjection > shoppingCarts );

    /**
     * 完整订单实体类 → 用户端查询订单明细 VO
     *
     * @param order 完整的订单实体对象
     * @return 用户端订单详情, <b>忽略字段</b>: orderDetailList 由业务类查找组装
     */
//    @Mapping( target = "userId", ignore = true )
//    @Mapping( target = "userName", ignore = true )
//    @Mapping( target = "checkoutTime", ignore = true )
//    @Mapping( target = "cancelReason", ignore = true )
//    @Mapping( target = "cancelTime", ignore = true )
//    @Mapping( target = "rejectionReason", ignore = true )
    @Mapping( target = "orderDetailList", ignore = true )
    UserOrderInfoVO toUserOrderInfoVO( Orders order );

    /**
     * 完整订单实体列表转管理端订单条件分页查询 VO 列表, converter 编译后循环调用{@link #toPageVO(Orders)}
     *
     * @param entities 完整的订单实体对象列表
     * @return 条件分页查询 vo 列表
     */
    List< OrderPageQueryItemVO > toPageVOList( List< Orders > entities );

    /**
     * 完整订单实体类 → 管理端订单条件分页查询 VO
     *
     * @param entity 完整的订单实体对象
     * @return 订单条件分页查询 vo, <b>映射时忽略的字段</b>: orderDishes, 由调用方手动设置
     */
    @Mapping( target = "orderDishes", ignore = true )
    OrderPageQueryItemVO toPageVO( Orders entity );

    /**
     * 完整订单实体类 → 管理端订单详情 VO
     *
     * @param order 完整订单实体类
     * @return 管理端订单详情 VO
     */
    @Mapping( target = "orderDetailList", ignore = true )
    AdminOrderInfoVO toAdminInfoVO( Orders order );
}
