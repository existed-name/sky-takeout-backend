package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.PageUtil;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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



    // ==================== 构造器 ====================
    public DishServiceImpl( DishMapper dishMapper, DishFlavorMapper dishFlavorMapper ){
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
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

        // 细节: 最终给前端看的不是 Dish，而是 DishVO，还需要 DishFlavor
        // 1 个 DishVO = 1 个 Dish + 对应的多个 DishFlavor

        // 思路 3，既然 VO = Dish + CategoryName + DishFlavor，而这 3 个又是关联表，于是在底层关联查询，直接拿到包装的 VO
//        return PageUtil.buildPageResult( pageQueryThought3( dishPageQueryDTO ) );

        // 思路 1，拿到所有 Dish 的 id，Mapper 一次性把所有 Dish 的 DishFlavor 查出来，形成嵌套链表
         // return pageQueryThought1( dishPageQueryDTO );

        // 思路 2，循环处理每个 Dish，1 个 Dish 查一次
        // return pageQueryThought2( dishPageQueryDTO );

        // 思路 1 改进
        return pageQueryThought1Optimized( dishPageQueryDTO );
    }



    // ==================== 私有辅助方法 ====================
    /**
     * 思路 1 改进: 分 2 次查询 Dish + CategoryName、DishFlavorList，然后拼装 DishVO
     * <br>
     * <strong>注意可以正常对第 1 次查询进行分页</strong>，因为第 1 次联表查询还是 1 对 1，而不是 1 对 N，total 统计一定真实
     */
    private PageResult pageQueryThought1Optimized( DishPageQueryDTO dishPageQueryDTO ){
        // 注意 PageHelper 只对第 1 次查询生效, 查到后就拿取 total，第 2 次查询就包装自己的链表了
        List< DishVO > dishVOList = dishMapper.selectDishVOByQueryWithoutFlavors( dishPageQueryDTO );
        Page< DishVO > dishVOPage = ( Page< DishVO > ) dishVOList;
        long dishVOPageTotal = dishVOPage.getTotal();

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



    /**
     * <pre>
     *     条件分页查询，组装 DishVO
     *     思路 3，既然 VO = Dish + CategoryName + DishFlavor，而这 3 个又是关联表，于是在底层关联查询，直接拿到包装的 VO
     *     - 改进教程的只有 Dish + Category、没有 DishFlavor
     *     - AI 评价缺点: total 计数错误( JOIN 会产生多条数据，远超原来的 Dish 数据量)、某道/多道菜的 DishFlavor 被截断
     *     - 也就是说 3 表一起联合，虽然查到了所有数据，但是联合的时候扩大了数据量，导致 PageHelper 出错
     * </pre>
     */
    private List< DishVO > pageQueryThought3( DishPageQueryDTO dishPageQueryDTO ){
        return dishMapper.selectDishVOByQuery( dishPageQueryDTO );
    }

    /**
     * <pre>
     *     - 条件分页查询，组装 DishVO
     *     - 思路 1，拿到所有 Dish 的 id，Mapper 一次性把所有 Dish 的 DishFlavor 查出来，形成嵌套链表
     *     - 思路 1 改进：原来想形成嵌套链表，但是 Mapper 封装比较麻烦，就只用 1 层链表装所有 DishFlavor 然后按 Dish.id 分组成嵌套列表
     *     - 最终测试: <strong> 自行整理成 PageResult，虽然没有联合 CategoryName，但是其他正常(Dish + DishFlavors)！！！</strong>
     * </pre>
     */
    private PageResult pageQueryThought1( DishPageQueryDTO dishPageQueryDTO ){
        // 注意 PageHelper 只对第 1 条查询 SQL 生效
        List< Dish > dishList = dishMapper.selectByQuery( dishPageQueryDTO );
        List< Long > dishIdList = dishList.stream().map( Dish::getId ).toList();
        // 需要记录数据总量 total( 链表 size 是分页后的数据量 )
        Page< Dish > dishPage = ( Page< Dish > ) dishList;
        long dishPageTotal = dishPage.getTotal();

        // PageHelper 不进行分页操作
        // 拿到所有 DishFlavor 然后按 Dish.id 分组
        List< DishFlavor > allDishFlavors = dishFlavorMapper.selectByDishIdList( dishIdList );

        // 注意: 非 JOIN 的单表查询，不会返回 NULL 数据，因此这里的 DishFlavor 都找得到对应的 Dish
        Map< Long, List< DishFlavor > > dishIdToFlavorsMap = allDishFlavors.stream()
                .collect( Collectors.groupingBy( DishFlavor::getDishId ) );

        // 再把分好组的 DishFlavorList 组装进来
        List< DishVO > dishVOList = new ArrayList<>();
        for ( Dish dish : dishList ){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties( dish, dishVO );
            dishVO.setFlavors(
                    dishIdToFlavorsMap.getOrDefault(
                            dish.getId(), // 菜品 id -> 口味链表
                            dishVO.getFlavors() // 由于 VO 自带空链表，就继续用原来的，而不用 null 覆盖
                    )
            );
            dishVOList.add( dishVO );
        }
        // 整理到最后，干脆自己包装 PageResult 了
        // return dishVOList;
        return PageUtil.buildPageResult( dishPageTotal, dishVOList );
    }

    /**
     * <pre>
     *     - 条件分页查询，组装 DishVO
     *     - 思路 2，循环处理每个 Dish，1 个 Dish 查一次
     *     - 最终测试: <b>同思路 1</b>
     * </pre>
     */
    private PageResult pageQueryThought2( DishPageQueryDTO dishPageQueryDTO ){
        List< Dish > dishList = dishMapper.selectByQuery( dishPageQueryDTO );;
        // 需要记录数据总量 total( 链表 size 是分页后的数据量 )
        Page< Dish > dishPage = ( Page< Dish > ) dishList;
        long dishPageTotal = dishPage.getTotal();

        List< DishVO > dishVOList = new ArrayList<>();
        dishList.forEach( dish -> {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties( dish, dishVO );
            // PageHelper 只执行 1 次，第 2 次查询不会分页
            dishVO.setFlavors( dishFlavorMapper.selectByDishId( dish.getId() ) );
            dishVOList.add( dishVO );
        } );

        // 自己包装 PageResult
        return PageUtil.buildPageResult( dishPageTotal, dishVOList );
    }
}
