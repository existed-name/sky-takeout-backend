package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.IllegalParameterException;
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
        // 审计字段交给 AOP 处理
        dishMapper.insert( dish );

        // 注意 Mapper 是否保存了所有内容: 实体类对象所有属性( DTO 属性 + 审计字段 ) + 关联内容 = 表数据
        List< DishFlavor > flavors = dishDTO.getFlavors();
        if ( flavors == null || flavors.isEmpty() ){
            return;
        }
        // 最细节: 设置关联 id => 需要设置主键回填，让关联对象拿到数据库表 id
        Long dishId = dish.getId();
        flavors.forEach( flavor -> flavor.setDishId( dishId ) );
        dishFlavorMapper.insertBatch( flavors );
    }

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

        /*
        List< Dish > dishList = dishMapper.selectByIds( ids );
        for ( Dish dish : dishList ){
            if ( dish.getStatus().equals( StatusConstant.ENABLE ) ){
                throw new DeletionNotAllowedException( MessageConstant.DISH_ON_SALE );
            }
        }

        List< Setmeal > setmealList = setmealMapper.selectByDishIds( ids );
        if ( ! ( setmealList == null || setmealList.isEmpty() ) ){
            throw new DeletionNotAllowedException( MessageConstant.DISH_BE_RELATED_BY_SETMEAL );
        }
         */
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

    @Override
    public DishVO getInfoById( Long id ){
        DishVO dishVO = dishMapper.selectDishVOByIdWithoutFlavors( id );
        // 注意 MyBatis 单条查询可能返回 null( 多条查询返回空链表 )
        if ( dishVO == null ){
            throw new IllegalParameterException( MessageConstant.DISH_NOT_FOUND );
        }
        dishVO.setFlavors( dishFlavorMapper.selectByDishId( id ) );
        return dishVO;
    }


    // ==================== 私有辅助方法 ====================

}
