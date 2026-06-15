package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.vo.ShoppingCartVO;

public interface ShoppingCartService {
    /**
     * 购物车添加单个菜品/套餐
     */
    void add( ShoppingCartDTO shoppingCartDTO );

    /**
     * 查看购物车所有内容
     */
    ShoppingCartVO list();
}
