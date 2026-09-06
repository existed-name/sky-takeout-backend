package com.sky.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.external.DirectionDTO;
import com.sky.external.GeoCodingDTO;
import com.sky.properties.BaiduMapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 百度地图 API, 提供以下功能
 * <pre>
 *     1. 将具体地址进行地理编码，拿到经度纬度
 *     2. 对 2 个地址（已有经度纬度）规划路线、返回各个路线的距离
 * </pre>
 *
 * @CreateTime 2026/9/4 9:11
 */
@Slf4j
@Component
public class BaiduMapUtil {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final BaiduMapProperties baiduMapProperties;
    private final ObjectMapper objectMapper;


    // ==================== 构造器 ====================
    public BaiduMapUtil( BaiduMapProperties baiduMapProperties, ObjectMapper objectMapper ){
        this.baiduMapProperties = baiduMapProperties;
        this.objectMapper = objectMapper;
    }


    // ==================== 公有方法 ====================

    /**
     * 对具体地址调用百度地图 API 地理编码
     *
     * @param address 非空的具体地址, 比如{@code 北京市海淀区上地十街10号}}
     * @return 封装该地址经度纬度的对象{@link GeoCodingDTO}
     */
    public GeoCodingDTO geoCoding( String address ){
        /*
            拿到具体地址
            拼接请求路径
            GET https://api.map.baidu.com/geocoding/v3/?address=北京市海淀区上地十街10号&output=json&ak=您的ak
            发送请求
            拿到响应、解析 json
            拿到需要的字段、拼装自己的对象
         */
        String geocodingUrl = baiduMapProperties.getUrl().getGeocoding();
        Map< String, String > requestParamsMap = Map.ofEntries(
                Map.entry( "address", address ),
                Map.entry( "output", "json" ),
                Map.entry( "ak", baiduMapProperties.getAccessKey() )
        );

        String responseString = HttpClientUtil.doGet( geocodingUrl, requestParamsMap );

        /*
            解析 json 字符串成目标对象
            {
                "status": 0,
                "result": {
                    "location": {
                        "lng": 116.4133836971231,
                        "lat": 39.910924547299568
                    },
                    // ...
                }
            }
         */
        GeoCodingDTO geoCodingDTO = null;
        try {
            geoCodingDTO = objectMapper.readValue( responseString, GeoCodingDTO.class );
        } catch ( JsonProcessingException e  ){
            log.error( "百度地图地理编码响应体 json 字符串解析失败, 字符串信息: {}", responseString );
            throw new IllegalStateException( e );
        }
        if ( geoCodingDTO == null ){
            String message = String.format( "百度地图地理编码响应体 json 字符串解析结果为 null, 字符串信息: %s", responseString );
            throw new IllegalStateException( message );
        }
        return geoCodingDTO;
    }

    /**
     * 根据 2 点地址规划 2 点之间的路线, 主要用于获取距离
     * <p>
     *     内部获取 2 点位置的纬度精度数据后, 调用{@link #directByGeoCoordinates( String destination, String origin ))}
     * </p>
     *
     * @param destinationAddress 终点具体位置, 比如{@code 北京市海淀区上地十街10号}
     * @param originAddress 起点具体位置
     * @return 2 点距离数据封装对象
     */
    public DirectionDTO directByAddresses( String originAddress, String destinationAddress ){
        GeoCodingDTO destinationGeoCodingDTO = geoCoding( destinationAddress );
        String destination = destinationGeoCodingDTO.concatLongitudeAndLatitude();
        GeoCodingDTO originGeoCodingDTO = geoCoding( originAddress );
        String origin = originGeoCodingDTO.concatLongitudeAndLatitude();
        return directByGeoCoordinates( destination, origin );
    }

    /**
     * 根据 2 点的地理坐标( 纬度经度 )规划 2 点之间的路线, 主要用于获取距离
     * <p>
     *     完整请求路径示例: {@code https://api.map.baidu.com/direction/v2/riding?origin=40.01116,116.339303&destination=39.936404,116.452562&ak=您的AK}
     * </p>
     *
     * @param destination 拼接好的终点纬度经度, 格式{@code 纬度,经度}, 小数点后不超过6位, 比如 {@code 40.056878,116.30815}
     * @param origin 拼接好的起点纬度经度字符串
     * @return 2 点距离数据封装对象
     *
     * @see <a href="https://lbsyun.baidu.com/docs/webapi?title=directionv2/webservice-direction/cycling">百度地图路线规划接口文档</a>
     */
    private DirectionDTO directByGeoCoordinates( String origin, String destination ){
        /*
            请求路径、请求参数、发起请求、解析响应
         */
        String directionUrl = baiduMapProperties.getUrl().getDirection();
        Map< String, String > requestParamsMap = Map.ofEntries(
                Map.entry( "origin", origin ),
                Map.entry( "destination", destination ),
                Map.entry( "ak", baiduMapProperties.getAccessKey() )
        );

        String responseJson = HttpClientUtil.doGet( directionUrl, requestParamsMap );
        DirectionDTO directionDTO = new DirectionDTO();
        try {
            /*
                {
                  // ...
                  "result": {
                    "routes": [
                      {
                        // ...
                        // 这个应该才是想要的, result.routes.distance
                        "distance": 17278,
                        "duration": 5879,
                        "steps": [
                           // 一堆规划
                         ],
                        // ...
                      }
                    ],
                    // ...
                  }
                }

                于是根据 ai 提示, 把 json 的 result.routes 提取出来
                然后拿到里面的 distance
             */
            JsonNode rootNode = objectMapper.readTree( responseJson );
            JsonNode routes = rootNode.path( "result" ).path( "routes" );
            List< Double > distances = new ArrayList<>();
            for ( JsonNode route : routes ){
                distances.add( route.path( "distance" ).asDouble() );
            }
            directionDTO.setDistances( distances );
        } catch ( JsonProcessingException e ){
            log.error( "解析 json 树节点失败, 可能是 json 语法/结构错误, 百度地图路线规划响应体信息: {}", responseJson );
            throw new IllegalStateException( e );
        }

        return directionDTO;
    }


    // ==================== 私有辅助方法 ====================

}
