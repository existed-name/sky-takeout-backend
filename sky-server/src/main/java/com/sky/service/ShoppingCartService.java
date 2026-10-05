package com.sky.service;

import com.sky.dto.shoppingCart.ShoppingCartDTO;
import com.sky.vo.ShoppingCartVO;

public interface ShoppingCartService {
    /**
     * 用户端添加单条购物车数据( 单个菜品/套餐 )
     *
     * @param userId 购物车数据所属的用户 id
     * @param shoppingCartDTO 购物车数据
     */
    void addWithUserId( Long userId, ShoppingCartDTO shoppingCartDTO );

    /**
     * 查看购物车所有内容
     */
    ShoppingCartVO list();

    /**
     * 清空购物车
     */
    void clean();

    /**
     * 减少加购的商品数量
     */
    void decrementById( Long id );
}
