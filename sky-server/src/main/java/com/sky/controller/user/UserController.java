package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping( "/user/user" ) // 用户端、user 模块
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
     * 用户登录
     */
    @ApiOperation( value = "用户登录" )
    @PostMapping( "/login" )
    public Result< UserLoginVO > login( @Valid @RequestBody UserLoginDTO userLoginDTO ){
        log.info( "用户登录，登录参数: {}", userLoginDTO );
        UserLoginVO userLoginVO = userService.login( userLoginDTO );
        return Result.success( userLoginVO );
    }


    // ==================== 私有辅助方法 ====================

}
