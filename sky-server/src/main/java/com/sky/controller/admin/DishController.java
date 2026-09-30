package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.enumeration.OperationType;
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

import javax.validation.Valid;
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
@RestController( "adminDishController" )
@RequestMapping( "/admin/dishes" )
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
    @LogOperation( description = "新增菜品", operationType = OperationType.INSERT )
    @PostMapping
    public Result< Void > saveWithFlavors(
            @Validated( value = ValidationGroups.Create.class )
            @RequestBody DishDTO dishDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员新增菜品: {}", operatorId, dishDTO );
        dishService.saveWithFlavors( operatorId, dishDTO );
        return Result.success();
    }

    /**
     * 条件分页查询菜品
     */
    @ApiOperation( value = "条件分页查询菜品" )
    @GetMapping
    public Result< PageResult > page(
            @Valid DishPageQueryDTO dishPageQueryDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员条件分页查询菜品, 查询参数 = {}", operatorId, dishPageQueryDTO );
        PageResult dishList = dishService.pageQuery( dishPageQueryDTO );
        return Result.success( dishList );
    }

    /**
     * 批量删除菜品
     */
    @ApiOperation( value = "批量删除菜品" )
    @LogOperation( description = "批量删除菜品", operationType = OperationType.DELETE )
    @DeleteMapping
    public Result< Void > delete(
            @NotEmpty( message = "选中的菜品不能为空" )
            @RequestParam( name = "ids", required = true )
            List< Long > ids
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员批量删除菜品, ids = {}", operatorId, ids );
        dishService.deleteByIds( operatorId, ids );
        return Result.success();
    }

    /**
     * 根据 id 查询菜品
     */
    @ApiOperation( value = "根据 id 查询菜品" )
    @GetMapping( "/{id}" )
    public Result< DishVO > getInfo(
            @PathVariable( name = "id" )
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
    @LogOperation( description = "修改菜品信息", operationType = OperationType.UPDATE )
    @PutMapping
    public Result< Void > update( @Validated( value = ValidationGroups.Update.class )
            @RequestBody DishDTO dishDTO ){
        log.info( "修改菜品信息: {}", dishDTO );
        dishService.update( dishDTO );
        return Result.success();
    }

    /**
     * 切换菜品状态(停售/起售)
     */
    @ApiOperation( value = "切换菜品状态(停售/起售)" )
    @LogOperation( description = "切换菜品状态", operationType = OperationType.UPDATE )
    @PutMapping( "/status/{status}" )
    public Result< Void > updateStatus(
            @RequestParam( name = "id" ) @NotNull( message = "菜品 id 不能为空" )
            @Positive( message = "菜品 id 必须是正整数" ) Long id,
            @PathVariable( name = "status" )
            @NotNull( message = "菜品状态不能为空" ) Integer status ){
        log.info( "修改 id = {} 的菜品状态为 {}", id, status );
        dishService.updateStatusById( id, status );
        return Result.success();
    }

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
