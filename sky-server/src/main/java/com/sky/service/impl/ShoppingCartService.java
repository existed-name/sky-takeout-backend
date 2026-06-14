package com.sky.service.impl;

import com.sky.constant.FoodCopiesConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.IllegalParameterException;
import com.sky.exception.NotFoundException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartService implements com.sky.service.ShoppingCartService {
    private final DishMapper dishMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final SetmealMapper setmealMapper;

    public ShoppingCartService( DishMapper dishMapper, ShoppingCartMapper shoppingCartMapper, SetmealMapper setmealMapper ){
        this.dishMapper = dishMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.setmealMapper = setmealMapper;
    }
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    /*
        * 教程说: 对同一个菜品/套餐，如果选择多份不需要添加多条记录，增加数量number即可
        * 我的理解: 每次点 `➕` 都会向后端发单条数据(dto)，同样的菜品/套餐就在后端累加
        * 于是我先检查 dto 的 id 是不是对的(有没有对应菜品/套餐)
          - 然后再查 shopping_cart 看是不是相同菜品/套餐
          - 再修改 number 份数
        * 但是是不是有更好的办法:
          - 交给前端处理数据，用户选好后、再整理数据发给后端
          - 先写入缓存，等选好后再入库
     */
    @Override
    public void add( ShoppingCartDTO shoppingCartDTO ){
        Long userId = BaseContext.getCurrentId();
        // 添加单个菜品/套餐，套餐 id | 菜品 id + 菜品口味
        if ( shoppingCartDTO.hasDish() ){
            Long dishId = shoppingCartDTO.getDishId();
            if ( dishMapper.countById( dishId ) != 1L ){
                // count 默认返回 bigint, 就用 long 来接了
                throw new NotFoundException( MessageConstant.DISH_NOT_FOUND );
            }

            // 只看这个用户的购物车，并且由于不同菜品在一个用户的订单里面是唯一的，所以用 userId, dishId 定位
            // 但是必须确保每回完成订餐，就清除该用户购物车内容，否则会影响之后的查询
            ShoppingCart shoppingCartFoodNumber = shoppingCartMapper.selecNumbertByUserIdAndDishId( userId, dishId );
            if ( shoppingCartFoodNumber == null ){
                Dish dish = dishMapper.selectById( dishId );
                ShoppingCart build = ShoppingCart.builder()
//                        .id()
                        .name( dish.getName() )
                        .userId( userId )
                        .dishId( dishId )
//                        .setmealId()
                        .dishFlavor( shoppingCartDTO.getDishFlavor() )
                        .number( FoodCopiesConstant.BASE_COPIES )
                        .amount( dish.getPrice() )
                        .image( dish.getImage() )
                        .createTime( LocalDateTime.now() )
                        .build();
                shoppingCartMapper.insert( build );
            } else {
                shoppingCartMapper.updateNumberByUserIdAndDishId(
                        FoodCopiesConstant.increment( shoppingCartFoodNumber.getNumber() ),
                        userId, dishId
                );
            }
        } else if ( shoppingCartDTO.hasSetmeal() ){
            Long setmealId = shoppingCartDTO.getSetmealId();
            if ( setmealMapper.countById( setmealId ) != 1L ){
                throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
            }

            ShoppingCart shoppingCartFoodNumber = shoppingCartMapper.selecNumbertByUserIdAndSetmealId( userId, setmealId );
            if ( shoppingCartFoodNumber == null ){
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
            } else {
                shoppingCartMapper.updateNumberByUserIdAndSetmealId(
                        FoodCopiesConstant.increment( shoppingCartFoodNumber.getNumber() ),
                        userId, setmealId
                );
            }
        } else { // 购物车添加内容不能为空
            throw new IllegalParameterException( MessageConstant.SHOPPING_CART_ADD_NULL );
        }
    }


    // ==================== 私有辅助方法 ====================

}
