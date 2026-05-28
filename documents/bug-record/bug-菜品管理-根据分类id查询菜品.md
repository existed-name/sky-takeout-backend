1. 请求路径 `/admin/dish/list?categoryId=xxx`

2. 异常描述
    * `categoryId` = null,
        - 查询所有菜品，`categoryName` 正常显示
        - 日志
      
            ```text
            2026-05-28 13:18:49.159  INFO 22232 --- [nio-8080-exec-8] com.sky.controller.admin.DishController  : 查询分类 id = null 的菜品
            2026-05-28 13:18:49.160 DEBUG 22232 --- [nio-8080-exec-8] c.s.m.D.selectDishVOByCategoryId         : ==>  Preparing: SELECT d.id, d.name, d.category_id, d.price, d.image, d.description, d.status, d.update_time , c.name AS categoryName FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id
            2026-05-28 13:18:49.160 DEBUG 22232 --- [nio-8080-exec-8] c.s.m.D.selectDishVOByCategoryId         : ==> Parameters:
            2026-05-28 13:18:49.163 DEBUG 22232 --- [nio-8080-exec-8] c.s.m.D.selectDishVOByCategoryId         : <==      Total: 27
            ```
    * `categoryId` = 无效数字，查询内容为空
        - 日志
      
        ```text
        2026-05-28 13:20:50.039  INFO 22232 --- [nio-8080-exec-1] com.sky.controller.admin.DishController  : 查询分类 id = 2 的菜品
        2026-05-28 13:20:50.040 ERROR 22232 --- [nio-8080-exec-1] c.a.druid.pool.DruidAbstractDataSource   : discard long time none received connection. , jdbcUrl : jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, jdbcUrl : jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, lastPacketReceivedIdleMillis : 120879
        2026-05-28 13:20:50.044 DEBUG 22232 --- [nio-8080-exec-1] c.s.m.D.selectDishVOByCategoryId         : ==>  Preparing: SELECT d.id, d.name, d.category_id, d.price, d.image, d.description, d.status, d.update_time , c.name AS categoryName FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE d.category_id = ?
        2026-05-28 13:20:50.044 DEBUG 22232 --- [nio-8080-exec-1] c.s.m.D.selectDishVOByCategoryId         : ==> Parameters: 2(Long)
        2026-05-28 13:20:50.046 DEBUG 22232 --- [nio-8080-exec-1] c.s.m.D.selectDishVOByCategoryId         : <==      Total: 0
        ```

    * `categoryId` = 有效数字，查询内容中 `categoryName` 为空
        - 日志
      
        ```text
        2026-05-28 13:20:53.196  INFO 22232 --- [nio-8080-exec-2] com.sky.controller.admin.DishController  : 查询分类 id = 1 的菜品
        2026-05-28 13:20:53.197 DEBUG 22232 --- [nio-8080-exec-2] c.s.m.D.selectDishVOByCategoryId         : ==>  Preparing: SELECT d.id, d.name, d.category_id, d.price, d.image, d.description, d.status, d.update_time , c.name AS categoryName FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE d.category_id = ?
        2026-05-28 13:20:53.197 DEBUG 22232 --- [nio-8080-exec-2] c.s.m.D.selectDishVOByCategoryId         : ==> Parameters: 1(Long)
        2026-05-28 13:20:53.199 DEBUG 22232 --- [nio-8080-exec-2] c.s.m.D.selectDishVOByCategoryId         : <==      Total: 3
        ```

3. 补充
    * 而且 `categoryId = 1`,查出来只有 3 条 DishVO，都是之前我手动添加菜品弄进去的
    * 然后细看对比数据库，才发现 `categoryId = 1` 是自己添加的,并没有对应的 `category`，所以 `categoryName = null`
    * 然后 Dish 还有其他教程已有的、正常的 `categoryId`
    * 所以这里只是我没看清楚数据😅
    * 实际 XML 中 `c.name AS categoryName` 并没有问题、也不需要加引号 `'categoryName'`
    * 另外还注意到 `categoryId` 非法数据时，DishFlavorMapper 因为空 `IN( )` 报错，猜想会不会跟这个有关，但实际上并不是
    * 不过有点奇怪，之前 `categoryId` 非法数据怎么没有报错 `SQLSyntaxErrorException`，会不会是框架吞掉了

