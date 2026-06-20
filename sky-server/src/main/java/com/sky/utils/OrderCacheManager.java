package com.sky.utils;

import com.sky.constant.MessageConstant;
import com.sky.constant.OrderConstant;
import com.sky.constant.RedisConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 订单相关缓存业务工具类，主要针对订单号的序列号管理缓存
 *
 * @CreateTime 2026/6/20
 */
@Component
public class OrderCacheManager {
    private final RedisTemplate< Object, Object > redisTemplate;

    public OrderCacheManager( RedisTemplate< Object, Object > redisTemplate ){
        this.redisTemplate = redisTemplate;
    }
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    /**
     * 获取<b>指定日期的订单数量</b>
     * <br>
     * <b>如果未初始化，则自动设置为 0 并返回</b>
     *
     * @param formatDate 格式化日期字符串，见{@link com.sky.constant.OrderConstant.OrderNumber#DATE_PATTERN}
     * @return 指定日期的订单数量<br>
     *          * <b>过期返回 null</b><br>
     *          * 0: 今天暂无用户下单 | 已过期、重置为 0
     */
    public long getOrderCount( String formatDate ){
        validateFormatDate( formatDate );
        String redisKey = RedisConstant.OrderNumber.formatRedisKey( formatDate );
        Object object = redisTemplate.opsForValue().get( redisKey );
        if ( object == null ){
            setOrderCount( formatDate, OrderConstant.OrderNumber.ORDER_COUNT_ZERO );
        }
        return OrderConstant.OrderNumber.ORDER_COUNT_ZERO;
    }

    /**
     * <b>指定日期的订单数量</b>自增 1, 用于创建订单时获取订单数量
     * <br>
     * 未初始化，则自动设为 0，然后自增为 1
     *
     * @param formatDate 格式化日期字符串，见{@link com.sky.constant.OrderConstant.OrderNumber#DATE_PATTERN}
     * @return 用户下单后的订单总数量
     */
    public long nextSequence( String formatDate ){
        validateFormatDate( formatDate );
        String redisKey = RedisConstant.OrderNumber.formatRedisKey( formatDate );
        Long afterIncrement = redisTemplate.opsForValue().increment( redisKey );
        if ( afterIncrement == null ){
            throw new IllegalStateException( "Redis 自增失败, RedisKey = " + redisKey );
        }
        // 初始化，null -> 0 -> 自增成 1，需要设置过期时间，其他时候就不需要设置，避免刷新过期时间(就一直在那里)
        if ( afterIncrement == 1L ){
            redisTemplate.expire( redisKey, RedisConstant.OrderNumber.CACHE_TTL );
        }
        return afterIncrement;
    }

    /**
     * 设置<b>指定日期的订单数量缓存</b>, 有效时间: {@link com.sky.constant.RedisConstant.OrderNumber#CACHE_TTL}
     *
     * @param formatDate 格式化日期字符串，见{@link com.sky.constant.OrderConstant.OrderNumber#DATE_PATTERN}
     * @param orderCount 订单数量，不能为空、负数
     */
    public void setOrderCount( String formatDate, Long orderCount ){
        validateFormatDate( formatDate );
        validateOrderCount( orderCount );
        String redisKey = RedisConstant.OrderNumber.formatRedisKey( formatDate );
        redisTemplate.opsForValue().set( redisKey, orderCount );
        // 首次初始化为 0 时设置过期时间，其他时候不设置(会重置过期时间)
        if ( OrderConstant.OrderNumber.ORDER_COUNT_ZERO.equals( orderCount ) ){
            redisTemplate.expire( redisKey, RedisConstant.OrderNumber.CACHE_TTL );
        }
    }



    // ==================== 私有辅助方法 ====================
    /**
     * 检验日期字符串
     *
     * @param formatDate 格式化日期字符串，见{@link com.sky.constant.OrderConstant.OrderNumber#DATE_PATTERN}
     */
    private void validateFormatDate( String formatDate ){
        if ( formatDate == null || formatDate.isBlank() ){
            throw new IllegalArgumentException( MessageConstant.FORMAT_DATE_IS_BLANK );
        }

        try {
            DateTimeFormatter
                    .ofPattern( OrderConstant.OrderNumber.DATE_PATTERN )
                    .parse( formatDate );
        } catch ( Exception e ){
            throw new IllegalArgumentException( MessageConstant.DATE_FORMAT_PARSE_ERROR );
        }
    }

    /**
     * 检验订单数量
     */
    private void validateOrderCount( Long orderCount ){
        if ( orderCount == null ){
            throw new IllegalArgumentException( MessageConstant.ORDER_COUNT_IS_NULL );
        }
        if ( orderCount < 0L ){
            throw new IllegalArgumentException( MessageConstant.ORDER_COUNT_IS_NEGATIVE );
        }
    }

}
