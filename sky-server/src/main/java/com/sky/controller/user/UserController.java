package com.sky.controller.user;

import com.sky.dto.user.user.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 用户模块接口
 *
 * @CreateTime 2026/6/4 14:14
 */
@Validated
@Slf4j
@Api( tags = "用户模块接口" )
@RestController
@RequestMapping( "/user/users" )
public class UserController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final UserService userService;



    // ==================== 构造器 ====================
    public UserController( UserService userService ){
        this.userService = userService;
    }


    // ==================== 公有方法 ====================
    /**
     * 微信用户登录
     */
    @ApiOperation( value = "微信用户登录" )
    @PostMapping( "/login" )
    public Result< UserLoginVO > wechatLogin( @Valid @RequestBody UserLoginDTO userLoginDTO ){
        log.info( "用户登录，登录参数: {}", userLoginDTO );
        UserLoginVO userLoginVO = userService.wechatLogin( userLoginDTO );
        return Result.success( userLoginVO );
    }


    // ==================== 私有辅助方法 ====================

}
