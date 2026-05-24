package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DishServiceImpl implements DishService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishMapper dishMapper;



    // ==================== 构造器 ====================
    public DishServiceImpl( DishMapper dishMapper ){
        this.dishMapper = dishMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void save( DishDTO dishDTO ){
        Dish dish = new Dish();
        BeanUtils.copyProperties( dishDTO, dish );
        // 审计字段交给 AOP 处理
        dishMapper.insert( dish );
    }


    // ==================== 私有辅助方法 ====================

}
