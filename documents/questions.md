# 对设计方面、写代码过程中的疑问

## 其他
* 拦截器
  1. 应该再多区分登录状态失效的情况
  2. 命名应该 [角色][技术][类型] -> `AdminJwtTokenInterceptor` 而不是 `JwtTokenAdminInterceptor`

* 全局异常处理器改进



## 管理端
### 员工管理
* EmployeeController 的 login 方法内部的生成 JWT 信息，应该提取到 Service

* 性别 Integer sex 是 `男1女2` ？
  - 男 1 女 0，从 [User 对象](/sky-pojo/src/main/java/com/sky/entity/User.java)看到的

* 分页查询的默认 page = 1, pageSize = 10 需不需要提取到常量类

* 修改分类状态(启用/禁用), 应该是 PutMapping 而不是 PostMapping ?

* 启用/禁用账号 
  1. `/status/{status}?id=1` 是不是应该修改为 `/{id}/status/{status}` 全部路径变量??
      - 还是拓展为 `/status/{status}?ids=1,2,3` 批量禁用/启用?
  2. 并且更新状态为啥直接混用 Mapper.update 不单独写一个 updateStatusById
      - 因为更新状态也要更新审计字段，就共用 update



### 菜品/套餐分类管理
* 分类分页查询`/page?page=1&pageSize=10&name=&type=`、根据类型查询分类`/list?type=`是不是应该合并
 
* 根据 id 删除分类 `/admin/category?id=` 需不需要改为路径变量??或者拓展为多条选中删除 `ids=1,2,3`

* `查询某种类型的所有分类`: 教程过滤了 `status = 1`，我觉得管理端应该全部看到，只有客户端才过滤

* 对于上传文件，
    - 在 `AliOssConfiguration` 配置类创建工具类时，日志输出 `AliOssProperties` 的内容，会暴露 OSS 密码
    - `CommonController` 的上传文件代码应该由专门的 Service 负责



### 菜品管理
* 为什么 Dish 不封装 `List< DishFlavor >`，要单独设计 DishVO 装 Dish + 对应的 DishFlavor?
    - VO 才是给前端看的

* 条件分页查询菜品 VO，教程只查询了 Dish + CategoryName，没有联合 DishFlavor
    - 可能是预留，后续单独查 DishFlavor 再组装 DishVO

* 删除菜品，dish 被 dish_flavor、order_detail、setmeal_dish、shopping_cart 关联
    - 但是教程的业务规则只说了
        ```
        - 起售中的菜品不能删除
        - 被套餐关联的菜品不能删除
        - 删除菜品后，关联的口味数据也需要删除掉
        ```
    - 好像没有检查订单、购物车

* 教程的 `根据分类 id 查询菜品`
  - `categoryId` 为必须，我觉得可以非必须、复用这个 `list`
  - 然后返回的 `Result< List< Dish > >` 而不是 VO

* `切换菜品状态(停售/起售)`
  - 如果是禁用菜品，需要禁用关联的套餐，就需要数据库批量修改 status，也就是传入链表
  - 然而 AOP 只支持单个对象填写审计字段，之后可以优化为批量填写审计字段



### 通用接口
* 教程的上传文件，主要是上传图片，比如菜品、套餐、头像等等，但是教程好像都共用的1个upload接口，没有分类管理文件业务前缀

* 并且 `CommonController` 太宽泛，可以改成 `FileController`



### 用户相关接口
* `User` 对象 / `user` 表只有 `createTime` 没有 `updateTime`, 并且 `user` 表的默认值设为 `DEFAULT CURRENT_TIMESTAMP` 可能好一点



## 用户端

### 套餐接口
* day06-4.导入商品浏览功能代码
  - 查询套餐，返回 SetmealVOList 但不包含 SetmealDishList 关联关系；反而单独设置根据套餐 id 查询关联关系接口
  - 前者对应了原型 `点击某个分类展示该分类所有套餐`, 后者对应 `点击某套餐，展示细节、关联菜品`
  - 难道说每个按键都要对应 1 个接口，而不能拿到数据后，交给其他按键里面继续用??


### 菜品接口
* day06-4.导入商品浏览功能代码
  - 查询菜品，实际上是查询起售状态的菜品
  - 但是教程是 Controller new Dish, 设置 status 然后传给 Service 筛选 status、categoryId，也是 Controller 涉及业务操作
  - 我觉得应该单独设置查询起售状态的 Service 方法，Controller 直接往 Service 传 categoryId 就行
  - 不过 AI 评价最终还是把状态筛选交给 Mapper 而不是 Service
