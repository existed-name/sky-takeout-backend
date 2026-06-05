package com.sky.interceptor;

import com.sky.constant.InterceptorConstant;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.entity.User;
import com.sky.exception.NotFoundException;
import com.sky.exception.auth.EmptyClaimsException;
import com.sky.exception.auth.EmptyJwtTokenException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户请求拦截器
 *
 * @CreateTime 2026/6/5 12:42
 */
@Component
public class UserJwtTokenInterceptor implements HandlerInterceptor {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;


    // ==================== 构造器 ====================
    public UserJwtTokenInterceptor( JwtProperties jwtProperties, UserMapper userMapper ){
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
    }


    // ==================== 公有方法 ====================

    /**
     *
     * @param request 请求对象，从中获取请求头、参数、URL 等
     * @param response 响应对象，用于设置状态码、写响应体
     * @param handler 被拦截的目标，通常是 HandlerMethod（Controller 的方法）
     *
     * @return true 放行(通过检验) | false 拦截
     */
    @Override
    public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ){
        /*
            手搓思路:
            1. 核心目标: 拦截用户访问请求，判断是否认证、放行
            2. 拦截时机: preHandle( 在 Controller 执行之前 )
            3. 内容:
                * 拦截对象: 目标 Controller 的方法
                * 部分内容不需要拦截
                  - 登录不需要拦截
                  - 允许直接 GET 的内容(只读)
                * 解析 JWT Token, 判断是否合法
                  1. 已登录，放行
                  2. 未登录
                  3. 登录状态失效( JWT 过期 )
                  4. 访问内容权限不够( 先不考虑 )
         */
        // 先看拦到的是不是 Controller 方法( HandlerMethod )
        if ( ! ( handler instanceof HandlerMethod ) ){
            return false;
        }

        HandlerMethod handlerMethod = ( HandlerMethod ) handler;
        // 登录直接放行
        if ( InterceptorConstant.isLogin( handlerMethod.getMethod().getName() ) ){
            return true;
        }

        // 请求路径 /user/shop/status 查看店铺营业状态直接放行( 教程业务需求 )
        String requestURL = request.getRequestURL().toString();
        if ( InterceptorConstant.User.isPathGetShopStatus( requestURL ) ){
            return true;
        }

        // 现在解析 JWT
        // ApiFox 测试时，Token 是放在 Headers 里面的
        // 于是反过来，从前端请求头找 Token
        String jwtToken = request.getHeader( jwtProperties.getUserTokenName() );
        if ( jwtToken == null || jwtToken.isBlank() ){
            // 请求头没带 token
            throw new EmptyJwtTokenException( MessageConstant.USER_NOT_LOGIN );
            // 由于登录次数会很多，可能不需要追踪，所以这里没有打日志
            // 然后不确定应该放哪种具体的报错信息
        }

        // claims 本质是 Map(看源码), 就是 token 携带的信息( "userId" : xx )
        Claims claims = JwtUtil.parseJWT( jwtProperties.getUserSecretKey(), jwtToken );
        if ( claims == null || claims.isEmpty() ){
            // claims 为空可能是 JWT 过期了
            throw new EmptyClaimsException( MessageConstant.TOKEN_EXPIRED );
        }

        // 系统生成的 JWT 跟解析的 JWT 必须一一对应
        // JWT = secretKey( 服务端 JWT 密钥 ) + tokenName( 前端传参命名 ) + ttl( 有效时间 ) + claims( 携带信息 )
        // 前 2 个已经指定，然后如果超出 ttl, claims 应该为空
        // 于是现在只需要比对 claims
        Object object = claims.get( JwtClaimsConstant.USER_ID );
        Long userId = ( Long ) object;
        User user = userMapper.selectById( userId );
        if ( user == null ){
            throw new NotFoundException( MessageConstant.ACCOUNT_NOT_FOUND );
        }

        BaseContext.setCurrentId( userId );
        return true;
    }

    /**
     * 删除记录的用户 id
     */
    @Override
    public void afterCompletion( HttpServletRequest request, HttpServletResponse response, Object handler,
                                 @Nullable Exception ex ){
        BaseContext.removeCurrentId();
    }


    // ==================== 私有辅助方法 ====================

}
