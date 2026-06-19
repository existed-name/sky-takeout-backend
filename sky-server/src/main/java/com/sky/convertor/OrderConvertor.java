package com.sky.convertor;

import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.order.OrderSubmitVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 订单对象转换映射(entity, vo, dto)
 *
 * @CreateTime 2026/6/19 10:16
 */
@Mapper( componentModel = "spring" )
public interface OrderConvertor {
    /**
     * 用户下单 dto 转订单 entity
     * <br>
     * 我猜教程用 Orders 而不是 Order 是为了跟 Spring 的 Order 顺序注解区分??
     */
    Orders toEntity( OrdersSubmitDTO submitDTO );

    /**
     * 用户下单后，订单 entity 转 vo 展示支付订单界面
     */
    @Mapping( source = "entity.number", target = "orderNumber" )
    @Mapping( source = "entity.amount", target = "orderAmount" )
    OrderSubmitVO toVO( Orders entity );

    /**
     * 用户购物车数据转为订单明细(记录的内容基本一样)
     */
    @Mapping( target = "id", ignore = true )  // 注意两者的 id 意义不同, 忽略后交给 Service 补充
    @Mapping( source = "price", target = "amount" )
    List< OrderDetail > toDetailList( List< ShoppingCartItemVO > shoppingCartItemVOList );
}
