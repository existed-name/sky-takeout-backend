package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - 菜品管理接口
 *
 * @CreateTime 2026/6/7 20:24
 */
@Slf4j
@Api( tags = "菜品管理接口" )
@RestController( "userDishController" )
@RequestMapping( "/user/dish" )
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
     * 根据分类 id 查询菜品
     */
    @ApiOperation( value = "根据分类 id 查询菜品" )
    @GetMapping( "/list" )
    public Result< List< DishVO > > list(
            @RequestParam( name = "categoryId", required = false )
            Long categoryId ){
        log.info( "查询分类 id = {} 的菜品", categoryId );
        List< DishVO > dishVOList = dishService.listByCategoryId( categoryId );
        return Result.success( dishVOList );
    }




    // ==================== 私有辅助方法 ====================

}
