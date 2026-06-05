package com.sky.interceptor;

import com.sky.constant.InterceptorConstant;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.exception.auth.AuthExpiredException;
import com.sky.exception.auth.EmptyClaimsException;
import com.sky.exception.auth.EmptyJwtTokenException;
import com.sky.exception.auth.InvalidJwtTokenException;
import com.sky.exception.auth.ParseJwtFailedException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class UserJwtTokenInterceptor implements HandlerInterceptor {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final JwtProperties jwtProperties;


    // ==================== 构造器 ====================
    public UserJwtTokenInterceptor( JwtProperties jwtProperties ){
        this.jwtProperties = jwtProperties;
    }


    // ==================== 公有方法 ====================

    /**
     *
     * @param request 请求对象，从中获取请求头、参数、URL 等
     * @param response 响应对象，用于设置状态码、写响应体
     * @param handler 被拦截的目标
     *                <p>
     *                - HandlerMethod => Controller 的方法
     *                <p>
     *                - ResourceHttpRequestHandler → 静态资源请求（如 /static/logo.png）
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
            // 不是 HandleMethod 那就是静态资源 ResourceHttpRequestHandler, 直接放行
            return true;
        }

        HandlerMethod handlerMethod = ( HandlerMethod ) handler;
        // 登录直接放行
        if ( InterceptorConstant.isLogin( handlerMethod.getMethod().getName() ) ){
            return true;
        }

        // 请求路径 /user/shop/status 查看店铺营业状态直接放行( 教程业务需求 )
        // 备注: requestURL 是完整 URL 路径: http://localhost:8080/user/shop/status
        // requestURI 才是纯请求路径 /user/shop/status
        // 为了避免极端情况 URL 包括目标路径，建议换成 URI
        String requestURI = request.getRequestURI();
        if ( InterceptorConstant.User.isRequestForShopStatus( requestURI ) ){
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
        Claims claims = null;
        try {
            // 注意源码底层 JwtParser#parseClaimsJws 会抛出运行时异常，需要手动捕获
            claims = JwtUtil.parseJWT( jwtProperties.getUserSecretKey(), jwtToken );
        } catch ( MalformedJwtException e ){
            // if the claimsJws string is not a valid JWS
            // 查看 JwtUtil, parseClaimsJws( token ), 也就是 claimsJws = 传入的 jwtToken
            throw new InvalidJwtTokenException( MessageConstant.LOGIN_FAILED );
        } catch ( SignatureException e ){
            //  if the claimsJws JWS signature validation fails
            throw new InvalidJwtTokenException( MessageConstant.LOGIN_FAILED );
        }  catch ( ExpiredJwtException e ){
            // if the specified JWT is a Claims JWT and the Claims has an expiration time before the time this method is invoked
            throw new AuthExpiredException( MessageConstant.TOKEN_EXPIRED );
        } catch ( JwtException e ){
            // 注意: 只捕获 Jwt 解析相关异常、不捕获所有 Exception，防止吞掉未知异常
            throw new ParseJwtFailedException( MessageConstant.PARSE_TOKEN_FAILED );
        }

        // 不过解析成功一定会返回非空 claims ?
        if ( claims == null || claims.isEmpty() ){
            throw new EmptyClaimsException( MessageConstant.PARSE_TOKEN_FAILED );
        }

        // JWT = secretKey( 服务端 JWT 密钥 ) + tokenName( 前端传参命名 ) + ttl( 有效时间 ) + claims( 携带信息 )
        // 最后只需要从 claims 中拿到用户信息( userId )
        Object object = claims.get( JwtClaimsConstant.USER_ID );
        // 注意数字在 claims 内部通常为 Integer，强转 Long 可能报错
        Long userId = Long.valueOf( object.toString() );

        // 由于 JWT 是服务端签发的，这里解析成功说明用户必存在，就不需要再查库验证了
        log.info( "当前用户 id = {}", userId );
        BaseContext.setCurrentId( userId );
        return true;
    }

    /**
     * 删除记录的用户 id
     */
    @Override
    public void afterCompletion( HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex ){
        BaseContext.removeCurrentId();
    }


    // ==================== 私有辅助方法 ====================

}
