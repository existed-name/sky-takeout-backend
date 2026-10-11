package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.convertor.CategoryConverter;
import com.sky.dto.category.CategoryDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.CategoryType;
import com.sky.enumeration.EnableStatus;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.utils.NotFoundUtil;
import com.sky.utils.PageUtil;
import com.sky.vo.admin.category.CategoryListItemVO;
import com.sky.vo.admin.category.CategoryPageQueryItemVO;
import com.sky.vo.user.category.UserCategoryListItemVO;
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
    public PageResult< CategoryPageQueryItemVO > page( CategoryPageQueryDTO categoryPageQueryDTO ){
        // 业务检验查询参数
        Integer type = categoryPageQueryDTO.getType();
        if ( type != null ){
            CategoryType.throwIfInvalid( type );
        }

        PageUtil.validateParamsAndStartPage( categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize() );
        List< CategoryPageQueryItemVO > categoryPageQueryItemVOList = categoryMapper.selectItemVOListByQuery( categoryPageQueryDTO );
        return PageUtil.buildPageResult( categoryPageQueryItemVOList );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteById( Long operatorId, Long id ){
        /*
            整理业务逻辑
            任何删除操作都应该考虑是否有关联数据
            而这里由于每个菜品、套餐必须放进指定的分类, 那么直接删掉分类就会导致它们所属的分类不存在,
            于是对于关联了商品的分类, 考虑拒绝删除、避免残留脏数据

            思路
            查找该分类 id 是否关联了商品( 菜品、套餐 ), 没有关联再删除,
            并且从删除行数判断改分类是否存在
            做判断的技巧: 用 count 数数代替 select 查询
         */
        long dishCount = dishMapper.countByCategoryId( id );
        if ( dishCount > 0L ){
            throw new DeletionNotAllowedException( MessageConstant.CATEGORY_BE_RELATED_BY_DISH );
        }

        long setMealCount = setmealMapper.countByCategoryId( id );
        if( setMealCount > 0L ){
            throw new DeletionNotAllowedException( MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL );
        }

        int row = categoryMapper.deleteById( id );
        NotFoundUtil.throwIfNotUpdated( row, MessageConstant.CATEGORY_NOT_FOUND );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateById( Long operatorId, Long id, CategoryDTO categoryDTO ){
        /*
            思路
            业务检验字段 type
            用 converter 把 dto 转 entity
            修改后拿到修改行数判断数据是否存在
            审计字段交给 AOP, 以后需要手动设置再使用 operatorId
         */
        CategoryType.throwIfInvalid( categoryDTO.getType() );
        Category category = categoryConverter.toEntity( categoryDTO );
        category.setId( id );
        int row = categoryMapper.updateById( category );
        NotFoundUtil.throwIfNotUpdated( row, MessageConstant.CATEGORY_NOT_FOUND );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatus( Long operatorId, Long id, Integer status ){
        EnableStatus.throwIfInvalid( status );
        Category category = Category.builder()
                .id( id )
                .status( status )
                .build();
        // AOP 填写审计字段
        int row = categoryMapper.updateById( category );
        NotFoundUtil.throwIfNotUpdated( row, MessageConstant.CATEGORY_NOT_FOUND );
    }

    @Transactional( readOnly = true )
    @Override
    public List< CategoryListItemVO > listByType( Integer type ){
        /*
            搞清楚业务场景就好办了
            下拉列表展示该类型的所有启用的分类
            于是对于传了类型的情况, 可以检验 type 是否有效
            没传类型, 直接在 sql 忽略 type
            然后注意排序、筛选 status
         */
        if ( type != null ){
            CategoryType.throwIfInvalid( type );
        }
        List< CategoryListItemVO > categoryListItemVOList = categoryMapper.selectItemVOListByStatusAndType(
                EnableStatus.ENABLED.getStatus(), type
        );
        return categoryListItemVOList;
    }

    @Transactional( readOnly = true )
    @Override
    public List< UserCategoryListItemVO > listEnabledByType( Integer type ){
        if ( type != null ){
            CategoryType.throwIfInvalid( type );
        }
        List< UserCategoryListItemVO > categoryListItemVOs = categoryMapper.selectByTypeAndStatus( type, EnableStatus.ENABLED.getStatus() );
        return categoryListItemVOs;
    }


    // ==================== 私有辅助方法 ====================

}
