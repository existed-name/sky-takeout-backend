# =============================================================================
# 苍穹外卖 · 后端镜像
# =============================================================================
# 作用：把 Maven 多模块工程（sky-common / sky-pojo / sky-server）构建成
#       可运行的 Spring Boot fat jar，并放进 JDK 17 运行环境。
#
# 构建上下文：本仓库根目录（在仓库根执行 `docker build -t sky-backend:1.0.0 .`）。
# 运行端口：8080（容器内），无 context-path（沿用 application.yml 的 server.port）。
#
# 安全说明（很重要）：
#   sky-server/src/main/resources/application-dev.yml 不在 git 里（被 .gitignore 忽略），
#   但它存在于本机工作区，且里面是明文密钥（阿里云 OSS、微信、百度地图、数据库口令）。
#   该文件会被 Maven 复制进 target/classes，进而被打进 fat jar —— 不处理的话，密钥就随镜像扩散。
#   本文件用三道防线阻断：
#     1) 仓库根 .dockerignore 排除它 —— 根本进不了构建上下文；
#     2) 本 Dockerfile 在构建阶段再物理删除一次（下面的 RUN rm -f）—— 兜住 .dockerignore 写漏的情况；
#     3) 运行时全部配置改由环境变量注入 —— Spring Boot 里环境变量优先级高于 jar 内的 yml。
#   注意：第 2 步删掉的是“容器内的副本”，不会影响本机工作区的原文件。
# =============================================================================

# ---------- 构建阶段 ----------
# 带 Maven 的 JDK 17 镜像；本工程字节码目标为 17（根 pom 锁定 source/target=17）。
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 容器内默认走 Maven 中央仓库，国内网络较慢；显式写入阿里云镜像源加速依赖下载。
# 这里直接在本层生成 settings.xml，不额外引入一个需要单独维护的仓库文件。
RUN mkdir -p /root/.m2 \
 && printf '%s\n' \
      '<?xml version="1.0" encoding="UTF-8"?>' \
      '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
      '  <mirrors>' \
      '    <mirror>' \
      '      <id>aliyun-public</id>' \
      '      <name>Aliyun Public</name>' \
      '      <url>https://maven.aliyun.com/repository/public</url>' \
      '      <mirrorOf>central</mirrorOf>' \
      '    </mirror>' \
      '  </mirrors>' \
      '</settings>' \
      > /root/.m2/settings.xml

# 复制工程源码。application-dev.yml / .git / .idea / .lingma / *.iml / docs / draft / db / target
# 已由仓库根 .dockerignore 挡在构建上下文之外。
COPY . .

# 第二道密钥防线：即使 .dockerignore 写漏，也在构建环境里把明文密钥文件删掉。
# 删除的只是容器内的副本，本机文件不受影响。
RUN rm -f sky-server/src/main/resources/application-dev.yml

# ---- 构建 fat jar ----
# 背景（已核对根 pom 与各子模块 pom）：
#   根 pom 把 spring-boot-maven-plugin 声明在 <build><plugins>（不是 pluginManagement），
#   而 spring-boot-starter-parent 给该插件绑定了一个 id=repackage 的执行（绑定在 package 阶段）。
#   于是 sky-common / sky-pojo / sky-server 三个模块都会继承并触发这个 repackage 执行；
#   而 sky-common / sky-pojo 是库模块、没有主类，repackage 会以 “Unable to find main class” 失败。
# 对策：分两步，无论 repackage 是否真的会失败，都能得到正确的可执行 jar：
#   第 1 步：关闭 repackage 做全量 install —— 把父 pom 与两个库模块装进本地仓库（不会触发 fat jar 打包）；
#   第 2 步：只对 sky-server 打开 repackage 打包 —— 从本地仓库取用 sky-common / sky-pojo，打出 fat jar。
# 说明：用 -Dmaven.test.skip=true（连测试编译一起跳过），比只跳过测试运行更稳，不会因本机测试代码问题拖垮镜像构建；
#       镜像构建本就不需要跑测试。
RUN mvn -B -Dmaven.test.skip=true -Dspring-boot.repackage.skip=true install \
 && mvn -B -Dmaven.test.skip=true -pl sky-server package

# ---------- 运行阶段 ----------
# 只需要 JRE；JDK 17 与本工程字节码目标一致。
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区与本机、数据库容器、JDBC URL 里的 Asia/Shanghai 对齐，避免统计报表按 UTC 算错日期。
ENV TZ=Asia/Shanghai
# 容器缺省 locale 下 JDK 17 的默认字符集可能落到 ASCII，影响中文输出；显式设为 UTF-8 更稳。
ENV LANG=C.UTF-8

# sky-server 的 finalName 未改，产物名固定为 sky-server-1.0-SNAPSHOT.jar（已核对 pom）。
COPY --from=build /build/sky-server/target/sky-server-1.0-SNAPSHOT.jar app.jar

# 后端不对公网发布（由 nginx 反向代理），这里只声明容器内监听端口。
EXPOSE 8080
# MaxRAMPercentage：让 JVM 按容器内存上限（而非宿主机内存）分配堆；
# file.encoding：与上面的 LANG 一起保证中文按 UTF-8 处理。
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
