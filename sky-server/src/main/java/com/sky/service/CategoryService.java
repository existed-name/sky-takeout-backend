package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

public interface CategoryService {
    /**
     * 新增分类(菜品/套餐分类)
     */
    void save( CategoryDTO categoryDTO );

    /**
     * 对分类进行条件分页查询
     */
    PageResult page( CategoryPageQueryDTO categoryPageQueryDTO );

    /**
     * 根据 id 删除分类
     */
    void deleteById( Long id );
}
