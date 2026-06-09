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
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.PageUtil;
import com.sky.vo.DishItemVO;
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
    private final DishMapper dishMapper;


    // ==================== 构造器 ====================
    public SetmealServiceImpl( SetmealMapper setmealMapper, SetmealDishMapper setmealDishMapper, DishMapper dishMapper ){
        this.setmealMapper = setmealMapper;
        this.setmealDishMapper = setmealDishMapper;
        this.dishMapper = dishMapper;
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
        // SetmealDTO = Setmeal + 关联关系 SetmealDish
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties( setmealDTO, setmeal );

        int updateRow = setmealMapper.update( setmeal );
        if ( updateRow <= 0 ){
            throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
        }

        // 关联子表内容删了重装
        // 本来应该分开单独写方法，但是这里的批量删除方法大概率不会改，所以就复用了
        Long setmealId = setmealDTO.getId();
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
        List< SetmealVO > enabled = setmealMapper.selectVOByCategoryIdAndStatusWithoutRelation( categoryId, StatusConstant.ENABLE );
        // 这里不需要关联关系
        // return setRelationListToSetmealVOList( enabled );
        return enabled;
    }

    @Override
    public List< DishItemVO > listDishItemVOById( Long id ){
        List< DishItemVO > dishItemVOList = setmealMapper.selectDishItemVOListById( id );
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
