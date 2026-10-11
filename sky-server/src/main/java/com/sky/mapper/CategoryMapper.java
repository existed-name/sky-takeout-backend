package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.admin.category.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.vo.admin.category.CategoryListItemVO;
import com.sky.vo.admin.category.CategoryPageQueryItemVO;
import com.sky.vo.user.category.UserCategoryListItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     *
     * @param id 分类 id
     * @return 删除行数, 通常 = 1, 不存在时返回 0
     */
    @Delete( """
            DELETE FROM category
            WHERE id = #{id}
            """ )
    int deleteById( @Param( "id" ) Long id );

    /**
     * 修改分类信息，包括修改分类状态(启用/禁用)
     */
    @AutoFill( value = OperationType.UPDATE )
    int updateById( @Param( "category" ) Category category );

    /**
     * 查询某种状态、某种类型的所有分类
     * <p>
     * 按照管理端设定的优先级升序排列，优先级相同按照创建时间降序排列，相同则按 id 降序排列
     * </p>
     *
     * @param type 商品分类的类型{@link com.sky.enumeration.CategoryType}, 为 null 时查询所有分类
     * @param status 启用状态{@link com.sky.enumeration.EnableStatus}
     * @return 分类数据项列表
     */
    List< UserCategoryListItemVO > selectByTypeAndStatus( @Param( "type" ) Integer type, @Param( "status" ) Integer status );

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

    /**
     * 查询某个商品的启用状态
     *
     * @param id 分类 id
     * @return 启用状态{@link com.sky.enumeration.EnableStatus},
     *          <b>分类不存在时返回 null</b>
     */
    @Select( """
            SELECT status
            FROM category
            WHERE id = #{id}
            """ )
    Integer selectStatusById( @Param( "id" ) Long id );

    /**
     * 判断某个分类是否存在
     *
     * @param id 分类 id
     * @return 1, 该分类存在; 0, 不存在
     */
    @Select( """
            SELECT count( * )
            FROM category
            WHERE id = #{id}
            """ )
    long countById( @Param( "id" ) Long id );
}
