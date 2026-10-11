package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.convertor.DishConverter;
import com.sky.dto.dish.DishDTO;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.enumeration.EnableStatus;
import com.sky.enumeration.GoodsStatus;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.CreationNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.projection.setmeal.SetmealIdProjection;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.DishCacheManager;
import com.sky.utils.ListUtils;
import com.sky.utils.NotFoundUtil;
import com.sky.utils.PageUtil;
import com.sky.utils.SetmealCacheManager;
import com.sky.vo.admin.dish.DishInfoVO;
import com.sky.vo.admin.dish.DishListItemVO;
import com.sky.vo.admin.dish.DishPageQueryItemVO;
import com.sky.vo.user.dish.UserDishListItemVO;
import lombok.extern.slf4j.Slf4j;
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
    public PageResult< DishPageQueryItemVO > pageQuery( DishPageQueryDTO dishPageQueryDTO ){
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
    public DishInfoVO getInfoById( Long id ){
        DishInfoVO dishInfoVO = dishMapper.selectVOByIdWithoutFlavors( id );
        // 注意 MyBatis 单条查询可能返回 null( 多条查询返回空链表 )
        NotFoundUtil.throwIfNotFound( dishInfoVO, MessageConstant.DISH_NOT_FOUND );
        dishInfoVO.setFlavors( dishFlavorMapper.selectByDishId( id ) );
        return dishInfoVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( Long operatorId, Long id, DishDTO dishDTO ){
        /*
            整理思路
            * 写操作需要清理旧缓存
              - 而我这里是按分类做的缓存, 每个分类一部分缓存
              - 于是受影响的分类都需要清理缓存
              - 也就是修改前、后涉及到的分类
            * 而更新菜品时
              - 一个是菜品本身
              - 一个是关联内容, 也就是菜品口味
              - 关联内容直接覆盖, 删除旧数据、插入新数据, 避免一个一个对太麻烦
            * 写操作顺序
              - 问 ai 先写 Mysql 数据库, 后删 redis 缓存, 避免其他地方读的时候把数据库旧数据填入 redis
         */
        // 清除缓存: 需要知道修改前后的分类 id, 注意旧的分类 id 在数据库里面、必须先查出来、避免被新的覆盖
        Long oldCategoryId = dishMapper.selectCategoryIdById( id );
        NotFoundUtil.throwIfNotFound( oldCategoryId, MessageConstant.DISH_NOT_FOUND );

        /*
            全面更新 Dish 信息 = Dish( 包含 CategoryId ) + DishFlavors
         */
        Dish dish = dishConverter.toEntity( dishDTO );
        dish.setId( id );
        // 注意审计字段交给 AOP 填充
        int affectedRow = dishMapper.update( dish );

        // 关联的子表 DishFlavor 删了重装更方便
        dishFlavorMapper.deleteBatchByDishIds( List.of( id ) );
        saveDishFlavors( id, dishDTO.getFlavors() );

        dishCacheManager.deleteRedisKey( oldCategoryId );
        // 受影响的分类都需要清缓存
        Long newCategoryId = dishDTO.getCategoryId();
        if ( ! oldCategoryId.equals( newCategoryId ) ){
            dishCacheManager.deleteRedisKey( newCategoryId );
        }
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatusById( Long operatorId, Long id, Integer status ){
        /*
            写操作通用逻辑
            更新关联数据/子表、更新主表( 这俩看依赖顺序 ), 在最后清理关联的所有缓存

            补充完整思路
            主线还是更新菜品状态
            而牵扯到的就是分类、套餐,
            更新菜品状态肯定不能干扰分类里面的其他菜品, 所以不管菜品所在的分类
            而菜品、套餐是捆绑销售的, 具体说套餐是各个菜品组成的, 1 个菜品卖不了, 那套餐肯定也不能卖, 也禁用了

            缓存
            菜品 →
              菜品本体信息、关联口味是组装在一起的, 然后把一整个分类的菜品列表存进缓存
              于是修改该菜品就会影响所在分类的整个列表
              所以就把这个分类的缓存删掉
            套餐 →
              套餐本体信息、包含的菜品信息是拆开的, 分开管理
              1 个分类对应 1 个套餐本体信息列表, 而 1 个套餐对应 1 个菜品信息列表
              于是修改某个套餐, 就需要删除所在分类的列表缓存、对应的菜品信息列表缓存
         */
        GoodsStatus.throwIfInvalid( status );

        Dish dish = Dish.builder()
                .id( id )
                .status( status )
                .build();
        // AOP 填写审计字段
        int affectedRow = dishMapper.updateStatusById( dish );
        NotFoundUtil.throwIfNotUpdated( affectedRow, MessageConstant.DISH_NOT_FOUND );

        // 业务约束: 如果是禁用菜品，那也要禁用包含此菜品的套餐
        if ( EnableStatus.isDisabled( status ) ){
            /*
                思路 2:
                先拿到 dishId 对应的所有 setmeal, 修改后塞回数据库
                但是这样就用不了 AOP 了(针对单个对象), 需要手动设置审计字段
                怪不得教程循环调用 Mapper 的单个对象 update
                不过我觉得也可以修改 AOP，让它支持链表批量修改审计字段
             */
            List< SetmealIdProjection > setmealIdProjections = setmealMapper.selectIdProjectionsByDishId( id );
            boolean isBlank = setmealIdProjections == null || setmealIdProjections.isEmpty();

            // 由于最后需要清理菜品缓存, 所以这里不能判空后 return
            // 空只能说明菜品没有关联套餐, 不能说明这个菜品不存在
            if ( ! isBlank ){
                LocalDateTime now = LocalDateTime.now();
                List< Setmeal > setmealList = setmealIdProjections.stream()
                        .map( setmealIdProjection -> Setmeal.builder()
                                .id( setmealIdProjection.getId() )
                                .status( EnableStatus.DISABLED.getStatus() )
                                .updateTime( now )
                                .updateUser( operatorId )
                                .build()
                        )
                        .toList();
                setmealMapper.updateBatchStatusByIds( setmealList );

                // TODO: 这里也可以拆成 setmealMapper.selectIdsByDishId 和 selectCategoryIdsByDishId 分开查套餐、套餐分类的 id
                List< Long > setmealCategoryIds = setmealIdProjections.stream().map( SetmealIdProjection::getCategoryId ).toList();
                List< Long > setmealIds = setmealIdProjections.stream().map( SetmealIdProjection::getId ).toList();
                setmealCacheManager.deleteRedisKeys( setmealCategoryIds, setmealIds );
            }
        }

        Long categoryId = dishMapper.selectCategoryIdById( id );
        dishCacheManager.deleteRedisKey( categoryId );
    }

    @Transactional( readOnly = true )
    @Override
    public List< DishListItemVO > listByCategoryId( Long categoryId ){
        /*
            管理端需要查看实时数据, 不做缓存, 除非特殊需要
         */
        List< DishListItemVO > dishListItemVOList = dishMapper.listItemVOsByCategoryIdWithoutFlavors( categoryId );
        return dishListItemVOList;
    }

    @Transactional( readOnly = true )
    @Override
    public List< UserDishListItemVO > listEnabledByCategoryId( Long categoryId ){
        /*
            检验是否存在该分类
            之后先看缓存有无该分类的菜品, 无则查数据库、缓存菜品
            key = 业务前缀:分类 id -> value = DishVOList
         */
        long countByCategoryId = categoryMapper.countById( categoryId );
        NotFoundUtil.throwIfCountZero( countByCategoryId, MessageConstant.CATEGORY_NOT_FOUND );

        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();
        String redisKey = RedisConstant.Dish.formatRedisKey( categoryId );
        Object object = valueOperations.get( redisKey );
        /*
            IDE 警告强转有问题, 但是为什么端到端测试正常展示数据, 前后端也没有遇到问题?
            主要原因是 com.sky.config.RedisConfiguration 配置了序列化器,
            显式设置 GenericJackson2JsonRedisSerializer
            写入时会在 JSON 里带 @class 类型信息，读取时自动还原具体对象，强转基本安全,
            但如果改了类、跨服务, 还是会有风险
            TODO: 封装泛型读取方法, 读取的对象用 objectMapper.convertValue + 指定 TypeReference 显式转换
         */
        List< UserDishListItemVO > dishListItemVOs = ( List< UserDishListItemVO > ) object;

        // 备注: 缓存中的空列表说明该分类确实没菜品，也应该返回前端, 无需 isEmpty 判断
        if ( dishListItemVOs != null ){
            return dishListItemVOs;
        }

        List< UserDishListItemVO > enabled = dishMapper.listItemVOsByCategoryIdAndStatusWithoutFlavors( categoryId, GoodsStatus.ON_SALE.getStatus() );
        setFlavorsToDishVOList( enabled );
        valueOperations.set( redisKey, enabled, RedisConstant.Dish.CACHE_TTL );

        return enabled;
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 提取 save, update 的公有逻辑 - 给某个菜品添加口味
     *
     * @param dishId 菜品 id, <strong>由调用方检验，确保传入合法参数</strong>
     * @param dishFlavors 菜品口味链表, <strong>null 或者空链表直接结束</strong>
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
     * 查询某批 dishVO 对应的口味, 并组装到 dishVO 中( DishVO = Dish + categoryName + DishFlavorList )
     * <p>
     *     如果其他地方同样需要查询、设置菜品口味, 可以考虑把那个地方的 dishVO 用{@link DishConverter}转成当前
     *     的参数类型, 然后再拿到菜品口味或者转回原类型列表
     * </p>
     *
     * @param dishItemVOs 通过 Mapper 查询到的 VO 列表, 不带 DishFlavors, 包含 id 等字段
     * @return 设置菜品口味的数据项列表, 可以忽略该返回值, 因为方法内部没有创建新链表、直接通过对象引用设置字段
     */
    private List< UserDishListItemVO > setFlavorsToDishVOList( List< UserDishListItemVO > dishItemVOs ){
        /*
            逻辑
            拿到这批 dish 的 id 查出对应的口味
            然后按照 dishId 分组、组装
         */
        // 提前结束, 同时避免空链表进入 sql in( . )
        if ( ListUtils.isEmpty( dishItemVOs ) ){
            return dishItemVOs;
        }

        List< Long > dishIdList = dishItemVOs.stream().map( UserDishListItemVO::getId ).toList();
        List< DishFlavor > dishFlavors = dishFlavorMapper.selectByDishIdList( dishIdList );
        Map< Long, List< DishFlavor > > dishIdToFlavorsMap = dishFlavors.stream().collect( Collectors.groupingBy( DishFlavor::getDishId ) );
        dishItemVOs.forEach( dishItemVO -> dishItemVO.setFlavors(
                dishIdToFlavorsMap.getOrDefault( dishItemVO.getId(), dishItemVO.getFlavors() )
        ) );

        return dishItemVOs;
    }




}
