package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.shoppingCart.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.impl.ShoppingCartServiceImpl;
import com.sky.vo.user.shoppingCart.ShoppingCartListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

/**
 * 用户端购物车管理接口
 */
@Validated
@Slf4j
@Api( tags = "用户端购物车管理接口" )
// 不过 admin 并没有购物车接口，所以这里也可以不用指定 Bean Name
@RestController( "userShoppingCartController" )
@RequestMapping( "/user/shopping-carts" )
public class ShoppingCartController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final ShoppingCartServiceImpl shoppingCartService;



    // ==================== 构造器 ====================
    public ShoppingCartController( ShoppingCartServiceImpl shoppingCartService ){
        this.shoppingCartService = shoppingCartService;
    }


    // ==================== 公有方法 ====================
    /**
     * 购物车添加菜品/套餐
     * <p>
     *     原来教程想的是单个菜品、单个添加、单条数据,每次添加菜品或者套餐，记录对应数据
     * </p>
     */
    @ApiOperation( "购物车添加菜品/套餐" )
    @PostMapping
    public Result< Void > add( @RequestBody ShoppingCartDTO shoppingCartDTO ){
        /*
            由于 DTO 的菜品/套餐是选一个，而又不确定用户会选哪个，所以不好用注解进行参数检验
            TODO: 拆分不同的 DTO 分别专门装套餐、菜品购物车数据
         */
        Long userId = BaseContext.getCurrentId();
        log.info( "id = {} 的用户添加购物车数据: {}", userId, shoppingCartDTO );
        shoppingCartService.addWithUserId( userId, shoppingCartDTO );
        return Result.success();
    }

    /**
     * 用户查看购物车的所有内容
     * <p>
     *     <ul>
     *         <li>教程逻辑为纯前端计算, 添加、删除、查看购物车数据, 总价都是前端计算</li>
     *         <li>我这里给查看购物车数据补充了总价计算, 把后端权威计算结果返回前端</li>
     *         <li>TODO: 增删购物车数据同步做后端总价计算, 返回计算结果回显, 避免完全依赖前端计算</li>
     *     </ul>
     * </p>
     */
    @ApiOperation( "用户查看购物车的所有内容" )
    @GetMapping
    public Result< ShoppingCartListVO > list(){
        Long userId = BaseContext.getCurrentId();
        log.info( "id = {} 的用户查看购物车所有内容", userId );
        ShoppingCartListVO shoppingCartListVO = shoppingCartService.listByUserId( userId );
        return Result.success( shoppingCartListVO );
    }

    /**
     * 清空购物车
     */
    @ApiOperation( "清空购物车" )
    @DeleteMapping
    public Result< Void > clean(){
        Long userId = BaseContext.getCurrentId();
        log.info( "id = {} 的用户清空购物车", userId );
        shoppingCartService.cleanByUserId( userId );
        return Result.success();
    }

    /**
     * 某个商品数量减一
     */
    @ApiOperation( "某个商品数量减一" )
    @PutMapping( "/{id}/decrement" )
    public Result< Void > decrement(
            @PathVariable( name = "id", required = true )
            @Positive( message = "购物车数据 id 必须是正整数" )
            Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "id = {} 的购物车商品数量减一, 进行操作的用户 id = {}", id, userId );
        shoppingCartService.decrementByIdAndUserId( id, userId );
        return Result.success();
    }



    // ==================== 私有辅助方法 ====================

}
