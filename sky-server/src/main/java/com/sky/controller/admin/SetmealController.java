package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.validation.ValidationGroups;
import com.sky.vo.SetmealVO;
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
 * 套餐管理接口
 *
 * @CreateTime 2026/5/28 19:16
 */
@Validated
@Slf4j
@RestController
@RequestMapping( "/admin/setmeal" )
@Api( tags = "套餐管理接口" )
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
     * 新增套餐
     */
    @ApiOperation( value = "新增套餐" )
    @LogOperation( description = "新增套餐", operationType = OperationType.INSERT )
    @PostMapping
    public Result< Void > save( @Validated( value = ValidationGroups.Create.class )
            @RequestBody SetmealDTO setmealDTO ){
        log.info( "新增套餐: {}", setmealDTO );
        setmealService.saveWithRelation( setmealDTO );
        return Result.success();
    }

    /**
     * 条件分页查询套餐
     */
    @ApiOperation( value = "条件分页查询套餐" )
    @GetMapping( "/page" )
    public Result< PageResult > page(
            @NotNull( message = "条件分页查询参数不能为空" )
            // TODO: 如果需要检验属性，则加上 @Valid
            SetmealPageQueryDTO setmealPageQueryDTO ){
        log.info( "条件分页查询套餐, 查询参数: {}", setmealPageQueryDTO );
        PageResult pageResult = setmealService.page( setmealPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 批量删除套餐
     */
    @ApiOperation( value = "批量删除套餐" )
    @LogOperation( description = "批量删除套餐", operationType = OperationType.DELETE )
    @DeleteMapping
    public Result< Void > delete( @NotEmpty( message = "选中的套餐不能为空" )
            @RequestParam( name = "ids" ) List< Long > ids ){
        log.info( "批量删除套餐 ids = {}", ids );
        setmealService.deleteBatch( ids );
        return Result.success();
    }

    /**
     * 根据 id 查询套餐
     */
    @ApiOperation( value = "根据 id 查询套餐" )
    @GetMapping( "/{id}" )
    public Result< SetmealVO > getInfo(
            @PathVariable( name = "id" )
            @NotNull( message = "套餐 id 不能为空" )
            @Positive( message = "套餐 id 必须是正整数" ) Long id ){
        log.info( "查询 id = {} 的套餐", id );
        SetmealVO setmealVO = setmealService.getInfo( id );
        return Result.success( setmealVO );
    }

    /**
     * 修改套餐信息
     */
    @ApiOperation( value = "修改套餐信息" )
    @LogOperation( description = "修改套餐信息", operationType = OperationType.UPDATE )
    @PutMapping
    public Result< Void > update( @Validated( value = ValidationGroups.Update.class )
            @RequestBody SetmealDTO setmealDTO ){
        log.info( "修改套餐: {}", setmealDTO );
        setmealService.update( setmealDTO );
        return Result.success();
    }

    /**
     * 修改套餐状态(起售/停售)
     */
    @ApiOperation( value = "修改套餐状态(起售/停售)" )
    @LogOperation( description = "修改套餐状态", operationType = OperationType.UPDATE )
    @PutMapping( "/status/{status}" )
    public Result< Void > updateStatus( @RequestParam( name = "id" )
            @NotNull( message = "套餐 id 不能为空" )
            @Positive( message = "套餐 id 必须是正整数" ) Long id,
            @PathVariable( name = "status" )
            @NotNull( message = "修改后的套餐状态不能为空" ) Integer status ){
        log.info( "修改 id = {} 的套餐状态为 {}", id, status );
        setmealService.updateStatusById( id, status );
        return Result.success();
    }


    // ==================== 私有辅助方法 ====================

}
