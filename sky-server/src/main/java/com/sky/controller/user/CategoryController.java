package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.vo.user.category.UserCategoryListItemVO;
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
@Api( tags = "用户端分类管理接口" )
// 注意多端区分 Controller Bean
@RestController( "userCategoryController" )
@RequestMapping( "/user/categories" )
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
     * 查询所有分类(菜品/套餐分类), <b>只展示启用状态的分类</b>
     */
    @ApiOperation( value = "查询所有启用的商品分类(菜品/套餐分类)" )
    @GetMapping
    public Result< List< UserCategoryListItemVO > > list(
            @RequestParam( name = "type", required = false )
            Integer type
    ){
        Long currentUserId = BaseContext.getCurrentId();
        /*
            主要问题在想要把参数数值转为业务描述, 但必须处理 null 的情况
            不传 type, 即 type 为 null 表示查询所有分类
            然而如果把 null 作为有效值计入枚举类, 可能会有歧义、空指针风险
            当然也可以在这里硬编码 type = null 时业务描述为 "所有类型", 但总感觉不规范
            所以暂时不考虑转业务描述, 只输出参数原始值
         */
        log.info( "id = {} 的用户查询商品分类, 分类类型 = {}", currentUserId, type );
        List< UserCategoryListItemVO > categoryListItemVOList = categoryService.listEnabledByType( type );
        return Result.success( categoryListItemVOList );
    }


    // ==================== 私有辅助方法 ====================

}
