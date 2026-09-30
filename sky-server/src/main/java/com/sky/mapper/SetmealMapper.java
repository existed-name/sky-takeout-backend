package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.GoodsStatusCount;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 查询某个分类关联的套餐数量
     *
     * @param categoryId 商品分类 id
     */
    @Select( """
            select count( * )
            from setmeal
            where category_id = #{categoryId}
            """)
    long countByCategoryId( @Param( "categoryId" ) Long categoryId );

    /**
     * 批量查询菜品关联的套餐
     * <br>
     * 用于删除菜品前检查是否有关联的套餐
     *
     * @param dishIds 菜品 id 链表
     * @return 菜品关联的套餐数量
     */
    long countByDishIds( @Param( "dishIds" ) List< Long> dishIds );

    /**
     * 查询某种菜品对应的套餐
     */
    List< Setmeal > selectByDishId( @Param( "dishId" ) Long dishId );

    /**
     * 修改多个套餐的状态
     * <br>
     * 本来想 SET 应该不能像 VALUES 那样设置多条不同值，然后 AI 提醒还可以用 CASE-WHEN，这样就可以每个对象单独设置自己的 status 了
     * <br>
     * 注意: <b>由于 AOP 暂不支持批量设置对象的审计字段，所有交给调用方设置审计字段</b>
     */
//    @AutoFill( OperationType.UPDATE )
    // TODO: 优化为 AOP 批量填写审计字段而不是只针对单个对象
    int updateBatchStatusByIds( @Param( "setmealList" ) List< Setmeal > setmealList );

    /**
     * 新增套餐
     * <br>
     * 注意: 由{@link com.sky.mapper.SetmealDishMapper}存储该套餐和相关菜品的关联关系
     */
    // 注意不要自己插入 id，交给数据库生成
    @Insert( """
            INSERT INTO setmeal
            ( category_id, name, price, status, description, image, create_time, update_time, create_user, update_user )
            VALUES ( #{setmeal.categoryId}, #{setmeal.name}, #{setmeal.price}, #{setmeal.status}, #{setmeal.description}, #{setmeal.image}, #{setmeal.createTime}, #{setmeal.updateTime}, #{setmeal.createUser}, #{setmeal.updateUser} )
            """ )
    @AutoFill( OperationType.INSERT )
    @Options( useGeneratedKeys = true, keyProperty = "setmeal.id" )
    int insert( @Param( "setmeal" ) Setmeal setmeal );

    /**
     * 条件分页查询 - PageHelper 负责分页
     *
     * @return {@link com.sky.vo.SetmealVO} 链表，包含 {@link com.sky.entity.Setmeal} + CategoryName, <B>不包含关联对象{@link com.sky.entity.SetmealDish}(由调用方拼装)</B>
     */
    List< SetmealVO > selectSetmealVOByQueryWithoutSetmealDishRelation( @Param( "dto" ) SetmealPageQueryDTO setmealPageQueryDTO );

    /**
     * 批量删除套餐
     */
    int deleteByIds( @Param( "ids" ) List< Long > ids );

    /**
     * 批量查询套餐, 用于批量删除套餐之前检查套餐状态
     * <br>
     * 返回完整的 Setmeal 对象列表
     */
    List< Setmeal > selectByIds( @Param( "ids" ) List< Long > ids );

    /**
     * 根据 id 查询套餐 VO,
     * 包含 {@link com.sky.entity.Setmeal} + CategoryName,
     * 关联关系 {@link com.sky.entity.SetmealDish} 由业务层单独查询再组装 VO
     */
    SetmealVO selectSetmealVOByIdWithoutSetmealDishRelation( @Param( "id" ) Long id );

    /**
     * 修改套餐信息
     * <br>
     * 注意: <b>仅修改 {@link com.sky.entity.Setmeal}</b>, 关联关系 {@link com.sky.entity.SetmealDish} 由 {@link com.sky.mapper.SetmealDishMapper} 负责
     */
    int update( @Param( "setmeal" ) Setmeal setmeal );

    /**
     * 修改套餐状态(起售/停售)
     * <br>
     * 注意: <b>传入对象用于 AOP 修改审计字段</b>
     */
    @Update( """
            UPDATE setmeal
            SET status = #{setmeal.status},
                update_time = #{setmeal.updateTime},
                update_user = #{setmeal.updateUser}
            WHERE id = #{setmeal.id}
            """ )
    @AutoFill( value = OperationType.UPDATE )
    int updateStatusById( @Param( "setmeal" ) Setmeal setmeal );

    /**
     * 根据套餐分类 id 查询对应的套餐 VO = setmeal + categoryName
     * <p>
     *     <b>不包含套餐-菜品关联关系, 参数可为空</b>
     * </p>
     * 按修改时间降序，修改时间相同按照 id 降序
     */
    List< SetmealVO > selectSetmealVOByCategoryIdWithoutSetmealDishRelation( @Param( "categoryId" ) Long categoryId );

    /**
     * 根据套餐分类 id、起售状态查询对应的套餐 VO = setmeal + categoryName
     * <p>
     *     <b>不包含套餐-菜品关联关系、前端用不到的字段, 参数不可为空</b>
     * </p>
     * 按修改时间降序，修改时间相同按照 id 降序
     */
    List< SetmealVO > selectVOByCategoryIdAndStatusWithoutRelation( @Param( "categoryId" ) Long categoryId, @Param( "status" ) Integer status );

    /**
     * 查询套餐包含的菜品信息
     * <br>
     * DishItemVO = SetmealDish + Dish
     * <br>
     * <b>无排序</b>
     */
    List< DishItemVO > selectDishItemVOListById( @Param( "setmealId" ) Long id );

    /**
     * 查询改套餐的分类 id, 用于组装 RedisKey
     *
     * @return 只包含 categoryId 的 Setmeal
     */
    @Select( """
            SELECT category_id
            FROM setmeal
            WHERE id = #{id}
            """ )
    Setmeal selectCategoryIdById( @Param( "id" ) Long id );

    /**
     * 判断该 id 对应的对象是否存在，如果存在返回 1，否则 0
     */
    @Select( """
            SELECT count( * )
            FROM setmeal
            WHERE id = #{id}
            """ )
    long countById( @Param( "id" ) Long setmealId );

    /**
     * 查询完整的 Setmeal 对象
     */
    @Select( """
            SELECT id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user
            FROM setmeal
            WHERE id = #{id}
            """ )
    Setmeal selectById( @Param( "id" ) Long setmealId );


    /**
     * 统计一批套餐里面、某种售卖状态的套餐数量
     *
     * @param setmealIds 套餐 id
     * @param status 售卖状态, {@link com.sky.constant.StatusConstant}
     * @return 指定售卖状态的套餐数量, <b>> 0 说明有, 否则无</b>
     */
    long countByIdsAndStatus( @Param( "ids" ) List< Long> setmealIds, @Param( "status" ) Integer status );

    /**
     * 统计一批 id 对应存在的数据条数, 用于和 idList 长度比较判断<b>是否有数据被删除或不存在</b>
     */
    long countByIds( @Param( "ids" ) List< Long > setmealIdList );

    /**
     * 统计所有状态的套餐数量
     *
     * @return 数量统计 Map, 可获取指定状态的封装对象、拿到对应的统计数量
     */
    @Select( """
            SELECT status, count( * ) AS count
            FROM setmeal
            GROUP BY status
            """ )
    @MapKey( "status" )
    Map< Integer, GoodsStatusCount > countGroupByStatus();
}