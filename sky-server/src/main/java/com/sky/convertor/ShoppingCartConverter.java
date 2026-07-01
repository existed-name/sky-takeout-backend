package com.sky.convertor;

import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * ShoppingCart 相关类字段映射、转换对象
 *
 * @CreateTime 2026/6/28 7:53
 */
@Mapper(
    // 让 MapStruct 接口编译后生成 Spring 的 `@Component`，方便直接 `@Autowired` 注入
    componentModel = "spring",
    // 源对象字段如果没有被用到，该怎么处理
    // 由于多数情况下源对象字段很多用不到，如果选择 ERROR 就必须手动指定忽略源对象字段、很多很麻烦
    // 所以通常 IGNORE, 只映射同名的属性、手动 Mapping 的属性
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    // 目标对象字段如果没有被赋值，该怎么处理
    // 因为目标对象通常需要填满，所以如果有字段没赋值就报错, 必须手动忽略部分字段, 确保所有字段都被处理
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ShoppingCartConverter {

    /**
     * orderDetail 订单明细批量转 shoppingCart
     * <p>
     * 复用字段: dishId, dishFlavor, setmealId, number
     * <p>
     * <b>需要手动更换</b>: orderId/userId, name, amount, image, createTime
     *
     * @param orderDetail 订单明细(关联的菜品/套餐数据)
     * @return 只含 dishId, dishFlavor, setmealId, number 的 shoppingCart
     */
    @Mapping( source = "orderDetail.id", target = "id", ignore = true )
    @Mapping( target = "userId", ignore = true )
    @Mapping( source = "orderDetail.name", target = "name", ignore = true )
    @Mapping( source = "orderDetail.amount", target = "amount", ignore = true )
    @Mapping( source = "orderDetail.image", target = "image", ignore = true )
    @Mapping( source = "orderDetail.createTime", target = "createTime", ignore = true )
    ShoppingCart toEntity( OrderDetail orderDetail );

    /**
     * orderDetail 订单明细批量转 shoppingCart, 见{@link #toEntity(OrderDetail)}
     */
    List< ShoppingCart > toEntities( List< OrderDetail > orderDetails );

    /**
     * 将 dish 的部分数据填入 dishShoppingCart: name, amount image
     *
     * @param dish 菜品实体类
     * @param dishShoppingCart 待修改的购物车实体类
     */
    // 更新对象建议先忽略所有字段，然后手动指定映射(只需要修改这几个字段)，避免写很多 ignore
    @BeanMapping( ignoreByDefault = true )
    @Mapping( source = "dish.id", target = "dishShoppingCart.id" )
    @Mapping( source = "dish.price", target = "dishShoppingCart.amount" )
    @Mapping( source = "dish.createTime", target = "dishShoppingCart.createTime" )
    void updateEntityWithDish( Dish dish, @MappingTarget ShoppingCart dishShoppingCart );

    /**
     * 将 setmeal 的部分数据填入 shoppingCart: name, amount image
     *
     * @param setmeal 套餐实体类
     * @param setmealShoppingCart 待修改的购物车实体类
     */
    @BeanMapping( ignoreByDefault = true )
    @Mapping( source = "setmeal.id", target = "setmealShoppingCart.id" )
    @Mapping( source = "setmeal.price", target = "setmealShoppingCart.amount" )
    @Mapping( source = "setmeal.createTime", target = "setmealShoppingCart.createTime" )
    void updateEntityWithSetmeal( Setmeal setmeal, ShoppingCart setmealShoppingCart );
}
