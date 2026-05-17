package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public PageResult page( CategoryPageQueryDTO categoryPageQueryDTO ){
        Integer page = categoryPageQueryDTO.getPage();
        page = ( page == null || page < 1 ) ? 1 : page;

        Integer pageSize = categoryPageQueryDTO.getPageSize();
        pageSize = ( pageSize == null || pageSize < 1 ) ? 10 : pageSize;

        PageHelper.startPage( page, pageSize );

        List< Category > categoryList = categoryMapper.selectByQuery( categoryPageQueryDTO );
        Page< Category > categoryPage = ( Page< Category > ) categoryList;

        return new PageResult( categoryPage.getTotal(), categoryPage.getResult() );
    }


    // ==================== 私有辅助方法 ====================

}
