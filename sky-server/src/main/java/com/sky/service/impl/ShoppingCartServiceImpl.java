package com.sky.service.impl;

import com.sky.constant.FoodCopiesConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.ShoppingCartQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.InvalidDataBaseDataException;
import com.sky.exception.NotFoundException;
import com.sky.exception.shoppingCart.IncompleteShoppingCartException;
import com.sky.exception.shoppingCart.ShoppingCartUserNotMatchException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.ShoppingCartVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
            throw new IncompleteShoppingCartException( MessageConstant.SHOPPING_CART_ADD_NULL );
        }

        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();

        // 只看这个用户的购物车，并且由于不同菜品在一个用户的订单里面是唯一的，所以用 userId, dishId 定位
        // 而且还要注意菜品要用 flavor 定位
        // 但是必须确保每回完成订餐，就清除该用户购物车内容，否则会影响之后的查询(查出来多条数据,也就是以前的旧数据)
        ShoppingCart shoppingCart = shoppingCartMapper.selectByQuery(
                ShoppingCartQueryDTO.builder()
                        .userId( userId )
                        .dishId( dishId )
                        .setmealId( setmealId )
                        .dishFlavor( dishFlavor )
                        .build()
        );

        // 该商品还不存在，新创建购物车数据，就不需要 if-else 分支重复创建、只需要设置各自的字段
        boolean isInDatabase = true; // 标记数据库是否存在该数据，用于判断是否需要设置各自的字段
        if ( shoppingCart == null ){
            isInDatabase = false;
            shoppingCart = ShoppingCart.builder()
//                        .id()
//                    .name(  )
                    .userId( userId )
                    .dishId( dishId )
                    .setmealId( setmealId )
                    .dishFlavor( dishFlavor )
                    .number( FoodCopiesConstant.BASE_COPIES )
//                    .amount(  )
//                    .image(  )
                    .createTime( LocalDateTime.now() )
                    .build();
        }

        // 数据库已存在，直接增加数量
        if ( isInDatabase ){
            Integer number = shoppingCart.getNumber();
            if ( number == null || number <= 0 ){
                // null, 非正数, 无效数据, 要么是上次插入非法数据，要嘛是数据库出问题
                // DTO 不带 number 字段，只能是内部设置的 number, 只能是内部错误
                // 所以用 JDK 系统异常
                throw new IllegalStateException(
                        String.format( "%s: id = %s 的购物车商品数量 = %s",
                                MessageConstant.INVALID_DATABASE_DATA,
                                shoppingCart.getId(), number )
                );
            }
            shoppingCartMapper.updateNumberById(
                    shoppingCart.getId(),
                    FoodCopiesConstant.increment( number )
            );
        } else {
            // 数据库中不存在，新建
            // 最终需要补填的字段就是 name, amount, image
            String name = null;
            BigDecimal amount = null;
            String image = null;

            // 于是需要查到对应 dish/setmeal 的信息
            if ( shoppingCartDTO.hasDish() ){ // shoppingCart.isDish
                Dish dish = dishMapper.selectById( dishId );
                if ( dish == null ){
                    throw new NotFoundException( MessageConstant.DISH_NOT_FOUND );
                }
                name = dish.getName();
                amount = dish.getPrice();
                image = dish.getImage();
            } else { // shoppingCart.isSetmeal
                Setmeal setmeal = setmealMapper.selectById( setmealId );
                if ( setmeal == null ){
                    throw new NotFoundException( MessageConstant.SETMEAL_NOT_FOUND );
                }
                name = setmeal.getName();
                amount = setmeal.getPrice();
                image = setmeal.getImage();
            }

            shoppingCart.setName( name );
            shoppingCart.setAmount( amount );
            shoppingCart.setImage( image );
            shoppingCartMapper.insert( shoppingCart );
        }
    }

    @Override
    public ShoppingCartVO list(){
        /*
            ShoppingCartVO = List< ShoppingCartDishVO > + List< ShoppingCartSetmealVO > + totalAmount(总价)
            于是有几种查法
              - 查全库，拿到所有数据，直接把 Object 拿给前端: shoppingCartMapper.selectByUserId
              - 查全库，List< Object > 在 Service 里面自己组装 VO、统计数据
              - 分出专用方法，
                - selectDish/SetmealVOByUserId( dish_id IS NOT NULL )查对象
                - 不需要联表 shopping_cart + dish / setmeal，联表是获得菜品/套餐的实时价格，否则直接用购物车里面的价格
                - 然后 Service 整理 totalAmount
              - 注意还要时间倒序
         */
        Long userId = BaseContext.getCurrentId();
        List< ShoppingCartItemVO > shoppingCartItemVOList = shoppingCartMapper.selectItemVOListByUserId( userId );
        BigDecimal totalAmount = calculateTotalAmount( shoppingCartItemVOList );
        return ShoppingCartVO.builder()
                .shoppingCartItemVOList( shoppingCartItemVOList )
                .totalAmount( totalAmount )
                .build();
    }

    @Override
    public void clean(){
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId( userId );
    }

    @Override
    public void decrementById( Long id ){
        Long userId = BaseContext.getCurrentId();
        // id 完全可定位该条数据，就不需要再添 userId 之类的了
        ShoppingCart shoppingCart = shoppingCartMapper.selectById( id );
        if ( shoppingCart == null ){
            throw new NotFoundException( MessageConstant.SHOPPING_CART_NOT_FOUND );
        }

        // AI 提醒: 由于使用 id 定位，需要确保 userId 相同
        if ( ! userId.equals( shoppingCart.getUserId() ) ){
            throw new ShoppingCartUserNotMatchException( MessageConstant.SHOPPING_CART_USER_NOT_MATCH );
        }

        // 数量为 1 -> 减为 0 -> 删除，否则只是减一
        Integer number = shoppingCart.getNumber();
        if ( number > 1 ){
            // 既然有了准确 id，就不需要 dishId, setmealId 定位了
            shoppingCartMapper.updateNumberById(
                    id, FoodCopiesConstant.decrement( shoppingCart.getNumber() )
            );
        } else if ( number == 1 ){
            int row = shoppingCartMapper.deleteById( id );
        } else { // null, 非正数, 无效数据
            throw new InvalidDataBaseDataException( MessageConstant.INVALID_DATABASE_DATA + ": 购物车商品数量 = " + number );
        }
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 计算总金额
     *
     * @param shoppingCartItemVOList 用户购物车的所有信息(菜品、套餐)=
     */
    private BigDecimal calculateTotalAmount( List< ShoppingCartItemVO > shoppingCartItemVOList ){
        BigDecimal totalAmount = BigDecimal.ZERO;
        for ( ShoppingCartItemVO itemVO : shoppingCartItemVOList ){
            totalAmount = totalAmount.add(
                    itemVO.getPrice().multiply(
                            BigDecimal.valueOf( itemVO.getNumber() )
                    )
            );
        }
        return totalAmount;
    }
}
