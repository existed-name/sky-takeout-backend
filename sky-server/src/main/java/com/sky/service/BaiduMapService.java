package com.sky.service;

import com.sky.external.GeoCodingDTO;

/**
 * 百度地图服务类, 提供地址地理编码、计算 2 点距离等功能
 *
 * @CreateTime 2026/9/5 14:41
 */
public interface BaiduMapService {
    /**
     * 对具体地址进行地理编码
     *
     * @param address 需要编码的地址, 越具体、越标准/结构化、编码效果越好,
     *                比如{@code 北京市海淀区上地十街10号}
     * @return 地理编码对象, 封装纬度经度数据
     */
    GeoCodingDTO geocode( String address );
}
