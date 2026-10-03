package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.setmeal.SetmealDTO;
import com.sky.dto.setmeal.SetmealPageQueryDTO;
import com.sky.dto.setmeal.SetmealStatusDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.validation.ValidationGroups;
import com.sky.vo.SetmealVO;
import com.sky.vo.setmeal.SetmealInfoVO;
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
 * 套餐管理接口
 *
 * @CreateTime 2026/5/28 19:16
 */
@Validated
@Slf4j
@RestController( "adminSetmealController" )
@RequestMapping( "/admin/setmeals" )
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
    public Result< Void > save(
            @Validated( value = ValidationGroups.Create.class )
            @RequestBody SetmealDTO setmealDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员新增套餐, 套餐信息 = {}", operatorId, setmealDTO );
        setmealService.saveWithRelation( operatorId, setmealDTO );
        return Result.success();
    }

    /**
     * 条件分页查询套餐
     */
    @ApiOperation( value = "条件分页查询套餐" )
    @GetMapping
    public Result< PageResult > page(
            @Valid SetmealPageQueryDTO setmealPageQueryDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员条件分页查询套餐, 查询参数 = {}", operatorId, setmealPageQueryDTO );
        PageResult pageResult = setmealService.page( setmealPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 批量删除套餐
     */
    @ApiOperation( value = "批量删除套餐" )
    @LogOperation( description = "批量删除套餐", operationType = OperationType.DELETE )
    @DeleteMapping
    public Result< Void > delete(
            @RequestParam( name = "ids", required = true )
            // RequestParam 拦住 null , NotEmpty 拦住 null 和空链表
            @NotEmpty( message = "选中的套餐不能为空" )
            List< Long > ids
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员批量删除套餐, 套餐 ids = {}", operatorId, ids );
        setmealService.deleteBatch( operatorId, ids );
        return Result.success();
    }

    /**
     * 根据 id 查询套餐
     */
    @ApiOperation( value = "根据 id 查询套餐" )
    @GetMapping( "/{id}" )
    public Result< SetmealInfoVO > getInfo(
            @PathVariable( name = "id", required = true )
            @Positive( message = "套餐 id 必须是正整数" )
            Long id
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员查询 id = {} 的套餐", operatorId, id );
        SetmealInfoVO setmealInfoVO = setmealService.getInfo( id );
        return Result.success( setmealInfoVO );
    }

    /**
     * 修改套餐信息
     */
    @ApiOperation( value = "修改套餐信息" )
    @LogOperation( description = "修改套餐信息", operationType = OperationType.UPDATE )
    @PutMapping( "/{id}" )
    public Result< Void > update(
            @PathVariable( name = "id", required = true )
            @Positive( message = "套餐 id 必须是正整数" )
            Long id,
            @Validated( value = ValidationGroups.Update.class )
            @RequestBody
            SetmealDTO setmealDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员修改套餐信息, 套餐 id = {}, 修改数据 = {}", operatorId, id, setmealDTO );
        setmealService.update( operatorId, id, setmealDTO );
        return Result.success();
    }

    /**
     * 修改套餐状态(起售/停售)
     */
    @ApiOperation( value = "修改套餐状态(起售/停售)" )
    @LogOperation( description = "修改套餐状态", operationType = OperationType.UPDATE )
    @PutMapping( "/{id}/status" )
    public Result< Void > updateStatus(
            @PathVariable( name = "id", required = true )
            @Positive( message = "套餐 id 必须是正整数" )
            Long id,
            @RequestBody @Valid
            SetmealStatusDTO setmealStatusDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        Integer status = setmealStatusDTO.getStatus();
        log.info( "id = {} 的管理员修改 id = {} 的套餐状态为 {}", operatorId, id, status );
        setmealService.updateStatusById( operatorId, id, status );
        return Result.success();
    }

    /**
     * 根据套餐分类 id 查询对应的套餐
     */
    public Result< List< SetmealVO > > list(
            @RequestParam( name = "categoryId", required = false )
            // @Positive: 参数非 null 时生效
            @Positive( message = "分类 id 必须是正整数" )
            Long categoryId
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员查询某个分类的所有套餐, 分类 id = {}", operatorId, categoryId );
        List< SetmealVO > setmealVOList = setmealService.listByCategoryId( categoryId );
        return Result.success( setmealVOList );
    }


    // ==================== 私有辅助方法 ====================

}
