package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.sky.vo.user.dish.UserDishListItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 用户端 - 菜品管理接口
 *
 * @CreateTime 2026/6/7 20:24
 */
@Validated
@Slf4j
@Api( tags = "菜品管理接口" )
@RestController( "userDishController" )
@RequestMapping( "/user/dishes" )
public class DishController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishService dishService;



    // ==================== 构造器 ====================
    public DishController( DishService dishService ){
        this.dishService = dishService;
    }


    // ==================== 公有方法 ====================
    /**
     * 根据分类 id 查询菜品( <b>只查询起售状态, 包含菜品口味</b> )
     */
    @ApiOperation( value = "根据分类 id 查询菜品" )
    @GetMapping( "/list" )
    public Result< List< UserDishListItemVO > > list(
            @RequestParam( name = "categoryId", required = true ) // 用户端要求必须分类
            @Positive( message = "菜品分类 id 必须是正整数" )
            Long categoryId
    ){
        Long currentUserId = BaseContext.getCurrentId();
        log.info( "id = {} 的用户查询分类 id = {} 的菜品", currentUserId, categoryId );
        List< UserDishListItemVO > dishListItemVOs = dishService.listEnabledByCategoryId( categoryId );
        return Result.success( dishListItemVOs );
    }




    // ==================== 私有辅助方法 ====================

}
