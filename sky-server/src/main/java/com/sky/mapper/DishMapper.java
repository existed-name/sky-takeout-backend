package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.GoodsStatusCount;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     */
    @Select( """
            select count( * ) from dish where category_id = #{categoryId}
            """)
    long countByCategoryId( @Param( "categoryId" ) Long categoryId );

    /**
     * 新增菜品
     */
    @AutoFill( OperationType.INSERT )
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    @Insert( """
            INSERT INTO dish
            ( name, category_id, price, image, description, status, create_time, update_time, create_user, update_user )
            values ( #{dish.name}, #{dish.categoryId}, #{dish.price}, #{dish.image}, #{dish.description}, #{dish.status}, #{dish.createTime}, #{dish.updateTime}, #{dish.createUser}, #{dish.updateUser} )
            """ )
    int insert( @Param( "dish" ) Dish dish );

    /**
     * 条件分页查询 - PageHelper 执行分页操作
     * <br>
     * 联表查询 Dish + CategoryName，之后单独查 DishFlavorList，再业务层拼装 DishVOList
     * <br>
     * 排序规则: 菜品修改时间降序、菜品 id 降序
     */
    List< DishVO > selectDishVOByQueryWithoutFlavors( @Param( "dto" ) DishPageQueryDTO dishPageQueryDTO );

    /**
     * 批量查询某种状态的菜品数量(比如正在起售的菜品数量)
     * <br>
     * 用于删除菜品前检查是否起售
     * <br>
     * 更推荐 java.lang.Long，因为 count(*) 在 MySQL 里默认返回 BIGINT
     */
    long countByIdsAndStatus( @Param( "ids" ) List< Long> ids, @Param( "status" ) Integer status );

    /**
     * 批量删除菜品
     */
    int deleteBatch( @Param( "ids" ) List< Long> ids );

    /**
     * 根据 id 查询菜品信息
     * <br>
     * DishVO = Dish + CategoryName + DishFlavorList, 这里只查 Dish、CategoryName，之后单独查 DishFlavor 在业务层组装
     */
    DishVO selectDishVOByIdWithoutFlavors( @Param( "id" ) Long id );

    /**
     * 修改菜品信息
     */
    @AutoFill( OperationType.UPDATE )
    int update( @Param( "dish" ) Dish dish );

    /**
     * 切换菜品状态(停售/起售)
     */
    @AutoFill( OperationType.UPDATE )
    int updateStatusById( @Param( "dish" ) Dish dish );

    /**
     * 根据分类 id 查询菜品
     * 按照修改时间降序排列，修改时间相同按照 id 降序
     *
     * @return 参数为空，查到所有菜品；参数无效则查询为空
     */
    List< DishVO > selectDishVOByCategoryId( @Param( "categoryId" ) Long categoryId );

    /**
     * 批量查询菜品状态 {@link com.sky.entity.Dish}
     * <br>
     * 用于起售套餐时检查有无停售的关联菜品
     * <br>
     * 无需排序
     */
    List< Integer > selectDishStatusListByIds( @Param( "ids" ) List< Long> ids );

    /**
     * 根据菜品分类、起售状态查询菜品 VO( <b>不包含菜品口味</b> )
     * <br>
     * 按照修改时间降序排列，修改时间相同按照 id 降序
     */
    List< DishVO > selectDishVOByCategoryIdAndStatusWithoutFlavors( @Param( "categoryId" ) Long categoryId, @Param( "status" ) Integer status );

    /**
     * 根据 id 查询该菜品的分类 id, 用于组装 RedisKey = 业务前缀:分类 id
     *
     * @return 只包含 categoryId 的 Dish
     */
    @Select( """
            SELECT category_id
            FROM dish
            WHERE id = #{id}
            """ )
    Dish selectCategoryIdById( @Param( "id" ) Long id );

    /**
     * 根据 id 批量查询菜品分类 id, 用于组装 RedisKey = 业务前缀:分类 id
     *
     * @return 只包含 categoryId 的 DishList
     */
    List< Dish > selectCategoryIdsByIds( @Param( "ids" ) List< Long > ids );

    /**
     * 判断该 id 对应的对象是否存在，如果存在返回 1，否则 0
     */
    @Select( """
            SELECT count( * )
            FROM dish
            WHERE id = #{id}
            """ )
    long countById( @Param( "id" ) Long dishId );

    /**
     * 查询完整的 Dish 对象
     */
    @Select( """
            SELECT id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user
            FROM dish
            WHERE id = #{id}
            """ )
    Dish selectById( @Param( "id" ) Long dishId );

    /**
     * 查询<b>不包含审计字段</b>的 Dishes
     */
    List< Dish > selectPartsByIds( @Param( "ids" ) List< Long > dishIdList );

    /**
     * 统计一批 id 对应存在的数据条数, 用于和 idList 长度比较判断<b>是否有数据被删除或不存在</b>
     */
    long countByIds( @Param( "ids" ) List< Long > dishIdList );

    /**
     * 统计所有状态的菜品数量
     *
     * @return 数量统计 Map, 可获取指定状态的封装对象、拿到对应的统计数量
     */
    @Select( """
            SELECT status, count( * ) AS count
            FROM dish
            GROUP BY status
            """ )
    @MapKey( "status" )
    Map< Integer, GoodsStatusCount > countGroupByStatus();
}