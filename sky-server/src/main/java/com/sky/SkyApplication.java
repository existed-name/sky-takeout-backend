package com.sky;

import com.sky.properties.BaiduMapProperties;
import com.sky.properties.ShopInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling // 开启 Spring Task 定时任务调度
@EnableCaching // Spring Cache 注解
@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
// 尽量显式扫描配置属性类、而不是给配置属性类加 @Component
@EnableConfigurationProperties( {
        // 交给 IoC 注册为 Spring Bean
        BaiduMapProperties.class,
        ShopInfoProperties.class
} )
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
