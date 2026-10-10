
发现bug
* 菜品管理-条件分页查询

* page, pageSize, name, categoryId, status 5 个 RequestParam，只要name或者status有1个，就会报错

* ApiFox接收消息

   ```
   {
       "code": 0,
       "msg": "未知错误",
       "data": null
   }
   ```

* 后端日志并无warning、error，只是少了一些东西(备注默认带分页参数)

  1. 只带 `name`

      ```
      2026-05-28 08:01:29.019  INFO 19276 --- [nio-8080-exec-5] com.sky.controller.admin.DishController  : 条件分页查询菜品, 查询参数: DishPageQueryDTO(page=1, pageSize=10, name=a, categoryId=null, status=null)
    
      2026-05-28 08:01:29.221 DEBUG 19276 --- [nio-8080-exec-5] .selectDishVOByQueryWithoutFlavors_COUNT : ==>  Preparing: SELECT count(0) FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE name LIKE concat('%', ?, '%')
    
      2026-05-28 08:01:29.236 DEBUG 19276 --- [nio-8080-exec-5] .selectDishVOByQueryWithoutFlavors_COUNT : ==> Parameters: a(String)
      ```

  2. 只带 `status`

     ```
     2026-05-28 08:02:22.553  INFO 19276 --- [nio-8080-exec-2] com.sky.controller.admin.DishController  : 条件分页查询菜品, 查询参数: DishPageQueryDTO(page=1, pageSize=10, name=, categoryId=null, status=1)
   
     2026-05-28 08:02:22.555 DEBUG 19276 --- [nio-8080-exec-2] .selectDishVOByQueryWithoutFlavors_COUNT : ==>  Preparing: SELECT count(0) FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE status = ?
   
     2026-05-28 08:02:22.556 DEBUG 19276 --- [nio-8080-exec-2] .selectDishVOByQueryWithoutFlavors_COUNT : ==> Parameters: 1(Integer)
     ```

  3. 都带 `name`, `status`

     ```
     2026-05-28 08:02:38.265  INFO 19276 --- [nio-8080-exec-4] com.sky.controller.admin.DishController  : 条件分页查询菜品, 查询参数: DishPageQueryDTO(page=1, pageSize=10, name=a, categoryId=null, status=1)
     2026-05-28 08:02:38.268 DEBUG 19276 --- [nio-8080-exec-4] .selectDishVOByQueryWithoutFlavors_COUNT : ==>  Preparing: SELECT count(0) FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE name LIKE concat('%', ?, '%') AND status = ?
     2026-05-28 08:02:38.269 DEBUG 19276 --- [nio-8080-exec-4] .selectDishVOByQueryWithoutFlavors_COUNT : ==> Parameters: a(String), 1(Integer)
     ```

  4. 不带 `name`, `status`

     ```
     2026-05-28 08:06:32.514  INFO 19276 --- [nio-8080-exec-3] com.sky.controller.admin.DishController  : 条件分页查询菜品, 查询参数: DishPageQueryDTO(page=1, pageSize=10, name=, categoryId=1, status=null)
     2026-05-28 08:06:32.524 ERROR 19276 --- [nio-8080-exec-3] c.a.druid.pool.DruidAbstractDataSource   : discard long time none received connection. , jdbcUrl : jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, jdbcUrl : jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, lastPacketReceivedIdleMillis : 234249
     2026-05-28 08:06:32.530 DEBUG 19276 --- [nio-8080-exec-3] .selectDishVOByQueryWithoutFlavors_COUNT : ==>  Preparing: SELECT count(0) FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE category_id = ?
     2026-05-28 08:06:32.530 DEBUG 19276 --- [nio-8080-exec-3] .selectDishVOByQueryWithoutFlavors_COUNT : ==> Parameters: 1(Integer)
     2026-05-28 08:06:32.549 DEBUG 19276 --- [nio-8080-exec-3] .selectDishVOByQueryWithoutFlavors_COUNT : <==      Total: 1
     2026-05-28 08:06:32.551 DEBUG 19276 --- [nio-8080-exec-3] .s.m.D.selectDishVOByQueryWithoutFlavors : ==>  Preparing: SELECT d.id, d.name, d.category_id, d.price, d.image, d.description, d.status, d.update_time , c.name AS categoryName FROM dish AS d LEFT JOIN category AS c ON d.category_id = c.id WHERE category_id = ? ORDER BY d.update_time DESC, d.id DESC LIMIT ?
     2026-05-28 08:06:32.551 DEBUG 19276 --- [nio-8080-exec-3] .s.m.D.selectDishVOByQueryWithoutFlavors : ==> Parameters: 1(Integer), 10(Integer)
     2026-05-28 08:06:32.556 DEBUG 19276 --- [nio-8080-exec-3] .s.m.D.selectDishVOByQueryWithoutFlavors : <==      Total: 3
     2026-05-28 08:06:32.558 DEBUG 19276 --- [nio-8080-exec-3] c.s.m.D.selectByDishIdList               : ==>  Preparing: SELECT id, dish_id, name, value FROM dish_flavor WHERE dish_id IN ( ? , ? , ? )
     2026-05-28 08:06:32.559 DEBUG 19276 --- [nio-8080-exec-3] c.s.m.D.selectByDishIdList               : ==> Parameters: 73(Long), 71(Long), 70(Long)
     2026-05-28 08:06:32.561 DEBUG 19276 --- [nio-8080-exec-3] c.s.m.D.selectByDishIdList               : <==      Total: 3
     ```

* 最终定位: SQL 的联表查询 `<where>` 标签内的字段名没有带表名，导致列名歧义
* 解决: 补上表名，比如 `name` -> `d.name`
