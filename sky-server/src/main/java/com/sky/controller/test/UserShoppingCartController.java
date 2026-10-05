package com.sky.controller.test;

import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartListVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

/**
 * 用户端购物车管理测试接口, 仅在 dev 配置下启用
 *
 * @CreateTime 2026/10/5 14:44
 */
@Validated
@Slf4j
@RestController( "testUserShoppingCartController" )
@RequestMapping( "/test/shopping-carts" )
@Profile( "dev" )
public class UserShoppingCartController {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final ShoppingCartService shoppingCartService;


    // ==================== 构造器 ====================
    public UserShoppingCartController( ShoppingCartService shoppingCartService ){
        this.shoppingCartService = shoppingCartService;
    }


    // ==================== 公有方法 ====================

    /**
     * 用户查看购物车的所有内容
     */
    @ApiOperation( "用户查看购物车的所有内容" )
    @GetMapping
    public Result< ShoppingCartListVO > list(
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的用户查看购物车所有内容", userId );
        ShoppingCartListVO shoppingCartListVO = shoppingCartService.listByUserId( userId );
        return Result.success( shoppingCartListVO );
    }

    /**
     * 清空购物车
     */
    @ApiOperation( "清空购物车" )
    @DeleteMapping
    public Result< Void > clean(
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
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
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的购物车商品数量减一, 进行操作的用户 id = {}", id, userId );
        shoppingCartService.decrementByIdAndUserId( id, userId );
        return Result.success();
    }


    // ==================== 私有辅助方法 ====================

}
