package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
@Configuration
public class RedisConfiguration {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * String 类型 key -> JSON 数据，比如储存对象值
     */
    @Bean
    public RedisTemplate< String, Object > // 加泛型类型安全
        redisTemplate( RedisConnectionFactory redisConnectionFactory ){

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

        // value
        // GenericJackson2JsonRedisSerializer 默认的 ObjectMapper 没有注册 Java 8 日期时间模块
        // 自定义 ObjectMapper，注册 Java 8 日期时间模块，解决 LocalDateTime 序列化问题
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule( new JavaTimeModule() );
        // 禁用日期时间戳格式，使用 ISO-8601 字符串格式（更易读）
        objectMapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer( objectMapper );
        // ↓ 反序列化时会丢失具体类型（变成 LinkedHashMap）
//        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
         redisTemplate.setValueSerializer(serializer);
         redisTemplate.setHashValueSerializer(serializer);

        // 初始化校验和设置默认值,调用它让 RedisTemplate 真正就绪
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    /**
     * 全 String 类型 Key -> Value
     * <br>
     * 这里是学习造轮子, <b>干活时要用 SpringBoot 自己的</b>
     * <br>
     * 等价于直接<code>return new StringRedisTemplate( facotry )</code>
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate( RedisConnectionFactory factory ){
        log.info( "创建自定义 StringRedisTemplate" );
        // 连接工厂对象
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate( factory );
        // 设置 Key 的序列化器
        stringRedisTemplate.setKeySerializer( new StringRedisSerializer() );
        stringRedisTemplate.setHashKeySerializer( new StringRedisSerializer() );
        // Value
        stringRedisTemplate.setValueSerializer( new StringRedisSerializer() );
        stringRedisTemplate.setHashValueSerializer( new StringRedisSerializer() );
        // 检验状态
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }



    // ==================== 私有辅助方法 ====================

}
