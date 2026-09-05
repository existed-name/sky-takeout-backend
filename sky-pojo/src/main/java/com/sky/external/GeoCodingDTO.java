package com.sky.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.DecimalFormat;

/**
 * 百度地图将具体位置地理编码后拿到的经度纬度包装对象
 * <p>
 *     注意官方的经纬度类型写的 float、所以这里没用 double
 * </p>
 *
 * @see <a href="https://lbsyun.baidu.com/docs/webapi?title=geocoding/guide/webservice-geocoding-base">百度地图地理编码接口文档</a>
 * @CreateTime 2026/9/4 10:48
 */
// 解析 json 字符串时忽略
@JsonIgnoreProperties( ignoreUnknown = true )
public class GeoCodingDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 精度 lng */
    private float longitude;
    /** 维度 lat */
    private float latitude;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 将该点的纬度、精度拼接为字符串, 作为百度地图规划路线的请求参数起点经纬度
     * <p>
     *     格式为{@code 纬度,经度}, 小数点后不超过6位, 比如 {@code 40.056878,116.30815}
     * </p>
     *
     * @see <a href="https://lbsyun.baidu.com/docs/webapi?title=directionv2/webservice-direction/cycling">百度地图路线规划接口文档</a>
     * @return 拼接好的纬度精度字符串
     */
    public String concatLongitudeAndLatitude(){
        DecimalFormat df = new DecimalFormat( "#.######" );
        String result = String.format( "%s,%s", df.format( latitude ), df.format( longitude ) );
        return result;
    }


    // ==================== 私有辅助方法 ====================

}
