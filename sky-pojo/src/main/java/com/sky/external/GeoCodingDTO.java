package com.sky.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.text.DecimalFormat;

/**
 * 百度地图将具体位置地理编码后拿到的地理坐标数据对象对象( 包装经度纬度 )
 * <p>
 *     注意<br>
 *     1. 官方的经纬度类型写的 float、所以这里没用 double<br>
 *     2. 内部类应该为 static 类级别
 * </p>
 * 响应示例
 * <pre>
 *     <code>
 {
     "status": 0,
     "result": {
         "location": {
             "lng": 116.4133836971231,
             "lat": 39.910924547299568
         },
         "precise": 0,
         "confidence": 20,
         "comprehension": 100,
         "level": "城市"
     }
 }
 *     </code>
 * </pre>
 *
 * @see <a href="https://lbsyun.baidu.com/docs/webapi?title=geocoding/guide/webservice-geocoding-base">百度地图地理编码接口文档</a>
 * @CreateTime 2026/9/4 10:48
 */
@Getter
// 解析 json 字符串时忽略不包含的字段
@JsonIgnoreProperties( ignoreUnknown = true )
public class GeoCodingDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 状态码，0 为正常 */
    @JsonProperty( "status" )
    private Integer status;
    /** 异常信息, 只在服务状态码异常（非0）时返回 */
    @JsonProperty( "msg" )
    private String msg;
    /** 响应结果信息, result.location.lng/lat 获取经纬度 */
    @JsonProperty( "result" )
    private Result result;

    /**
     * 百度地图地理编码响应 json 字符串里面的 result 对象
     */
    @Getter
    private static class Result {
        @JsonProperty( "location" )
        private Location location;
    }

    /**
     * 百度地图地理编码响应 json 的 result.location 对象
     */
    @Getter
    private static class Location {
        /** 经度 lng, 官方类型为 float */
        @JsonProperty( "lng" )
        private float longitude;
        /** 纬度 lat, 官方类型为 float */
        @JsonProperty( "lat" )
        private float latitude;
    }





    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 将该点的纬度、精度拼接为字符串, 作为百度地图规划路线的请求参数起点经纬度
     * <p>
     *     格式为{@code 纬度 latitude,经度 longitude}, 小数点后不超过6位, 比如 {@code 40.056878,116.30815}
     * </p>
     *
     * @see <a href="https://lbsyun.baidu.com/docs/webapi?title=directionv2/webservice-direction/cycling">百度地图路线规划接口文档</a>
     * @return 拼接好的纬度精度字符串
     */
    public String concatLongitudeAndLatitude(){
        DecimalFormat df = new DecimalFormat( "#.######" );
        String concat = String.format(
                "%s,%s",
                df.format( result.getLocation().getLatitude() ),
                df.format( result.getLocation().getLongitude() )
        );
        return concat;
    }


    // ==================== 私有辅助方法 ====================

}
