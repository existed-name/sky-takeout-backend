package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.shop.ShopStatusDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 店铺管理接口
 * <p>
 *     注意: 接口路径设计问题
 *     <ol>
 *         <li>{@code /shop}表示单例资源, 全项目只有 1 个 shop</li>
 *         <li>而{@code /shops}表示资源集合 => 有多个店铺</li>
 *         <li>由于当前项目确实只针对 1 个店铺, 所以选择单数</li>
 *         <li>如果会有拓展, 可以考虑{@code /shops/current}或者{@code /shops/{shopId}}标识当前店铺</li>
 *     </ol>
 * </p>
 *
 * @CreateTime 2026/6/3 10:07
 */
@Slf4j
@Api( tags = "店铺管理接口" )
// 同名类 / Service 多实现类，需要显式指定 value/name，平时不加
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
    @LogOperation( description = "修改店铺营业状态(营业/打烊)", operationType = OperationType.UPDATE )
    @ApiOperation( value = "修改店铺营业状态(营业/打烊)" )
    @PutMapping( "/status" )
    public Result< Void > updateStatus(
            @RequestBody @Valid
            ShopStatusDTO shopStatusDTO
    ){
        Long operatorId = BaseContext.getCurrentId();
        Integer status = shopStatusDTO.getStatus();
        log.info( "id = {} 的管理员修改店铺营业状态为 {}", operatorId, status );
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
