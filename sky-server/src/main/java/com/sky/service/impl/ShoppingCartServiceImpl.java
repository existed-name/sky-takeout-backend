package com.sky.service.impl;

import com.sky.constant.FoodCopiesConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.shoppingCart.ShoppingCartDTO;
import com.sky.dto.shoppingCart.ShoppingCartQueryDTO;
import com.sky.entity.ShoppingCart;
import com.sky.exception.InvalidDataBaseDataException;
import com.sky.exception.NotFoundException;
import com.sky.exception.shoppingCart.ShoppingCartIncompleteException;
import com.sky.exception.shoppingCart.ShoppingCartInvalidException;
import com.sky.exception.shoppingCart.ShoppingCartUserNotMatchException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.projection.dish.DishShoppingCartProjection;
import com.sky.projection.setmeal.SetmealShoppingCartProjection;
import com.sky.service.ShoppingCartService;
import com.sky.utils.ListUtils;
import com.sky.utils.NotFoundUtil;
import com.sky.vo.ShoppingCartItemVO;
import com.sky.vo.ShoppingCartListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserMapper userMapper;


    // ==================== 构造器 ====================
    public ShoppingCartServiceImpl( DishMapper dishMapper, ShoppingCartMapper shoppingCartMapper, SetmealMapper setmealMapper, UserMapper userMapper ){
        this.dishMapper = dishMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.setmealMapper = setmealMapper;
        this.userMapper = userMapper;
    }


    // ==================== 公有方法 ====================
    /*
        * 用户每次添加 food，就往数据库记录，
        * 每次要么套餐、要么菜品，因为不是批量添加、是一个一个点击
        * 并且相同菜品还需要用不同口味区分
     */
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void addWithUserId( Long userId, ShoppingCartDTO shoppingCartDTO ){
        /*
            初步思路
            * 目标是组装完整 ShoppingCart 实体对象, 写入数据库
              并且 ShoppingCart 没有类似 SetmealDish、DishFlavor 的关联关系表, 只需要写好这个实体就行
            * ShoppingCart 分成菜品、套餐 2 种情况
              分别需要查到 name、price、image
              可以共同设置 userId, createTime
            * number 计数,
              看 ShoppingCart 是不是跟数据库已有的是同一类型, 同一类型则累加份数
              数据库没有这种类型的 ShoppingCart 就单独计数
              同一类型: dishId、dishFlavor 相同( 完全相同的菜品 )或者 setmealId 相同( 同一个套餐 )
                        并且也必须限定是同一个用户的数据

            具体实现
            * 数据库查询该类型的 ShoppingCart
              - 查到了说明有, 只需要修改数量
              - 没查到则需要手动组装完整对象
            * 由于 dto 字段很少, 暂不考虑 converter 转换
         */
        if ( shoppingCartDTO.hasNoDishOrSetmeal() ){
            throw new ShoppingCartIncompleteException( MessageConstant.SHOPPING_CART_ADD_NULL );
        }

        if ( shoppingCartDTO.hasDishAndSetmeal() ){
            throw new ShoppingCartInvalidException( MessageConstant.SHOPPING_CART_ADD_TOO_MANY );
        }

        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();

        /*
            只看这个用户的购物车，
            由于不同菜品在一个用户的订单里面是唯一的，所以用 userId, dishId/setmealId 定位
            菜品还要额外用 flavor 定位
            必须确保每次完成订单，就清除该用户购物车内容，否则会影响之后的查询(查出来多条数据,也就是以前的旧数据)
         */
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
                /*
                    null, 非正数, 无效数据, 要么是上次插入非法数据，要嘛是数据库出问题
                    DTO 不带 number 字段，只能是内部设置的 number, 只能是内部错误
                    所以用 JDK 系统异常
                    TODO: 输出日志 + 包装业务异常提醒用户该商品暂时不可用
                 */
                throw new IllegalStateException(
                        String.format( "%s: id = %s 的购物车商品数量 = %s, 至少应该 = 1",
                                MessageConstant.DATABASE_DATA_ERROR,
                                shoppingCart.getId(), number
                        )
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
                DishShoppingCartProjection dish = dishMapper.selectShoppingCartProjectionById( dishId );
                NotFoundUtil.throwIfNotFound( dish, MessageConstant.DISH_NOT_FOUND );
                name = dish.getName();
                amount = dish.getPrice();
                image = dish.getImage();
            } else { // shoppingCart.isSetmeal
                SetmealShoppingCartProjection setmeal = setmealMapper.selectShoppingCartProjectionById( setmealId );
                NotFoundUtil.throwIfNotFound( setmeal, MessageConstant.SETMEAL_NOT_FOUND );
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

    @Transactional( readOnly = true )
    @Override
    public ShoppingCartListVO listByUserId( Long userId ){
        /*
            ShoppingCartVO = List< ShoppingCartItemVO > + totalAmount(总价)
            不缓存、查数据库
              - 用户频繁修改购物车数据, 如果存缓存会频繁清理缓存
              - 缓存只能针对 1 个用户, 不能全局共享, 收益很小
              - 用户需要对账、需要查询实时数据
         */
        NotFoundUtil.throwIfCountZero( userMapper.countById( userId ), MessageConstant.USER_NOT_FOUND );

        List< ShoppingCartItemVO > shoppingCartItemVOList = shoppingCartMapper.selectItemVOListByUserId( userId );
        if ( ListUtils.isEmpty( shoppingCartItemVOList ) ){
            // 创建 ShoppingCartListVO 时内部初始化了属性、没有 null 字段, 可以安全返回
            return new ShoppingCartListVO();
        }

        BigDecimal totalAmount = calculateTotalAmount( shoppingCartItemVOList );
        return ShoppingCartListVO.builder()
                .items( shoppingCartItemVOList )
                .totalAmount( totalAmount )
                .build();
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void cleanByUserId( Long userId ){
        NotFoundUtil.throwIfCountZero( userMapper.countById( userId ), MessageConstant.USER_NOT_FOUND );
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
            throw new InvalidDataBaseDataException( MessageConstant.DATABASE_DATA_ERROR + ": 购物车商品数量 = " + number );
        }
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 计算总金额 = Σ( 某个商品的单价 * 数量 )
     *
     * @param shoppingCartItemVOList 用户的所有购物车数据
     * @return 该用户的购物车总金额
     */
    private BigDecimal calculateTotalAmount( List< ShoppingCartItemVO > shoppingCartItemVOList ){
        BigDecimal totalAmount = BigDecimal.ZERO;
        for ( ShoppingCartItemVO itemVO : shoppingCartItemVOList ){
            totalAmount = totalAmount.add(
                    itemVO.getAmount().multiply(
                            BigDecimal.valueOf( itemVO.getNumber() )
                    )
            );
        }
        return totalAmount;
    }
}
