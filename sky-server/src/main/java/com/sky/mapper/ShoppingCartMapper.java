package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 新增购物车内容(菜品/套餐)
     */
    @Insert( """
            INSERT INTO shopping_cart
                ( name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time )
            values ( #{shoppingCart.name}, #{shoppingCart.image}, #{shoppingCart.userId}, #{shoppingCart.dishId}, #{shoppingCart.setmealId}, #{shoppingCart.dishFlavor}, #{shoppingCart.number}, #{shoppingCart.amount}, #{shoppingCart.createTime} )
            """ )
    void insert( @Param( "shoppingCart" ) ShoppingCart shoppingCart );
}
