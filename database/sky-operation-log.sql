# DROP TABLE IF EXISTS 'operation_log'
# CREATE TABLE IF NOT EXISTS 'operation_log'(
#     id bigint not null primary key auto_increment comment '操作日志表 id',
#     operator_id bigint not null comment '操作员工的 id',
#     operation varchar( 30 ) not null comment '操作',
#     operation_time datetime not null default now() comment '操作时间',
#     cost int not null comment '操作耗时'
# ) comment '操作日志表'


# Lingma + DeepSeek
CREATE TABLE IF NOT EXISTS operation_log (
   id             BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
   operator_id    BIGINT       NOT NULL                            COMMENT '操作员工ID，对应 employee.id',
   operation_type   VARCHAR(20)  NOT NULL                            COMMENT '操作类型枚举：INSERT/UPDATE/DELETE/LOGIN/SELECT',
   description    VARCHAR(200) NOT NULL                            COMMENT '操作描述，如"新增菜品-鱼香肉丝"、"启用账号-admin"',
   method_name    VARCHAR(150) NOT NULL                            COMMENT '被调用的Controller方法全限定名，如 com.sky.controller.admin.DishController.save',
   request_params TEXT                                               COMMENT '请求参数(JSON格式)，用于追查当时传入的数据',
   status         TINYINT      NOT NULL DEFAULT 1                  COMMENT '操作结果：1成功 0失败',
   error_msg      TEXT                                               COMMENT '操作失败时的异常信息，成功时为NULL',
   cost_time      INT          NOT NULL DEFAULT 0                  COMMENT '方法执行耗时(毫秒), long 需要强转 int 传入',
   create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '记录创建时间，即操作时间',

   INDEX idx_operator ( operator_id ) COMMENT '按操作人查询的索引',
   INDEX idx_create   ( create_time ) COMMENT '按时间范围查询的索引'
) COMMENT '操作日志表，记录管理员的关键操作';



