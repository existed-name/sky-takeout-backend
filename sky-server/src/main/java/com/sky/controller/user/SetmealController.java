package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - 套餐接口
 *
 * @CreateTime 2026/6/7 20:42
 */
@Slf4j
@Api( tags = "套餐接口" )
@RestController( "userSetmealController" )
@RequestMapping( "/user/setmeal" )
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
     * 根据套餐分类 id 查询对应的套餐
     */
    public Result< List< SetmealVO > > list(
            @RequestParam( name = "categoryId", required = false ) Long categoryId ){
        String logInfo = ( categoryId == null ) ? "所有套餐" : "分类 id = " + categoryId;
        log.info( "查询套餐: {}", logInfo );
        List< SetmealVO > setmealVOList = setmealService.listByCategoryId( categoryId );
        return Result.success( setmealVOList );
    }


    // ==================== 私有辅助方法 ====================

}
