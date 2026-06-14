package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {
    /**
     * 购物车添加单个菜品/套餐
     */
    void add( ShoppingCartDTO shoppingCartDTO );
}
