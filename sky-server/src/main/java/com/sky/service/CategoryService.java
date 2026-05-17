package com.sky.service;

import com.sky.dto.CategoryDTO;

public interface CategoryService {
    /**
     * 新增分类(菜品/套餐分类)
     */
    void save( CategoryDTO categoryDTO );


}
