package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端组件
 *
 * @CreateTime 2026/9/14 6:11
 */
@Slf4j
@Component
@ServerEndpoint( "/ws/{sid}" )
public class WebSocketServer {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================
    /** 存放会话对象, 并发用 ConcurrentHashMap */
    private static final Map< String, Session > sessionMap = new ConcurrentHashMap<>();


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 连接建立成功调用 onOpen 方法
     *
     * @param session 客户端会话
     * @param sid 会话 id
     */
    @OnOpen
    public void onOpen( Session session, @PathParam( "sid" ) String sid ){
        log.info( "WebSocket 与客户端建立连接, sid = {}", sid );

        // 直接 put 可能覆盖旧会话、所以先关闭了旧会话、再把新会话放进来
        Session oldSession = sessionMap.remove( sid );
        if ( oldSession != null ){
            try {
                oldSession.close();
            } catch ( IOException e ){
                log.error( "WebSocket 关闭会话失败, sid = {}, session = {}", sid, oldSession, e );
            }
        }
        sessionMap.put( sid, session );
    }

    /**
     * 收到客户端信息后调用 onMessage 方法
     *
     * @param message 客户端发送的信息
     * @param sid 会话 id
     */
    @OnMessage
    public void onMessage( String message, @PathParam( "sid" ) String sid ){
        log.debug( "WebSocket 收到客户端信息, sid = {}, message = {}", sid, message );
    }

    /**
     * 关闭连接后调用 onClose 方法
     *
     * @param sid 会话 id
     */
    @OnClose
    public void onClose( @PathParam( "sid" ) String sid ){
        log.info( "WebSocket 与客户端断开连接, sid = {}", sid );
        Session session = sessionMap.remove( sid );
        if ( session != null ){
            try {
                session.close();
            } catch ( IOException e ){
                log.error( "WebSocket 关闭会话失败, sid = {}, session = {}"
                        , sid, session, e );
            }
        }
    }

    /**
     * 群发消息
     *
     * @param message 单个消息, 比如弹幕、群消息
     */
    public void sendToAllClients( String message ){
        Collection< Session > sessions = sessionMap.values();
        for ( Session session : sessions ){
            try {
                session.getBasicRemote().sendText( message );
            } catch ( IOException e ){
                log.error( "WebSocket 群发消息时出错, 出错的 sid = {}, 发送信息 = {}"
                        , session.getId(), message, e );
                // 只记录、不阻断其他会话
                // 甚至可以 remove 该出问题的 session
            }
        }
    }



    // ==================== 私有辅助方法 ====================

}
