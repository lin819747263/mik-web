-- 用户角色初始化脚本

-- 1. 插入角色数据
INSERT INTO `role` (`role_name`, `create_time`, `update_time`) VALUES
('citizen', NOW(), NOW()),
('operator', NOW(), NOW()),
('admin', NOW(), NOW());

-- 2. 创建运维人员部门关联表（可选，用于按部门过滤工单）
CREATE TABLE IF NOT EXISTS `operator_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `dept` varchar(50) NOT NULL COMMENT '所属部门',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept` (`dept`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维人员部门关联表';

-- 3. 为现有用户分配角色（示例：将第一个用户设为管理员）
-- 请根据实际情况修改 user_id
-- INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`)
-- SELECT u.user_id, r.role_id, NOW(), NOW()
-- FROM `user` u, `role` r
-- WHERE r.role_name = 'admin'
-- LIMIT 1;

-- 4. 创建工单状态变更日志表（可选，用于审计）
CREATE TABLE IF NOT EXISTS `order_status_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '工单ID',
  `order_no` varchar(30) NOT NULL COMMENT '工单号',
  `from_status` varchar(20) NOT NULL COMMENT '原状态',
  `to_status` varchar(20) NOT NULL COMMENT '目标状态',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `desc` varchar(200) DEFAULT NULL COMMENT '操作说明',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单状态变更日志表';
