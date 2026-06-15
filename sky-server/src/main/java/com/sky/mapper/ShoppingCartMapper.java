package com.sky.mapper;

import com.sky.dto.ShoppingCartQueryDTO;
import com.sky.entity.ShoppingCart;
import com.sky.vo.ShoppingCartItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 新增购物车内容(菜品/套餐)
     */
    @Insert( """
            INSERT INTO shopping_cart
                ( name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time )
            values ( #{shoppingCart.name}, #{shoppingCart.image}, #{shoppingCart.userId}, #{shoppingCart.dishId}, #{shoppingCart.setmealId}, #{shoppingCart.dishFlavor}, #{shoppingCart.number}, #{shoppingCart.amount}, #{shoppingCart.createTime} )
            """ )
    int insert( @Param( "shoppingCart" ) ShoppingCart shoppingCart );

    /**
     * 查询用户购物车已存在的某条内容，
     * <br>
     * 查询条件: <b>用户 id(必须)</b>、菜品 id、菜品口味、套餐 id,
     * <br>
     * 可用于判断用户是否加购了某种 food
     * <br>
     * <b>只查一条数据，需要确保用户下单后清除该用户购物车</b>
     *
     * @return 完整字段的对象
     */
    ShoppingCart selectExistingItemByQuery( @Param( "dto" ) ShoppingCartQueryDTO dto );

    /**
     * 修改购物车的某种菜品份数, 注意用 <b>dishId + flavor 定位</b>菜品
     */
    @Update( """
            UPDATE shopping_cart
            SET number = #{number}
            WHERE user_id = #{userId}
              AND dish_id = #{dish_id}
              AND dish_flavor = #{flavor}
            """ )
    int updateNumberByUserIdAndDishIdAndFlavor(
            @Param( "number" ) int newNumber,
            @Param( "userId" ) Long userId,
            @Param( "dishId" ) Long dishId,
            @Param( "flavor" ) String dishFlavor );

    /**
     * 修改购物车的某种套餐份数, 注意用 <b>setmealId 定位</b>套餐
     */
    @Update( """
            UPDATE shopping_cart
            SET number = #{number}
            WHERE user_id = #{userId}
              AND setmeal_id = #{setmealId}
            """ )
    int updateNumberByUserIdAndSetmealId(
            @Param( "number" ) int number,
            @Param( "userId" ) Long userId,
            @Param( "setmealId" ) Long setmealId );

    /**
     * 查询该用户加购的所有菜品、套餐信息
     * <p>
     *     <b>按照加购时间倒序排列</b>，时间相同按照 id 倒序排列
     * </p>
     * <b>单价是加购时的单价，而不是实时单价</b>( dish / setmeal 表为实时单价)
     */
    @Select( """
            SELECT id, name, image, dish_id, setmeal_id, dish_flavor, number, amount, create_time
            FROM shopping_cart
            WHERE user_id = #{userId}
            ORDER BY create_time DESC, id DESC
            """ )
    List< ShoppingCartItemVO > selectItemVOByUserId( @Param( "userId" ) Long userId );

    /**
     * 清空该用户的购物车
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
     */
    @Delete( """
            DELETE FROM shopping_cart
            WHERE id = #{id}
            """ )
    int deleteById( @Param( "id" ) Long id );

    /**
     * 根据主键 id 修改商品份数
     */
    @Update( """
            UPDATE shopping_cart
            SET number = #{number}
            WHERE id = #{id}
            """ )
    int updateNumberById( @Param( "id" ) Long id, @Param( "number" ) int newNumber );
}
