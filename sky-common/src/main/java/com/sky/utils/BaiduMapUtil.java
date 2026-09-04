package com.sky.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.external.GeoCodingDTO;
import com.sky.properties.BaiduMapProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final BaiduMapProperties baiduMapProperties;
    private final ObjectMapper objectMapper;

    public BaiduMapUtil( BaiduMapProperties baiduMapProperties, ObjectMapper objectMapper ){
        this.baiduMapProperties = baiduMapProperties;
        this.objectMapper = objectMapper;
    }
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


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

        // 拼接请求路径
        URIBuilder uriBuilder = new URIBuilder(  );
        uriBuilder.setPath( geocodingUrl );
        requestParamsMap.forEach(
                ( k, v ) -> uriBuilder.addParameter( k, v )
        );

        /*
            发送请求:
            需要用到 HttpClient.execute
            进而需要构建请求对象 HttpUriRequest request
            而 org.apache.http.client.methods.HttpUriRequest 是接口
            进而找到实现类的子类 org.apache.http.client.methods.HttpGet
         */
        String responseString = "";
        try(
                CloseableHttpClient httpClient = HttpClients.createDefault()
        ){
            URI uri = uriBuilder.build();
            HttpGet httpGet = new HttpGet( uri );
            /*
                发请求、拿到响应体 json 字符串
                额外检验: 遇到了再做 → 判空、状态码检验
             */
            try(
                    CloseableHttpResponse httpResponse = httpClient.execute( httpGet )
            ){
                if ( httpResponse == null || httpResponse.getEntity() == null ){
                    String message = String.format( "请求方法 GET, 访问 uri = %s 时响应体为空, 可能是 uri 写错或者远程服务器不可用", uri );
                    log.error( message );
                    throw new IllegalStateException( message );
                }

                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if ( statusCode < 200 || statusCode >= 300 ){
                    String message = String.format( "请求方法 GET, 访问 uri = %s 时响应失败, 状态码 = %d", uri, statusCode );
                    log.error( message );
                    throw new IllegalStateException( message );
                }

                HttpEntity responseEntity = httpResponse.getEntity();
                responseString = EntityUtils.toString( responseEntity, StandardCharsets.UTF_8 );
            }
        } catch ( IOException e ){
            log.error( "创建默认 HttpClient 失败", e );
            throw new RuntimeException( e );
        } catch ( URISyntaxException e ){
            log.error( "构建 URI 对象失败", e );
            throw new RuntimeException( e );
        }
        /*
            解析 json 字符串成目标对象
         */
        GeoCodingDTO geoCodingDTO = null;
        try {
            geoCodingDTO = objectMapper.readValue( responseString, GeoCodingDTO.class );
        } catch ( JsonProcessingException e  ){
            log.error( "解析百度地图地理编码响应体 json 字符串失败: {}", responseString, e );
            throw new RuntimeException( e );
        }
        if ( geoCodingDTO == null ){
            String message = String.format( "百度地图地理编码响应体 json 字符串解析结果为 null: %s", responseString );
            log.error( message );
            throw new RuntimeException( message );
        }
        return geoCodingDTO;
    }


    // ==================== 私有辅助方法 ====================

}
