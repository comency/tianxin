-- 已执行 mall-shop-2026-08-26.sql 的本地环境修复脚本。
-- 1. product_shop 加入多租户字段，避免租户拦截器查询时报 tenant_id 不存在。
ALTER TABLE `product_shop` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `status`;

-- 2. 使用 UTF-8 文件执行时修复首次错误转码写入的菜单文本。
UPDATE `system_menu` SET `name` = '企业店铺' WHERE `id` = 25000;
UPDATE `system_menu` SET `name` = '店铺查询' WHERE `id` = 25001;
UPDATE `system_menu` SET `name` = '店铺创建' WHERE `id` = 25002;
UPDATE `system_menu` SET `name` = '店铺更新' WHERE `id` = 25003;

-- 3. 补充店铺删除按钮，并授予已有店铺更新权限的角色。
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(25004, '店铺删除', 'product:shop:delete', 3, 4, 25000, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `updater`, `tenant_id`)
SELECT source.`role_id`, 25004, '1', '1', source.`tenant_id`
FROM `system_role_menu` source
WHERE source.`menu_id` = 25003 AND source.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` target
    WHERE target.`role_id` = source.`role_id` AND target.`menu_id` = 25004
      AND target.`tenant_id` = source.`tenant_id` AND target.`deleted` = b'0'
  );
