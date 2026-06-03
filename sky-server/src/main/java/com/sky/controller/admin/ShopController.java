package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * 店铺管理接口
 *
 * @CreateTime 2026/6/3 10:07
 */
@Slf4j
@Api( tags = "店铺管理接口" )
// 推荐显式命名 value/name，防止 Spring 容器中 Bean 名冲突（尤其 admin/user 都有 ShopController 时）
@RestController( "adminShopController" )
@RequestMapping( "/admin/shop" )
public class ShopController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final ShopService shopService;



    // ==================== 构造器 ====================
    public ShopController( ShopService shopService ){
        this.shopService = shopService;
    }


    // ==================== 公有方法 ====================
    /**
     * 修改店铺营业状态(营业/打烊)
     */
    @ApiOperation( value = "修改店铺营业状态(营业/打烊)" )
    @PutMapping( "/{status}" )
    public Result< Void > updateStatus( @PathVariable( name = "status" )
            @NotNull( message = "店铺营业状态不能为空" ) Integer status ){
        log.info( "修改营业状态为: {}, {}", status, StatusConstant.ShopStatus.getDescription( status ) );
        shopService.updateStatus( status );
        return Result.success();
    }

    /**
     * 查询店铺营业状态
     */
    @ApiOperation( value = "查询店铺营业状态" )
    @GetMapping( "/status" )
    public Result< Integer > getStatus(){
        log.info( "查询店铺营业状态" );
        Integer status = shopService.getStatus();
        return Result.success( status );
    }


    // ==================== 私有辅助方法 ====================

}
