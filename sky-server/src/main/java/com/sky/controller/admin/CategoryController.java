package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.constant.TypeConstant;
import com.sky.context.BaseContext;
import com.sky.dto.category.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.validation.ValidationGroups;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Validated
@Slf4j
@RestController( "adminCategoryController" )
@RequestMapping( "/admin/categories" )
@Api( tags = "分类管理接口")
public class CategoryController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final CategoryService categoryService;


    // ==================== 构造器 ====================
    public CategoryController( CategoryService categoryService ){
        this.categoryService = categoryService;
    }


    // ==================== 公有方法 ====================
    /**
     * 新增分类(菜品/套餐分类)
     */
    @PostMapping
    @ApiOperation( value = "新增分类(菜品/套餐分类)" )
    @LogOperation( description = "新增分类", operationType = OperationType.INSERT )
    public Result< Void > save(
            @Validated( value = ValidationGroups.Create.class )
            @RequestBody CategoryDTO categoryDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员新增分类: {}", operatorId, categoryDTO );
        categoryService.save( operatorId, categoryDTO );
        return Result.success();
    }

    /**
     * 对分类进行条件分页查询
     */
    @GetMapping
    @ApiOperation( value = "对分类进行条件分页查询" )
    public Result< PageResult > page( @Valid CategoryPageQueryDTO categoryPageQueryDTO ){
        log.info( "对分类进行条件分页查询，查询参数: {}", categoryPageQueryDTO );
        PageResult pageResult = categoryService.page( categoryPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 根据 id 删除分类
     */
    @DeleteMapping
    @ApiOperation( value = "根据 id 删除分类")
    @LogOperation( description = "删除分类", operationType = OperationType.DELETE )
    public Result< Void > delete(
            @RequestParam( name = "id" )
            @NotNull( message = "分类 id 不能为空" )
            @Positive( message = "分类 id 必须是正整数" ) Long id ){
        log.info( "删除 id = {} 的分类", id );
        categoryService.deleteById( id );
        return Result.success();
    }

    /**
     * 修改分类信息
     */
    @PutMapping
    @ApiOperation( value = "修改分类信息" )
    @LogOperation( description = "修改分类信息", operationType = OperationType.UPDATE )
    public Result< Void > update( @Validated( ValidationGroups.Update.class )
            @RequestBody CategoryDTO categoryDTO ){
        log.info( "修改分类信息: {}", categoryDTO );
        categoryService.update( categoryDTO );
        return Result.success();
    }

    /**
     * 修改分类状态(启用/禁用)
     */
    @PutMapping( "/status/{status}" )
    @ApiOperation( value = "修改分类状态(启用/禁用)" )
    @LogOperation( description = "修改分类状态(启用/禁用)", operationType = OperationType.UPDATE )
    public Result< Void > updateStatus(
            @RequestParam( name = "id" )
            @NotNull( message = "分类 id 不能为空" )
            @Positive( message = "分类 id 必须是正整数" ) Long id,
            @PathVariable( name = "status" )
            @NotNull( message = "分类状态不能为空" ) Integer status ){
        log.info( "修改 id = {} 的分类状态为 {}", id, status );
        categoryService.updateStatus( id, status );
        return Result.success();
    }

    /**
     * 查询某种类型的所有分类
     */
    @GetMapping( "/list" )
    @ApiOperation( value = "查询某种类型的所有分类" )
    public Result< List< Category > > listByType(
            @RequestParam( name = "type", required = false ) Integer type ){
        log.info( "查询分类, type = {}, 业务描述 = {}", type, TypeConstant.toDescription( type ) );
        List< Category > categoryList = categoryService.listByType( type );
        return Result.success( categoryList );
    }


    // ==================== 私有辅助方法 ====================

}
