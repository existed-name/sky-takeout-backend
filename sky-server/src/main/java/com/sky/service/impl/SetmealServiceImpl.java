package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.NotFoundException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.PageUtil;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
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



    // ==================== 构造器 ====================
    public SetmealServiceImpl( SetmealMapper setmealMapper, SetmealDishMapper setmealDishMapper ){
        this.setmealMapper = setmealMapper;
        this.setmealDishMapper = setmealDishMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveWithRelation( SetmealDTO setmealDTO ){
        // setmealDTO = setmeal + setmealDish 关联对象，分 2 批插入数据库
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties( setmealDTO, setmeal );
        setmealMapper.insert( setmeal );

        // 唉，忽略了 SetmealDish 的 setmealId 并不是前端自带的，而是绑定当前套餐
        // 于是需要从当前套餐拿到 id
        // 就需要设置主键回填
        List< SetmealDish > setmealDishes = setmealDTO.getSetmealDishes();
        if ( setmealDishes == null || setmealDishes.isEmpty() ){
            return;
        }
        setmealDishes.forEach( setmealDish -> setmealDish.setSetmealId( setmeal.getId() ) );
        setmealDishMapper.insertBatch( setmealDishes );
    }

    @Transactional( readOnly = true )
    @Override
    public PageResult page( SetmealPageQueryDTO setmealPageQueryDTO ){
        PageUtil.validateParameterAndStartPage( setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize() );
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
    public void deleteBatch( List< Integer > ids ){
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
    }


    // ==================== 私有辅助方法 ====================

}
