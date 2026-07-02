package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.order.OrderInfoVO;
import com.sky.vo.order.OrderSubmitVO;
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
     * <br>
     * 我猜教程用 Orders 而不是 Order 是为了跟 Spring 的 Order 顺序注解区分??
     */
    Orders toEntity( OrdersSubmitDTO submitDTO );

    /**
     * 用户下单后，订单 entity 转 vo 展示支付订单界面
     */
    @Mapping( source = "order.number", target = "orderNumber" )
    @Mapping( source = "order.amount", target = "orderAmount" )
    OrderSubmitVO toSubmitVO( Orders order );

    /**
     * 用户购物车数据转为订单明细(记录的内容基本一样)
     */
    @Mapping( target = "id", ignore = true )  // 注意两者的 id 意义不同, 忽略后交给 Service 补充
    @Mapping( source = "price", target = "amount" )
    List< OrderDetail > toDetailList( List< ShoppingCartItemVO > shoppingCartItemVOList );

    /**
     * 完整订单实体类 → 用户端查询订单明细 VO
     * <p>
     * 由于字段接近实体类，直接查询完整实体 + MapStruct 去除不需要的字段
     */
    @Mapping( target = "userId", ignore = true )
    @Mapping( target = "userName", ignore = true )
    @Mapping( target = "checkoutTime", ignore = true )
    @Mapping( target = "cancelReason", ignore = true )
    @Mapping( target = "cancelTime", ignore = true )
    @Mapping( target = "rejectionReason", ignore = true )
    OrderInfoVO toInfoVO( Orders order );





}
