package com.sky.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Spring Task 测试, 定时任务类
 *
 * @CreateTime 2026/9/12 13:18
 */
@Slf4j
@Component
public class MyTask {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 不限起点, 每隔 5 秒进行一次定时任务
     * <p>
     *     注意{@code /}前后都有内容, 前面是{@code *}就从任意起点开始
     * </p>
     */
//    @Scheduled( cron = "*/5 * * * * *" )
    public void executeTask(){
        LocalDateTime now = LocalDateTime.now( );
        log.info( "执行定时任务, 当前时间 = {}", now );
    }


    // ==================== 私有辅助方法 ====================

}
