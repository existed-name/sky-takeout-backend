package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * <b>管理端</b>: 新增分类(菜品/套餐分类)
     */
    void save( CategoryDTO categoryDTO );

    /**
     * <b>管理端</b>: 对分类进行条件分页查询
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
