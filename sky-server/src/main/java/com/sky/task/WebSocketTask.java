package com.sky.task;

import com.sky.websocket.WebSocketServer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * WebSocket 任务类, 定时向客户端推送数据
 *
 * @CreateTime 2026/9/14 6:54
 */
@Component
public class WebSocketTask {
    private final WebSocketServer webSocketServer;

    public WebSocketTask( WebSocketServer webSocketServer ){
        this.webSocketServer = webSocketServer;
    }
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 通过WebSocket每隔5秒向所有客户端发送消息
     */
    @Scheduled( cron = "*/5 * * * * ?" )
    public void sendMessageToAllClients(){
        String message = String.format(
                "WebSocket 服务端向所有客户端发送信息, 当前时间 = %s",
                LocalDateTime.now()
        );
        webSocketServer.sendToAllClients( message );
    }


    // ==================== 私有辅助方法 ====================

}
