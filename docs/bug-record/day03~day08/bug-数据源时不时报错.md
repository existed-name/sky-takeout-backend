1. 描述
    * 时不时发现有 ERROR 日志

    ```text
    2026-05-28 21:48:29.622 ERROR 28332 --- [nio-8080-exec-7] c.a.druid.pool.DruidAbstractDataSource   : discard long time none received connection. , jdbcUrl : jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, jdbcUrl : jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, lastPacketReceivedIdleMillis : 176561
    ```
   
2. 分析
    * AI 说是 ` Druid 连接池检测到连接长时间未收到数据包，主动丢弃了该连接`

3. 解决方法
    * 修改 `application.yml` 的  `spring.datasource.druid` 增加连接池配置
    * 不过因为不影响使用，所以先不管



