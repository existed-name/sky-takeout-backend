package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务端发送给客户端的消息类型
 *
 * @CreateTime 2026/9/15 6:28
 */
@Getter
@AllArgsConstructor
public enum WebSocketMessageType {
    /** 用户来单提醒 */
    NEW_ORDER_ALERT( 1, "用户来单提醒" ),
    /** 用户催单 */
    USER_URGENT_REQUEST( 2, "用户催单" );

    /** 消息类型数值 */
    private final int type;
    /** 消息类型 */
    private final String typeName;
}
