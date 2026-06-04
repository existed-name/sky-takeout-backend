

# 修改 user 表
-- 新增 update_time
ALTER TABLE user
ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP
;

-- 修改 create_time 默认值
ALTER TABLE user
MODIFY COLUMN create_time datetime DEFAULT CURRENT_TIMESTAMP
;

