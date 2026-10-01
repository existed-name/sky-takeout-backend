package com.sky.service;

import com.sky.dto.dish.DishDTO;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 管理端新增菜品
     *
     * @param operatorId 操作者 id, 目前由 AOP 自动填写审计字段,
     *                   <b>手动设置审计字段时才会用到该参数</b>
     * @param dishDTO 菜品信息
     */
    void saveWithFlavors( Long operatorId, DishDTO dishDTO );

    /**
     * 管理端条件分页查询菜品
     *
     * @param dishPageQueryDTO 查询条件
     * @return 分页包装列表数据
     */
    PageResult pageQuery( DishPageQueryDTO dishPageQueryDTO );

    /**
     * 管理端批量删除菜品
     *
     * @param operatorId 操作者 id, <b>当前为硬删除( 删除整条数据 ), 如果之后改为软删除( 打标记 ), 才需要记录操作者 id</b>
     * @param ids        菜品 id
     */
    void deleteByIds( Long operatorId, List< Long > ids );

    /**
     * 管理端查询指定的菜品信息
     */
    DishVO getInfoById( Long id );

    /**
     * 管理端修改菜品信息
     *
     * @param operatorId 操作者 id
     * @param id 菜品 id
     * @param dishDTO    更新后的菜品信息
     */
    void update( Long operatorId, Long id, DishDTO dishDTO );

    /**
     * 管理端修改菜品起售状态(停售/起售)
     *
     * @param operatorId 操作者 id
     * @param id 菜品 id
     * @param status    更新后的菜品起售状态
     */
    void updateStatusById( Long operatorId, Long id, Integer status );

    /**
     * <b>管理端</b>: 根据分类 id 查询菜品 VO( 包含菜品口味 )
     * <br>
     * <b>并没有检验参数</b>: 由于分类 id 非必须，这里参数可能为空，就会查到所有菜品；参数无效则查询为空
     */
    List< DishVO > listByCategoryId( Long categoryId );

    /**
     * <b>用户端</b>: 根据分类 id 查询菜品 VO( 包含菜品口味 )
     * <br>
     * <b>只查询起售状态</b>
     */
    List< DishVO> listEnabledByCategoryId( Long categoryId );
}
