package com.sky.mapper;

import com.sky.dto.shoppingCart.ShoppingCartQueryDTO;
import com.sky.entity.ShoppingCart;
import com.sky.projection.shoppingCart.ShoppingCartOrderDetailProjection;
import com.sky.vo.ShoppingCartItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 新增购物车数据(菜品/套餐)并进行主键回填
     *
     * @param shoppingCart 完整的购物车数据, <b>由调用方设置审计字段</b>
     * @return 新增数据行数, 通常 = 1
     */
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    @Insert( """
            INSERT INTO shopping_cart
                ( name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time )
            values ( #{shoppingCart.name}, #{shoppingCart.image}, #{shoppingCart.userId}, #{shoppingCart.dishId}, #{shoppingCart.setmealId}, #{shoppingCart.dishFlavor}, #{shoppingCart.number}, #{shoppingCart.amount}, #{shoppingCart.createTime} )
            """ )
    int insert( @Param( "shoppingCart" ) ShoppingCart shoppingCart );

    /**
     * 查询某个用户创建的某个类型的购物车数据, 用于判断该用户是否加购了某种商品
     * <p>
     *     <b>只查一条数据，需要确保用户下单后清除该用户购物车</b>
     * </p>
     *
     * @param dto 查询条件<br>
     *            用户 id, 用于隔离不同用户数据<br>
     *            套餐 id 或者 菜品 id + 菜品口味, 用于筛选特定类型的购物车数据
     * @return 完整字段的购物车实体对象, <b>不存在该类型数据时返回 null</b>
     */
    ShoppingCart selectByQuery( @Param( "dto" ) ShoppingCartQueryDTO dto );

    /**
     * 查询该用户的购物车数据
     * <p>
     *     <b>按照加购时间倒序排列</b>，时间相同按照 id 倒序排列
     * </p>
     * <b>单价是加购时的单价，而不是 dish / setmeal 表的实时单价</b>
     *
     * @param userId 用户 id
     * @return 购物车数据项列表
     */
    List< ShoppingCartItemVO > selectItemVOListByUserId( @Param( "userId" ) Long userId );

    /**
     * 清空该用户的购物车
     *
     * @param userId 用户 id
     * @return 删除数据行数
     */
    @Delete( """
            DELETE FROM shopping_cart
            WHERE user_id = #{userId}
            """ )
    int deleteByUserId( @Param( "userId" ) Long userId );

    /**
     * 根据 id 查询购物车某条数据
     * <p>
     *     <b>只包含用到的字段</b>: userId, dishId, dishFlavor, setmealId, number
     * </p>
     */
    @Select( """
            SELECT user_id, dish_id, setmeal_id, dish_flavor, number
            FROM shopping_cart
            WHERE id = #{id}
            """ )
    ShoppingCart selectById( @Param( "id" ) Long id );

    /**
     * 删除某条购物车数据
     * <p>
     *     涉及数据归属权限时请使用{@link #deleteByIdAndUserId}
     * </p>
     * @param id 购物车数据 id
     * @return 删除数据行数, 通常为 1, <b>数据不存在时返回 0</b>
     */
    @Delete( """
            DELETE FROM shopping_cart
            WHERE id = #{id}
            """ )
    int deleteById( @Param( "id" ) Long id );

    /**
     * 修改某条购物车数据的商品份数
     * <p>
     *     涉及数据归属权限时请使用{@link #updateNumberByIdAndUserId}
     * </p>
     *
     * @param id 购物车 id
     * @param newNumber 修改后的份数
     * @return 修改行数, <b>返回 0 表示购物车数据不存在</b>
     */
    @Update( """
            UPDATE shopping_cart
            SET number = #{number}
            WHERE id = #{id}
            """ )
    int updateNumberById( @Param( "id" ) Long id, @Param( "number" ) int newNumber );

    /**
     * 批量新增购物车数据
     */
    int insertBatch( @Param( "shoppingCarts" ) List< ShoppingCart > shoppingCarts );

    /**
     * 查询某个用户、某条购物车数据的商品数量
     *
     * @param id 购物车数据 id
     * @param userId 用户 id
     * @return 该购物车的商品数量, <b>购物车数据不存在或者不属于该用户时返回 null</b>
     */
    @Select( """
            SELECT number
            FROM shopping_cart
            WHERE id = #{id}
            AND user_id = #{userId}
            """ )
    Integer selectNumberByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 判断购物车数据是否存在
     *
     * @param id 购物车数据 id
     * @return 0 不存在, 1 存在
     */
    @Select( """
            SELECT count( * )
            FROM shopping_cart
            WHERE id = #{id}
            """ )
    long countById( @Param( "id" ) Long id );

    /**
     * 修改某个用户、某条购物车数据的商品数量
     *
     * @param id 购物车数据 id, 用于确保数据存在
     * @param userId 用户 id, 用于确保数据属于该用户
     * @param newNumber 修改后的商品数量
     * @return 修改行数, <b>返回 0 说明购物车数据不存在或者不属于该用户</b>
     */
    @Update( """
            UPDATE shopping_cart
            SET number = #{newNumber}
            WHERE id = #{id}
            AND user_id = #{userId}
            """ )
    int updateNumberByIdAndUserId(
            @Param( "id" ) Long id,
            @Param( "userId" ) Long userId,
            @Param( "newNumber" ) int newNumber
    );

    /**
     * 删除某个用户的某条购物车数据
     *
     * @param id 购物车数据 id, 用于确保数据存在
     * @param userId 用户 id, 用于确保数据属于该用户
     * @return 删除数据行数, 通常为 1,
     *          <b>返回 0 说明购物车数据不存在或者不属于该用户</b>
     */
    @Delete( """
            DELETE FROM shopping_cart
            WHERE id = #{id}
            AND user_id = #{userId}
            """ )
    int deleteByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 查询用户的购物车数据, 用于组装订单明细{@link com.sky.entity.OrderDetail}
     * <p>
     *     排序规则: 无
     * </p>
     *
     * @param userId 用户 id, 用于定位该用户的数据
     * @return 组装订单明细需要的字段
     */
    List< ShoppingCartOrderDetailProjection > selectOrderDetailProjectionByUserId( @Param( "userId" ) Long userId );
}
