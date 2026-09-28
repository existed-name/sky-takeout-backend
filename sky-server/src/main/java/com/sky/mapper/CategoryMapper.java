package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.vo.category.CategoryListItemVO;
import com.sky.vo.category.CategoryPageQueryItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

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
    @AutoFill( value = OperationType.INSERT )
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    int insert( Category category );

    /**
     * 根据 id 删除分类
     */
    @Delete( """
            DELETE FROM category
            WHERE id = #{id}
            """ )
    void deleteById( @Param( "id" ) Long id );

    /**
     * 修改分类信息，包括修改分类状态(启用/禁用)
     */
    @AutoFill( value = OperationType.UPDATE )
    int update( @Param( "category" ) Category category );

    /**
     * 查询某种状态、某种类型对应的所有分类
     * <p>
     *     按照设定优先级升序排列，优先级相同按照创建时间降序排列
     * </p>
     * <b>SQL 直接根据 status 筛选，传参不能为空</b>
     */
    List< Category> selectByTypeAndStatus( @Param( "type" ) Integer type, @Param( "status" ) Integer enable );

    /**
     * 条件查询分类, 由 PageHelper 完成分页
     *
     * @param categoryPageQueryDTO 查询条件
     * @return 分页查询数据项列表
     */
    List< CategoryPageQueryItemVO > selectItemVOListByQuery( @Param( "query" ) CategoryPageQueryDTO categoryPageQueryDTO );

    /**
     * 查询某种分类类型、启用状态对应的所有分类数据
     * <p>
     *     按照设定的业务优先级升序排列，优先级相同按照创建时间降序排列, 创建时间相同按照 id 降序排列
     * </p>
     * @param status 启用状态, {@link com.sky.enumeration.EnableStatus}
     * @param type 分类类型, {@link com.sky.enumeration.CategoryType},
     *               <b>为 null 时查询所有类型的分类</b>
     * @return 分类列表数据
     */
    List< CategoryListItemVO > selectItemVOListByStatusAndType( @Param( "status" ) int status, @Param( "type" ) Integer type );
}
