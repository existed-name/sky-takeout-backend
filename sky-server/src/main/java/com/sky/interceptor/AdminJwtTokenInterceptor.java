package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.exception.auth.AuthExpiredException;
import com.sky.exception.auth.EmptyJwtTokenException;
import com.sky.exception.auth.InvalidJwtTokenException;
import com.sky.exception.auth.NotLoginException;
import com.sky.exception.auth.ParseJwtFailedException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class AdminJwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    public AdminJwtTokenInterceptor( JwtProperties jwtProperties ){
        this.jwtProperties = jwtProperties;
    }

    /**
     * 校验管理员(员工) JwtToken
     *
     * @param request 请求信息，包含请求体、请求头、请求参数等等
     * @param response 响应信息
     * @param handler HandlerMethod( Controller 方法 ) | ResourceHttpRequestHandler 静态资源
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        // 不是 Controller 方法，而是静态资源请求，直接放行
        if ( ! ( handler instanceof HandlerMethod ) ){
            return true;
        }

        // 已在 WebMvcConfiguration 注册特定请求路径，可直接放行，于是这里不需要单独判断

        // Jwt = secret 服务端密钥 + tokenName 前端参数名称 + ttl 有效时间 + Claims 携带信息
        // Token 从 Headers 获取
        String jwtToken = request.getHeader( jwtProperties.getAdminTokenName() );
        if ( jwtToken == null || jwtToken.isBlank() ){
            throw new EmptyJwtTokenException( MessageConstant.NOT_LOGIN );
        }

        // 解析 JwtToken 拿到携带信息, 注意捕获解析异常
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT( jwtProperties.getAdminSecretKey(), jwtToken );
        } catch ( MalformedJwtException | SignatureException e ){
            // MalformedJwtException – if the claimsJws string is not a valid JWS
            // SignatureException – if the claimsJws JWS signature validation fails
            // claimsJws 参数就是传入的 jwtToken
            throw new InvalidJwtTokenException( MessageConstant.TOKEN_INVALID );
        } catch ( ExpiredJwtException e ){
            // ExpiredJwtException – if the specified JWT is a Claims JWT and the Claims has an expiration time before the time this method is invoked
            throw new AuthExpiredException( MessageConstant.TOKEN_EXPIRED );
        } catch ( JwtException e ){
            // 父类兜底, 但不捕获所有异常 Exception
            throw new ParseJwtFailedException( MessageConstant.PARSE_TOKEN_FAILED );
        }

        // 解析 Claims 不可能为 null、empty，否则已经被捕获异常
        // Claims 内的数字默认 Integer, 但要注意判空
        Object object = claims.get( JwtClaimsConstant.EMP_ID );
        if ( object == null ){
            // 服务端签发的 claims 包含了用户 id，但没有解析出来，那就是解析异常
            throw new ParseJwtFailedException( MessageConstant.LOGIN_FAILED );
        }
        Long empId = Long.valueOf( object.toString() );

        log.info( "当前员工 id: {}", empId );
        BaseContext.setCurrentId( empId );
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex){
        BaseContext.removeCurrentId();
    }
}
