package com.sky.controller.test;

import com.sky.external.DirectionDTO;
import com.sky.external.GeoCodingDTO;
import com.sky.result.Result;
import com.sky.service.BaiduMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * 百度地图封装工具测试接口, 测试地址地理编码、计算 2 点距离等功能
 *
 * @CreateTime 2026/9/5 15:09
 */
@Validated
@Slf4j
@RestController
@RequestMapping( "/test/baidu-map" )
@Api( tags = "百度地图封装工具测试接口" )
// 只在 dev 环境使用: application.yaml 的 spring.profiles.active=dev
@Profile( "dev" )
public class BaiduMapController {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final BaiduMapService baiduMapService;


    // ==================== 构造器 ====================
    public BaiduMapController( BaiduMapService baiduMapService ){
        this.baiduMapService = baiduMapService;
    }


    // ==================== 公有方法 ====================

    /**
     * 对具体地址进行地理编码
     */
    @ApiOperation( "对具体地址进行地理编码" )
    @GetMapping( "/geocode" )
    public Result< GeoCodingDTO > geocode(
            @RequestParam( name = "address", required = true )
            @NotBlank( message = "具体地址不能为空" )
            String address
    ){
        log.info( "测试百度地图封装工具的地理编码功能, 测试地址 = {}", address );
        GeoCodingDTO geoCodingDTO = baiduMapService.geocode( address );
        return Result.success( geoCodingDTO );
    }

    /**
     * 规划 2 地址间路线, 获取各路线距离
     */
    @ApiOperation( "获取 2 地址间各路线的距离" )
    @GetMapping( "/distances" )
    public Result< DirectionDTO > listDistances(
            @RequestParam( name = "origin", required = true )
            @NotBlank( message = "起始地址不能为空" )
            String originAddress,
            @RequestParam( name = "destination", required = true )
            @NotBlank( message = "目标地址不能为空" )
            String destinationAddress
    ){
        log.info( "测试百度地图封装工具的规划 2 点距离功能, 起点 = {} , 终点 = {}", originAddress, destinationAddress );
        DirectionDTO directionDTO = baiduMapService.listDistances( originAddress, destinationAddress );
        return Result.success( directionDTO );
    }


    // ==================== 私有辅助方法 ====================

}
