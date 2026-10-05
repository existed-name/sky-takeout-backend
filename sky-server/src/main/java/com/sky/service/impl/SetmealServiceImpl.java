package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.convertor.SetmealConverter;
import com.sky.dto.setmeal.SetmealDTO;
import com.sky.dto.setmeal.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.GoodsStatus;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.AssertUtils;
import com.sky.utils.ListUtils;
import com.sky.utils.NotFoundUtil;
import com.sky.utils.PageUtil;
import com.sky.utils.SetmealCacheManager;
import com.sky.vo.SetmealVO;
import com.sky.vo.setmeal.SetmealInfoVO;
import com.sky.vo.setmeal.SetmealPageQueryItemVO;
import com.sky.vo.user.setmeal.UserSetmealDishItemVO;
import com.sky.vo.user.setmeal.UserSetmealListItemVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final DishMapper dishMapper;
    private final RedisTemplate< String, Object > redisTemplate;
    private final SetmealCacheManager setmealCacheManager;
    private final SetmealConverter setmealConverter;
    private final CategoryMapper categoryMapper;


    // ==================== 构造器 ====================
    public SetmealServiceImpl( SetmealMapper setmealMapper, SetmealDishMapper setmealDishMapper, DishMapper dishMapper, RedisTemplate< String, Object > redisTemplate, SetmealCacheManager setmealCacheManager, SetmealConverter setmealConverter, CategoryMapper categoryMapper ){
        this.setmealMapper = setmealMapper;
        this.setmealDishMapper = setmealDishMapper;
        this.dishMapper = dishMapper;
        this.redisTemplate = redisTemplate;
        this.setmealCacheManager = setmealCacheManager;
        this.setmealConverter = setmealConverter;
        this.categoryMapper = categoryMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithRelation( Long operatorId, SetmealDTO setmealDTO ){
        /*
            整理思路
            setmealDTO = setmeal + setmealDish 关联对象，分 2 批插入数据库
            setmeal 用 converter 转换映射
            setmealDish 需要设置逻辑外键 setmealId( 前端可能没有自带, 需要绑定当前套餐、主键回填 )

            然后清理缓存
            套餐缓存 => 整个分类的缓存、关联菜品的缓存
            该套餐所在分类的缓存
            由于是新增, 之前并没有缓存关联菜品, 所以删、不删都一样

            额外的注意事项
            dto 部分字段选填, 比如 status 需要手动设置, 但如果前端显式传入需要判断有效性
            关联的分类是否存在
         */
        //
        Setmeal setmeal = setmealConverter.toEntity( setmealDTO );
        Integer status = setmeal.getStatus();
        if ( status != null ){
            GoodsStatus.throwIfInvalid( status );
        } else {
            setmeal.setStatus( GoodsStatus.DEFAULT_SETMEAL_STATUS.getStatus() );
        }

        Long categoryId = setmealDTO.getCategoryId();
        long countByCategoryId = categoryMapper.countById( categoryId );
        NotFoundUtil.throwIfCountZero( countByCategoryId, MessageConstant.CATEGORY_NOT_FOUND );
        setmealMapper.insert( setmeal );

        Long id = setmeal.getId();
        String message = String.format( "插入套餐数据时, 主键回填失败, 套餐名称 = %s , 分类 id = %s", id, categoryId );
        AssertUtils.assertIdBackfilled( id, message );

        List< SetmealDish > setmealDishes = setmealDTO.getSetmealDishes();
        if ( ! ListUtils.isEmpty( setmealDishes ) ){
            setmealDishes.forEach( setmealDish -> setmealDish.setSetmealId( id ) );
            setmealDishMapper.insertBatch( setmealDishes );
        }

        /*
                增删改对缓存的影响 -> 只看有缓存的那端(用户端)
            * 套餐缓存 RedisKey = setmeal:category:{categoryId}
              - 该分类内容发生变化，就需要删除缓存
            * 套餐关联菜品缓存 RedisKey = setmeal:dish:{setmealId}
              - 该套餐发生变化，就需要删除缓存
              - 虽然新增套餐, 之前还并没有缓存关联菜品, 不过也可以把 id 传进去删除
         */
        setmealCacheManager.deleteRedisKey( categoryId, id );
    }


    @Transactional( readOnly = true )
    @Override
    public PageResult page( SetmealPageQueryDTO setmealPageQueryDTO ){
        /*
            前端只展示套餐本体信息, 不用查询关联菜品
            只需要套餐、分类联合拿到分类名称就行
         */
        Integer status = setmealPageQueryDTO.getStatus();
        if ( status != null ){
            GoodsStatus.throwIfInvalid( status );
        }

        PageUtil.validateParamsAndStartPage( setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize() );
        List< SetmealPageQueryItemVO > setmealPageQueryItemVOList = setmealMapper.selectItemVOByQueryWithoutSetmealDishRelation( setmealPageQueryDTO );
        return PageUtil.buildPageResult( setmealPageQueryItemVOList );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteBatch( Long operatorId, List< Long > ids ){
        /*
            整理思路
            删除套餐, 关联数据是套餐和菜品之间的关系, 可以直接删掉
            最后再清理缓存

            特殊业务要求
            起售的套餐不能删, 这里考虑只要选中的套餐中有一个是起售状态, 那么整批都不能删除
            技巧: 直接用 ids、状态作统计条件, 而不是查询完整对象
         */
        if ( ListUtils.isEmpty( ids ) ){
            return;
        }

        // 可加可不加
        ids = ids.stream()
                .filter( Objects::nonNull )
                .filter( id -> id > 0 )
                .distinct()
                .toList();
        if ( ids.isEmpty() ){
            return;
        }

        // 业务检查: 起售的套餐不能删除
        if ( setmealMapper.countByIdsAndStatus( ids, GoodsStatus.ON_SALE.getStatus() ) > 0L ){
            throw new DeletionNotAllowedException( MessageConstant.SETMEAL_ON_SALE );
        }

        // 先删除关联内容(子表)，后删主表
        setmealDishMapper.deleteBySetmealIds( ids );
        setmealMapper.deleteByIds( ids );

        // 清除目标分类、套餐关联菜品缓存
        List< Long > categoryIds = setmealMapper.selectCategoryIdsByIds( ids );
        setmealCacheManager.deleteRedisKeys( categoryIds, ids );
    }


    @Transactional( readOnly = true )
    @Override
    public SetmealInfoVO getInfo( Long id ){
        // setmealInfoVO = setmeal + categoryName + setmealDishes
        SetmealInfoVO setmealInfoVO = setmealMapper.selectInfoVOByIdWithoutSetmealDishRelation( id );
        NotFoundUtil.throwIfNotFound( setmealInfoVO, MessageConstant.SETMEAL_NOT_FOUND );

        List< SetmealDish > setmealDishes = setmealDishMapper.selectBySetmealId( id );
        setmealInfoVO.setSetmealDishes( setmealDishes );

        return setmealInfoVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( Long operatorId, Long id, SetmealDTO setmealDTO ){
        /*
            整理思路
            更新套餐本体、用新的关联菜品覆盖旧的关联关系
            注意关联菜品信息需要设置逻辑外键( setmealId )

            最后来清理缓存
            套餐缓存 → 所在分类的缓存( 套餐本体列表 ) + 对应的关联菜品缓存( 关联关系列表 )
            细节在于清理所有受影响的缓存 → 更新前、更新后的分类 → 必须在更新数据库之前查出旧的分类 id
         */
        Long oldCategoryId = setmealMapper.selectCategoryIdById( id );
        NotFoundUtil.throwIfNotFound( oldCategoryId, MessageConstant.SETMEAL_NOT_FOUND );

        Integer status = setmealDTO.getStatus();
        if ( status != null ){
            GoodsStatus.throwIfInvalid( status );
        }

        // SetmealDTO = Setmeal + 关联关系 SetmealDish
        Setmeal setmeal = setmealConverter.toEntity( setmealDTO );
        setmeal.setId( id );
        int updateRow = setmealMapper.updateById( setmeal );

        // 关联子表内容删了重装
        setmealDishMapper.deleteBatchBySetmealId( id );
        List< SetmealDish > setmealDishes = setmealDTO.getSetmealDishes();
        if ( ! ListUtils.isEmpty( setmealDishes ) ){
            // 又忘了写子表的关联逻辑外键
            setmealDishes.forEach( setmealDish -> setmealDish.setSetmealId( id ) );
            setmealDishMapper.insertBatch( setmealDishes );
        }

        // 之前的 categoryId
        setmealCacheManager.deleteRedisKey( oldCategoryId, id );
        // 修改后的
        Long newCategoryId = setmealDTO.getCategoryId();
        if ( ! oldCategoryId.equals( newCategoryId ) ){
            // 虽然说这里重复删了 id
            setmealCacheManager.deleteRedisKey( newCategoryId, id );
        }
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatusById( Long operatorId, Long id, Integer newStatus ){
        /*
            更新套餐状态、清理相关缓存
            业务约束: 包含停售菜品的套餐不能起售
         */
        GoodsStatus.throwIfInvalid( newStatus );

        if ( GoodsStatus.isOnSale( newStatus ) ){
            /*
                之前想的
                先从套餐-菜品关联关系拿到对应的菜品 id
                然后去查有多少菜品
                防止 in() 里面进入空链表

                问 ai 知道 in() 里面子查询为空不会报错, 于是改成了子查询
             */
            if ( dishMapper.countBySetmealIdAndStatus( id, GoodsStatus.DISCONTINUED.getStatus() ) > 0L ){
                throw new SetmealEnableFailedException( MessageConstant.SETMEAL_ENABLE_FAILED_BECAUSE_DISH_DISABLED );
            }
        }

        // 注意还要更新审计字段，需要传对象
        Setmeal updatedSetmeal = Setmeal.builder().id( id ).status( newStatus ).build();
        int updateRow = setmealMapper.updateStatusById( updatedSetmeal );
        NotFoundUtil.throwIfNotUpdated( updateRow, MessageConstant.SETMEAL_NOT_FOUND );

        // 精确删除缓存需要查到 categoryId
        Long categoryId = setmealMapper.selectCategoryIdById( id );
        setmealCacheManager.deleteRedisKey( categoryId, id );
    }

    @Transactional( readOnly = true )
    @Override
    public List< SetmealVO > listByCategoryId( Long categoryId ){
        List< SetmealVO > setmealVOList = setmealMapper.selectSetmealVOByCategoryIdWithoutSetmealDishRelation( categoryId );
        return setRelationListToSetmealVOList( setmealVOList );
    }

    @Transactional( readOnly = true )
    @Override
    public List< UserSetmealListItemVO > listEnabledByCategoryId( Long categoryId ){
        /*
            分类存在性判断
            缓存是否包含套餐列表, 无缓存则查库、设置缓存

            容易昏的是缓存 RedisKey
            套餐 = 套餐本体 + 关联菜品信息
            这里根据业务场景拆分的
            1 个分类 id → 1 个套餐本体列表
            1 个套餐 id → 1 个关联菜品信息列表
         */
        long countById = categoryMapper.countById( categoryId );
        NotFoundUtil.throwIfCountZero( countById, MessageConstant.CATEGORY_NOT_FOUND );

        String redisKey = RedisConstant.Setmeal.formatRedisKey( categoryId );
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();
        Object object = valueOperations.get( redisKey );
        List< UserSetmealListItemVO > setmealListItemVOs = ( List< UserSetmealListItemVO > ) object;
        // 空列表也算缓存内容
        if ( setmealListItemVOs != null ){
            return setmealListItemVOs;
        }

        List< UserSetmealListItemVO > enabled = setmealMapper.listItemVOsByCategoryIdAndStatusWithoutRelation( categoryId, GoodsStatus.ON_SALE.getStatus() );
        valueOperations.set( redisKey, enabled, RedisConstant.Setmeal.CACHE_TTL );
        return enabled;
    }

    @Transactional( readOnly = true )
    @Override
    public List< UserSetmealDishItemVO > listDishItemVOById( Long id ){
        /*
            判断套餐存在性
            查缓存、不存在则查数据库 + 设置缓存
            空列表也算缓存
            对象 UserSetmealDishItemVO = SetmealDish 的 copies + Dish 的 name, description, image

            1 个分类对应的套餐列表
            setmeal:categoryId:{categoryId} → List< Setmeal... >
            1 个套餐对应的菜品列表
            setmeal:dish:{setmealId} → List< SetmealDishItemVO >
         */
        NotFoundUtil.throwIfCountZero( setmealMapper.countById( id ), MessageConstant.SETMEAL_NOT_FOUND );

        String redisKey = RedisConstant.Setmeal.formatDishRedisKey( id );
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();
        Object object = valueOperations.get( redisKey );
        List< UserSetmealDishItemVO > setmealDishItemVOList = ( List< UserSetmealDishItemVO > ) object;
        if ( setmealDishItemVOList != null ){
            return setmealDishItemVOList;
        }

        setmealDishItemVOList = setmealDishMapper.selectDishItemVOListBySetmealId( id );
        valueOperations.set( redisKey, setmealDishItemVOList, RedisConstant.Setmeal.DISH_CACHE_TTL );
        return setmealDishItemVOList;
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 抽取公共逻辑: SetmealVOList 判空、设置 setmealDish 关联对象
     * <br>
     * SetmealVO = Setmeal + categoryName + SetmealDishList 关联关系
     *
     * @param setmealVOList Mapper 查询好的 VO 列表(不带 setmealDish 关联关系 )
     * @return 完整的 VO 列表
     */
    private List< SetmealVO> setRelationListToSetmealVOList( List< SetmealVO > setmealVOList ){
        // 由于之后要查关联关系, 所以先判空看需不需要后续查询
        if ( setmealVOList == null || setmealVOList.isEmpty() ){
            return setmealVOList;
        }

        List< Long > setmealIds = setmealVOList.stream().map( SetmealVO::getId ).toList();
        List< SetmealDish > allSetmealDishes = setmealDishMapper.selectBySetmealIds( setmealIds );
        // 再分出各个套餐对应的关系
        Map< Long, List< SetmealDish > > setmealIdToRelationMap = allSetmealDishes.stream().collect( Collectors.groupingBy( SetmealDish::getSetmealId ) );
        setmealVOList.forEach(
                setmealVO -> setmealVO.setSetmealDishes(
                        setmealIdToRelationMap.getOrDefault(
                                setmealVO.getId(), setmealVO.getSetmealDishes()
                        )
                ) );

        return setmealVOList;
    }

}
