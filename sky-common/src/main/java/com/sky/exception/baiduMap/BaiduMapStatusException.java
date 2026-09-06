package com.sky.exception.baiduMap;

import com.sky.exception.BusinessException;

/**
 * 百度地图 API 响应状态码异常,
 * <p>
 *     正常状态码 = 0 {@link com.sky.enumeration.BaiduMapAPIStatus}, <br>
 *     状态码非 0 为异常: 地址不存在、AccessKey 错误、请求参数错误、不在 IP 白名单等等情况,
 * </p>
 *
 * @CreateTime 2026/9/6 20:09
 */
public class BaiduMapStatusException extends BusinessException {
    public BaiduMapStatusException( String message ){
        super( message );
    }
}
