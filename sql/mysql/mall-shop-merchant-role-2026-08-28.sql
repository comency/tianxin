-- 产业链商城：商家后台角色初始化
-- 使用方法：先执行本脚本，再在“系统管理 -> 用户管理”创建商家账号并分配“企业店铺商家”角色，
-- 最后在“商城系统 -> 商品中心 -> 企业店铺”将该账号设置为店铺负责人。
-- 数据隔离由商品、交易模块按店铺负责人自动处理，不能仅依赖菜单权限。

SET @tenant_id = 1;
SET @role_code = _utf8mb4'mall_shop_merchant' COLLATE utf8mb4_unicode_ci;

INSERT INTO `system_role` (`name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
                           `creator`, `updater`, `deleted`, `tenant_id`)
SELECT '企业店铺商家', @role_code, 100, 1, '', 0, 1, '仅处理本人负责店铺的商城订单和售后',
       'system', 'system', b'0', @tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM `system_role`
    WHERE `code` = @role_code AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

SET @role_id = (
    SELECT `id` FROM `system_role`
    WHERE `code` = @role_code AND `tenant_id` = @tenant_id AND `deleted` = b'0'
    ORDER BY `id` DESC LIMIT 1
);

-- 2000 是商品中心父菜单；分类、品牌、规格、运费模板的“查询”按钮用于商品编辑表单的下拉数据；
-- 2014-2018 为商品管理；
-- 2072 是商城订单中心父菜单，其余为订单、售后页面与可执行操作按钮。
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `updater`, `deleted`, `tenant_id`)
SELECT @role_id, `id`, 'system', 'system', b'0', @tenant_id
FROM `system_menu`
WHERE `id` IN (2000, 2003, 2009, 2020, 2174, 2014, 2015, 2016, 2017, 2018,
               2072, 2073, 2074, 2076, 2547, 2548, 2751, 2752, 2753, 2754)
  AND `deleted` = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `system_role_menu`
      WHERE `role_id` = @role_id AND `menu_id` = `system_menu`.`id`
        AND `tenant_id` = @tenant_id AND `deleted` = b'0'
  );
