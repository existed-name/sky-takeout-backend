package com.sky.test;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootTest
public class HttpClientTest {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    /**
     * 测试 HttpClient 发送 GET 请求
     */
    @Test
    public void testGET(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        log.info( "创建 HttpClient 对象 = {}", httpClient );

        String url = "http://localhost:8080/user/shop/status";
        HttpGet httpGet = new HttpGet( url );
        log.info( "创建请求对象 = {}", httpGet );

        String httpGetMethod = httpGet.getMethod();
        log.info( "httpGetMethod = {}", httpGetMethod );
        // 发送请求
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute( httpGet );
            log.info( "HttpClient 对象发送请求后的响应结果: {}", httpResponse );

            StatusLine statusLine = httpResponse.getStatusLine();
            log.info( "拿到响应结果的 statusLine = {}", statusLine );

            int statusCode = statusLine.getStatusCode();
            log.info( "服务端返回状态码 statusCode = {}", statusCode );

            HttpEntity httpEntity = httpResponse.getEntity();
            log.info( "响应结果的 httpEntity = {}", httpEntity );

            log.info( "HttpEntity.getContent() 返回的 InputStream 是一次性的，读取完后流就关闭了, \n 再读就是空流，EntityUtils.toString() 内部会尝试重新读取，结果抛出异常" );
//            InputStream content = httpEntity.getContent();
//            byte[] allBytes = content.readAllBytes();
//            log.info( "响应结果读取 bytes = {}", allBytes );
//            log.info( "bytes 转为 String = {}", new String( allBytes, StandardCharsets.UTF_8 ) );

            String body = EntityUtils.toString( httpEntity );
            log.info( "解析 httpEntity = {}", body );
        } catch ( IOException e ){
            log.warn( "HttpClient 对象发送请求时遇到异常" );
        } finally {
            if ( httpResponse != null ){
                try {
                    httpResponse.close();
                } catch ( IOException e ){
                    log.warn( "httpResponse 关闭失败" );
                }
            }
        }

        try {
            httpClient.close();
        } catch ( IOException e ){
            log.warn( "httpClient 关闭失败" );
        }
    }

    /**
     * 测试 HttpClient Post
     * <br>
     * 注意: <b>启动项目后再启动测试方法，否则连接不到 Controller</b>，日志<code>响应体 httpResponse 创建失败</code>
     * <pre>
     *     <code>
     *创建 HttpClient 对象 = org.apache.http.impl.client.InternalHttpClient@7843b65e
     *
     * 创建请求对象 = POST http://localhost:8080/admin/employee/login HTTP/1.1
     *
     * jsonObject 添加( username, xiaomei ) , put1 = null
     *
     * jsonObject 添加( password, 123456 ) , put2 = null
     *
     * jsonObject = {"password":"123456","username":"xiaomei"}
     *
     * 创建请求体 stringEntity = [Content-Type: text/plain; charset=ISO-8859-1,Content-Length: 42,Chunked: false]
     *
     * 响应码 statusCode = 200
     *
     * responseEntity = ResponseEntityProxy{[Content-Type: application/json,Chunked: true]}
     *
     * 响应数据 = {"code":1,"msg":null,"data":{"id":5,"userName":"xiaomei","name":"小美","token":"eyJhbGciOiJIUzI1NiJ9.eyJlbXBJZCI6NSwiZXhwIjoxNzgwNTQwMjg5fQ.Iy75sFNSlhRu85JSAtue1Z4mxSRHAkQoC4jPHXJWEgA"}}
     *     </code>
     * </pre>
     */
    @Test
    public void testPOST(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        log.info( "创建 HttpClient 对象 = {}", httpClient );

        String url = "http://localhost:8080/admin/employee/login";
        HttpPost httpPost = new HttpPost( url );
        log.info( "创建请求对象 = {}", httpPost );

        JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        String usernameKey = "username";
        String usernameValue = "xiaomei";
        String passwordKey = "password";
        String passwordValue = "123456";
        Object put1 = jsonObject.put( usernameKey, usernameValue );
        log.info( "jsonObject 添加( {}, {} ) , put1 = {}", usernameKey, usernameValue, put1 );
        Object put2 = jsonObject.put( passwordKey, passwordValue );
        log.info( "jsonObject 添加( {}, {} ) , put2 = {}", passwordKey, passwordValue, put2 );
        log.info( "jsonObject = {}", jsonObject.toString() );

        // 需要设置请求体 body
        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity( jsonObject.toString() );
            log.info( "创建请求体 stringEntity = {}", stringEntity );
            // 网络请求的编码方式
            stringEntity.setContentEncoding(
                     StandardCharsets.UTF_8.toString()
//                    Encoding.DEFAULT_CHARSET.name()
            );
            // 数据格式
            stringEntity.setContentType( ContentType.APPLICATION_JSON.getMimeType() );
            // 设置请求体
            httpPost.setEntity( stringEntity );
        } catch ( UnsupportedEncodingException e ){
            log.warn( "请求体 stringEntity 创建失败" );
        }

        if ( stringEntity != null ){
            // 创建响应体
            try (  // 自动关闭 httpResponse
                    CloseableHttpResponse httpResponse = httpClient.execute( httpPost )
            ) {
                log.info( "响应体 httpResponse = {}", httpResponse );
                log.info( "响应码 statusCode = {}", httpResponse.getStatusLine().getStatusCode() );
                HttpEntity responseEntity = httpResponse.getEntity();
                log.info( "responseEntity = {}", responseEntity );
                // 解析数据
                log.info( "响应数据 = {}", EntityUtils.toString( responseEntity ) );
            } catch ( IOException e ){
                log.warn( "响应体 httpResponse 创建失败" );
            } finally {
                // 再关闭 httpClient
                if ( httpClient != null ){
                    try {
                        httpClient.close();
                    } catch ( IOException e ){
                        log.warn( "httpClient 关闭失败" );
                    }
                }
            }
        }
    }


    // ==================== 私有辅助方法 ====================

}
