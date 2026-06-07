package com.sky.controller.user;

import com.sky.constant.TypeConstant;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - 分类管理接口
 *
 * @CreateTime 2026/6/7 17:04
 */
@Slf4j
@Api( tags = "分类管理接口" )
// 注意多端区分 Controller Bean
@RestController( "userCategoryController" )
@RequestMapping( "/user/category" )
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
     * 查询所有分类(菜品/套餐分类)
     */
    @ApiOperation( value = "查询所有分类(菜品/套餐分类)" )
    @GetMapping( "/list" )
    public Result< List< Category > > list( @RequestParam( name = "type", required = false )
                Integer type ){
        log.info( "查询分类, type = {}, 业务描述 = {} )", type, TypeConstant.toDescription( type ) );
        List< Category > categoryList = categoryService.listByType( type );
        return Result.success( categoryList );
    }


    // ==================== 私有辅助方法 ====================

}
