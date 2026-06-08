package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 用户端 - 套餐接口
 *
 * @CreateTime 2026/6/7 20:42
 */
@Slf4j
@Api( tags = "套餐接口" )
@RestController( "userSetmealController" )
@RequestMapping( "/user/setmeal" )
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
    @GetMapping
    public Result< List< SetmealVO > > list(
            @RequestParam( name = "categoryId" ) // 用户端接口文档要求不能为空
            @NotNull( message = "套餐分类 id 不能为空" )
            @Positive( message = "套餐分类 id 必须是正整数" ) Long categoryId ){
        String logInfo = // ( categoryId == null ) ? "所有套餐" :
                "分类 id = " + categoryId;
        log.info( "查询套餐: {}", logInfo );
        List< SetmealVO > setmealVOList = setmealService.listEnabledByCategoryId( categoryId );
        return Result.success( setmealVOList );
    }

    /**
     * 查询套餐包含的菜品信息
     */
    @ApiOperation( value = "查询套餐包含的菜品" )
    @GetMapping( "/dish/{id}" )
    public Result< List< DishItemVO > > listRelationBySetmealId(
            @PathVariable( name = "id" )
            @NotNull( message = "套餐 id 不能为空" )
            @Positive( message = "套餐 id 必须是正整数" ) Long id ){
        log.info( "查询 id = {} 的套餐包含的菜品", id );
        List< DishItemVO > dishItemVOList = setmealService.listDishItemVOById( id );
        return Result.success( dishItemVOList );
    }



    // ==================== 私有辅助方法 ====================

}
