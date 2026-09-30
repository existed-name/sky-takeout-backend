package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
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
     * <b>管理端</b>: 根据 id 查询菜品
     */
    DishVO getInfoById( Long id );

    /**
     * <b>管理端</b>: 修改菜品信息
     */
    void update( DishDTO dishDTO );

    /**
     * <b>管理端</b>: 切换菜品状态(停售/起售)
     */
    void updateStatusById( Long id, Integer status );

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
