package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
    @AutoFill
    void insert( Category category );

    /**
     * 对分类进行条件查询 + PageHelper 分页
     */
    List< Category> selectByQuery( CategoryPageQueryDTO categoryPageQueryDTO );

    /**
     * 根据 id 删除分类
     */
    @Delete( """
            DELETE FROM category
            WHERE id = #{id}
            """ )
    void deleteById( @Param( "id" ) Long id );

    /**
     * 修改分类信息
     */
    @AutoFill
    void update( Category category );

    /**
     * 修改分类状态(启用/禁用)
     */
    @Update( """
            UPDATE category
            SET status = #{status}
            WHERE id = #{id}
            """ )
    void updateStatusById( @Param( "id" ) Long id, @Param( "status" ) Integer status );

    /**
     * 查询某种类型的所有分类
     */
    List< Category> selectByType( @Param( "type" ) Integer type );
}
