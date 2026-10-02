package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.convertor.SetmealConverter;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.GoodsStatus;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.NotFoundException;
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
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
        PageUtil.validateParamsAndStartPage( setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize() );
        // SetmealVO = Setmeal + CategoryName + 关联关系 SetmealDish
        // 先联表查 Setmeal + CategoryName
        List< SetmealVO > setmealVOList = // null; 测试
                setmealMapper.selectSetmealVOByQueryWithoutSetmealDishRelation( setmealPageQueryDTO );

        // 考虑判空
        if ( setmealVOList == null ){
            return PageUtil.buildEmptyPageResult();
        }

        // 注意第 1 次查询后记录总数据数
        // null 可以强转为任意类型还是 null, 再调 getTotal 就可能空指针
        Page< SetmealVO > setmealVOPage = ( Page< SetmealVO > ) setmealVOList;
        long setmealVOPageTotal = setmealVOPage.getTotal();

        // 再查关联对象 SetmealDish
        // 查询之前先检验参数
        if ( setmealVOPage.isEmpty() ){
            return PageUtil.buildPageResult( setmealVOPageTotal, setmealVOPage );
        }

        List< Long > setmealIds = setmealVOList.stream().map( SetmealVO::getId ).toList();
        List< SetmealDish > setmealDishList = setmealDishMapper.selectBySetmealIds( setmealIds );

        // 注意多个套餐 id 对应的一大批关联对象需要分组
        Map< Long, List< SetmealDish > > setmealIdToSetmealDishesMap = setmealDishList.stream().collect( Collectors.groupingBy( SetmealDish::getSetmealId ) );
        // 最后组装完整 SetmealVO
        setmealVOList.forEach( setmealVO -> setmealVO.setSetmealDishes(
                setmealIdToSetmealDishesMap.getOrDefault(
                        setmealVO.getId(), setmealVO.getSetmealDishes()
                )
        ) );

        return PageUtil.buildPageResult( setmealVOPageTotal, setmealVOPage );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteBatch( List< Long > ids ){
        if ( ids == null || ids.isEmpty() ){
            return;
        }

        // 业务检查: 起售的套餐不能删除
        List< Setmeal > setmealList = setmealMapper.selectByIds( ids );
        if ( setmealList == null || setmealList.isEmpty() ){
            throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
        }
        for ( Setmeal setmeal : setmealList ){
            // 查询获得的单个元素不可能为 null
            if ( StatusConstant.isEnable( setmeal.getStatus() ) ){
                throw new DeletionNotAllowedException( MessageConstant.SETMEAL_ON_SALE );
            }
        }

        // 先删除关联内容(子表)，后删主表
        setmealDishMapper.deleteBySetmealIds( ids );
        setmealMapper.deleteByIds( ids );

        // 清除目标分类、套餐关联菜品缓存
        // TODO: 如果 setmealMapper.selectByIds 改为只查套餐状态而不是完整对象，那这里需要再单独查 categoryId
        List< Long > categoryIds = setmealList.stream().map( Setmeal::getCategoryId ).toList();
        setmealCacheManager.deleteRedisKeys( categoryIds, ids );
    }


    @Transactional( readOnly = true )
    @Override
    public SetmealVO getInfo( Long id ){
        if ( id == null || id <= 0 ){
            return null;
        }

        // setmealVO = setmeal + categoryName + setmealDishes
        SetmealVO setmealVO = setmealMapper.selectSetmealVOByIdWithoutSetmealDishRelation( id );
        if ( setmealVO == null ){
            throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
        }
        List< SetmealDish > setmealDishes = setmealDishMapper.selectBySetmealId( id );
        setmealVO.setSetmealDishes( setmealDishes );

        return setmealVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( SetmealDTO setmealDTO ){
        // 删除缓存, 由于是修改信息操作，需要注意修改前后 redisKey 的组成部分 categoryId 有无变化
        Long setmealId = setmealDTO.getId();
        Setmeal setmealCategoryId = setmealMapper.selectCategoryIdById( setmealId );
        if ( setmealCategoryId == null ){
            throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
        }
        Long oldCategoryId = setmealCategoryId.getCategoryId();
        // 之前的 categoryId
        setmealCacheManager.deleteRedisKey( oldCategoryId, setmealId );
        // 修改后的
        Long newCategoryId = setmealDTO.getCategoryId();
        if ( ! oldCategoryId.equals( newCategoryId ) ){
            // 虽然说这里重复删了 setmealId
            setmealCacheManager.deleteRedisKey( newCategoryId, setmealId );
        }

        // SetmealDTO = Setmeal + 关联关系 SetmealDish
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties( setmealDTO, setmeal );

        int updateRow = setmealMapper.update( setmeal );
        /*
            前面组装 RedisKey 使用 id 查了对象，已经判断了对象存在
        if ( updateRow <= 0 ){
            throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
        }
         */

        // 关联子表内容删了重装
        // 本来应该分开单独写方法，但是这里的批量删除方法大概率不会改，所以就复用了
        setmealDishMapper.deleteBySetmealIds( List.of( setmealId ) );
        List< SetmealDish > setmealDishes = setmealDTO.getSetmealDishes();
        if ( setmealDishes == null || setmealDishes.isEmpty() ){
            return;
        }
        // 又忘了写子表的关联逻辑外键
        setmealDishes.forEach( setmealDish -> setmealDish.setSetmealId( setmealId ) );
        setmealDishMapper.insertBatch( setmealDishes );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatusById( Long id, Integer status ){
        StatusConstant.validateStatus( status );

        // 起售套餐时需要检查包含的菜品是不是停售的
        // 停售套餐则不需要修改菜品状态
        if ( StatusConstant.isEnable( status ) ){
            // 从套餐-菜品关联关系拿到对应的菜品 id
            List< SetmealDish > setmealDishList = setmealDishMapper.selectBySetmealId( id );
            if ( ! ( setmealDishList == null || setmealDishList.isEmpty() ) ){
                List< Long > dishIds = setmealDishList.stream().map( SetmealDish::getDishId ).toList();
                // 可以只查需要的字段，而不用查整个对象
                List< Integer > statusList = dishMapper.selectDishStatusListByIds( dishIds );
                for ( Integer dishStatus : statusList ){
                    if ( StatusConstant.isDisable( dishStatus ) ){
                        throw new SetmealEnableFailedException( MessageConstant.SETMEAL_ENABLE_FAILED );
                    }
                }
            }
        }

        // 注意还要更新审计字段，需要传对象
        Setmeal updatedSetmeal = Setmeal.builder().id( id ).status( status ).build();
        int updateRow = setmealMapper.updateStatusById( updatedSetmeal );
        if ( updateRow <= 0 ){
            throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
        }

        // 精确删除缓存需要查到 categoryId
        Setmeal setmealCategoryId = setmealMapper.selectCategoryIdById( id );
        setmealCacheManager.deleteRedisKey( setmealCategoryId.getCategoryId(), id );
    }

    @Transactional( readOnly = true )
    @Override
    public List< SetmealVO > listByCategoryId( Long categoryId ){
        List< SetmealVO > setmealVOList = setmealMapper.selectSetmealVOByCategoryIdWithoutSetmealDishRelation( categoryId );
        return setRelationListToSetmealVOList( setmealVOList );
    }

    @Transactional( readOnly = true )
    @Override
    public List< SetmealVO > listEnabledByCategoryId( Long categoryId ){
        /*
            缓存操作: 确定 RedisKey -> 查看是否已缓存对应的值 -> 有则直接返回，否则查数据库
            不过这里的缺点是 SetmealVO 跟关联菜品分开的，不过也可以分开缓存
         */
        String redisKey = RedisConstant.Setmeal.formatRedisKey( categoryId );
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();
        List< SetmealVO > setmealVOList = ( List< SetmealVO > ) valueOperations.get( redisKey );
        if ( setmealVOList != null ){
            // 空列表也算缓存内容
            return setmealVOList;
        }

        List< SetmealVO > enabled = setmealMapper.selectVOByCategoryIdAndStatusWithoutRelation( categoryId, StatusConstant.ENABLE );
        valueOperations.set( redisKey, enabled, RedisConstant.Setmeal.CACHE_TTL );
        // 这里不需要关联关系
        // return setRelationListToSetmealVOList( enabled );
        return enabled;
    }

    @Override
    public List< DishItemVO > listDishItemVOById( Long id ){
        // 业务模块: 套餐 - 改套餐关联的所有菜品，setmealId -> dishItemVoList
        // 业务前缀: setmeal:dish: 来区分
        String redisKey = RedisConstant.Setmeal.formatDishRedisKey( id );
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();
        List< DishItemVO > dishItemVOList = ( List< DishItemVO > ) valueOperations.get( redisKey );
        if ( dishItemVOList != null ){
            return dishItemVOList;
        }

        dishItemVOList = setmealMapper.selectDishItemVOListById( id );
        valueOperations.set( redisKey, dishItemVOList, RedisConstant.Setmeal.DISH_CACHE_TTL );
        return dishItemVOList;
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
