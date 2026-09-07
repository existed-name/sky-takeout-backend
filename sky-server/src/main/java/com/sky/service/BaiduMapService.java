package com.sky.service;

import com.sky.external.DirectionDTO;
import com.sky.external.GeoCodingDTO;

import javax.validation.constraints.NotBlank;

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

    /**
     * 获取 2 点之间各路线的距离
     * <p>
     *     调用{@link #geocode( String address )}解析 2 点纬度经度,
     *     再调用百度地图路线规划 API 获取路线距离
     * </p>
     *
     * @param originAddress 起点地址
     * @param destinationAddress 终点地址
     * @return 封装 2 点距离数据的对象
     */
    DirectionDTO listDistances( String originAddress, String destinationAddress );
}
