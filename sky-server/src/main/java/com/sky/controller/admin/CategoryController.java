package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.category.CategoryDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.dto.category.CategoryStatusDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.validation.ValidationGroups;
import com.sky.vo.category.CategoryListItemVO;
import com.sky.vo.category.CategoryPageQueryItemVO;
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
    public Result< PageResult< CategoryPageQueryItemVO > > page( @Valid CategoryPageQueryDTO categoryPageQueryDTO ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员条件分页查询商品分类, 查询参数 = {}", operatorId, categoryPageQueryDTO );
        PageResult< CategoryPageQueryItemVO > pageResult = categoryService.page( categoryPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 根据 id 删除分类
     */
    @DeleteMapping( "/{id}" )
    @ApiOperation( value = "根据 id 删除分类")
    @LogOperation( description = "删除分类", operationType = OperationType.DELETE )
    public Result< Void > delete(
            @PathVariable( name = "id", required = true )
            @Positive( message = "分类 id 必须是正整数" )
            Long id
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员删除 id = {} 的商品分类", operatorId, id );
        categoryService.deleteById( operatorId, id );
        return Result.success();
    }

    /**
     * 修改分类信息
     */
    @PutMapping( "/{id}" )
    @ApiOperation( value = "修改分类信息" )
    @LogOperation( description = "修改分类信息", operationType = OperationType.UPDATE )
    public Result< Void > update(
            @PathVariable( name = "id", required = true )
            @Positive( message = "分类 id 必须是正整数" )
            Long id,
            @Validated( ValidationGroups.Update.class )
            @RequestBody CategoryDTO categoryDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员修改 id = {} 的分类信息为 {}", operatorId, id, categoryDTO );
        categoryService.updateById( operatorId, id, categoryDTO );
        return Result.success();
    }

    /**
     * 修改分类启用状态
     */
    @PutMapping( "/{id}/status" )
    @ApiOperation( value = "修改分类启用状态" )
    @LogOperation( description = "修改分类启用状态", operationType = OperationType.UPDATE )
    public Result< Void > updateStatus(
            @PathVariable( name = "id", required = true )
            @Positive( message = "分类 id 必须是正整数" )
            Long id,
            @RequestBody @Valid
            CategoryStatusDTO categoryStatusDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        Integer status = categoryStatusDTO.getStatus();
        log.info( "id = {} 的管理员修改 id = {} 的商品分类状态为 {}", operatorId, id, status );
        categoryService.updateStatus( operatorId, id, status );
        return Result.success();
    }

    /**
     * 查询某种类型的所有分类
     * <p>
     *     业务场景: 创建菜品、套餐需要选择分类,
     *     此时就会下拉列表展示该类型的所有启用的分类
     * </p>
     */
    @GetMapping( "/list" )
    @ApiOperation( value = "查询某种类型的所有分类" )
    public Result< List< CategoryListItemVO > > listByType(
            @RequestParam( name = "type", required = false )
            Integer type
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员根据分类类型查询商品分类, 类型参数 = {}", operatorId, type );
        List< CategoryListItemVO > categoryListItemVOList = categoryService.listByType( type );
        return Result.success( categoryListItemVOList );
    }


    // ==================== 私有辅助方法 ====================

}
