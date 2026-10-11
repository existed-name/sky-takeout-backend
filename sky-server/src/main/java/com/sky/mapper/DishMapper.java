package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.dto.GoodsStatusCount;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.projection.dish.DishShoppingCartProjection;
import com.sky.vo.admin.dish.DishInfoVO;
import com.sky.vo.admin.dish.DishListItemVO;
import com.sky.vo.admin.dish.DishPageQueryItemVO;
import com.sky.vo.user.dish.UserDishListItemVO;
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
     * 查询某个分类关联的菜品数量
     *
     * @param categoryId 商品分类 id
     */
    @Select( """
            select count( * )
            from dish where
            category_id = #{categoryId}
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
    List< DishPageQueryItemVO > selectItemVOByQueryWithoutFlavors( @Param( "query" ) DishPageQueryDTO dishPageQueryDTO );

    /**
     * 批量查询某种状态的菜品数量(比如正在起售的菜品数量)
     * <br>
     * 可用于删除菜品前检查是否起售、起售套餐前检查是否包含停售菜品...
     *
     * @param ids 菜品 id 链表
     * @param status 启用状态{@link com.sky.enumeration.EnableStatus}
     * @return 菜品数量, 推荐 java.lang.Long，因为 count(*) 在 MySQL 里默认返回 BIGINT
     */
    long countByIdsAndStatus( @Param( "ids" ) List< Long > ids, @Param( "status" ) Integer status );

    /**
     * 批量删除菜品
     *
     * @param ids 菜品 id 链表
     * @return 删除的行数
     */
    int deleteBatch( @Param( "ids" ) List< Long > ids );

    /**
     * 根据 id 查询菜品信息
     * <br>
     * DishVO = Dish + CategoryName + DishFlavorList, 这里只查 Dish、CategoryName，之后单独查 DishFlavor 在业务层组装
     *
     * @param id 菜品 id
     * @return 菜品信息展示对象, <b>不存在时返回 null</b>
     */
    DishInfoVO selectVOByIdWithoutFlavors( @Param( "id" ) Long id );

    /**
     * 修改菜品信息
     */
    @AutoFill( OperationType.UPDATE )
    int update( @Param( "dish" ) Dish dish );

    /**
     * 切换菜品状态(停售/起售)
     *
     * @param dish 设置了 id, status 字段的菜品实体, 由 AOP 填写审计字段
     * @return 修改行数, <b>当数据不存在时返回 0</b>
     */
    @AutoFill( OperationType.UPDATE )
    int updateStatusById( @Param( "dish" ) Dish dish );

    /**
     * 根据分类 id 查询菜品
     * <p>
     *     按照修改时间降序排列，修改时间相同按照 id 降序
     * </p>
     *
     * @param categoryId 分类 id, 可为空
     * @return 菜品数据项列表, 参数为空，查询所有菜品；参数无效则查询为空列表
     */
    List< DishListItemVO > listItemVOsByCategoryIdWithoutFlavors( @Param( "categoryId" ) Long categoryId );

    /**
     * 根据菜品分类、起售状态查询菜品( <b>不包含菜品口味、分类名称</b> )
     * <p>
     *     按照修改时间降序排列，修改时间相同按照 id 降序
     * </p>
     *
     * @param categoryId 分类 id, 不可为空
     * @param status 起售状态{@link com.sky.enumeration.GoodsStatus}
     * @return 菜品数据项列表
     */
    List< UserDishListItemVO > listItemVOsByCategoryIdAndStatusWithoutFlavors( @Param( "categoryId" ) Long categoryId, @Param( "status" ) Integer status );

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
    Long selectCategoryIdById( @Param( "id" ) Long id );

    /**
     * 根据 id 批量查询菜品分类 id, 用于组装 RedisKey = 业务前缀:分类 id
     *
     * @param ids 菜品 id 链表
     * @return 这批菜品所属的分类 id
     */
    List< Long > selectCategoryIdsByIds( @Param( "ids" ) List< Long > ids );

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
     * 查询某个菜品
     *
     * @param id 菜品 id
     * @return <b>字段完整</b>的实体对象, <b>不存在时返回 null</b>
     */
    Dish selectById( @Param( "id" ) Long id );

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

    /**
     * 统计某个套餐包含的某种状态的菜品数量, 比如 1 个套餐包含了多少个停售的菜品( 用于判断是否可以起售套餐 )
     * <p>
     *     内部联合 setmeal_dish 关系边获取菜品 id
     * </p>
     *
     * @param setmealId 套餐 id
     * @param status 商品状态{@link com.sky.enumeration.GoodsStatus}
     * @return 菜品数量
     */
    long countBySetmealIdAndStatus( @Param( "setmealId" ) Long setmealId, @Param( "status" ) int status );

    /**
     * 查询某个菜品的部分字段, 用于组装购物车数据
     *
     * @param id 菜品 id
     * @return 菜品对象部分字段, <b>不存在时返回 null</b>
     */
    DishShoppingCartProjection selectShoppingCartProjectionById( @Param( "id" ) Long id );
}