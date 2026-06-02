package com.sky.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class RedisTest {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /**
     * 注意是导入自己的 RedisTemplate
     */
    @Autowired // 生产代码用构造器注入，测试代码随意
    private RedisTemplate< String, Object > redisTemplate;


    // ==================== 构造器 ====================



    // ==================== 公有方法 ====================
    @Test
    public void testRedisTemplate(){
        log.info( "log.debug - 这里是 Redis 模板对象: {}", redisTemplate );
        System.out.println( "System.out.println - 这里是 Redis 模板对象: " + redisTemplate );
        // 操作 string 数据
        ValueOperations< String, Object > valueOperations = redisTemplate.opsForValue();

        HashOperations< String, Object, Object > hashOperations = redisTemplate.opsForHash();

        ListOperations< String, Object > listOperations = redisTemplate.opsForList();

        SetOperations< String, Object > setOperations = redisTemplate.opsForSet();
        // 有序集合
        ZSetOperations< String, Object > zSetOperations = redisTemplate.opsForZSet();
    }

    /**
     * 操作字符串数据类型
     */
    @Test
    public void testString() throws InterruptedException{
        checkSerializer();

        String redisKey = "KEY";
        String redisValue = "VALUE";
        // 设置键值对
        redisTemplate.opsForValue().set( redisKey, redisValue );
        log.info( "设置 key = {}, value = {}", redisKey, redisValue );

        // 通过键拿到值
        String value = ( String ) redisTemplate.opsForValue().get( redisKey );
        log.info( "通过 key = {} 拿到 value = {}", redisKey, value );

        // 设置有效时间
        String key2 = "pass_word";
        String value2 = "123";
        int validPeriod = 5;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        redisTemplate.opsForValue().set( key2, value2, validPeriod, timeUnit );
        log.info( "设置 key = {}, value = {}, 有效时间 {} {}",
                key2, value2, validPeriod, timeUnit );

        // 超过有效时间再来看
        Thread.sleep( validPeriod * 1000 );
        value2  = ( String ) redisTemplate.opsForValue().get( key2 );
        log.info( "过了有效时间 {} {} 后，通过 key = {} 获取到 value = {}",
                validPeriod, timeUnit, key2, value2 );

        // Set key to hold the string value if key is absent.
        // SetNX = Set If Not Exist
        Boolean flag = redisTemplate.opsForValue().setIfAbsent( redisKey, value2 = "123456" );
        log.info( "尝试设置 key = {} 的 value 为 {}", redisKey, value2 );
        log.info( "标记 flag = {}", flag );
        String tempValue = ( String ) redisTemplate.opsForValue().get( redisKey );
        log.info( "通过 key = {} 拿到 value = {}", redisKey, tempValue );

        String key3 = "张雪峰老师";
        String value3 = "我还记得你";
        flag = redisTemplate.opsForValue().setIfAbsent( key3, value3 );
        log.info( "尝试设置 key = {} 的 value 为 {}", key3, value3 );
        log.info( "标记 flag = {}", flag );

        // 删除 key - value
//        Boolean deleteFlag = redisTemplate.delete( redisKey );
//        log.info( "删除 key = {}, 结果 = {}", redisKey, deleteFlag );
//        deleteFlag = redisTemplate.delete( key3 );
//        log.info( "删除 key = {}, 结果 = {}", key3, deleteFlag );
    }

    /**
     * 测试哈希(Map)数据类型
     */
    @Test
    public void testHash(){
        HashOperations< String, String, String > hashOperations = redisTemplate.opsForHash();

        String redisKey = "RedisKey";
        String hashKey = "HashKey";
        String hashValue = "HashValue";
        hashOperations.put( redisKey, hashKey, hashValue );
        log.info( "设置 redis key = {} -> 键值对( {}, {} )", redisKey, hashKey, hashValue );

        String hashKey2 = "HASH_KEY";
        String hashValue2 = "HASH_VALUE";
        hashOperations.put( redisKey, hashKey2, hashValue2  );
        log.info( "再设置 redis key = {} -> 键值对( {}, {} )", redisKey, hashKey2, hashValue2 );

        String getValue = hashOperations.get( redisKey, hashKey );
        log.info( "通过 redis key = {}, hash key = {}, 拿到 hash value = {}", redisKey, hashKey, getValue );
        getValue = hashOperations.get( redisKey, hashKey2 );
        log.info( "通过 redis key = {}, hash key = {}, 拿到 hash value = {}", redisKey, hashKey2, getValue );

        Set< String > keys = hashOperations.keys( redisKey );
        log.info( "通过 redis key = {},  拿到对应的所有字段  {}", redisKey, keys );

        List< String > values = hashOperations.values( redisKey );
        log.info( "通过 redis key = {},  拿到对应的所有值  {}", redisKey, values );

        Long countDelete = hashOperations.delete( redisKey, hashKey, hashKey2 );
        log.info( "删除数据组数: {}", countDelete );

        keys = hashOperations.keys( redisKey );
        log.info( "通过 redis key = {},  拿到对应的所有字段  {}", redisKey, keys );

        values = hashOperations.values( redisKey );
        log.info( "通过 redis key = {},  拿到对应的所有值  {}", redisKey, values );
    }

    /**
     * 测试列表数据
     */
    @Test
    public void testList(){
        ListOperations< String, Object > listOperations =  redisTemplate.opsForList();

        String redisKey = "RedisKey";
        Long size = listOperations.size( redisKey );
        log.info( "初始大小 {}", size );

        String value1 = "v1";
        Long l = listOperations.leftPush( redisKey, value1 );
        log.info( "设置键 {} -> 值 {}", redisKey, value1 );

        List< String > strings = new ArrayList<>( List.of( "a", "b", "c", "d" ) );
        Long count = listOperations.leftPushAll( redisKey,
                strings.toArray() // V.. values 可变数组，不能用链表
        );
        size = listOperations.size( redisKey );
        log.info( "redis key = {}, left push values = {}", redisKey, strings );
        log.info( "count = {}, size = {}", count, size );

        int beginIndx = 0;
        int endIndx = 2;
        List< Object > range = listOperations.range( redisKey, beginIndx, endIndx );
        log.info( "redis key = {} -> range index[ {}, {} ] = {}",
                redisKey, beginIndx,  endIndx, range );

        String rightPop = ( String ) listOperations.rightPop( redisKey );
        size = listOperations.size( redisKey );
        log.info( "redis key = {} -> left pop = {}, size = {}",
                redisKey, rightPop, size );

//        while ( ( size = listOperations.size( redisKey ) ) != null && size  > 0 ){
//            listOperations.rightPop( redisKey );
//        }
        Boolean delete = redisTemplate.delete( redisKey );
        size = listOperations.size( redisKey );
        log.info( "删除 redis key = {}, 删除标记 = {}, 对应列表大小 = {}", redisKey, delete, size );
    }

    /**
     * 操作集合
     */
    @Test
    public void testSet(){
        SetOperations< String, Object > setOperations = redisTemplate.opsForSet();

        String redisKey = "Redis-Key";
        Long size = setOperations.size( redisKey );
        log.info( "初始大小 {}", size );

        String addValue = "a";
        Long count = setOperations.add( redisKey, addValue );
        log.info( "key = {}, add value = {}", redisKey, addValue );
        log.info( "count = {}", count );

        String[] addValues = { "b", "c", "d", "e" };
         count = setOperations.add( redisKey, addValues );
         size = setOperations.size( redisKey );
        log.info( "count = {}, size = {}", count, size );

        Set< Object > members = setOperations.members( redisKey );
        log.info( "members = {}", members );

        String redisKey2 = "Redis-Key-2";
        String[] addValues2 = {
            "a", "b", "x", "y", "z"
        };
        setOperations.add( redisKey2, addValues2 );
        log.info( "另一个 redis key = {}, add values = {}", redisKey2, addValues2 );

        Set< Object > intersect = setOperations.intersect( redisKey, redisKey2 );
        log.info( "{} 和 {} 的交集 = {}", redisKey, redisKey2, intersect );

        Set< Object > union = setOperations.union( redisKey, redisKey2 );
        log.info( "{} 和 {} 的并集 = {}", redisKey, redisKey2, union );

        Boolean flag = deleteValues( redisKey );
        flag = deleteValues( redisKey2 );

//        setOperations.remove( redisKey, "a", "b" )
    }

    /**
     * 操作有序集合
     */
    @Test
    public void testZset(){
        ZSetOperations< String, Object > zSetOperations = redisTemplate.opsForZSet();

        String redisKey = "Redis-KEY";
        Long size = zSetOperations.size( redisKey );
        log.info( "初始大小 {}", size );

        Map< String, Double > stringToDoubleMap = Map.ofEntries(
                new AbstractMap.SimpleEntry<>( "a", 4.0 ),
                new AbstractMap.SimpleEntry<>( "b", 3.0 ),
                Map.entry( "c", 2.0 ),
                Map.entry( "d", 1.0 )
        );
        Set< Map.Entry< String, Double > > stringDoubleEntrieySet = stringToDoubleMap.entrySet();

//        ZSetOperations.TypedTuple< String > typedTuple = ZSetOperations.TypedTuple.of( "a", 10.0 );

        Set< ZSetOperations.TypedTuple< Object > > typedTupleSet = new HashSet<>();
        for ( Map.Entry< String, Double > entry : stringDoubleEntrieySet ){
            typedTupleSet.add(
                    ZSetOperations.TypedTuple.of(
                            entry.getKey(), entry.getValue()
                    )
            );
        }
        Long countAdd = zSetOperations.add( redisKey, typedTupleSet );
        log.info( "redis key = {}, 源数据 = {}", redisKey, stringToDoubleMap );
        log.info( "添加数据 = {}, countAdd = {}", typedTupleSet, countAdd );

        long start = 1L;
        long end = 3L;
        Set< Object > range = zSetOperations.range( redisKey, start, end );
        log.info( "获取在数值 [ {}, {} ] 之间的元素 = {}", start, end, range );

        String hashKey = "a";
        double delta = -2.0;
        Double v = zSetOperations.incrementScore( redisKey, hashKey, delta );
        Double score = zSetOperations.score( redisKey, hashKey );
        log.info( "key = {} 的分数变化 = {}, 现在分数 = {}", hashKey, delta, score );

//        Long countRemove = zSetOperations.remove( redisKey, hashKey );
        deleteValues( redisKey );
    }

    /**
     * 通用命令
     */
    @Test
    public void testCommon(){
        Set< String > allRedisKeys = redisTemplate.keys( "*" );
        log.info( "所有的 key = {}", allRedisKeys );

        log.info( "遍历所有 key 的 dataType" );
        for ( String redisKey : allRedisKeys ){
            DataType dataType = redisTemplate.type( redisKey );
            log.info( "dataType name = {}, toString = {}", dataType.name(), dataType.toString() );
        }

        String hashKey = "name";
        Boolean hasKey = redisTemplate.hasKey( hashKey );
        log.info( "Redis 是否含有 hash key = {} [ {} ]", hashKey, hasKey );

        // 以及删除操作
    }



    // ==================== 私有辅助方法 ====================

    /**
     * 如果成功使用自定义的序列化器，应该打印<code>StringRedisSerializer</code>
     * <br>
     * 否则就用的 SpringBoot 自己的<code>JdkXxx</code>
     */
    public void checkSerializer() {
        System.out.println("Key Serializer: " + redisTemplate.getKeySerializer().getClass());
        System.out.println("Value Serializer: " + redisTemplate.getValueSerializer().getClass());
    }

    /**
     * 删除 key 对应的值, 返回删除标记
     */
    private Boolean deleteValues( String redisKey ){
        Boolean flag = redisTemplate.delete( redisKey );
        log.info( "删除 key = {} 的值, 删除标记 = {}", redisKey, flag );
        return flag;
    }
}
