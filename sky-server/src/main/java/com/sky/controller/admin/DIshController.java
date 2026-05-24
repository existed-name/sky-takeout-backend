package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.validation.ValidationGroups;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * 菜品管理接口
 *
 * @CreateTime 2026/5/23 13:22
 */
@Slf4j
@Api( tags = "菜品管理" )
@RestController
@RequestMapping( "/admin/dish" )
public class DIshController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final DishService dishService;



    // ==================== 构造器 ====================
    public DIshController( DishService dishService ){
        this.dishService = dishService;
    }


    // ==================== 公有方法 ====================
    /**
     * 新增菜品
     */
    @ApiOperation( value = "新增菜品" )
    @PostMapping
    public Result< Void > save(
            @Validated( ValidationGroups.Create.class )
//            @NotNull( message = "菜品信息不能为空" ) // @ResponseBody 自动检验 null
            @RequestBody DishDTO dishDTO ){
        log.info( "新增菜品: {}", dishDTO );
        dishService.save( dishDTO );
        return Result.success();
    }


    // ==================== 私有辅助方法 ====================

}
