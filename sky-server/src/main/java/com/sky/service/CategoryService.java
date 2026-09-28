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
     * 管理端删除某个商品分类
     *
     * @param operatorId 操作者 id, <b>当前为硬删除( 删除整条数据 ), 如果之后改为软删除( 打标记 ), 才需要记录审计字段</b>
     * @param id 分类 id
     */
    void deleteById( Long operatorId, Long id );

    /**
     * 管理端修改商品分类信息
     *
     * @param id 分类 id
     * @param operatorId  操作者 id
     * @param categoryDTO 更新后的信息
     */
    void updateById( Long operatorId, Long id, CategoryDTO categoryDTO );

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
