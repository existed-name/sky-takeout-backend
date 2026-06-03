package com.sky.service.impl;

import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.service.ShopService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final RedisTemplate< String, Object > redisTemplate;



    // ==================== 构造器 ====================
    public ShopServiceImpl( RedisTemplate< String, Object > redisTemplate ){
        this.redisTemplate = redisTemplate;
    }


    // ==================== 公有方法 ====================
    // Redis 还可以用 @Transactional ??
    // @Transactional( rollbackFor = Exception.class )
    // Redis 不需要这些注解，而且因为非关系型数据库，也不需要持久层 @Mapper/Repository
    @Override
    public void updateStatus( Integer status ){
        StatusConstant.validateStatus( status );

        // 这里就通过 Redis 操作营业状态字段
        // RedisKey = ShopStatus -> filed = 1 / 0
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();
        valueOperations.set( RedisConstant.Key.SHOP_STATUS, status );
        // 由于只调用 1 次，其实不需要创建对象、直接调方法就行
    }

    @Override
    public Integer getStatus(){
        return ( Integer ) redisTemplate.opsForValue().get( RedisConstant.Key.SHOP_STATUS );
    }


    // ==================== 私有辅助方法 ====================

}
