package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.NotFoundException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.PageUtil;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealMapper setmealMapper;


    // ==================== 构造器 ====================
    public DishServiceImpl( DishMapper dishMapper, DishFlavorMapper dishFlavorMapper, SetmealMapper setmealMapper ){
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
        this.setmealMapper = setmealMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithFlavor( DishDTO dishDTO ){
        Dish dish = new Dish();
        BeanUtils.copyProperties( dishDTO, dish );

        // 注意部分字段如果没有设置值，需要手动设置默认值，比如 status 需要显示启用
        if ( dish.getStatus() == null ){
            dish.setStatus( StatusConstant.ENABLE );
        }

        // 审计字段交给 AOP 处理
        dishMapper.insert( dish );

        // 注意 Mapper 是否保存了所有内容: 实体类对象所有属性( DTO 属性 + 审计字段 ) + 关联内容 = 表数据
        // 新增菜品时 dishDTO.getId() 为 null，应该用主键回填后的 dish.getId()
        saveDishFlavors( dish.getId(), dishDTO.getFlavors() );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult pageQuery( DishPageQueryDTO dishPageQueryDTO ){
        PageUtil.validateParameterAndStartPage( dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize() );

        /*
         * 思路 1 改进: 分 2 次查询 Dish + CategoryName、DishFlavorList，然后拼装 DishVO
         * <br>
         * <strong>注意可以正常对第 1 次查询进行分页</strong>，因为第 1 次联表查询还是 1 对 1，而不是 1 对 N，total 统计一定真实
         */

        // 注意 PageHelper 只对第 1 次查询生效, 查到后就拿取 total，第 2 次查询就包装自己的链表了
        List< DishVO > dishVOList = dishMapper.selectDishVOByQueryWithoutFlavors( dishPageQueryDTO );
        Page< DishVO > dishVOPage = ( Page< DishVO > ) dishVOList;
        long dishVOPageTotal = dishVOPage.getTotal();

        // AI 检查后补充的判空逻辑
        if ( dishVOList.isEmpty() ){
            return PageUtil.buildPageResult( dishVOPageTotal, dishVOList );
        }

        // DishVO = Dish + CategoryName + DishFlavorList，现在就需要找到每个 Dish 的 DishFlavorList
        // 跟着 AI 学，然后自己删了重写、多敲几次
        List< Long > dishIdList = dishVOList.stream()
                .map( DishVO::getId ).collect( Collectors.toList() );
        List< DishFlavor > allDishFlavors = dishFlavorMapper.selectByDishIdList( dishIdList );
        // 查到所有 DishFlavor，然后根据 DishId 分组
        Map< Long, List< DishFlavor > > dishIdToFlavorsMap = allDishFlavors
                .stream().collect( Collectors.groupingBy( DishFlavor::getDishId ) );

        // 再组装完整的 DishVO
        for ( DishVO dishVO : dishVOList ){
            dishVO.setFlavors(
                    dishIdToFlavorsMap.getOrDefault(
                            dishVO.getId(),
                            dishVO.getFlavors() // 由于 VO 本身带了 ArrayList，就直接用已有的了
                            // Collections.EMPTY_LIST 会有警告 Unchecked assignment: 'java.util.List' to 'java.util.List<com.sky.entity.DishFlavor>'
                    )
            );
        }

        return PageUtil.buildPageResult( dishVOPageTotal, dishVOList );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void deleteByIds( List< Long > ids ){
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
        if ( ids == null || ids.isEmpty() ){
            return;
        }

        // 不需要 SQL 查对象、业务层再看链表数据，直接底层 count 计数即可
        if ( dishMapper.countByIdsAndStatus( ids, StatusConstant.ENABLE ) > 0 ){
            throw new DeletionNotAllowedException( MessageConstant.DISH_ON_SALE );
        }

        if ( setmealMapper.countByDishIds( ids ) > 0 ){
            throw new DeletionNotAllowedException( MessageConstant.DISH_BE_RELATED_BY_SETMEAL );
        }

        // 先删子表，后删主表
        dishFlavorMapper.deleteBatchByDishIds( ids );
        dishMapper.deleteBatch( ids );
    }

    @Transactional( readOnly = true )
    @Override
    public DishVO getInfoById( Long id ){
        DishVO dishVO = dishMapper.selectDishVOByIdWithoutFlavors( id );
        // 注意 MyBatis 单条查询可能返回 null( 多条查询返回空链表 )
        if ( dishVO == null ){
            throw new NotFoundException( MessageConstant.DISH_NOT_FOUND );
        }
        dishVO.setFlavors( dishFlavorMapper.selectByDishId( id ) );
        return dishVO;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void update( DishDTO dishDTO ){
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
        dishFlavorMapper.deleteBatchByDishIds( List.of( dishDTO.getId() ) );
        saveDishFlavors( dishDTO.getId(), dishDTO.getFlavors() );
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

        // 还要注意业务约束: 如果是禁用菜品，那也要禁用包含此菜品的套餐
        if ( StatusConstant.isDisable( status ) ){
            /*
            // 思路 1: 直接传 setmeal 对象，修改 stauts
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
        }
    }

    @Transactional( readOnly = true )
    @Override
    public List< DishVO > listByCategoryId( Long categoryId ){
        List< DishVO > dishVOList = dishMapper.selectDishVOByCategoryId( categoryId );
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

    @Transactional( readOnly = true )
    @Override
    public List< DishVO > listEnabledByCategoryId( Long categoryId ){
        List< DishVO > enabled = dishMapper.selectDishVOByCategoryIdAndStatusWithoutFlavors( categoryId, StatusConstant.ENABLE );
        if ( enabled == null || enabled.isEmpty() ){
            return enabled;
        }

        List< Long > dishIds = enabled.stream().map( DishVO::getId ).toList();
        List< DishFlavor > allDishFlavors = dishFlavorMapper.selectByDishIdList( dishIds );
        Map< Long, List< DishFlavor > > dishIdToFlavorsMap = allDishFlavors.stream().collect( Collectors.groupingBy( DishFlavor::getDishId ) );
        enabled.forEach(
                dishVO -> dishVO.setFlavors(
                        dishIdToFlavorsMap.getOrDefault( dishVO.getId(), dishVO.getFlavors() )
                )
        );

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

}
