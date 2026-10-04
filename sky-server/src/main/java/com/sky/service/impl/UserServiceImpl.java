package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.WeChatHttpClientConstant;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.user.user.UserLoginDTO;
import com.sky.enumeration.WeChatLoginStatus;
import com.sky.external.WeChatSessionDTO;
import com.sky.entity.User;
import com.sky.exception.auth.WeChatAuthFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.AssertUtils;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.utils.StringUtils;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final WeChatProperties weChatProperties;


    // ==================== 构造器 ====================
    public UserServiceImpl( UserMapper userMapper, JwtProperties jwtProperties, WeChatProperties weChatProperties ){
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
        this.weChatProperties = weChatProperties;
    }


    // ==================== 公有方法 ====================

    @Transactional( rollbackFor = Exception.class )
    @Override
    public UserLoginVO wechatLogin( UserLoginDTO userLoginDTO ){
        /*
            传入参数: 微信用户授权码 code
            官网 API 要求 RequestParam = appid + secret + js_code + grant_type
            只需填入 js_code 即可拿到 session_id, openid
            于是手搓 HttpClient、设置请求体、访问目标路径、解析响应体
            然后检验微信登录响应信息是否有效( 登录成功 )
            之后判断是否需要注册用户
         */
        String code = userLoginDTO.getCode();
        WeChatSessionDTO wechatSessionDTO = wechatCode2Session( code );
        validateWeChatSessionDTO( wechatSessionDTO );

        String openid = wechatSessionDTO.getOpenid();
        Long userId = userMapper.selectIdByOpenid( openid );
        if ( userId == null ){
            /*
                被调用方法加入调用方的事务
             */
            User user = registerWithOpenid( openid );
            userId = user.getId();
        }

        return UserLoginVO.builder()
                .id( userId )
                .openid( openid )
                .token( generateUserJwtToken( userId ) )
                .build();
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public User registerWithOpenid( String openid ){
        User user = User.builder()
                // .id(  ) 交给主键回填
                .openid( openid )
                // .name().phone().sex().idNumber().avatar()
                .createTime( LocalDateTime.now() )
                .updateTime( LocalDateTime.now() )
                .build();
        userMapper.insert( user );

        Long userId = user.getId();
        String message = String.format( "注册用户时, 主键回填失败, 用户 openid = %s", openid );
        AssertUtils.assertIdBackfilled( userId, message );

        return user;
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 通过微信登录 API 获取认证信息( sessionId, openid )
     *
     * @param code 微信用户授权码, 由调用方保证非空、有效
     * @return 微信登录认证信息, <b>保证非空, 空则抛异常</b>
     * @throws WeChatAuthFailedException 当请求失败、响应体 json 反序列化失败时,
     *              将内部异常包装为业务异常抛出, 同时记录日志信息
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信官网登录 API 使用操作</alink="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">
     */
    private WeChatSessionDTO wechatCode2Session( String code ){
        /*
            组装完整 uri, 发送 GET 请求拿到响应体
            把响应体序列化为 json 字符串
            再序列化为目标 dto 对象
         */

        String responseString = null;
        Map< String, String > queryParamsMap = null;
        try {
            // GET https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE
            queryParamsMap = Map.ofEntries(
                    Map.entry( WeChatHttpClientConstant.StringValue.APP_ID, weChatProperties.getAppid() ),
                    Map.entry( WeChatHttpClientConstant.StringValue.SECRET, weChatProperties.getSecret() ),
                    Map.entry( WeChatHttpClientConstant.StringValue.JS_CODE, code ),
                    Map.entry( WeChatHttpClientConstant.StringValue.GRANT_TYPE, WeChatHttpClientConstant.DEFAULT_GRANT_TYPE )
            );
            responseString = HttpClientUtil.doGet(
                    WeChatHttpClientConstant.WECHAT_LOGIN_URL,
                    queryParamsMap
            );
        } catch ( Exception e ){
            log.error( "微信登录请求失败, 请求参数 = {}, 响应体 json 字符串 = {}",
                    queryParamsMap, responseString, e );
            throw new WeChatAuthFailedException( MessageConstant.WECHAT_LOGIN_SERVICE_ERROR );
        }

        /*
            JSON 解析成 Map 不好拓展、维护, 推荐封装对象
         */
        ObjectMapper objectMapper = new ObjectMapper();
        WeChatSessionDTO wechatSessionDTO = null;
        try {
            wechatSessionDTO = objectMapper.readValue( responseString, WeChatSessionDTO.class );
        } catch ( JsonProcessingException e ){
            log.error( "微信登录响应体字符串反序列化为 WeChatSessionDTO 失败, 响应体 json 字符串 = {}", responseString, e );
            throw new WeChatAuthFailedException( MessageConstant.WECHAT_LOGIN_STATUS_PARSE_ERROR );
        }
        if ( wechatSessionDTO == null ){
            log.error( "微信登录响应体字符串反序列化的 WeChatSessionDTO 为 null, 响应体 json 字符串 = {}", responseString );
            throw new WeChatAuthFailedException( MessageConstant.WECHAT_LOGIN_STATUS_PARSE_ERROR );
        }
        return wechatSessionDTO;
    }

    /**
     * 检验微信登录响应信息是否有效, 无效则抛出异常并输出日志信息
     *
     * @param wechatSessionDTO 微信登录接口响应信息
     * @throws WeChatAuthFailedException 当微信登录失败、响应的 openid 为空时抛出
     */
    private void validateWeChatSessionDTO( WeChatSessionDTO wechatSessionDTO ){
//        log.info( "微信登录响应参数 = {}", wechatSessionDTO );

        Integer errorCode = wechatSessionDTO.getErrorCode();
        String errorMessage = wechatSessionDTO.getErrorMessage();
        if ( WeChatLoginStatus.loginFailed( errorCode, errorMessage ) ){
            log.warn( "微信登录失败, 错误码 = {}, 错误信息 = {}", errorCode, errorMessage );

            String message = MessageConstant.WECHAT_LOGIN_FAILED;
            if ( WeChatLoginStatus.isWeChatCodeInvalid( errorCode ) ){
                message = MessageConstant.WECHAT_CODE_INVALID;
            } else if ( WeChatLoginStatus.isWeChatCodeUsed( errorCode ) ){
                message = MessageConstant.WECHAT_LOGIN_REPEATED;
            }
            throw new WeChatAuthFailedException( message );
        }

        String openid = wechatSessionDTO.getOpenid();
        if ( StringUtils.isBlank( openid ) ){
            log.warn( "微信登录用户唯一标识 openid 为空, 可能是微信登录接口异常, 或者 dto 字段映射失败, 错误码 = {}, 错误信息 = {}", errorCode, errorMessage );
            throw new WeChatAuthFailedException( MessageConstant.WECHAT_LOGIN_FAILED );
        }
    }

    /**
     * 调用{@link com.sky.utils.JwtUtil}生成微信用户登录 token
     *
     * @param userId 用户 id, 用于组装 jwt claims
     * @return 后端用户端的 jwt 字符串
     */
    private String generateUserJwtToken( Long userId ){
        /*
            claims 声明: token 携带的信息，比如不敏感的 userId 等等
            注意 jjwt-0.9.1 内部会往 claims Map 里 put 东西
            所以这里不能用 Map.of/ofEntries 不可变 Map
         */
        Map< String, Object > claims = new HashMap<>();
        claims.put( JwtClaimsConstant.USER_ID, userId );
        String jwt = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );
        return jwt;
    }
}
