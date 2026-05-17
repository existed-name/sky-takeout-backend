package com.sky.mapper;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 新增分类(菜品/套餐分类)
     */
    @Insert( """
            INSERT INTO category
            ( type, name, sort, status, create_time, update_time, create_user, update_user )
            values ( #{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser} )
            """ )
    void insert( Category category );

    /**
     * 对分类进行条件查询 + PageHelper 分页
     */
    List< Category> selectByQuery( CategoryPageQueryDTO categoryPageQueryDTO );
}
