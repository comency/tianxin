-- 产业链商城：企业店铺与商品归属
CREATE TABLE IF NOT EXISTS `product_shop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '店铺编号',
  `enterprise_id` bigint NOT NULL COMMENT '企业名录编号',
  `name` varchar(64) NOT NULL COMMENT '店铺名称',
  `logo_url` varchar(512) DEFAULT NULL COMMENT '店铺 Logo',
  `contact_name` varchar(32) DEFAULT NULL COMMENT '联系人',
  `contact_mobile` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `introduction` varchar(1024) DEFAULT NULL COMMENT '店铺简介',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_shop_enterprise` (`enterprise_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产业链企业店铺';

ALTER TABLE `product_spu` ADD COLUMN `shop_id` bigint DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
CREATE INDEX `idx_product_spu_shop_id` ON `product_spu` (`shop_id`);

-- PC 管理端菜单与按钮权限。执行后为需要使用的角色分配以下菜单即可。
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(25000, '企业店铺', '', 2, 5, 2000, 'shop', 'ep:shop', 'mall/product/shop/index', 'ProductShop', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(25001, '店铺查询', 'product:shop:query', 3, 1, 25000, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(25002, '店铺创建', 'product:shop:create', 3, 2, 25000, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(25003, '店铺更新', 'product:shop:update', 3, 3, 25000, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(25004, '店铺删除', 'product:shop:delete', 3, 4, 25000, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
