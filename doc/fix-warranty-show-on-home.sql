-- 修复保修分类的 show_on_home 字段
-- 将所有一级分类的 show_on_home 设置为 1

UPDATE `warranty_category`
SET `show_on_home` = 1
WHERE `level` = 1 AND `parent_id` = 0;

-- 验证更新结果
SELECT `id`, `name`, `level`, `show_on_home`
FROM `warranty_category`
WHERE `level` = 1
ORDER BY `sort`;
