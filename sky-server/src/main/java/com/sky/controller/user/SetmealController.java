package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.user.setmeal.UserSetmealDishItemVO;
import com.sky.vo.user.setmeal.UserSetmealListItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 用户端 - 套餐接口
 *
 * @CreateTime 2026/6/7 20:42
 */
@Validated
@Slf4j
@Api( tags = "用户端套餐接口" )
@RestController( "userSetmealController" )
@RequestMapping( "/user/setmeals" )
public class SetmealController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final SetmealService setmealService;



    // ==================== 构造器 ====================
    public SetmealController( SetmealService setmealService ){
        this.setmealService = setmealService;
    }


    // ==================== 公有方法 ====================
    /**
     * 根据套餐分类 id 查询起售的套餐
     */
    @ApiOperation( value = "根据套餐分类 id 查询起售的套餐" )
    @GetMapping( "/list" )
    public Result< List< UserSetmealListItemVO > > list(
            @RequestParam( name = "categoryId", required = true )
            @Positive( message = "套餐分类 id 必须是正整数" )
            Long categoryId
    ){
        Long currentUserId = BaseContext.getCurrentId();
        log.info( "id = {} 的用户查看分类 id = {} 的套餐", currentUserId, categoryId );
        List< UserSetmealListItemVO > setmealListItemVOs = setmealService.listEnabledByCategoryId( categoryId );
        return Result.success( setmealListItemVOs );
    }

    /**
     * 查询套餐包含的菜品信息
     */
    @ApiOperation( value = "查询套餐包含的菜品" )
    @GetMapping( "/{id}/dishes" )
    public Result< List< UserSetmealDishItemVO > > listRelationBySetmealId(
            @PathVariable( name = "id", required = true )
            @Positive( message = "套餐 id 必须是正整数" )
            Long id
    ){
        Long currentUserId = BaseContext.getCurrentId();
        log.info( "id = {} 的用户查询 id = {} 的套餐包含的菜品", currentUserId, id );
        List< UserSetmealDishItemVO > setmealDishItemVOList = setmealService.listDishItemVOById( id );
        return Result.success( setmealDishItemVOList );
    }



    // ==================== 私有辅助方法 ====================

}
