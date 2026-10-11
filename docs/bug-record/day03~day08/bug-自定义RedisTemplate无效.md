## 描述

* [Redis 配置类](/com/sky/config/RedisConfiguration.java) 配置 `RedisTemplate` Bean

    ```java
        @Bean
        public RedisTemplate< String, Object > // 加泛型类型安全
        getRedisTemplate( RedisConnectionFactory redisConnectionFactory ){
            log.info( "正在创建自定义 RedisTemplate" );
            RedisTemplate< String, Object > redisTemplate = new RedisTemplate<>();
            // Redis 的连接工厂对象
            redisTemplate.setConnectionFactory( redisConnectionFactory );
            // Redis Key 的序列化器
            redisTemplate.setKeySerializer( new StringRedisSerializer() );
            return redisTemplate;
        }
    ```

* 然而 [Test](/com/sky/test/RedisTest.java) 时发现程序内部字符显示正常，而 Redis Insight 的 Key-Value 均出现乱码
* 并且日志也有 `正在创建自定义 RedisTemplate`, 说明创建了自定义 `RedisTemplate`


## 排查过程

1. 补全 Key、Value 的序列化器设置
   * 测试发现 Redis Insight 依然乱码
2. 再补上 `RedisTemplate` 的初始化检验 `afterProperties`
    * 现在代码如下
   
    ```java
        @Bean
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
    
            // value
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            redisTemplate.setHashValueSerializer(new StringRedisSerializer());
    
            // 初始化校验和设置默认值,调用它让 RedisTemplate 真正就绪
            redisTemplate.afterPropertiesSet();
    
            return redisTemplate;
        }
    ```
   
    * 然而 Redis Insight 里面的 Key-Value 还是有乱码

3. 测试时保证
    * 每次的 key, value 均和上次不同，避免上次值造成干扰
    * 代码末尾调用 `redisTemplate.delete` 删除数据
    * 或者 Redis Insight 可视化删除数据

4. 又在方法上面补充 `@ConditionalOnMissingBean`
    * 条件创建: 容器已有对象，就不重复创建；否则创建对象
    * 让 Spring 用我的对象，而不是它自己的 `RedisTemplate`
    * 不过测试， Redis Insight 里面的 Key-Value 还是有乱码


## 转折点

1. AI 提出手动查看调用的 `redisTemplate` 对象的序列化器

   ```java
   /**
    * 如果成功使用自定义的序列化器，应该打印<code>StringRedisSerializer</code>
    * <br>
    * 否则就用的 SpringBoot 自己的<code>JdkXxx</code>
    */
   public void checkSerializer(){
      log.info( "Key Serializer: {}", redisTemplate.getKeySerializer().getClass() );
      log.info( "Value Serializer: {}", redisTemplate.getValueSerializer().getClass() );
   }
   ```

   * 发现确实还是 `JdkSerializationRedisSerializer`, 不是自定义的 `StringXxx`

2. AI 分析是 SpringBoot 的 `RedisTemplate` 优先级更高，于是使用 `@Primary` 注解强制用自定义对象
   * 确实成功了，是 `StringRedisSerializer` + Redis Insight 无乱码


## 后续分析

1. 框架代码
   * Spring Boot 自动配置了一个 `RedisTemplate< Object, Object > ( JdkSerializationRedisSerializer )`
   * Spring Boot 的 `RedisAutoConfiguration` 里有 `@ConditionalOnMissingBean( name = "redisTemplate" )`
   * 如果没有名叫 `redisTemplate` 的 Bean，他就会创建自己的 Bean
   * 我的方法名是 `getRedisTemplate`，不是 `redisTemplate`
   * 而 `@Bean` 的 `value/name`, 也就是 Bean 的名称默认 = 方法名
   * 于是他认为没有创建 `redisTemplate`, 就用了自己的 Bean

2. 解决方法
   * 之前的 `@Primary`
   * OR 把方法名改为 `redisTemplate`
   * OR 显式指定名称 `@Bean( name = "redisTemplate" )`

3. 最终: 推荐改方法名
   * 只有方法名不适合作为 Bean 名（如动词开头 getXxx、createXxx）时，才显式指定 name
   * `@Primary` 治标不治本，创建了 2 个 Bean，只是强制优先用我的
