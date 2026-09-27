package com.sky.service;

import com.sky.dto.category.CategoryDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {


    /**
     * 管理端新增分类(菜品/套餐分类)
     *
     * @param operatorId 操作者 id, 显式从 controller 注入而不是 service 自行获取
     * @param categoryDTO 分类信息
     */
    void save( Long operatorId, CategoryDTO categoryDTO );

    /**
     * 管理端条件分页查询商品分类
     *
     * @param categoryPageQueryDTO 查询条件
     * @return 分页包装数据链表
     */
    PageResult page( CategoryPageQueryDTO categoryPageQueryDTO );

    /**
     * <b>管理端</b>: 根据 id 删除分类
     */
    void deleteById( Long id );

    /**
     * <b>管理端</b>: 修改分类信息
     */
    void update( CategoryDTO categoryDTO );

    /**
     * <b>管理端</b>: 修改分类状态(启用/禁用)
     */
    void updateStatus( Long id, Integer status );

    /**
     * <b>管理端</b>: 查询某种类型的所有分类
     */
    List< Category> listByType( Integer type );

    /**
     * <b>用户端</b>: 查询启用的所有分类(菜品/套餐分类)
     */
    List< Category> listEnabledByType( Integer type );
}
