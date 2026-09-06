package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 百度地图 API 响应状态, 包含地理编码、路线规划的正常状态, 后续可开新枚举类单独记录各个接口的状态码
 *
 * @CreateTime 2026/9/6 19:54
 */
@AllArgsConstructor
@Getter
public enum BaiduMapAPIStatus {
    /** 地理编码 API - 状态正常 */
    GEO_CODING_OK( 0 ),
    /** 路线规划  API - 状态正常 */
    DIRECTION_OK( 0 );

    private final int statusCode;


}
