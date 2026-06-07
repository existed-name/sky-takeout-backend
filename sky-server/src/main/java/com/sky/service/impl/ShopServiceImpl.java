package com.sky.service.impl;

import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.service.ShopService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Service
public class ShopServiceImpl implements ShopService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final StringRedisTemplate stringRedisTemplate;



    // ==================== 构造器 ====================
    public ShopServiceImpl( StringRedisTemplate stringRedisTemplate ){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * <b>启动初始化</b>: 应用启动时自动执行，且只执行一次
     * <br>
     * setIfAbsent = "如果不存在才设置"，不会覆盖已有值
     */
    @PostConstruct
    public void init(){
        // String -> String
        stringRedisTemplate.opsForValue().setIfAbsent(
                RedisConstant.Key.SHOP_STATUS,
                StatusConstant.ShopStatus.DEFAULT.toString()  // 默认营业中 1
        );
    }


    // ==================== 公有方法 ====================
    // Redis 还可以用 @Transactional ??
    // @Transactional( rollbackFor = Exception.class )
    // Redis 不需要这些注解，而且因为非关系型数据库，也不需要持久层 @Mapper/Repository
    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateStatus( Integer status ){
        StatusConstant.validateStatus( status );

        // 这里就通过 Redis 操作营业状态字段
        // RedisKey = ShopStatus -> filed = 1 / 0
        ValueOperations< String, String > valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set( RedisConstant.Key.SHOP_STATUS, status.toString() );
        // 由于只调用 1 次，其实不需要创建对象、直接调方法就行
    }

    @Transactional( readOnly = true )
    @Override
    public Integer getStatus(){
        Object status = stringRedisTemplate.opsForValue().get( RedisConstant.Key.SHOP_STATUS );
        // 注意可能并没有设置默认值，导致查到空值
        return ( status == null ) ?
                StatusConstant.ShopStatus.DEFAULT :
                Integer.valueOf( status.toString() );
    }


    // ==================== 私有辅助方法 ====================

}
