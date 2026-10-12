# CLAUDE.md — 苍穹外卖后端

本文件只记录项目约定、关键事实与容易踩的坑。项目介绍与快速开始请见 README（由作者另写）。

## 模块与构建

- Maven 多模块，根 `pom.xml` 的 `<modules>`：`sky-common`（通用工具/常量/异常/`Result`/配置属性类/注解）、`sky-pojo`（DTO/VO/entity）、`sky-server`（Web 层、Service、Mapper、启动类 `com.sky.SkyApplication`）。
- 依赖方向：`sky-server` → `sky-common` → `sky-pojo`。公共 DTO/VO 放 `sky-pojo`，与 Spring 无关的工具放 `sky-common`。
- 父 POM 为 `spring-boot-starter-parent:2.7.3`；编译目标 JDK 17（根 `pom.xml` 的 `<java.version>17</java.version>` 与 `maven-compiler-plugin` 均显式锁定）。
- 注解处理器有顺序要求：Lombok 先、MapStruct 后（根 `pom.xml` 的 `<annotationProcessorPaths>` 已按此顺序声明）。MapStruct 用于 DTO/VO/entity 互转。
- **可执行 fat jar 名固定为 `sky-server-1.0-SNAPSHOT.jar`**：`sky-server/pom.xml` 与根 POM 均**未**声明 `<finalName>`，故取默认 `${artifactId}-${version}.jar`（`Dockerfile` 也按此名 `COPY`）。

## 运行时与接口约定

- 端口 `8080`，**无 context-path**（`sky-server/src/main/resources/application.yml` 只有 `server.port`，无 `server.servlet.context-path`）。
- 路径前缀按控制器包区分：管理端 `com.sky.controller.admin` → `/admin/**`；用户端 `com.sky.controller.user` → `/user/**`。依据各 Controller 的 `@RequestMapping`（如 `EmployeeController` = `/admin/employees`，`UserController` = `/user/users`）。
- 统一响应 `com.sky.result.Result<T>`（`sky-common`）。**成功判据 `code == 1`，失败为 `0` 或其它值**；成功走 `Result.success(...)`，失败走 `Result.error(msg)`。
- 分页请求参数名 `page` / `pageSize`（`com.sky.dto.common.PageQueryDTO`，`@NotNull @Positive`）；分页响应字段 `total` + `records`（`com.sky.result.PageResult`，Controller 返回 `Result<PageResult<...>>`）。
- PageHelper 已开启 `reasonable: true`（非法页码自动纠正）与 `support-methods-arguments: true`。
- 登录/未鉴权失败：拦截器抛 `NotLoginException` 及其子类，`GlobalExceptionHandler` 对其响应 `HTTP 401` + `Result.error`（`@ResponseStatus(HttpStatus.UNAUTHORIZED)`）。其余业务异常（`BusinessException`）仍返回 HTTP 200 + `code=0`。

## 鉴权（JWT）

- 两个拦截器：`AdminJwtTokenInterceptor`（`/admin/**`，放行 `/admin/employees/login`）与 `UserJwtTokenInterceptor`（`/user/**`，放行 `/user/users/login`、`/user/users/login/web`、`/user/shop/status`）。注册见 `config/WebMvcConfiguration#addInterceptors`，路径常量在 `com.sky.constant.InterceptorConstant`。
- **令牌直接放在请求头里，没有 `Bearer` 前缀**：管理端请求头名为 `token`，用户端为 `authentication`（`application.yml` 的 `sky.jwt.admin-token-name` / `user-token-name`，由 `JwtProperties` 读取）。拦截器直接 `request.getHeader(...)` 后交给 `JwtUtil.parseJWT`。
- 解析成功后将员工/用户 id 写入 `BaseContext`（ThreadLocal），`afterCompletion` 中清理。

## WebSocket

- 端点 `@ServerEndpoint("/ws/{sid}")`，类 `com.sky.websocket.WebSocketServer`；由 `config/WebSocketConfiguration` 注册 `ServerEndpointExporter`。静态页面 `static/websocket.html`。
- **握手不做鉴权**（拦截器只覆盖 `/admin/**` 与 `/user/**`，不覆盖 `/ws`）。
- 推送为**广播**：`sendToAllClients(message)` 遍历 `ConcurrentHashMap<sid, Session>` 群发，用于来单提醒（`OrderServiceImpl`）与定时任务 `task/WebSocketTask`。

## 配置

- `application.yml` 共 **24 个 `${...}` 占位符**，全部位于 `sky.*` 命名空间，实际值由 `spring.profiles.active: dev` 指向的 `application-dev.yml` 提供。
- 该 active profile 用到的占位符包括：`sky.datasource.{driver-class-name,host,port,database,username,password}`、`sky.redis.{host,port,password,database}`、`sky.jwt.{admin-secret-key,user-secret-key}`、`sky.wechat.{appid,secret,url.login}`、`sky.alioss.{access-key-id,access-key-secret,bucket-name,endpoint}`、`sky.baidu-map.{access-key,url.geocoding,url.direction}`、`sky.shop.{address,limit-distance}`。
- 模板文件 `sky-server/src/main/resources/application-dev.yml.example` **已入库**，可复制去 `.example` 后填写。

## 数据库与缓存

- MySQL（`mysql-connector-java`，驱动串含 `serverTimezone=Asia/Shanghai`、`useUnicode=true&characterEncoding=utf-8`），连接池 Druid，ORM MyBatis（`map-underscore-to-camel-case: true`，Mapper XML 位于 `classpath*:com/sky/mapper/**/*.xml`，含子目录 `common/`）。
- 字符集以 dump 为准：`utf8mb4` / `utf8mb4_0900_ai_ci`（MySQL 8）。
- Redis 用作缓存（店铺状态、菜品/套餐、订单号计数等），Key 前缀与 TTL 集中在 `com.sky.constant.RedisConstant`。
- `db/` 脚本：`sky.sql`（**部署实际使用的表结构**，12 张表，字符集 `utf8mb4`/`utf8mb4_0900_ai_ci`，需 MySQL 8.0+；数据仅保留教程种子 `category`/`dish`/`dish_flavor`/`employee`）、`sky_takeout.sql`（MySQL 8.0.44 全量 dump，含开发期数据，最完整）。

## 容器部署

- 仓库根有 `Dockerfile`（多阶段：`maven:3.9-eclipse-temurin-17` 构建 → `eclipse-temurin:17-jre` 运行）与 `.dockerignore`。构建上下文为仓库根。
- 因根 POM 把 `spring-boot-maven-plugin` 放在 `<build><plugins>`（非 `pluginManagement`），库模块 `sky-common`/`sky-pojo` 也会触发 repackage 而无主类报错。`Dockerfile` 因此分两步：先 `-Dspring-boot.repackage.skip=true ... install`，再单模块 `-pl sky-server package`。
- 运行时 `EXPOSE 8080`，`ENTRYPOINT` 带 `-Dfile.encoding=UTF-8`、`TZ=Asia/Shanghai`；配置由环境变量注入（环境变量优先级高于 jar 内 yml）。

## 操作日志（横切）

- 注解 `com.sky.annotation.LogOperation`（`sky-common`）+ 切面 `com.sky.aspect.LogAspect`（`sky-server`）。切点：`execution(* com.sky.controller..*.*(..)) && @annotation(...)`。
- 记录落库到 **`operation_log`** 表，经 `OperationLogMapper` 写入；`status` 成功 1 / 失败 0（`com.sky.constant.OperationLogConstant.Status`）。写库异常只记日志、不阻断业务流程。

## 容易踩的坑

1. **少一个配置项就起不来**：`application.yml` 有 24 个 `${...}` 占位符且 `active: dev`，一旦 `application-dev.yml` 缺失、或缺少任何一个 `sky.*` 键，Spring 启动阶段就会因占位符无法解析而失败。给全的办法：复制 `application-dev.yml.example` 去掉 `.example`，逐项填好全部 24 个键（注意示例里的数据库端口未必与本地一致）。
2. **含真实密钥的配置文件被 git 忽略**：`sky-server/src/main/resources/application-dev.yml` 是明文密钥文件（数据库口令、JWT 密钥、微信/OSS/百度地图凭据），被 `.gitignore` 忽略。**禁止用 `git add -f` 把它提交**；需要改动时只改本地，并同步更新入库的 `.example` 模板。另注意 Maven 会把它复制进 `target/classes`、进而打进 fat jar，构建镜像时务必保留 `.dockerignore` 与 `Dockerfile` 中的两道屏蔽。
3. **根 POM 的 repackage 会让库模块打包失败**：`spring-boot-maven-plugin` 声明在 `<build><plugins>` 而被所有子模块继承，`sky-common`/`sky-pojo` 无主类 → `Unable to find main class`。命令行打包要显式 `-Dspring-boot.repackage.skip=true`（参见 `Dockerfile`）。
4. **令牌头名两端不一致且无 `Bearer`**：管理端 `token`、用户端 `authentication`；前端若统一加 `Bearer ` 或写错头名，会被判为未登录（401）。
5. **循环依赖靠开关兜底**：`application.yml` 设了 `spring.main.allow-circular-references: true`，说明 Bean 间存在循环依赖，删掉该开关可能启动失败。
6. **WebSocket 不鉴权且是广播**：`/ws/{sid}` 无握手鉴权，`sendToAllClients` 会把消息发给所有在线会话；`sid` 复用时旧会话会被关闭。
