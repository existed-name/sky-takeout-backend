package com.sky.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类，向远程发送请求，封装了 Get、Post 操作
 *
 * @UpdateTime 2026-09-05 6:44
 */
@Slf4j
public class HttpClientUtil {

    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 向目标路径/网址发送 GET 请求
     *
     * @param url 请求参数之前的所有网址 = 目标接口的{@code 协议://域名或者IP/部分路径}, 比如{@code https://api.map.baidu.com/geocoding/v3}
     * @param nameToRequestParamsMap 请求参数, 以 RequestParam 形式拼接在请求路径后面, 比如{@code https://api.map.baidu.com/geocoding/v3?address=北京市海淀区上地十街10号&output=json&ak=您的ak}
     * @return 响应体{@link org.springframework.http.ResponseEntity}解析出的 json 字符串
     */
    public static String doGet( String url, Map< String, String > nameToRequestParamsMap ){
        /*
            1. 创建默认 HttpClient 操作客户端
            2. 发送请求:
                需要用到 HttpClient.execute
                进而需要构建请求对象 HttpUriRequest request
                而 org.apache.http.client.methods.HttpUriRequest 是接口
                进而找到实现类的子类 org.apache.http.client.methods.HttpGet
            3. 于是需要提前构建 HttpGet
            4. 于是需要提前构建 uri 拼接请求路径
         */
        URI uri = null;
        try {
            URIBuilder uriBuilder = new URIBuilder( url );
            nameToRequestParamsMap.forEach(
                    /*
                        本来是在 try 外面定义 URIBuilder、try 内赋值
                        但是 lambda 要求变量不变
                        Variable used in lambda expression should be final or effectively final
                        所以就合在一起构建、只输出产物 uri
                      */
                    ( k, v ) -> uriBuilder.addParameter( k, v )
            );
            try {
                uri = uriBuilder.build();
            } catch ( URISyntaxException e ){
                log.error( "构建 URI 对象失败, url = {}", url );
                throw new IllegalStateException( e );
            }
        } catch ( URISyntaxException e ){
            log.error( "URIBuilder 创建失败, 可能是 url 格式错误导致解析失败, url = {}", url );
            throw new IllegalStateException( e );
        }


        String responseString = "";
        try(
            CloseableHttpClient httpClient = HttpClients.createDefault();
        ){
            HttpGet httpGet = new HttpGet( uri );

            try(
                CloseableHttpResponse httpResponse = httpClient.execute( httpGet );
            ){
            /*
                发请求、拿到响应体 json 字符串
                额外检验: 遇到了再做 → 判空、状态码检验
             */
                if ( httpResponse == null || httpResponse.getEntity() == null ){
                    String message = String.format( "请求方法 GET, 访问 uri = %s 时响应体为空, 可能是 uri 写错或者远程服务器不可用", uri );
                    /*
                    对于日志的理解
                    手动 if 检验：抛异常 + 带上信息就行、交给全局异常处理器输出日志
                    catch 异常：log 当前操作及参数（不带异常）、异常上抛
                    log.error( message );
                     */
                    throw new IllegalStateException( message );
                }

                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if ( statusCode < 200 || statusCode >= 300 ){
                    String message = String.format( "请求方法 GET, 访问 uri = %s 时响应失败, 状态码 = %d", uri, statusCode );
                    throw new IllegalStateException( message );
                }

                /*
                    【注意】
                    嵌套 try-catch 可能会吞掉内部异常
                    一定要看看去掉最外层 catch 看看内部代码会不会有编译时异常
                    否则会内外层异常混到一起、区分不开
                 */
                HttpEntity responseEntity = httpResponse.getEntity();
                try {
                    responseString = EntityUtils.toString( responseEntity, StandardCharsets.UTF_8 );
                } catch ( IOException e ){
                    log.error( "请求方法 GET, uri = {} , 响应体转换字符串失败, 响应体 = {}", uri, responseEntity );
                    throw new UncheckedIOException( e );
                }
            } catch ( IOException e ){
                log.error( "执行 GET 请求失败, uri = {}", uri );
                throw new UncheckedIOException( e );
            }
        } catch ( IOException e ){
            log.error( "创建默认 HttpClient 失败" );
            throw new UncheckedIOException( e );
        }

        // 高频调用 / 成功率大 / 技术细节省略日志 最多 debug
        log.debug( "GET 请求执行成功, uri = {}, 响应结果字符串 = {}", uri.toString(), responseString );
        return responseString;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(),param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }

}
