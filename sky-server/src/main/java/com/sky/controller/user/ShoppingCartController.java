package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.impl.ShoppingCartServiceImpl;
import com.sky.vo.ShoppingCartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Slf4j
@Api( tags = "用户端购物车管理接口" )
// 不过 admin 并没有购物车接口，所以这里也可以不用指定 Bean Name
@RestController( "userShoppingCartController" )
@RequestMapping( "/user/shoppingCart" )
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
    @PostMapping( "/add" )
    public Result< Void > add( @RequestBody ShoppingCartDTO shoppingCartDTO ){
        // 由于 DTO 的菜品/套餐是选一个，而又不晓得用户会选哪个，所以不好用注解进行参数检验
        log.info( "用户购物车添加菜品/套餐: {}", shoppingCartDTO );
        shoppingCartService.add( shoppingCartDTO );
        return Result.success();
    }

    /**
     * 查看购物车所有内容
     */
    @ApiOperation( "查看购物车所有内容" )
    @GetMapping( "/list" )
    public Result< ShoppingCartVO > list(){
        log.info( "查看购物车所有内容" );
        ShoppingCartVO shoppingCartVO = shoppingCartService.list();
        return Result.success( shoppingCartVO );
    }

    /**
     * 清空购物车
     */
    @ApiOperation( "清空购物车" )
    @DeleteMapping( "/clean" )
    public Result< Void > clean(){
        log.info( "清空购物车" );
        shoppingCartService.clean();
        return Result.success();
    }

    /**
     * 减少加购的商品数量
     */
    @ApiOperation( "减少加购的商品数量" )
    @PutMapping( "/decrement/{id}" )
    public Result< Void > decrement(
            @PathVariable( name = "id" )
            @NotNull( message = "购物车数据 id 不能为空" )
            @Positive( message = "购物车数据 id 必须是正整数" ) Long id ){
        log.info( "减少购物车数据 id = {} 的商品数量", id );
        shoppingCartService.decrementById( id );
        return Result.success();
    }



    // ==================== 私有辅助方法 ====================

}
