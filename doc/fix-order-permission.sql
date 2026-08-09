-- 工单权限修复脚本
-- 请在数据库中执行此脚本

-- 1. 确保父权限存在且 path 正确
INSERT INTO `permission` (`code`, `name`, `type`, `parent`, `icon`, `path`, `sort`, `create_time`, `update_time`)
SELECT 'order', '工单管理', 0, 0, 'Tickets', '/home/order', 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE code = 'order');

UPDATE `permission` SET `path` = '/home/order' WHERE `code` = 'order' AND (`path` IS NULL OR `path` = '');

-- 2. 确保子权限存在
INSERT INTO `permission` (`code`, `name`, `type`, `parent`, `icon`, `path`, `sort`, `create_time`, `update_time`)
SELECT 'order:unresolved', '待处理工单', 1, p_id, 'WarningFilled', '', 1, NOW(), NOW()
FROM permission WHERE code = 'order'
AND NOT EXISTS (SELECT 1 FROM permission WHERE code = 'order:unresolved');

INSERT INTO `permission` (`code`, `name`, `type`, `parent`, `icon`, `path`, `sort`, `create_time`, `update_time`)
SELECT 'order:history', '历史工单', 1, p_id, 'Clock', '', 2, NOW(), NOW()
FROM permission WHERE code = 'order'
AND NOT EXISTS (SELECT 1 FROM permission WHERE code = 'order:history');

INSERT INTO `permission` (`code`, `name`, `type`, `parent`, `icon`, `path`, `sort`, `create_time`, `update_time`)
SELECT 'order:all', '全部工单', 1, p_id, 'List', '', 3, NOW(), NOW()
FROM permission WHERE code = 'order'
AND NOT EXISTS (SELECT 1 FROM permission WHERE code = 'order:all');

INSERT INTO `permission` (`code`, `name`, `type`, `parent`, `icon`, `path`, `sort`, `create_time`, `update_time`)
SELECT 'order:setting', '工单设置', 1, p_id, 'Setting', '', 4, NOW(), NOW()
FROM permission WHERE code = 'order'
AND NOT EXISTS (SELECT 1 FROM permission WHERE code = 'order:setting');

-- 3. 确保管理员角色有工单权限
INSERT INTO `role_permission` (`role_id`, `p_id`, `create_time`, `update_time`)
SELECT 1, p_id, NOW(), NOW() FROM permission WHERE code = 'order'
AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = 1 AND p_id = (SELECT p_id FROM permission WHERE code = 'order'));

INSERT INTO `role_permission` (`role_id`, `p_id`, `create_time`, `update_time`)
SELECT 1, p_id, NOW(), NOW() FROM permission WHERE code IN ('order:unresolved', 'order:history', 'order:all', 'order:setting')
AND p_id NOT IN (SELECT p_id FROM role_permission WHERE role_id = 1);

-- 4. 验证结果
SELECT '修复完成' as result;
SELECT p_id, code, name, path FROM permission WHERE code LIKE 'order%';
SELECT rp.role_id, p.code, p.name FROM role_permission rp JOIN permission p ON rp.p_id = p.p_id WHERE rp.role_id = 1 AND p.code LIKE 'order%';
