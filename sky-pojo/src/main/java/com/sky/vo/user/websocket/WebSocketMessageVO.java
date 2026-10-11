package com.sky.vo.user.websocket;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * WebSocket 服务端向客户端发送的消息包装对象
 *
 * @CreateTime 2026/9/15 6:40
 */
@Builder
@Data
@ApiModel( description = "WebSocket 服务端向客户端发送的消息包装对象" )
public class WebSocketMessageVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /**
     * 消息类型数值，1为来单提醒 2为客户催单
     * <p>
     *     #{@link com.sky.enumeration.WebSocketMessageType}
     * </p>
     */
    @ApiModelProperty( value = "消息类型", example = "1" )
    private int type;
    /** 订单 id */
    @ApiModelProperty( value = "订单 id", example = "1" )
    private Long orderId;
    /** 消息内容 */
    @ApiModelProperty( value = "消息内容", example = "您有一个订单待处理, 订单号: xxx, 请及时处理" )
    private String content;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
