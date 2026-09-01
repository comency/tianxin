-- 产业链商城：店铺结算一期
-- 金额字段单位为分；commission_rate 单位为万分比（500 = 5%）。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `trade_shop_settlement` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算单编号',
    `no` varchar(32) NOT NULL COMMENT '结算单号',
    `shop_id` bigint NOT NULL COMMENT '店铺编号',
    `shop_name` varchar(64) NOT NULL COMMENT '店铺名称快照',
    `period_start_time` datetime NOT NULL COMMENT '结算周期开始时间',
    `period_end_time` datetime NOT NULL COMMENT '结算周期结束时间',
    `order_count` int NOT NULL COMMENT '订单数',
    `order_pay_amount` int NOT NULL COMMENT '订单实付金额',
    `refund_amount` int NOT NULL COMMENT '退款金额',
    `settlement_base_amount` int NOT NULL COMMENT '结算基数',
    `commission_rate` int NOT NULL COMMENT '平台佣金万分比',
    `platform_commission_amount` int NOT NULL COMMENT '平台佣金金额',
    `settlement_amount` int NOT NULL COMMENT '店铺应结金额',
    `status` int NOT NULL COMMENT '状态：0待审核 10已审核 20已结算 30已驳回',
    `audit_user_id` bigint DEFAULT NULL COMMENT '审核人',
    `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
    `audit_remark` varchar(512) DEFAULT NULL COMMENT '审核备注',
    `settle_user_id` bigint DEFAULT NULL COMMENT '确认结算人',
    `settle_time` datetime DEFAULT NULL COMMENT '确认结算时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_shop_settlement_no` (`tenant_id`, `no`),
    KEY `idx_trade_shop_settlement_shop_status` (`tenant_id`, `shop_id`, `status`),
    KEY `idx_trade_shop_settlement_period` (`tenant_id`, `period_start_time`, `period_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺结算单';

CREATE TABLE IF NOT EXISTS `trade_shop_settlement_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细编号',
    `settlement_id` bigint NOT NULL COMMENT '结算单编号',
    `shop_id` bigint NOT NULL COMMENT '店铺编号',
    `order_id` bigint NOT NULL COMMENT '订单编号',
    `order_no` varchar(64) NOT NULL COMMENT '订单号快照',
    `order_finish_time` datetime NOT NULL COMMENT '订单完成时间',
    `pay_amount` int NOT NULL COMMENT '订单实付金额',
    `refund_amount` int NOT NULL COMMENT '退款金额',
    `settlement_base_amount` int NOT NULL COMMENT '结算基数',
    `platform_commission_amount` int NOT NULL COMMENT '平台佣金金额',
    `settlement_amount` int NOT NULL COMMENT '店铺应结金额',
    `active` bit(1) DEFAULT b'1' COMMENT '是否占用订单；驳回后置空以允许重新生成',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_shop_settlement_active_order` (`tenant_id`, `order_id`, `active`),
    KEY `idx_trade_shop_settlement_order_settlement` (`settlement_id`),
    KEY `idx_trade_shop_settlement_order_shop` (`tenant_id`, `shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺结算订单明细';

-- 后台菜单与权限，重复执行不会重复插入。
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
SELECT '店铺结算', '', 2, 3, 2072, 'shop-settlement', 'ep:wallet', 'mall/trade/settlement/index',
       'TradeShopSettlement', 0, b'1', b'1', b'1', 'system', 'system', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component` = 'mall/trade/settlement/index' AND `deleted` = b'0');

UPDATE `system_menu`
SET `name` = '店铺结算', `updater` = 'system'
WHERE `component` = 'mall/trade/settlement/index' AND `deleted` = b'0';

SET @settlement_menu_id = (SELECT `id` FROM `system_menu`
                           WHERE `component` = 'mall/trade/settlement/index' AND `deleted` = b'0'
                           ORDER BY `id` DESC LIMIT 1);

INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
SELECT '结算查询', 'trade:shop-settlement:query', 3, 1, @settlement_menu_id, '', '', '', '', 0, b'1', b'1', b'1', 'system', 'system', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'trade:shop-settlement:query' AND `deleted` = b'0');
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
SELECT '生成结算单', 'trade:shop-settlement:create', 3, 2, @settlement_menu_id, '', '', '', '', 0, b'1', b'1', b'1', 'system', 'system', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'trade:shop-settlement:create' AND `deleted` = b'0');
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
SELECT '审核结算单', 'trade:shop-settlement:audit', 3, 3, @settlement_menu_id, '', '', '', '', 0, b'1', b'1', b'1', 'system', 'system', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'trade:shop-settlement:audit' AND `deleted` = b'0');
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
SELECT '确认结算', 'trade:shop-settlement:confirm', 3, 4, @settlement_menu_id, '', '', '', '', 0, b'1', b'1', b'1', 'system', 'system', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'trade:shop-settlement:confirm' AND `deleted` = b'0');

-- 企业店铺商家只获得查询权限；生成、审核、确认保留给平台账号。
SET @merchant_role_id = (SELECT `id` FROM `system_role`
                         WHERE `code` = 'mall_shop_merchant' AND `tenant_id` = 1 AND `deleted` = b'0'
                         ORDER BY `id` DESC LIMIT 1);
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `updater`, `deleted`, `tenant_id`)
SELECT @merchant_role_id, m.`id`, 'system', 'system', b'0', 1
FROM `system_menu` m
WHERE @merchant_role_id IS NOT NULL
  AND (m.`id` = @settlement_menu_id OR m.`permission` = 'trade:shop-settlement:query')
  AND m.`deleted` = b'0'
  AND NOT EXISTS (SELECT 1 FROM `system_role_menu` rm
                  WHERE rm.`role_id` = @merchant_role_id AND rm.`menu_id` = m.`id`
                    AND rm.`tenant_id` = 1 AND rm.`deleted` = b'0');
