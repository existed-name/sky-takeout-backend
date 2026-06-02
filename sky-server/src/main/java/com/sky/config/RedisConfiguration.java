package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfiguration {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    @Bean
    @Primary // 强制使用自定义 Bean(这玩意儿硬要用它自己的，导致我设置序列化器，存储的 key-value 还是乱码)
//    @ConditionalOnMissingBean // 条件创建: 容器已有对象，就不重复创建；否则创建对象
    // 去掉 @ConditionalOnMissingBean, 和 @Primary 一起用反而容易混淆，直接去掉
    public RedisTemplate< String, Object > // 加泛型类型安全
        getRedisTemplate( RedisConnectionFactory redisConnectionFactory ){

        log.info( "正在创建自定义 RedisTemplate" );
        RedisTemplate< String, Object > redisTemplate = new RedisTemplate<>();
        // Redis 的连接工厂对象
        redisTemplate.setConnectionFactory( redisConnectionFactory );
        /*
        Spring Boot 默认用 JdkSerializationRedisSerializer，
        存的是 Java 二进制序列化数据，RedisInsight
        直接显示就会有乱码前缀
         */
        // Redis Key 的序列化器
        redisTemplate.setKeySerializer( new StringRedisSerializer() );
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // value - 二选一
        // 方案1: 纯 String
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        // 方案2: JSON（存对象用这个）
        // Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // redisTemplate.setValueSerializer(serializer);
        // redisTemplate.setHashValueSerializer(serializer);

        // 初始化校验和设置默认值,调用它让 RedisTemplate 真正就绪
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }


    // ==================== 私有辅助方法 ====================

}
