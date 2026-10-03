package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.HttpClientConstant;
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
     * @param code 微信用户授权码
     * @return <b>保证非空, 空则抛异常</b>
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信官网登录 API 使用操作</alink="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">
     */
    private WeChatSessionDTO wechatCode2Session( String code ){
        /*
            对比 2 种操作
            * 外层都是 try-with-resources 自动管理 HttpClient
            * 内部还是需要手动关闭创建的对象
            1. 内部直接创建对象，抛出异常，交给 try-with-resources 统一捕获、统一抛出业务异常
                * 好处: 代码清晰、重复性小
                * 缺点: 不好排查哪里创建出错
            2. 内部创建对象时 try-catch-业务异常 + 日志 + 关闭资源
                * 好处: 便于排查
                * 缺点: 太丑陋了、重复性高、每个 catch 块都要记得关闭资源、而且顺利进行后也要关闭资源
                * 如果创建对象抛出的异常不一样就好了，可以直接外层多个 catch 分类
         */
        try ( CloseableHttpClient httpClient = HttpClients.createDefault() ) {
            // GET https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JS_CODE&grant_type=GRANT_TYPE
            HttpGet httpGet = new HttpGet(
                    HttpClientConstant.WECHAT_LOGIN_URL
                    + HttpClientConstant.formatQueryParams(
                            weChatProperties.getAppid(),
                            weChatProperties.getSecret(),
                            code
                    ) );

            // 响应体  -  局部变量不会初始化，必须显式赋 null
            CloseableHttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute( httpGet );
            } catch ( IOException e ){
                log.error( "获取用户登录信息时, HttpClient 执行 Get 操作失败", e );
                // 出现异常记得提前关闭资源
                // 由于异常、正常情况 finally 都会执行，而正常情况不需要提前关闭资源，就不需要 finally，直接在 catch 块里面关闭资源
                closeResource( httpResponse );
                throw new WeChatAuthFailedException( MessageConstant.LOGIN_FAILED );
            }

            // 响应体数据
            HttpEntity responseEntity = httpResponse.getEntity();
            String responseString = null;
            try {
                responseString = EntityUtils.toString( responseEntity );
            } catch ( IOException e ){
                log.error( "解析用户登录响应体失败", e );
                closeResource( httpResponse );
                throw new WeChatAuthFailedException( MessageConstant.LOGIN_FAILED );
            }

            ObjectMapper objectMapper = new ObjectMapper();

            /*  JSON 解析成 Map 不好拓展、维护, 推荐封装对象
            Map< String, String > responseMap = objectMapper.readValue( responseString, new TypeReference< Map< String, String > >() {} );
            String sessionId = responseMap.get( HttpClientConstant.SESSION_ID_STRING );
            String openid = responseMap.get( HttpClientConstant.OPEN_ID_STRING );
             */

            WeChatSessionDTO wechatSessionDTO = null;
            try {
                wechatSessionDTO = objectMapper.readValue( responseString, WeChatSessionDTO.class );
            } catch ( JsonProcessingException e ){
                log.error( "用户登录响应体转换 WeChatSessionDTO 失败", e );
            }

            // 最后还需要再手动关闭
            closeResource( httpResponse );
            // 转换 DTO 失败后，DTO 应该为 null
            // 单独从 catch 块拎出来，是为了少写 1 次关闭资源 + 避免传回空对象
            if ( wechatSessionDTO == null ){
                throw new WeChatAuthFailedException( MessageConstant.LOGIN_FAILED );
            }
            return wechatSessionDTO;
        } catch ( IOException e ){
            log.error( "HttpClient 创建失败", e );
            throw new WeChatAuthFailedException( MessageConstant.LOGIN_FAILED );
        }
    }

    /**
     * 抽取公共的关闭资源逻辑
     */
    private void closeResource( Closeable closeable ){
        if ( closeable != null ){
            try {
                closeable.close();
            } catch ( IOException e ){
                // 感觉关闭失败不影响系统、业务运行，就只是 warn 而不是 error
                log.warn( "{} 关闭失败", closeable.toString() );
            }
        }
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
