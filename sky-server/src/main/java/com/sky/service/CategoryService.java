package com.sky.service;

import com.sky.dto.category.CategoryDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.vo.category.CategoryListItemVO;

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
     * 管理端修改商品分类信息
     *
     * @param operatorId 操作者 id
     * @param categoryDTO 更新后的信息
     */
    void update( Long operatorId, CategoryDTO categoryDTO );

    /**
     * 管理端修改商品分类启用状态
     *
     * @param operatorId 操作者 id
     * @param id 分类 id
     * @param status 启用状态, {@link com.sky.enumeration.EnableStatus}
     */
    void updateStatus( Long operatorId, Long id, Integer status );

    /**
     * 管理端查询某种类型的所有启用分类
     *
     * @param type 分类类型, {@link com.sky.enumeration.CategoryType}
     * @return 分类列表数据
     */
    List< CategoryListItemVO > listByType( Integer type );

    /**
     * <b>用户端</b>: 查询启用的所有分类(菜品/套餐分类)
     */
    List< Category> listEnabledByType( Integer type );
}
