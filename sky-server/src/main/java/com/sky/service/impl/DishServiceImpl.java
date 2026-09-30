package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.convertor.DishConverter;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.enumeration.EnableStatus;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.CreationNotAllowedException;
import com.sky.exception.NotFoundException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.DishCacheManager;
import com.sky.utils.NotFoundUtil;
import com.sky.utils.PageUtil;
import com.sky.utils.SetmealCacheManager;
import com.sky.vo.DishVO;
import com.sky.vo.dish.DishPageQueryItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealMapper setmealMapper;
    private final RedisTemplate< String, Object > redisTemplate;
    private final SetmealCacheManager setmealCacheManager;
    private final DishCacheManager dishCacheManager;
    private final DishConverter dishConverter;
    private final CategoryMapper categoryMapper;


    // ==================== 构造器 ====================
    public DishServiceImpl( DishMapper dishMapper, DishFlavorMapper dishFlavorMapper, SetmealMapper setmealMapper, RedisTemplate< String, Object > redisTemplate, SetmealCacheManager setmealCacheManager, DishCacheManager dishCacheManager, DishConverter dishConverter, CategoryMapper categoryMapper ){
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
        this.setmealMapper = setmealMapper;
        this.redisTemplate = redisTemplate;
        this.setmealCacheManager = setmealCacheManager;
        this.dishCacheManager = dishCacheManager;
        this.dishConverter = dishConverter;
        this.categoryMapper = categoryMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithFlavors( Long operatorId, DishDTO dishDTO ){
        /*
            * 业务逻辑
              1. 新增菜品, 同时新增菜品关联的口味
              2. 由于用户端使用缓存展示菜品数据, 改动菜品数据需要清理旧缓存
              3. 我自己补了一个: 如果关联的分类被禁用, 则拒绝创建该菜品, 理论上前端创建菜品时只会展示启用的分类
            * 思路
              - converter 把 dto 转实体对象用来插入数据
              - 菜品字段设置默认值
              - 拿出 dto 里面的菜品口味, 设置关联的菜品 id 逻辑外键再写入数据库
              - 拼接 redis 键, 清理缓存
         */
        Long categoryId = dishDTO.getCategoryId();
        Integer categoryStatus = categoryMapper.selectStatusById( categoryId );
        NotFoundUtil.throwIfNotFound( categoryStatus, MessageConstant.CATEGORY_NOT_FOUND );

        if ( EnableStatus.isDisabled( categoryStatus ) ){
            throw new CreationNotAllowedException( MessageConstant.GOODS_CREATE_FAILED_BECAUSE_CATEGORY_DISABLED );
        }

        Dish dish = dishConverter.toEntity( dishDTO );

        // 注意部分字段如果没有设置值，需要手动设置默认值，比如 status 需要显示启用
        if ( dish.getStatus() == null ){
            dish.setStatus( EnableStatus.ENABLED.getStatus() );
        }

        // 审计字段交给 AOP 处理
        dishMapper.insert( dish );
        Long id = dish.getId();
        if ( id == null ){
            String message = "插入菜品数据时, 主键回填失败";
            log.error( "{}, 菜品信息 = {}", message, dish );
            throw new IllegalStateException( message );
        }

        // 注意 Mapper 是否保存了所有内容: 实体类对象所有属性( DTO 属性 + 审计字段 ) + 关联内容 = 表数据
        // 新增菜品时 dishDTO.getId() 为 null，应该用主键回填后的 dish.getId()
        saveDishFlavors( id, dishDTO.getFlavors() );

        /*
            精细修改缓存: 只修改该菜品所在的分类缓存, 也就需要拿到 RedisKey = 业务前缀:分类 id
         */
        // categoryId 一定非空
        dishCacheManager.deleteRedisKey( categoryId );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult pageQuery( DishPageQueryDTO dishPageQueryDTO ){
        /*
            * 补充思路描述
            1. 之前的思路
                * 联合 dish, category 查出不带口味的 dishVoList
                * 再拿着查出来的 dishId 去找对应的口味、按 dishId 分组、组装完整 dishVOList
                * 并且之前以为 dishVOList 设置 dishFlavors 后就不能转换成 Page< DishVO > 了
                  - 于是之前是先拿到 total 行数、再用 total 和链表来组装 pageResult
                  - 但实际上直接组装 PageResult 就行
            2. 现在
                * 只展示必要的字段, 而前端列表展示不需要口味之类的
            3. 也在查询的时候对查询字段做一些业务检验
         */
        Integer status = dishPageQueryDTO.getStatus();
        if ( status != null ){
            EnableStatus.throwIfInvalid( status );
        }

        PageUtil.validateParamsAndStartPage( dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize() );
        List< DishPageQueryItemVO > dishPageQueryItemVOList = dishMapper.selectItemVOByQueryWithoutFlavors( dishPageQueryDTO );

        return PageUtil.buildPageResult( dishPageQueryItemVOList );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteByIds( Long operatorId, List< Long > ids ){
        /*
            思路1: ✅
                - 先查目标菜品，检查有无 status = 1 的菜品，有则抛异常
                - 然后查有无关联目标菜品的套餐，有则抛异常
                - 没有异常才进行删除
            思路2:
                - 底层直接删除，但是避开 status = 1、被套餐关联的菜品
                - 拿到返回的 affectedRows 跟 ids 数量对比，少了就抛异常
                - 也就是已经完成删除，但还是提醒用户有不符合条件的数据(没删)
         */
        // 提前结束, 同时避免 WHERE id IN( .. ) 报错
        if ( ids == null || ids.isEmpty() ){
            return;
        }

        // 安全感
        ids = ids.stream()
                .filter( Objects::nonNull )
                .filter( id -> id > 0L )
                .distinct()
                .toList();
        if ( ids.isEmpty() ){
            return;
        }

        // 不需要查对象，直接 count 计数
        if ( dishMapper.countByIdsAndStatus( ids, EnableStatus.ENABLED.getStatus() ) > 0L ){
            throw new DeletionNotAllowedException( MessageConstant.DISH_ON_SALE );
        }

        if ( setmealMapper.countByDishIds( ids ) > 0L ){
            throw new DeletionNotAllowedException( MessageConstant.DISH_BE_RELATED_BY_SETMEAL );
        }

        // 精准删除 RedisKey, 需要知道该批菜品的 categoryId, 然后删除这些分类的缓存
        List< Long > categoryIds = dishMapper.selectCategoryIdsByIds( ids );
        dishCacheManager.deleteRedisKeys( categoryIds );

        // 先删子表，后删主表
        dishFlavorMapper.deleteBatchByDishIds( ids );
        dishMapper.deleteBatch( ids );
    }


    @Transactional( readOnly = true )
    @Override
    public DishVO getInfoById( Long id ){
        DishVO dishVO = dishMapper.selectVOByIdWithoutFlavors( id );
        // 注意 MyBatis 单条查询可能返回 null( 多条查询返回空链表 )
        NotFoundUtil.throwIfNotFound( dishVO, MessageConstant.DISH_NOT_FOUND );
        dishVO.setFlavors( dishFlavorMapper.selectByDishId( id ) );
        return dishVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( DishDTO dishDTO ){
        // 清除缓存: 需要知道修改前后的分类 id
        Long dishId = dishDTO.getId();
        Dish oldDishCategoryId = dishMapper.selectCategoryIdById( dishId );
        Long oldCategoryId = oldDishCategoryId.getCategoryId();
        dishCacheManager.deleteRedisKey( oldCategoryId );
        // 受影响的分类都需要清缓存
        Long newCategoryId = dishDTO.getCategoryId();
        if ( ! oldCategoryId.equals( newCategoryId ) ){
            dishCacheManager.deleteRedisKey( newCategoryId );
        }

        /*
            全面更新 Dish 信息 = Dish( 包含 CategoryId ) + DishFlavors
         */
        // Dish 主体直接改, 只需要注意空字段不写入数据库
        Dish dish = new Dish();
        BeanUtils.copyProperties( dishDTO, dish );
        // 注意审计字段交给 AOP 填充
        int affectedRow = dishMapper.update( dish );
        if ( affectedRow <= 0 ){
            // 修改行数 = 0 => DTO 的 DishId 不正确，
            throw new NotFoundException( MessageConstant.DISH_NOT_FOUND );
        }

        // 关联的子表 DishFlavor 删了重装更方便
        dishFlavorMapper.deleteBatchByDishIds( List.of( dishId ) );
        saveDishFlavors( dishId, dishDTO.getFlavors() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatusById( Long id, Integer status ){
        StatusConstant.validateStatus( status );
        Dish dish = new Dish();
        dish.setId( id );
        dish.setStatus( status );
        // AOP 填写审计字段
        int affectedRow = dishMapper.updateStatusById( dish );
        if ( affectedRow <= 0 ){
            throw new NotFoundException( MessageConstant.DISH_NOT_FOUND );
        }

        // RedisKey = 业务前缀:分类 id, 就需要查到分类 id
        // 由于下面对 setmealList 判空、return，放到最后可能清不了缓存
        Dish dishCategoryId = dishMapper.selectCategoryIdById( id ); // 由于之前判断过修改行数，所以这里 dishId 一定有效
        dishCacheManager.deleteRedisKey( dishCategoryId.getCategoryId() );

        // 还要注意业务约束: 如果是禁用菜品，那也要禁用包含此菜品的套餐
        if ( StatusConstant.isDisable( status ) ){
            /*
            // 思路 1: 直接传 setmeal 对象，修改 status
            // 但是发现它没有 dishId 属性
            // 而且 1 个 dishId 对应多个 setmeal
            Setmeal setmeal = new Setmeal();
            setmeal.sestDishId( id );
            setmeal.setStatus( StatusConstant.DISABLE );
            setmealMapper.updateBatchStatusByDishId( setmeal );
             */

            // 思路 2: 先拿到 dishId 对应的所有 setmeal, 修改后塞回数据库
            // 但是这样就用不了 AOP 了(针对单个对象), 需要手动设置审计字段
            // 怪不得教程循环调用 Mapper 的单个对象 update
            List< Setmeal > setmealList = setmealMapper.selectByDishId( id );
            if ( setmealList == null || setmealList.isEmpty() ){
                // 经 AI 提醒还要防止空数据入库
                return;
            }
            setmealList.forEach( setmeal -> {
                setmeal.setStatus( StatusConstant.DISABLE );
                setmeal.setUpdateTime( LocalDateTime.now() );
                setmeal.setUpdateUser( BaseContext.getCurrentId() );
                // 不过我觉得也可以修改 AOP，让它支持链表批量修改审计字段
            } );
            setmealMapper.updateBatchStatusByIds( setmealList );

            /*
                注意关联缓存:
                * 禁用菜品 -> 禁用套餐 -> 修改套餐状态 -> 需要拿到 categoryId, setmealId
             */
            // TODO: 如果 setmealMapper.selectByDishId 返回不完整的 setmeal 对象，这里需要调整
            List< Long > categoryIds = setmealList.stream().map( Setmeal::getCategoryId ).toList();
            List< Long > setmealIds = setmealList.stream().map( Setmeal::getId ).toList();
            // 调 SetmealServiceImpl 的 private / default 清除缓存方法 | 内部再写重复方法 | 放入 RedisConstant
            // 最终提取业务工具类
            setmealCacheManager.deleteRedisKeys( categoryIds, setmealIds );
        }
    }

    @Transactional( readOnly = true )
    @Override
    public List< DishVO > listByCategoryId( Long categoryId ){
        List< DishVO > dishVOList = dishMapper.selectDishVOByCategoryId( categoryId );
        return setFlavorsToDishVOList( dishVOList );
    }

    @Transactional( readOnly = true )
    @Override
    public List< DishVO > listEnabledByCategoryId( Long categoryId ){
        /*
            先看缓存有无该分类的菜品, 无则缓存菜品
            业务前缀:分类 id -> DishVOList 对象
            RedisKey -> Value
         */
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();

        String redisKey = RedisConstant.Dish.formatRedisKey( categoryId );
        Object object = valueOperations.get( redisKey );
        List< DishVO > dishVOList = ( List< DishVO > ) object;

        // 备注: 缓存中的空列表说明该分类确实没菜品，也应该返回前端, 无需 isEmpty 判断
        if ( dishVOList != null ){
            return dishVOList;
        }

        List< DishVO > enabled = dishMapper.selectDishVOByCategoryIdAndStatusWithoutFlavors( categoryId, StatusConstant.ENABLE );
        enabled = setFlavorsToDishVOList( enabled );
        // 业务前缀:分类 id -> DishVOList 对象, 同时注意缓存有效时间
        valueOperations.set( redisKey, enabled, RedisConstant.Dish.CACHE_TTL );

        return enabled;
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 提取 save, update 的公有逻辑 - 给某个菜品添加口味
     *
     * @param dishId 菜品 id, <strong>由调用方检验，确保传入合法参数</strong>
     * @param dishFlavors 菜品口味链表, <strong>由于业务允许菜品口味为空，所以这里判空后不抛异常</strong>
     */
    private void saveDishFlavors( Long dishId, List< DishFlavor > dishFlavors ){
        // 空链表不能丢进 SQL
        if ( dishFlavors == null || dishFlavors.isEmpty() ){
            return;
        }
        // 关联表设置关联字段
        dishFlavors.forEach( dishFlavor -> dishFlavor.setDishId( dishId ) );
        dishFlavorMapper.insertBatch( dishFlavors );
    }

    /**
     * 抽取公共逻辑: dishVOList 判空、设置 DishFlavorList
     * <br>
     * DishVO = Dish + categoryName + DishFlavorList
     *
     * @param dishVOList Mapper 查询好的 VO 列表(不带 DishFlavors )
     * @return 完整的 VO 列表
     */
    private List< DishVO > setFlavorsToDishVOList( List< DishVO > dishVOList ){
        if ( dishVOList == null || dishVOList.isEmpty() ){
            // 测试发现 DishFlavorMapper 报错空 in( )，需要提前对 id 链表判空
            return dishVOList;
        }

        // 管理端原型只需要展示 name, price, status, 接口文档也只涉及 Dish 属性 + CategoryName，无 DishFlavor
        // 但是用户端需要全部展示
        // 而且从业务上来看，应该保持 VO 完整，所以这里还是要设置 DishFlavor
        List< Long > dishIdList = dishVOList.stream().map( DishVO::getId ).toList();
        List< DishFlavor > dishFlavors = dishFlavorMapper.selectByDishIdList( dishIdList );
        Map< Long, List< DishFlavor > > dishIdToFlavorsMap = dishFlavors.stream().collect( Collectors.groupingBy( DishFlavor::getDishId ) );
        dishVOList.forEach( dishVO -> dishVO.setFlavors(
                dishIdToFlavorsMap.getOrDefault( dishVO.getId(), dishVO.getFlavors() )
        ) );

        return dishVOList;
    }




}
