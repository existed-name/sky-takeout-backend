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
     * orderDetail 订单明细转 shoppingCart
     * <p>
     * 复用字段: dishId, dishFlavor, setmealId, number, name, amount, image
     * <p>
     * 假如想要<b>展示最新数据, 需要手动更换</b>: name, amount, image
     * <p>
     * <b>需要手动设置</b>: orderId/userId, createTime
     * </p>
     * @param orderDetail 订单明细(关联的菜品/套餐数据)
     * @return 不含 id, userId, createTime 的 shoppingCart
     */
    @Mapping(  target = "id", ignore = true )
    @Mapping( target = "userId", ignore = true )
    /*
    @Mapping(  target = "name", ignore = true )
    @Mapping(  target = "amount", ignore = true )
    @Mapping(  target = "image", ignore = true )
     */
    @Mapping(  target = "createTime", ignore = true )
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
    @Mapping( source = "dish.image", target = "image" )
    @Mapping( source = "dish.name", target = "name" ) // 指定 MappingTarget 后不需要指定参数 "xxx."
    @Mapping( source = "dish.price", target = "amount" )
    void updateFromDish( Dish dish, @MappingTarget ShoppingCart dishShoppingCart );

    /**
     * 将 setmeal 的部分数据填入 shoppingCart: name, amount image
     *
     * @param setmeal 套餐实体类
     * @param setmealShoppingCart 待修改的购物车实体类
     */
    @BeanMapping( ignoreByDefault = true )
    @Mapping( source = "setmeal.image", target = "image" )
    @Mapping( source = "setmeal.name", target = "name" )
    @Mapping( source = "setmeal.price", target = "amount" )
    void updateFromSetmeal( Setmeal setmeal, @MappingTarget ShoppingCart setmealShoppingCart );
}
