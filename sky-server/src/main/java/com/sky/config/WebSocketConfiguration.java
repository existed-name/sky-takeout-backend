package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类, 将 WebSocket 注册为 Bean
 *
 * @CreateTime 2026/9/14 6:34
 */
@Configuration
public class WebSocketConfiguration {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }


    // ==================== 私有辅助方法 ====================

}
