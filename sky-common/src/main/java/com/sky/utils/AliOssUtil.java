package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.lang.IllegalArgumentException;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes 文件字节数组
     * @param objectName OSS中的完整路径（例如：avatar/2026/04/xxx.jpg）
     * @return 文件访问URL
     */
    /*
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        String url = stringBuilder.toString();
        log.info("文件上传到: {}", url );
        return url;
    }

     */

    // ==================== 优化后的上传方法 ====================

    /**
     * 优化版文件上传（异常上抛 + 配置校验 + URL 返回）。
     * <p>
     * <b>优化点：</b>
     * <ul>
     *   <li><b>P0 异常处理：</b>OSS异常直接上抛为 {@link IllegalArgumentException}，由全局异常处理器统一处理，避免返回无效URL</li>
     *   <li><b>P1 配置校验：</b>上传前校验核心配置非空，提前暴露配置错误</li>
     *   <li><b>P2 协议兼容：</b>自动适配带/不带协议头的 endpoint 配置</li>
     * </ul>
     *
     * @param bytes      文件字节数组，不允许为空或空数组
     * @param objectName OSS对象完整路径，不允许为空（如：avatar/2026/05/23/a1b2c3d4.jpg）
     * @return 文件公网访问URL，格式：https://BucketName.Endpoint/ObjectName
     * @throws IllegalArgumentException 当配置非法、OSS服务异常或网络异常时抛出
     */
    public String upload(byte[] bytes, String objectName) throws IllegalArgumentException {
        // ---------- P1: 配置校验（防御式编程，提前失败） ----------
        validateConfig();

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("上传文件内容不能为空");
        }
        if ( objectName == null || objectName.isBlank() ) {
            throw new IllegalArgumentException("OSS对象路径(objectName)不能为空");
        }

        // ---------- 执行上传 ----------
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObject请求，将字节流上传至指定Bucket的指定路径
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
            log.info("OSS文件上传成功，objectName={}", objectName);
        } catch (OSSException oe) {
            // OSS服务端异常：请求已到达OSS，但被拒绝（如权限不足、Bucket不存在、文件过大等）
            log.error("OSS服务端异常，ErrorCode={}，RequestId={}，ErrorMessage={}",
                    oe.getErrorCode(), oe.getRequestId(), oe.getErrorMessage());
            throw new IllegalArgumentException(
                    String.format("文件上传失败(OSS): %s", oe.getErrorMessage()));
        } catch (ClientException ce) {
            // 客户端异常：网络不通、连接超时、本地配置错误等
            log.error("OSS客户端异常，ErrorMessage={}", ce.getMessage());
            throw new IllegalArgumentException(
                    String.format("文件上传失败(网络/客户端): %s", ce.getMessage()));
        } finally {
            // 确保OSSClient资源释放，避免连接泄漏
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // ---------- 拼接公网访问URL ----------
        // 格式：https://BucketName.Endpoint/ObjectName
        // 兼容 endpoint 已带协议头的情况
        String normalizedEndpoint = endpoint;
        if (normalizedEndpoint.startsWith("https://")) {
            normalizedEndpoint = normalizedEndpoint.substring(8);
        } else if (normalizedEndpoint.startsWith("http://")) {
            normalizedEndpoint = normalizedEndpoint.substring(7);
        }

        String url = new StringBuilder("https://")
                .append(bucketName).append(".")
                .append(normalizedEndpoint).append("/")
                .append(objectName)
                .toString();

        log.info("文件上传完成，访问URL={}", url);
        return url;
    }



    /**
     * 校验OSS核心配置是否完整。
     * <p>
     * 在构造器或上传前调用，提前暴露配置错误，避免运行时才发现。
     *
     * @throws IllegalArgumentException 当 endpoint、accessKeyId、accessKeySecret、bucketName 任一为空时抛出
     */
    private void validateConfig() {
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalArgumentException("OSS配置错误：endpoint 不能为空");
        }
        if (!StringUtils.hasText(accessKeyId)) {
            throw new IllegalArgumentException("OSS配置错误：accessKeyId 不能为空");
        }
        if (!StringUtils.hasText(accessKeySecret)) {
            throw new IllegalArgumentException("OSS配置错误：accessKeySecret 不能为空");
        }
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalArgumentException("OSS配置错误：bucketName 不能为空");
        }
    }
}
