package com.sky.service.impl;

import com.sky.constant.RedisConstant;
import com.sky.enumeration.ShopStatus;
import com.sky.service.ShopService;
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
     * 启动应用时初始化店铺状态缓存
     * <p>
     *     <b>只在店铺状态缓存不存在时才设置</b>( 不会覆盖已有值 )
     * </p>
     */
    @PostConstruct
    public void initIfAbsent(){
        stringRedisTemplate.opsForValue().setIfAbsent(
                RedisConstant.Shop.SHOP_STATUS,
                Integer.toString( ShopStatus.DEFAULT.getStatus() )  // 默认营业中 1
        );
    }


    // ==================== 公有方法 ====================

    /*
    关系型数据库: 数据模型非常整齐统一, 通常是表格, 能够看出各个字段的对应关系, 比如 MySQL
    它可以直接加事务注解
    非关系型数据库: 数据模型很灵活, 可以存储多种数据结构, 比如 redis、neo4j( 图节点、关系边 )
    没有配置对应的事务管理器就用不了事务注解, 还可能因为事务注解降低性能
    @Transactional( rollbackFor = Exception.class )
     */
    @Override
    public void updateStatus( Integer newStatus ){
        /*
            店铺状态只有 1 个字段, 存到数据库性价比太低且不灵活, 于是直接使用缓存存储
            最开始我设置了缓存过期时间,
            现在想, 如果过期的话, 要么提醒管理员再次设置店铺状态,
            要么在管理端、用户端有人看店铺状态时判断缓存还在不在、不在的话重新设置默认值
            这样就比较麻烦
            而且店铺状态其实是全局共享、需要持久保留展示的字段,
            所以考虑不设置过期时间, 跟真正的数据库字段一样永久保留
         */
        ShopStatus.throwIfInvalid( newStatus );

        ValueOperations< String, String > valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(
                RedisConstant.Shop.SHOP_STATUS,
                newStatus.toString()
//                , RedisConstant.Shop.CACHE_TTL
        );
    }

    @Transactional( readOnly = true )
    @Override
    public Integer getStatus(){
        /*
            从缓存中查店铺状态

            有个选择问题
            没查到( 过期、没初始化 )再设置默认值, 还是先 setIfAbsent 再查?

            初步想法
            缓存的目的是加快查询速度, 通常先查询、有的话直接用, 没有才来设置缓存,
            如果先设置了初始缓存, 可能多了一层性能开销, 但只是单个字段、可能性能都差不多
            
            问 ai: 这种场景, 默认值就是有效值, 先设置、后查询, 避免多个请求同时查询为空、再同时设置
         */
        initIfAbsent();
        Object status = stringRedisTemplate.opsForValue().get( RedisConstant.Shop.SHOP_STATUS );
        return Integer.valueOf( status.toString() );
    }


    // ==================== 私有辅助方法 ====================

}
