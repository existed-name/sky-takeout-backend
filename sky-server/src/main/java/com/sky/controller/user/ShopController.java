package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺相关接口
 *
 * @CreateTime 2026/6/3 14:57
 */
@Slf4j
@Api( tags = "店铺相关接口" )
// 同名类 / Service 多实现类，需要显式指定 value/name，平时不加
@RestController( "userShopController" )
@RequestMapping( "/user/shop" )
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
