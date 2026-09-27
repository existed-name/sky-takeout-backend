package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.convertor.CategoryConverter;
import com.sky.dto.category.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.CategoryType;
import com.sky.enumeration.EnableStatus;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.utils.PageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final CategoryConverter categoryConverter;


    // ==================== 构造器 ====================
    public CategoryServiceImpl( CategoryMapper categoryMapper, DishMapper dishMapper, SetmealMapper setmealMapper, CategoryConverter categoryConverter ){
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.categoryConverter = categoryConverter;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void save( Long operatorId, CategoryDTO categoryDTO ){
        /*
            * 补充思路
            * 业务检验 → dto 字段
              - 主要是分类类型 type 必须有效
            * 之后 converter 把 dto 转为实体类用于插入对象数据
         */
        CategoryType.throwIfInvalid( categoryDTO.getType() );
        Category category = categoryConverter.toEntity( categoryDTO );
        // 分类: 默认禁用
        category.setStatus( EnableStatus.DISABLED.getStatus() );
        // 由 AOP 自动填写审计字段, 手动填写时才用到 operatorId
        categoryMapper.insert( category );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult page( CategoryPageQueryDTO categoryPageQueryDTO ){
        PageUtil.validateParamsAndStartPage( categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize() );
        List< Category > categoryList = categoryMapper.selectByQuery( categoryPageQueryDTO );
        return PageUtil.buildPageResult( categoryList );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteById( Long id ){
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        long count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException( MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        categoryMapper.deleteById( id );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( CategoryDTO categoryDTO ){
        Category category = new Category();
        BeanUtils.copyProperties( categoryDTO, category );
//        category.setUpdateTime( LocalDateTime.now() );
//        category.setUpdateUser( BaseContext.getCurrentId() );
        categoryMapper.update( category );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatus( Long id, Integer status ){
        StatusConstant.validateStatus( status );
        Category category = new Category();
        category.setId( id );
        category.setStatus( status );
        // AOP 填写审计字段
        categoryMapper.update( category );
    }

    @Transactional( readOnly = true )
    @Override
    public List< Category > listByType( Integer type ){
        // 不需要额外检验 type，动态 SQL 处理了 null 查所有，无效 type 也只是返回空数组
        List< Category > categoryList = categoryMapper.selectByType( type );
        return categoryList;
    }

    @Transactional( readOnly = true )
    @Override
    public List< Category > listEnabledByType( Integer type ){
        List< Category > categoryList = categoryMapper.selectByTypeAndStatus( type, StatusConstant.ENABLE );
        return categoryList;
    }


    // ==================== 私有辅助方法 ====================

}
