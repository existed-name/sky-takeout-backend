package com.sky.service;

import com.sky.dto.shoppingCart.ShoppingCartDTO;
import com.sky.vo.ShoppingCartListVO;

public interface ShoppingCartService {
    /**
     * 用户端添加单条购物车数据( 单个菜品/套餐 )
     *
     * @param userId 购物车数据所属的用户 id
     * @param shoppingCartDTO 购物车数据
     */
    void addWithUserId( Long userId, ShoppingCartDTO shoppingCartDTO );

    /**
     * 用户端查看购物车所有内容
     *
     * @param userId 当前用户 id, 业务类内部判断用户是否存在
     * @return 购物车数据项列表 + 后端计算总价
     */
    ShoppingCartListVO listByUserId( Long userId );

    /**
     * 用户端某个用户清空自己的购物车
     *
     * @param userId 用户 id, 业务类内部判断用户是否存在
     */
    void cleanByUserId( Long userId );

    /**
     * 减少加购的商品数量
     */
    void decrementById( Long id );
}
