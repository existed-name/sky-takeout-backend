package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CategoryServiceImpl implements CategoryService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final CategoryMapper categoryMapper;



    // ==================== 构造器 ====================
    public CategoryServiceImpl( CategoryMapper categoryMapper ){
        this.categoryMapper = categoryMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public void save( CategoryDTO categoryDTO ){
        Category category = new Category();
        BeanUtils.copyProperties( categoryDTO, category );

        category.setStatus( StatusConstant.DISABLE );
        category.setCreateTime( LocalDateTime.now() );
        category.setUpdateTime( LocalDateTime.now() );

        Long currentUserId = BaseContext.getCurrentId();
        category.setCreateUser( currentUserId );
        category.setUpdateUser( currentUserId );

        categoryMapper.insert( category );
    }


    // ==================== 私有辅助方法 ====================

}
