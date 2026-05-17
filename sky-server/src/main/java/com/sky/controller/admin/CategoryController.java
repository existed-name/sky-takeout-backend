package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping( "/admin/category" )
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
    public Result< Object > save( @RequestBody CategoryDTO categoryDTO ){
        log.info( "新增分类: {}", categoryDTO );
        categoryService.save( categoryDTO );
        return Result.success();
    }

    /**
     * 对分类进行条件分页查询
     */
    @GetMapping( "/page" )
    @ApiOperation( value = "对分类进行条件分页查询" )
    public Result< PageResult > page( CategoryPageQueryDTO categoryPageQueryDTO ){
        log.info( "对分类进行条件分页查询，查询参数: {}", categoryPageQueryDTO );
        PageResult pageResult = categoryService.page( categoryPageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 根据 id 删除分类
     */
    @DeleteMapping
    @ApiOperation( value = "根据 id 删除分类")
    public Result< Object > delete( @RequestParam Long id ){
        log.info( "删除 id = {} 的分类", id );
        categoryService.deleteById( id );
        return Result.success();
    }


    // ==================== 私有辅助方法 ====================

}
