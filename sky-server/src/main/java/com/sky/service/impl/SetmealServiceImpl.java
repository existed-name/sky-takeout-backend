package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;



    // ==================== 构造器 ====================
    public SetmealServiceImpl( SetmealMapper setmealMapper, SetmealDishMapper setmealDishMapper ){
        this.setmealMapper = setmealMapper;
        this.setmealDishMapper = setmealDishMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithRelation( SetmealDTO setmealDTO ){
        // setmealDTO = setmeal + setmealDish 关联对象，分 2 批插入数据库
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties( setmealDTO, setmeal );
        setmealMapper.insert( setmeal );

        // 唉，忽略了 SetmealDish 的 setmealId 并不是前端自带的，而是绑定当前套餐
        // 于是需要从当前套餐拿到 id
        // 就需要设置主键回填
        List< SetmealDish > setmealDishes = setmealDTO.getSetmealDishes();
        if ( setmealDishes == null || setmealDishes.isEmpty() ){
            return;
        }
        setmealDishes.forEach( setmealDish -> setmealDish.setSetmealId( setmeal.getId() ) );
        setmealDishMapper.insertBatch( setmealDishes );
    }


    // ==================== 私有辅助方法 ====================

}
