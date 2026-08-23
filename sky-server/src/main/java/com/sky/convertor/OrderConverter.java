package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.order.OrderPageQueryVO;
import com.sky.vo.order.OrderInfoVO;
import com.sky.vo.order.OrderSubmitVO;
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
     * 用户下单 dto 转订单 entity
     * <p>
     * <b>Orders 忽略字段</b>: 除了 dto 包含的字段, 其他都忽略、业务类设置
     */
    /*
    Entity 字段 >> DTO, 建议手动映射
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "number", ignore = true )
    @Mapping( target = "status", ignore = true )
    @Mapping( target = "userId", ignore = true )
    @Mapping( target = "orderTime", ignore = true )
    @Mapping( target = "checkoutTime", ignore = true )
    @Mapping( target = "payStatus", ignore = true )
    @Mapping( target = "userName", ignore = true )
    @Mapping( target = "phone", ignore = true )
    @Mapping( target = "address", ignore = true )
    @Mapping( target = "consignee", ignore = true )
    @Mapping( target = "cancelReason", ignore = true )
    @Mapping( target = "rejectionReason", ignore = true )
    @Mapping( target = "cancelTime", ignore = true )
    @Mapping( target = "deliveryTime", ignore = true )
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
    Orders toEntity( OrdersSubmitDTO submitDTO );


    /**
     * 用户下单后，订单 entity 转 vo 展示支付订单界面
     * <p>
     * <b>OrderSubmitVO 忽略字段</b>: 无
     */
    @Mapping( source = "order.number", target = "orderNumber" )
    @Mapping( source = "order.amount", target = "orderAmount" )
    OrderSubmitVO toSubmitVO( Orders order );

    /**
     * 用户购物车数据转为订单明细, 2 种对象记录的内容基本一样
     * <p>
     *     <b>orderDetail 忽略字段</b>: id, orderId
     * </p>
     */
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "orderId", ignore = true )
    OrderDetail toDetail( ShoppingCartItemVO shoppingCart );

    /**
     * 用户购物车数据批量转为订单明细, 该接口方法编译后循环调用{@link #toDetail(ShoppingCartItemVO)}
     */
    List< OrderDetail > toDetailList( List< ShoppingCartItemVO > shoppingCartItemVOList );

    /**
     * 完整订单实体类 → 用户端查询订单明细 VO
     * <p>
     * <b>OrderInfoVO 忽略字段</b>: orderDetailList 由业务类查找组装
     */
//    @Mapping( target = "userId", ignore = true )
//    @Mapping( target = "userName", ignore = true )
//    @Mapping( target = "checkoutTime", ignore = true )
//    @Mapping( target = "cancelReason", ignore = true )
//    @Mapping( target = "cancelTime", ignore = true )
//    @Mapping( target = "rejectionReason", ignore = true )
    @Mapping( target = "orderDetailList", ignore = true )
    OrderInfoVO toInfoVO( Orders order );

    /**
     * 完整订单实体类转管理端订单条件分页查询 VO 列表, converter 会循环调用{@link #toPageVO(Orders)}
     */
    List< OrderPageQueryVO > toPageVOList( List< Orders > entities );

    /**
     * 完整订单实体类 → 管理端订单条件分页查询 VO
     * <br>
     * 由于 source 已经覆盖了 target 的所有字段, 所以这里直接 converter 自动转换、丢弃没用到的字段就行
     */
    @Mapping( target = "orderDishes", ignore = true )
    OrderPageQueryVO toPageVO( Orders entity );



}
