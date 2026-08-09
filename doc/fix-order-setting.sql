-- 检查工单设置表是否有数据
SELECT COUNT(*) as count FROM order_setting;

-- 如果没有数据，插入一条默认记录（需要替换为实际的用户ID）
-- 请先查询用户表获取实际的用户ID
-- SELECT user_id, nickname FROM user LIMIT 10;

-- 插入默认设置（示例，请根据实际用户ID修改）
-- INSERT INTO order_setting (default_assignee_id, default_assignee_name, default_reviewer_id, default_reviewer_name, create_time, update_time)
-- VALUES (1, '张三', 1, '张三', NOW(), NOW());

-- 查看当前设置
SELECT * FROM order_setting;
