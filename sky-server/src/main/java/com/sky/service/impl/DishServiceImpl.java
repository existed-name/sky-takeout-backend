package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;



    // ==================== 构造器 ====================
    public DishServiceImpl( DishMapper dishMapper, DishFlavorMapper dishFlavorMapper ){
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithFlavor( DishDTO dishDTO ){
        Dish dish = new Dish();
        BeanUtils.copyProperties( dishDTO, dish );
        // 审计字段交给 AOP 处理
        dishMapper.insert( dish );

        // 注意 Mapper 是否保存了所有内容: 实体类对象所有属性( DTO 属性 + 审计字段 ) + 关联内容 = 表数据
        List< DishFlavor > flavors = dishDTO.getFlavors();
        if ( flavors == null || flavors.isEmpty() ){
            return;
        }
        // 最细节: 设置关联 id => 需要设置主键回填，让关联对象拿到数据库表 id
        Long dishId = dish.getId();
        flavors.forEach( flavor -> flavor.setDishId( dishId ) );
        dishFlavorMapper.insertBatch( flavors );
    }


    // ==================== 私有辅助方法 ====================

}
