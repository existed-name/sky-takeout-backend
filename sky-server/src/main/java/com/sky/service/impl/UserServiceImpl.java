package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.HttpClientConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.external.WechatSessionDTO;
import com.sky.entity.User;
import com.sky.exception.WechatAuthFailed;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final UserMapper userMapper;



    // ==================== 构造器 ====================
    public UserServiceImpl( UserMapper userMapper ){
        this.userMapper = userMapper;
    }


    // ==================== 公有方法 ====================
    @Override
    public UserLoginVO login( UserLoginDTO userLoginDTO ){
        // 传入参数: 微信用户授权码 code
        // 官网 API 要求 RequestParam = appid + secret + js_code + grant_type
        // 只需填入 js_code 即可拿到 session_id, openid
        // 于是手搓 HttpClient、设置请求体、访问目标路径、解析响应体
        String code = userLoginDTO.getCode();
        WechatSessionDTO wechatSessionDTO = wechatCode2Session( code );

        String openid = wechatSessionDTO.getOpenid();
        User user = userMapper.selectByOpenid( openid );
        if ( user == null ){
            user = registerWithOpenid( openid );
        }

        return UserLoginVO.builder()
                .id( user.getId() )
                .openid( openid )
                .token( wechatSessionDTO.getSessionKey() )
                .build();
    }



    // ==================== 私有辅助方法 ====================
    /**
     * 注册用户
     *
     * @param openid 微信用户唯一标识
     * @return 字段完整的用户对象
     */
    private User registerWithOpenid( String openid ){
        User user = User.builder()
                // .id(  )
                .openid( openid )
                // .name().phone().sex().idNumber().avatar()
                .createTime( LocalDateTime.now() )
                .updateTime( LocalDateTime.now() )
                .build();
        userMapper.insert( user );
        return userMapper.selectByOpenid( openid );
    }

    /**
     * 通过微信登录 API 获取认证信息( sessionId, openid )
     *
     * @param code 微信用户授权码
     * @return <b>保证非空, 空则抛异常</b>
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信官网登录 API 使用操作</alink="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">
     */
    private WechatSessionDTO wechatCode2Session( String code ){
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
            HttpGet httpGet = new HttpGet( HttpClientConstant.WECHAT_LOGIN_URL
                    + HttpClientConstant.formatQueryParams( code ) );

            // 响应体  -  局部变量不会初始化，必须显式赋 null
            CloseableHttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute( httpGet );
            } catch ( IOException e ){
                log.error( "获取用户登录信息时, HttpClient 执行 Get 操作失败", e );
                // 出现异常记得提前关闭资源
                // 由于异常、正常情况 finally 都会执行，而正常情况不需要提前关闭资源，就不需要 finally，直接在 catch 块里面关闭资源
                closeResource( httpResponse );
                throw new WechatAuthFailed( MessageConstant.LOGIN_FAILED );
            }

            // 响应体数据
            HttpEntity responseEntity = httpResponse.getEntity();
            String responseString = null;
            try {
                responseString = EntityUtils.toString( responseEntity );
            } catch ( IOException e ){
                log.error( "解析用户登录响应体失败", e );
                closeResource( httpResponse );
                throw new WechatAuthFailed( MessageConstant.LOGIN_FAILED );
            }

            ObjectMapper objectMapper = new ObjectMapper();

            /*  JSON 解析成 Map 不好拓展、维护, 推荐封装对象
            Map< String, String > responseMap = objectMapper.readValue( responseString, new TypeReference< Map< String, String > >() {} );
            String sessionId = responseMap.get( HttpClientConstant.SESSION_ID_STRING );
            String openid = responseMap.get( HttpClientConstant.OPEN_ID_STRING );
             */

            WechatSessionDTO wechatSessionDTO = null;
            try {
                wechatSessionDTO = objectMapper.readValue( responseString, WechatSessionDTO.class );
            } catch ( JsonProcessingException e ){
                log.error( "用户登录响应体转换 WechatSessionDTO 失败", e );
            }

            // 最后还需要再手动关闭
            closeResource( httpResponse );
            // 转换 DTO 失败后，DTO 应该为 null
            // 单独从 catch 块拎出来，是为了少写 1 次关闭资源 + 避免传回空对象
            if ( wechatSessionDTO == null ){
                throw new WechatAuthFailed( MessageConstant.LOGIN_FAILED );
            }
            return wechatSessionDTO;
        } catch ( IOException e ){
            log.error( "HttpClient 创建失败", e );
            throw new WechatAuthFailed( MessageConstant.LOGIN_FAILED );
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
}
