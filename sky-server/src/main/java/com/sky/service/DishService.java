package com.sky.service;

import com.sky.dto.admin.dish.DishDTO;
import com.sky.dto.admin.dish.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.admin.dish.DishInfoVO;
import com.sky.vo.admin.dish.DishListItemVO;
import com.sky.vo.admin.dish.DishPageQueryItemVO;
import com.sky.vo.user.dish.UserDishListItemVO;

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
    PageResult< DishPageQueryItemVO > pageQuery( DishPageQueryDTO dishPageQueryDTO );

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
    DishInfoVO getInfoById( Long id );

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
     * 管理端查询某种分类对应的菜品列表( 不包含菜品口味 )
     * <p>
     *      业务场景: 新增套餐需要添加菜品, 从各个菜品分类选择菜品
     * </p>
     *
     * @param categoryId 分类 id, 选填, 为空时查询所有菜品；参数无效则查询为空
     * @return 菜品数据项列表
     */
    List< DishListItemVO > listByCategoryId( Long categoryId );

    /**
     * 用户端查询某个分类、<b>起售</b>的菜品( 包含菜品口味 )
     * <p>
     *     TODO: 拓展为{@code  listByCategoryIdAndStatus}由 controller 指定 status
     * </p>
     *
     * @param categoryId 分类 id
     * @return 菜品数据项列表
     */
    List< UserDishListItemVO > listEnabledByCategoryId( Long categoryId );
}
