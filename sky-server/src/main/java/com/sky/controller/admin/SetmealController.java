package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.validation.ValidationGroups;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 套餐管理接口
 *
 * @CreateTime 2026/5/28 19:16
 */
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
    @PostMapping
    public Result< Void > save( @RequestBody
        @Validated( value = ValidationGroups.Create.class ) SetmealDTO setmealDTO ){
        log.info( "新增套餐: {}", setmealDTO );
        setmealService.saveWithRelation( setmealDTO );
        return Result.success();
    }


    // ==================== 私有辅助方法 ====================

}
