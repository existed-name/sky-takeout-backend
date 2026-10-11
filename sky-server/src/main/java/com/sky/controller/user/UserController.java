package com.sky.controller.user;

import com.sky.constant.InterceptorConstant;
import com.sky.dto.user.user.UserLoginDTO;
import com.sky.dto.user.user.WebEndUserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.user.user.UserLoginVO;
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
 * 用户模块接口,
 * <p>
 *     TODO: 注意修改{@code /user/users/login}请求路径需要同步{@link InterceptorConstant.User#REGISTRY_EXCLUDE_PATHS}
 * </p>
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
        log.info( "微信用户登录，登录参数: {}", userLoginDTO );
        UserLoginVO userLoginVO = userService.wechatLogin( userLoginDTO );
        return Result.success( userLoginVO );
    }

    /**
     * Web 用户端登录, 用于绕开微信 API 体验业务流程
     * <p>
     *     注意: <b>该端点仅用于本地端到端测试以及 Web 用户端登录使用, 真实场景请使用正规的{@link #wechatLogin}</b>
     * </p>
     * <p>
     *     约定: Web 前端由用户输入用户名, 后端拼装格式为{@code tourist_{username}}的模拟 openid 返回前端
     * </p>
     */
    @ApiOperation( "Web 用户端登录, 用于绕开微信 API 体验业务流程" )
    @PostMapping( "/login/web" )
    public Result< UserLoginVO > webLogin( @Valid @RequestBody WebEndUserLoginDTO userLoginDTO ){
        log.info( "Web 用户端登录，登录参数: {}", userLoginDTO );
        UserLoginVO userLoginVO = userService.webLogin( userLoginDTO );
        return Result.success( userLoginVO );
    }


    // ==================== 私有辅助方法 ====================

}
