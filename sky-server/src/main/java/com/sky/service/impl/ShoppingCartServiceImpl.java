package com.sky.service.impl;

import com.sky.constant.FoodCopiesConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.ShoppingCartQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.IllegalParameterException;
import com.sky.exception.NotFoundException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishMapper dishMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final SetmealMapper setmealMapper;



    // ==================== 构造器 ====================
    public ShoppingCartServiceImpl( DishMapper dishMapper, ShoppingCartMapper shoppingCartMapper, SetmealMapper setmealMapper ){
        this.dishMapper = dishMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.setmealMapper = setmealMapper;
    }


    // ==================== 公有方法 ====================
    /*
        * 用户每次添加 food，就往数据库记录，
        * 每次要么套餐、要么菜品，因为不是批量添加、是一个一个点击
        * 并且相同菜品还需要用不同口味区分
     */
    @Override
    public void add( ShoppingCartDTO shoppingCartDTO ){
        if ( ! shoppingCartDTO.hasDish() && ! shoppingCartDTO.hasSetmeal() ){
            // 早判断早结束
            throw new IllegalParameterException( MessageConstant.SHOPPING_CART_ADD_NULL );
        }

        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();

        // 只看这个用户的购物车，并且由于不同菜品在一个用户的订单里面是唯一的，所以用 userId, dishId 定位
        // 而且还要注意菜品要用 flavor 定位
        // 但是必须确保每回完成订餐，就清除该用户购物车内容，否则会影响之后的查询(查出来多条数据,也就是以前的旧数据)
        ShoppingCart shoppingCart = shoppingCartMapper.selectExistingItemByQuery(
                ShoppingCartQueryDTO.builder()
                        .userId( userId )
                        .dishId( dishId )
                        .setmealId( setmealId )
                        .dishFlavor( dishFlavor )
                        .build()
        );

        // 检查菜品
        if ( shoppingCartDTO.hasDish() ){
            if ( dishMapper.countById( dishId ) != 1L ){
                // count 默认返回 bigint, 就用 long 来接了
                throw new NotFoundException( MessageConstant.DISH_NOT_FOUND );
            }

            // 暂无数据
            if ( shoppingCart == null ){
                Dish dish = dishMapper.selectById( dishId );
                ShoppingCart build = ShoppingCart.builder()
//                        .id()
                        .name( dish.getName() )
                        .userId( userId )
                        .dishId( dishId )
//                        .setmealId()
                        .dishFlavor( dishFlavor )
                        .number( FoodCopiesConstant.BASE_COPIES )
                        .amount( dish.getPrice() )
                        .image( dish.getImage() )
                        .createTime( LocalDateTime.now() )
                        .build();
                shoppingCartMapper.insert( build );
            } else {
                // 已有数据
                shoppingCartMapper.updateNumberByUserIdAndDishIdAndFlavor(
                        FoodCopiesConstant.increment( shoppingCart.getNumber() ),
                        userId, dishId, dishFlavor
                );
            }
        } else { // 检查套餐
            if ( setmealMapper.countById( setmealId ) != 1L ){
                throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
            }
            // 新增套餐内容
            if ( shoppingCart == null ){
                Setmeal setmeal = setmealMapper.selectById( setmealId );
                ShoppingCart build = ShoppingCart.builder()
//                        .id()
                        .name( setmeal.getName() )
                        .userId( userId )
//                        .dishId(  )
                        .setmealId( setmealId )
//                        .dishFlavor(  )
                        .number( FoodCopiesConstant.BASE_COPIES )
                        .amount( setmeal.getPrice() )
                        .image( setmeal.getImage() )
                        .createTime( LocalDateTime.now() )
                        .build();
                shoppingCartMapper.insert( build );
            } else { // 修改套餐份数
                shoppingCartMapper.updateNumberByUserIdAndSetmealId(
                        FoodCopiesConstant.increment( shoppingCart.getNumber() ),
                        userId, setmealId
                );
            }
        }
    }


    // ==================== 私有辅助方法 ====================

}
