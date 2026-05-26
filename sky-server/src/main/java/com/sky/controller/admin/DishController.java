package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.validation.ValidationGroups;
import com.sky.vo.DishVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 菜品管理接口
 *
 * @CreateTime 2026/5/23 13:22
 */
@Slf4j
@Api( tags = "菜品管理" )
@RestController
@RequestMapping( "/admin/dish" )
@Validated
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
     * 新增菜品
     */
    @ApiOperation( value = "新增菜品" )
    @PostMapping
    public Result< Void > saveWithFlavor(
            @Validated( value = ValidationGroups.Create.class )
            @RequestBody DishDTO dishDTO ){
        log.info( "新增菜品: {}", dishDTO );
        dishService.saveWithFlavor( dishDTO );
        return Result.success();
    }

    /**
     * 条件分页查询菜品
     */
    @ApiOperation( value = "条件分页查询菜品" )
    @GetMapping( "/page" )
    public Result< PageResult > page( // TODO: 如果 DTO 拓展，可能需要 @Validated 检验
            DishPageQueryDTO dishPageQueryDTO ){
        log.info( "条件分页查询菜品, 查询参数: {}", dishPageQueryDTO );
        PageResult dishList = dishService.pageQuery( dishPageQueryDTO );
        return Result.success( dishList );
    }

    /**
     * 批量删除菜品
     */
    @ApiOperation( value = "批量删除菜品" )
    @DeleteMapping
    public Result< Void > delete( @RequestParam
        @NotEmpty( message = "选中的菜品不能为空" ) List< Long > ids ){
        log.info( "批量删除菜品, ids = {}", ids );
        dishService.deleteByIds( ids );
        return Result.success();
    }

    /**
     * 根据 id 查询菜品
     */
    @ApiOperation( value = "根据 id 查询菜品" )
    @GetMapping( "/{id}" )
    public Result< DishVO > getInfo( @PathVariable
         @NotNull( message = "菜品 id 不能为空" )
         @Positive( message = "菜品 id 必须是正整数" ) Long id ){
        log.info( "查询 id = {} 的菜品", id );
        DishVO dishVO = dishService.getInfoById( id );
        return Result.success( dishVO );
    }

    /**
     * 修改菜品信息
     */
    @ApiOperation( value = "修改菜品信息" )
    @PutMapping
    public Result< Void > update( @RequestBody
          @Validated( value = ValidationGroups.Update.class ) DishDTO dishDTO ){
        log.info( "修改菜品信息: {}", dishDTO );
        dishService.update( dishDTO );
        return Result.success();
    }



    // ==================== 私有辅助方法 ====================

}
